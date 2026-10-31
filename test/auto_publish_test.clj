(ns auto-publish-test
  (:require [publication.workflow :as pub])
  (:require [publication.analysis :as analysis])
  (:require [publication.ai-process :as ai])
  (:require [publication.sava-centar :as sava])
  (:require [publication.event-time :as time])
  (:require ["node:test" :as t])
  (:require ["node:assert/strict" :as a])
  (:require ["node:fs" :as fs])
  (:require ["node:os" :as os])
  (:require ["node:path" :as path])
  (:require ["node:child_process" :as cp])
  (:require ["node:timers/promises" :as timers]))

(def- listing (fs/readFileSync "../../test/fixtures/sava-list.html" "utf8"))
(def- detail (fs/readFileSync "../../test/fixtures/sava-event.html" "utf8"))

(defn- reply [id decision]
  (JSON/stringify {:event_id id :decision decision :reason "Инструментальная программа"
                   :text_ru "Krcko Orašić — балет. Sava Centar, Blue Hall, Belgrade. 20.12.2026, 20:00. https://tickets.rs/event/the_nutcracker_27347"}))

(defn- stream [text]
  (JSON/stringify {:type "text" :part {:type "text" :text text :time {:start 1 :end 2}}}))

(defn- pipeline-check []
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "events-test-"))
        requests []
        analyzed []
        config {:token "test-token" :chat_id "test-channel"}
        io {:now (fn [] (Date. "2026-10-13T10:00:00Z"))
            :sleep (fn [ms] (a/fail "one post needs no pause"))
            :analyze (fn [event now]
                       (.push analyzed (:event_id event))
                       (a/equal (:status (JSON/parse (fs/readFileSync (path/join directory "events/5657.json") "utf8"))) "started")
                       (Promise/resolve (JSON/parse (reply (:event_id event) "publish"))))
            :fetch (fn [url props]
                     (Promise/resolve
                       (cond
                         (= url sava/listing-url) (Response. listing)
                         (= url "https://savacentar.rs/en/events/krcko-orascic/") (Response. detail)
                         :else (do (.push requests {:url url :props props})
                                   (Response. "{\"ok\":true,\"result\":{\"message_id\":42}}")))))}]
    (-> (pub/run-publication directory config io)
        (.then (fn [] (pub/run-publication directory config io)))
        (.then (fn []
                 (a/equal (count requests) 1)
                 (a/equal (count analyzed) 1)
                 (let [request (get requests 0)
                       body (JSON/parse (get-in request [:props :body]))
                       record (JSON/parse (fs/readFileSync (path/join directory "events/5657.json") "utf8"))]
                   (a/equal (:url request) "https://api.telegram.org/bottest-token/sendMessage")
                   (a/equal (:chat_id body) "test-channel")
                   (a/equal (:text body) (:text_ru (JSON/parse (reply "5657" "publish"))))
                   (a/equal (:status record) "published")
                   (a/equal (:message_id record) 42))))
        (.finally (fn [] (fs/rmSync directory {:recursive true :force true}))))))

(defn- failure-check [mode]
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "events-failure-"))
        analyzed []
        posts []
        config {:token "secret-test-token" :chat_id "test-channel"}
        io {:now (fn [] (Date. "2026-10-13T10:00:00Z"))
            :sleep (fn [ms] (a/fail "unexpected pause"))
            :analyze (fn [event now]
                       (.push analyzed (:event_id event))
                       (cond
                         (= mode "ai-error") (Promise/reject (Error. "provider failure"))
                         (= mode "timeout") (Promise/reject (Error. "ai_timeout"))
                         (= mode "malformed") (analysis/read-answer event 0 (stream "malformed"))
                         :else (Promise/resolve (JSON/parse (reply (:event_id event) (if (= mode "skip") "skip" "publish"))))))
            :fetch (fn [url props]
                     (cond
                       (= url sava/listing-url) (Promise/resolve (Response. listing))
                       (= url "https://savacentar.rs/en/events/krcko-orascic/") (Promise/resolve (Response. detail))
                       :else (do (.push posts url)
                                 (if (= mode "unknown")
                                   (Promise/reject (Error. (str "Lost response: " url)))
                                   (Promise/resolve (Response. "{\"ok\":false,\"error_code\":403}"))))))}]
    (fs/mkdirSync (path/join directory "events"))
    (if (= mode "started") (fs/writeFileSync (path/join directory "events/5657.json") "") nil)
    (-> (pub/run-publication directory config io)
        (.then (fn [] (pub/run-publication directory config io)))
        (.then (fn []
                 (a/equal (count analyzed) (if (= mode "started") 0 1))
                 (a/equal (count posts) (if (or (= mode "unknown") (= mode "rejected")) 1 0))
                 (if (= mode "started") nil
                   (let [content (fs/readFileSync (path/join directory "events/5657.json") "utf8")
                         record (JSON/parse content)]
                     (a/equal (:status record)
                       (case mode "skip" "skipped" "unknown" "unknown" "error"))
                     (a/ok (:reason record))
                     (a/equal (.includes content "secret-test-token") false)))))
        (.finally (fn [] (fs/rmSync directory {:recursive true :force true}))))))

