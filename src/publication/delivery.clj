(ns publication.delivery
  (:require [vendor.telegram :as tg]))

(defn send [config io text]
  (-> (Promise/resolve nil)
      (.then (fn []
               ((tg/send-message {:token (:token config)} (:chat_id config) text)
                 {:effects-promise.fetch:fetch
                   (fn [request]
                     (-> ((:fetch io) (:url request) (:props request))
                         (.then (fn [response] (.json response)))))})))
      (.then (fn [answer]
               (cond
                 (and (= (:ok answer) true) (Number/isSafeInteger (get-in answer [:result :message_id]))
                      (> (get-in answer [:result :message_id]) 0))
                 {:status "published" :reason "telegram_confirmed" :message_id (get-in answer [:result :message_id])}
                 (= (:ok answer) false) {:status "error" :reason "telegram_rejected"}
                 :else {:status "unknown" :reason "telegram_result_unknown"})))
      (.catch (fn [] {:status "unknown" :reason "telegram_result_unknown"}))))
