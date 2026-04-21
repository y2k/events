(ns main-test
  (:require ["wrangler" :as wrangler]
            ["node:test" :as t]
            ["node:assert" :as assert]))

(def- worker-atom (atom nil))

(t/describe
 "Events Worker"
 (fn []
   (t/before (fn []
               (-> (wrangler/unstable_startWorker
                    {:entrypoint "bin/test/main_dev.js"
                     :bindings {:TELEGRAM_BOT_TOKEN {:type "plain_text" :value "test-token"}
                                :TELEGRAM_CHAT_ID {:type "plain_text" :value "test-chat-id"}}
                     :dev {:logLevel "error"}})
                   (.then (fn [w] (reset! worker-atom w))))))

   (t/after (fn []
              (if (deref worker-atom)
                (.dispose (deref worker-atom)))))

   (t/test "GET / returns 200 with HTML form"
           (fn []
             (-> (.fetch (deref worker-atom) "http://localhost/")
                 (.then (fn [resp]
                          (assert/strictEqual resp.status 200)
                          (.text resp)))
                 (.then (fn [body]
                          (assert/ok (.includes body "Рекомендовать событие"))
                          (assert/ok (.includes body "<form")))))))

   (t/test "POST /submit returns 200 and sends telegram message"
           (fn []
             (-> (.fetch (deref worker-atom) "http://localhost/submit"
                         {:method "POST"
                          :headers {"Content-Type" "application/x-www-form-urlencoded"}
                          :body "link_to_event=https://example.com/event"})
                 (.then (fn [resp]
                          (assert/strictEqual resp.status 200)
                          (.text resp)))
                 (.then (fn [body]
                          (assert/ok (.includes body "https://example.com/event"))))
                 (.then (fn []
                          (.fetch (deref worker-atom) "http://localhost/_test/effect-calls")))
                 (.then (fn [resp] (.text resp)))
                 (.then (fn [body]
                          (assert/ok (.includes body "Новая рекомендация: https://example.com/event") body))))))))