(defn- batch-check []
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "events-batch-"))
        actions []
        early (-> listing (.replaceAll "5657" "5793") (.replaceAll "20.12.2026" "19.12.2026")
                          (.replaceAll "krcko-orascic/" "krcko-orascic-early/"))
        io {:now (fn [] (Date. "2026-10-13T10:00:00Z"))
            :sleep (fn [ms] (.push actions ms) (Promise/resolve nil))
            :analyze (fn [event now] (Promise/resolve {:decision "publish" :reason "балет" :text_ru (:event_id event)}))
            :fetch (fn [url props]
                     (cond
                       (= url sava/listing-url) (Promise/resolve (Response. (str listing early)))
                       (= url "https://savacentar.rs/en/events/krcko-orascic/") (Promise/resolve (Response. detail))
                       (= url "https://savacentar.rs/en/events/krcko-orascic-early/")
                       (Promise/resolve (Response. (-> detail (.replaceAll "5657" "5793") (.replaceAll "20.12.2026" "19.12.2026"))))
                       :else (do (.push actions (:text (JSON/parse (:body props))))
                                 (Promise/resolve (Response. "{\"ok\":true,\"result\":{\"message_id\":42}}")))))}]
    (-> (pub/run-publication directory {:token "test" :chat_id "test"} io)
        (.then (fn [] (a/deepEqual actions ["5793" 3000 "5657"])))
        (.finally (fn [] (fs/rmSync directory {:recursive true :force true}))))))

(defn- storage-check []
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "events-storage-"))
        config {:token "test" :chat_id "test"}
        io {:now (fn [] (Date. "2026-10-13T10:00:00Z"))
            :fetch (fn [url props]
                     (a/equal url sava/listing-url "no event work after failed claim")
                     ; Replacing the journal directory after listing simulates a failed claim write.
                     (fs/rmSync (path/join directory "events") {:recursive true})
                     (fs/writeFileSync (path/join directory "events") "not a directory")
                     (Promise/resolve (Response. listing)))
            :analyze (fn [event now] (a/fail "AI before durable claim"))}]
    (-> (a/rejects (pub/run-publication directory config io))
        (.finally (fn [] (fs/rmSync directory {:recursive true :force true}))))))

(defn- process-tree-check []
  (let [directory (fs/mkdtempSync (path/join (os/tmpdir) "events-process-"))
        pid-file (path/join directory "pid")]
    (-> (ai/run-process process.execPath ["test/auto_publish_test.js"]
          {:env (Object/assign {} process.env {:EVENTS_TEST_CHILD "timeout-controller" :EVENTS_CHILD_PID_FILE pid-file})
           :timeout 10000})
        (.then (fn [result]
                 (a/equal (:code result) 1)
                 (let [pid (Number (fs/readFileSync pid-file "utf8"))]
                   (a/throws (fn [] (.kill process pid 0)) (RegExp. "ESRCH") "timeout left a local descendant alive"))))
        (.finally (fn []
                    (-> (Promise/resolve nil)
                        (.then (fn []
                                 (if (fs/existsSync pid-file)
                                   (.kill process (Number (fs/readFileSync pid-file "utf8")) "SIGKILL") nil)))
                        (.catch (fn [] nil))
                        (.finally (fn [] (fs/rmSync directory {:recursive true :force true})))))))))

