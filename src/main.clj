(ns main
  (:require [context-fetch :as fetch]
            [xml :as xml]
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
                         link (:link_to_event form-data)
                         response (Response. (xml/to-string (views/submit-result link))
                                             {:headers {"Content-Type" "text/html"}})]
                     (-> (tg/send-message env.TELEGRAM_CHAT_ID
                                          (str "Новая рекомендация: " link))
                         (.then (fn [] response)
                                (fn [err]
                                  (eprintln err)
                                  response)))))))

      :else
      (Response. "Not Found" {:status 404}))))

(export-default
 {:fetch (fn [request env ctx]
           (fetch/with-fetch js/fetch
             (fn []
               (tg/with-config {:token env.TELEGRAM_BOT_TOKEN}
                 (fn []
                   (handle-request request env ctx))))))})
