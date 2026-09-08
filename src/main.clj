(ns main
  (:require [effect :as fx])
  (:require [xml :as xml])
  (:require [views :as views])
  (:require [telegram :as tg]))

(defn- parse-form-data [text]
  (let [params (URLSearchParams. text)]
    (Object/fromEntries (.entries params))))

(defn- get-text [request]
  (fx/thunk
   (fn []
     (.text request))))

(defn- perform-fetch [{url :url props :props}]
  (-> (fetch url props)
      (.then (fn [response]
               (case (:decoder props)
                 :json (.json response)
                 :text (.text response)
                 response)))))

(defn handle-fetch [request env ctx]
  (let [url (URL. request.url)
        path url.pathname
        method request.method]
    (if (and (= path "/") (= method "GET"))
      (fx/pure
       (Response. (xml/to-string (views/home-page))
                  {:headers {"Content-Type" "text/html"}}))
      (if (and (= path "/submit") (= method "POST"))
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
        (fx/pure
         (Response. "Not Found" {:status 404}))))))

(export-default
 :fetch (fn [request env ctx]
          ((handle-fetch request env ctx)
           {:effects-promise.fetch:fetch perform-fetch})))
