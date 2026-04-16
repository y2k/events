(ns telegram
  (:require ["node:async_hooks" :as async_hooks]))

(def- api-base "https://api.telegram.org/bot")

(def- storage (async_hooks/AsyncLocalStorage.))

(defn with-config [config f]
  (.run storage config f))

(defn- get-config []
  (.getStore storage))

(defn send-message [text]
  (let [config (get-config)
        url (str api-base config.token "/sendMessage")]
    (eprintln "CONFIG: " config)
    (-> (fetch url
               {:method "POST"
                :headers {"Content-Type" "application/json"}
                :body (JSON/stringify
                       {:chat_id config.chat_id
                        :text text})})
        (.then (fn [resp]
                 (.json resp))))))
