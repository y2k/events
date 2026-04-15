(ns main
  (:require [db :as db]
            [xml :as xml]
            [views :as views]))

(defn handle-request [request env ctx]
  ;; (eprintln request)
  (let [url (js/URL. request.url)
        path url.pathname]
    (cond
      (= path "/")
      (Response. (xml/to-string (views/home-page))
                 {:headers {"Content-Type" "text/html"}})

      :else
      (Response. "Not Found" {:status 404}))))

(export-default
 {:fetch handle-request})
