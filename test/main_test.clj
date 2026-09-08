(ns main-test
  (:require ["wrangler" :as w])
  (:require ["node:test" :as t])
  (:require ["node:assert" :as assert]))

(defn- base64-decode [text]
  (-> (Buffer/from text "base64")
      (.toString "utf8")))

(defn- assert-fetch-snapshot [request expected-base64]
  (fn []
    (-> (w/unstable_startWorker
         {:entrypoint "test/entrypoint.js"
          :config ""
          :compatibilityDate "2026-04-01"
          :compatibilityFlags ["nodejs_compat"]
          :bindings {:TELEGRAM_BOT_TOKEN {:type "plain_text" :value "test-token"}
                     :TELEGRAM_CHAT_ID {:type "plain_text" :value "test-chat"}}})
        (.then
         (fn [worker]
           (-> (if (= request.method "GET")
                 (Promise/resolve nil)
                 (.text request))
               (.then (fn [body]
                        (.fetch worker
                                request.url
                                {:method request.method
                                 :headers request.headers
                                 :body body})))
               (.then (fn [response] (.json response)))
               (.then (fn [actual]
                        (assert/deepStrictEqual
                         actual
                         (JSON/parse (base64-decode expected-base64))
                         (-> (Buffer/from (.stringify JSON actual))
                             (.toString "base64")))))
               (.finally (fn [] (.dispose worker)))))))))

(t/test "GET /"
        (assert-fetch-snapshot
         (Request. "http://localhost/")
         "eyJlZmZlY3RzIjpbXSwicmVzcG9uc2UiOiI8aHRtbD48aGVhZD48bWV0YSBjaGFyc2V0PSdVVEYtOCc+PC9tZXRhPjxtZXRhIG5hbWU9J3ZpZXdwb3J0JyBjb250ZW50PSd3aWR0aD1kZXZpY2Utd2lkdGgsIGluaXRpYWwtc2NhbGU9MSc+PC9tZXRhPjx0aXRsZT7QoNC10LrQvtC80LXQvdC00L7QstCw0YLRjCDRgdC+0LHRi9GC0LjQtTwvdGl0bGU+PGxpbmsgcmVsPSdzdHlsZXNoZWV0JyBocmVmPSdodHRwczovL2Nkbi5qc2RlbGl2ci5uZXQvbnBtL0BwaWNvY3NzL3BpY29AMi9jc3MvcGljby5taW4uY3NzJz48L2xpbms+PC9oZWFkPjxib2R5PjxtYWluIGNsYXNzPSdjb250YWluZXInPjxmb3JtIG1ldGhvZD0nUE9TVCcgYWN0aW9uPScvc3VibWl0Jz48ZmllbGRzZXQ+PGxhYmVsPtCh0YHRi9C70LrQsCDQvdCwINGB0L7QsdGL0YLQuNC1IChsaW5rIHRvIGV2ZW50KTxpbnB1dCBuYW1lPSdsaW5rX3RvX2V2ZW50JyBwbGFjZWhvbGRlcj0naHR0cHM6Ly8nIHR5cGU9J3VybCcgcmVxdWlyZWQ9J3RydWUnPjwvaW5wdXQ+PC9sYWJlbD48L2ZpZWxkc2V0PjxpbnB1dCB0eXBlPSdzdWJtaXQnIHZhbHVlPSfQodC+0LLQtdGC0YPRjiDRgdGF0L7QtNC40YLRjCEnPjwvaW5wdXQ+PC9mb3JtPjwvbWFpbj48L2JvZHk+PC9odG1sPiJ9"))

(t/test "POST /submit"
        (assert-fetch-snapshot
         (Request. "http://localhost/submit"
                   {:method "POST"
                    :headers {"Content-Type" "application/x-www-form-urlencoded"}
                    :body (str "link_to_event=" (encodeURIComponent "https://example.com/event"))})
          "eyJlZmZlY3RzIjpbeyJ1cmwiOiJodHRwczovL2FwaS50ZWxlZ3JhbS5vcmcvYm90dGVzdC10b2tlbi9zZW5kTWVzc2FnZSIsInByb3BzIjp7Im1ldGhvZCI6IlBPU1QiLCJoZWFkZXJzIjp7IkNvbnRlbnQtVHlwZSI6ImFwcGxpY2F0aW9uL2pzb24ifSwiZGVjb2RlciI6Impzb24iLCJib2R5Ijoie1wiY2hhdF9pZFwiOlwidGVzdC1jaGF0XCIsXCJ0ZXh0XCI6XCLQndC+0LLQsNGPINGA0LXQutC+0LzQtdC90LTQsNGG0LjRjzogaHR0cHM6Ly9leGFtcGxlLmNvbS9ldmVudFwifSJ9LCJ0eXBlIjoiZWZmZWN0cy1wcm9taXNlLmZldGNoOmZldGNoIn1dLCJyZXNwb25zZSI6IjxodG1sPjxoZWFkPjxtZXRhIGNoYXJzZXQ9J1VURi04Jz48L21ldGE+PG1ldGEgbmFtZT0ndmlld3BvcnQnIGNvbnRlbnQ9J3dpZHRoPWRldmljZS13aWR0aCwgaW5pdGlhbC1zY2FsZT0xJz48L21ldGE+PHRpdGxlPtCg0LXQutC+0LzQtdC90LTQvtCy0LDRgtGMINGB0L7QsdGL0YLQuNC1PC90aXRsZT48bGluayByZWw9J3N0eWxlc2hlZXQnIGhyZWY9J2h0dHBzOi8vY2RuLmpzZGVsaXZyLm5ldC9ucG0vQHBpY29jc3MvcGljb0AyL2Nzcy9waWNvLm1pbi5jc3MnPjwvbGluaz48L2hlYWQ+PGJvZHk+PG1haW4gY2xhc3M9J2NvbnRhaW5lcic+PGFydGljbGU+PHA+0KHQv9Cw0YHQuNCx0L4hINCh0YHRi9C70LrQsCDQv9C+0LvRg9GH0LXQvdCwOiBodHRwczovL2V4YW1wbGUuY29tL2V2ZW50PC9wPjxhIGhyZWY9Jy8nPtCS0LXRgNC90YPRgtGM0YHRjzwvYT48L2FydGljbGU+PC9tYWluPjwvYm9keT48L2h0bWw+In0="))
