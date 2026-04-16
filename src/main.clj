(ns main
  (:require [xml :as xml]
            [views :as views]))

(defn parse-form-data [text]
  (let [params (js/URLSearchParams. text)]
    (Object/fromEntries (.entries params))))

(defn handle-request [request env ctx]
  ;; (eprintln request)
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
                     (Response. (xml/to-string (views/submit-result link))
                                {:headers {"Content-Type" "text/html"}})))))

      :else
      (Response. "Not Found" {:status 404}))))

(export-default
 {:fetch handle-request})
