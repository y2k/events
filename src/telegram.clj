(ns telegram
  (:require ["node:async_hooks" :as async_hooks]
            [fetch :as f]))

(def- api-base "https://api.telegram.org/bot")

(def- storage (async_hooks/AsyncLocalStorage.))

(defn with-config [config f]
  (.run storage {:token config.token
                 :chat_id config.chat_id} f))

(defn- get-config []
  (.getStore storage))

(defn send-message [text]
  (let [config (get-config)
        url (str api-base config.token "/sendMessage")]
    (->
     (f/fetch url
              {:method "POST"
               :headers {"Content-Type" "application/json"}
               :body (.stringify js/JSON
                                 {:chat_id config.chat_id
                                  :text text})})
     (.then (fn [resp]
              (.json resp))))))
