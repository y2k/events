(ns main-test
  (:require ["wrangler" :as wrangler]
            ["node:test" :as t]
            ["node:assert" :as assert]))

(def- worker-atom (atom nil))

(t/describe
 "Events Worker"
 (fn []
   (t/before (fn []
               (-> (wrangler/unstable_dev "bin/test/main_dev.js"
                                          {:experimental {:disableExperimentalWarning true}})
                   (.then (fn [w] (reset! worker-atom w))))))

   (t/after (fn []
              (if (deref worker-atom)
                (.stop (deref worker-atom)))))

   (t/test "GET / returns 200 with HTML form"
           (fn []
             (-> (.fetch (deref worker-atom))
                 (.then (fn [resp]
                          (assert/strictEqual resp.status 200)
                          (.text resp)))
                 (.then (fn [body]
                          (assert/ok (.includes body "Рекомендовать событие"))
                          (assert/ok (.includes body "<form")))))))))
