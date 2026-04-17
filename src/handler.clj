(ns handler
  (:require [xml :as xml]
            [views :as views]
            [telegram :as tg]))

(defn- parse-form-data [text]
  (let [params (js/URLSearchParams. text)]
    (Object/fromEntries (.entries params))))

(defn handle-request [request env ctx]
  (let [url (js/URL. request.url)
        path url.pathname
        method request.method]
    (cond
      (and (= path "/") (= method "GET"))
      (Response. (xml/to-string (views/home-page))
                 {:headers {"Content-Type" "text/html"}})

      (and (= path "/submit") (= method "POST"))
      (-> (.text request)
          (.then (fn [body]
                   (let [form-data (parse-form-data body)
                         link (:link_to_event form-data)]
                     (tg/with-config {:token env.TELEGRAM_BOT_TOKEN
                                      :chat_id env.TELEGRAM_CHAT_ID}
                       (fn []
                         (-> (tg/send-message (str "Новая рекомендация: " link))
                             (.then (fn [r]
                                      (Response. (xml/to-string (views/submit-result link))
                                                 {:headers {"Content-Type" "text/html"}}))))))))))

      :else
      (Response. "Not Found" {:status 404}))))
