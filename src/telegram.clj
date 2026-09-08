(ns telegram)

(def- api-base "https://api.telegram.org/bot")

(defn send-message [config chat_id text]
  (fn [env]
    ((:effects-promise.fetch:fetch env)
     {:url (str api-base (:token config) "/sendMessage")
      :props {:method "POST"
              :headers {"Content-Type" "application/json"}
              :decoder :json
              :body (.stringify JSON
                                {:chat_id chat_id
                                 :text text})}})))
