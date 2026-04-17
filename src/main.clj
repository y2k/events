(ns main
  (:require [handler :as handler]))

(defn- handle-request [request env ctx]
  (handler/handle-request request (Object/assign {:fetch js/fetch} env) ctx))

(export-default
 {:fetch handle-request})
