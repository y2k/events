(ns fetch
  (:require ["node:async_hooks" :as async_hooks]))

(def- storage (async_hooks/AsyncLocalStorage.))

(defn with-fetch [fetch f]
  (.run storage fetch f))

(defn fetch [url opts]
  (let [fetch-fn (.getStore storage)]
    (fetch-fn url opts)))
