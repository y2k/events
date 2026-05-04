(ns telegram
  (:require [effect-fetch :as f]))

(def- api-base "https://api.telegram.org/bot")

(defn send-message [config chat_id text]
  (f/fetch
   (str api-base (:token config) "/sendMessage")
   {:method "POST"
    :headers {"Content-Type" "application/json"}
    :decoder :json
    :body (.stringify js/JSON
                      {:chat_id chat_id
                       :text text})}))
