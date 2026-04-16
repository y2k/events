(ns main-test
  (:require ["wrangler" :as wrangler]
            ["node:test" :as t]
            ["node:assert" :as assert]))

(def- worker-atom (atom nil))

(t/describe
 "Events Worker"
 (fn []
   (t/before (fn []
               (-> (wrangler/unstable_startWorker {:config "wrangler.toml"})
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
                          (assert/ok (.includes body "<form")))))))))
