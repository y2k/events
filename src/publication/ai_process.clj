(ns publication.ai-process
  (:require ["node:child_process" :as cp]))

(defn- signal-group [child signal]
  (-> (Promise/resolve nil)
      (.then (fn [] (if child.pid (.kill process (- 0 child.pid) signal) nil)))
      (.catch (fn [] nil))))

(defn run-process [command args options]
  (Promise.
    (fn [resolve reject]
      (let [child (cp/spawn command args
                    {:cwd (:cwd options) :env (:env options) :detached true
                     :stdio ["ignore" "pipe" "pipe"]})
            output (atom "")
            errors-size (atom 0)
            failure (atom nil)
            cleanup (atom (Promise/resolve nil))
            stop (fn [reason]
                   (if (deref failure) nil
                     (do (reset! failure reason)
                         (signal-group child "SIGTERM")
                         (reset! cleanup
                           (Promise. (fn [done]
                             (setTimeout
                               (fn [] (.then (signal-group child "SIGKILL") done)) 5000)))))))
            timer (setTimeout (fn [] (stop "ai_timeout")) (:timeout options))]
        (.setEncoding child.stdout "utf8")
        (.setEncoding child.stderr "utf8")
        (.on child.stdout "data"
          (fn [chunk]
            (if (> (+ (Buffer/byteLength (deref output)) (Buffer/byteLength chunk)) 1048576)
              (stop "ai_output_limit")
              (swap! output (fn [old] (str old chunk))))))
        (.on child.stderr "data"
          (fn [chunk]
            (swap! errors-size (fn [old] (+ old (Buffer/byteLength chunk))))
            (if (> (deref errors-size) 1048576) (stop "ai_output_limit") nil)))
        (.on child "error" (fn [] (clearTimeout timer) (reject (Error. "ai_spawn_failed"))))
        (.on child "close"
          (fn [code]
            (clearTimeout timer)
            (.then (deref cleanup)
              (fn []
                (if (deref failure)
                  (reject (Error. (deref failure)))
                  (resolve {:code code :stdout (deref output)}))))))))))
