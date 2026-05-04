(ns main
  (:require [effect :as fx]
            [effect-fetch :as fetch]
            [xml :as xml]
            [views :as views]
            [telegram :as tg]))

(defn- parse-form-data [text]
  (let [params (js/URLSearchParams. text)]
    (Object/fromEntries (.entries params))))

(defn- get-text [request]
  (fx/thunk
   (fn []
     (.text request))))

(defn handle-fetch [request env ctx]
  (let [url (js/URL. request.url)
        path url.pathname
        method request.method]
    (cond
      (and (= path "/") (= method "GET"))
      (fx/pure
       (Response. (xml/to-string (views/home-page))
                  {:headers {"Content-Type" "text/html"}}))

      (and (= path "/submit") (= method "POST"))
      (-> (get-text request)
          (fx/then (fn [body]
                     (let [form-data (parse-form-data body)
                           link (:link_to_event form-data)
                           response (Response. (xml/to-string (views/submit-result link))
                                               {:headers {"Content-Type" "text/html"}})]
                       (-> (tg/send-message {:token env.TELEGRAM_BOT_TOKEN}
                                            env.TELEGRAM_CHAT_ID
                                            (str "Новая рекомендация: " link))
                           (fx/then (fn []
                                      (fx/pure response)))
                           (fx/recover (fn [err]
                                         (eprintln err)
                                         (fx/pure response))))))))

      :else
      (fx/pure
       (Response. "Not Found" {:status 404})))))

(export-default
 {:fetch (fn [request env ctx]
           ((fetch/with-fetch js/fetch (handle-fetch request env ctx)) {}))})
