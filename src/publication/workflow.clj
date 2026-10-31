(ns publication.workflow
  (:require [publication.delivery :as delivery])
  (:require [publication.journal :as journal])
  (:require [publication.sava-centar :as sava])
  (:require [publication.event-time :as time])
  (:require ["node:assert/strict" :as check]))

(defn- match [pattern text]
  (.match text (RegExp. pattern "i")))

(defn- nonblank? [value]
  (and (= (.call Object.prototype.toString value) "[object String]")
       (.test (RegExp. "\\S") value)))

(defn- log [id action reason]
  (.log console (JSON/stringify {:time (.toISOString (Date.)) :event_id id :action action :reason reason})))

(defn- error-reason [fallback error]
  ; Only our fixed diagnostic codes, never raw HTTP errors containing bot URLs.
  (if (and (nonblank? error.message) (match "^[a-z_]+$" error.message)) error.message fallback))

(defn- source-text [io url]
  (-> (Promise/resolve nil)
      (.then (fn [] ((:fetch io) url {:headers {"User-Agent" "Mozilla/5.0"}})))
      (.then (fn [response]
               (check/ok response.ok "source_http_failed")
               (.text response)))))

(defn- assess [card io]
  (-> (source-text io (:url card))
      (.then (fn [html]
               (-> (Promise/resolve nil)
                   (.then (fn [] (sava/read-event card html)))
                   (.catch (fn [error] {:status "skipped" :reason (error-reason "invalid_event" error)})))))
      (.catch (fn [error] {:status "error" :reason (error-reason "source_fetch_failed" error)}))
      (.then (fn [event]
               (cond
                 (:status event) event
                 (not (time/future? event ((:now io)))) {:status "skipped" :reason "not_future"}
                 :else (-> (Promise/resolve nil)
                           (.then (fn [] ((:analyze io) event ((:now io)))))
                           (.then (fn [answer]
                                    {:event event :status (if (= (:decision answer) "publish") "ready" "skipped")
                                     :reason (:reason answer) :text_ru (:text_ru answer)}))
                           (.catch (fn [error] {:status "error" :reason (error-reason "ai_failed" error)}))))))))

(defn- collect-card [directory card io queue]
  (if (journal/attempted? directory (:event_id card))
    (log (:event_id card) "already_attempted" nil)
    (let [started {:event_id (:event_id card) :url (:url card) :status "started"
                   :reason "attempt_started" :started_at (.toISOString ((:now io)))}]
      (-> (journal/start! directory started)
          (.then (fn []
                   (log (:event_id card) "started" nil)
                   (assess card io)))
          (.then (fn [result]
                   (if (= (:status result) "ready")
                     (do (.push queue (Object/assign {} started result))
                         (log (:event_id card) "ready" nil))
                      (-> (journal/save! directory (Object/assign {} started result))
                         (.then (fn [] (log (:event_id card) (:status result) (:reason result))))))))))))

(defn- send-item [directory item config io sent]
  (-> (if (deref sent) ((:sleep io) 3000) (Promise/resolve nil))
      (.then (fn []
               (if (time/future? (:event item) ((:now io)))
                 (do (reset! sent true)
                     (log (:event_id item) "sending" nil)
                      (delivery/send config io (:text_ru item)))
                 {:status "skipped" :reason "no_longer_future"})))
      (.then (fn [result]
                (-> (journal/save! directory
                      (Object/assign {} item result {:selection_reason (:reason item)}))
                   (.then (fn [] (log (:event_id item) (:status result) (:reason result)))))))))

(defn- sort-key [item]
  (str (get-in item [:event :date]) (or (get-in item [:event :time]) "") (:event_id item)))

(defn run-publication [directory config io]
  (-> (Promise/resolve nil)
      (.then (fn []
               (check/ok (and (nonblank? (:token config)) (nonblank? (:chat_id config))) "missing_telegram_config")
               (let [events-directory (journal/open! directory)
                     queue []
                     sent (atom false)]
                 (log nil "run_started" nil)
                 (-> (source-text io sava/listing-url)
                     (.then (fn [html] (sava/read-list html)))
                     (.then (fn [cards]
                              (reduce (fn [previous card]
                                        (.then previous (fn [] (collect-card events-directory card io queue))))
                                      (Promise/resolve nil) cards)))
                     (.then (fn []
                              (.sort queue (fn [left right] (.localeCompare (sort-key left) (sort-key right))))
                              (reduce (fn [previous item]
                                        (.then previous (fn [] (send-item events-directory item config io sent))))
                                      (Promise/resolve nil) queue)))))))
      (.then (fn [] (log nil "run_finished" nil)))
      (.catch (fn [error]
                (let [reason (error-reason "io_or_journal_failed" error)]
                  (log nil "run_failed" reason)
                  (Promise/reject (Error. reason)))))))
