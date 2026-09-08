(ns entrypoint
  (:require [main :as m]))

(defn- effects-world [effects]
  (Proxy.
   {}
   {:get (fn [_target prop _receiver]
           (fn [args]
             (.push effects
                    {:url (:url args)
                     :props (:props args)
                     :type prop})
             (Promise/resolve nil)))}))

(export-default
 :fetch (fn [request env ctx]
          (let [effects []]
            (-> ((m/handle-fetch request env ctx) (effects-world effects))
                (.then (fn [response] (.text response)))
                (.then
                 (fn [body]
                   (Response.
                    (.stringify JSON {:effects effects
                                      :response body})
                    {:headers {"Content-Type" "application/json"}})))))))