(case process.env.EVENTS_TEST_CHILD
  "hang" (timers/setTimeout 30000)
  "fail" (.exit process 7)
  "stubborn" (do (.on process "SIGTERM" (fn [] nil))
                  (fs/writeFileSync process.env.EVENTS_CHILD_PID_FILE (str process.pid))
                  (timers/setTimeout 30000))
  "tree" (do (cp/spawn process.execPath ["test/auto_publish_test.js"]
               {:env (Object/assign {} process.env {:EVENTS_TEST_CHILD "stubborn"}) :stdio "ignore"})
             (timers/setTimeout 30000))
  "timeout-controller" (-> (ai/run-process process.execPath ["test/auto_publish_test.js"]
                             {:env (Object/assign {} process.env {:EVENTS_TEST_CHILD "tree"}) :timeout 1000})
                           (.catch (fn [] (Object/assign process {:exitCode 1}))))
  (t/test "automatic publication preserves facts and never repeats an attempted ID"
  (fn []
    (let [cards (sava/read-list listing)
          card (get cards 0)
          event (sava/read-event card detail)]
      (a/equal (count cards) 1)
      (a/equal (:event_id card) "5657")
      (a/equal (:url card) "https://savacentar.rs/en/events/krcko-orascic/")
      (a/equal (:title event) "Krcko Orašić")
      (a/equal (:date event) "2026-12-20")
      (a/equal (:time event) "20:00")
      (a/equal (:ticket_url event) "https://tickets.rs/event/the_nutcracker_27347")
      (a/equal (time/future? event (Date. "2026-12-20T18:59:00Z")) true)
      (a/equal (time/future? event (Date. "2026-12-20T19:00:00Z")) false)
      (a/equal (time/future? {:date "2026-12-20" :time nil} (Date. "2026-12-19T23:30:00Z")) false)
      (a/equal (time/future? {:date "2026-12-21" :time nil} (Date. "2026-12-19T23:30:00Z")) true)
      (a/throws (fn [] (sava/read-list "<html>Access denied</html>")))
      (a/equal (count (sava/read-list (str listing listing))) 1)
      (a/throws (fn [] (sava/read-list (str listing (.replaceAll listing "20.12.2026" "19.12.2026")))))
      (a/throws (fn [] (sava/read-list (.replaceAll listing "e-loop-item-5657" "missing-id"))))
      (a/throws (fn [] (sava/read-event card (.replaceAll detail "20.12.2026" "31.02.2026")))))
    (let [ai-event (sava/read-event (get (sava/read-list listing) 0) detail)
          valid (stream (reply "5657" "publish"))]
      (a/equal (:decision (analysis/read-answer ai-event 0 valid)) "publish")
      (run! (fn [[code text]]
              (a/throws (fn [] (analysis/read-answer ai-event code text))))
        [[1 valid] [0 "not JSON"] [0 (str valid "\n" valid)]
         [0 (stream "broken")]
         [0 (stream (reply "other-id" "publish"))]
         [0 (stream (reply "5657" "other-decision"))]
         [0 (.replace valid "\"end\":2" "\"end\":null")]
         [0 (str valid "\n{\"type\":\"error\"}")]
         [0 (str valid "\n{\"type\":\"tool_use\"}")]])
      (-> (ai/run-process process.execPath ["test/auto_publish_test.js"]
             {:env (Object/assign {} process.env {:EVENTS_TEST_CHILD "fail"}) :timeout 2000})
          (.then (fn [result] (a/equal (:code result) 7)))
          (.then (fn []
                   (a/rejects
                     (ai/run-process process.execPath ["test/auto_publish_test.js"]
                       {:env (Object/assign {} process.env {:EVENTS_TEST_CHILD "hang"}) :timeout 50})
                     (RegExp. "ai_timeout"))))
          (.then (fn [] (pipeline-check)))
          (.then (fn []
                   (reduce (fn [previous mode] (.then previous (fn [] (failure-check mode))))
                     (Promise/resolve nil) ["skip" "ai-error" "malformed" "timeout" "unknown" "rejected" "started"])))
          (.then (fn [] (batch-check)))
          (.then (fn [] (storage-check)))
          (.then (fn [] (process-tree-check))))))))
