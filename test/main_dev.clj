(ns main-dev
  (:require [handler :as handler]))

(defn- mock-fetch [url opts]
  (eprintln "[DEV]\nfetch:" url "\n" opts)
  (Promise/resolve (Response. (JSON/stringify {:ok true}))))

(defn- handle-request [request env ctx]
  (handler/handle-request request (Object/assign {:fetch mock-fetch} env) ctx))

(export-default
 {:fetch handle-request})
