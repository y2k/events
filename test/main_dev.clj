(ns main-dev
  (:require [handler :as handler]
            [telegram :as tg]))

(def- fetch-calls (atom []))

(defn- mock-fetch [url opts]
  (reset! fetch-calls (.concat (deref fetch-calls) [{:url url :opts opts}]))
  (Promise/resolve (Response. (JSON/stringify {:ok true}))))

(defn- handle-request [request env ctx]
  (let [url (js/URL. request.url)
        path url.pathname]
    (cond
      (= path "/_test/fetch-calls")
      (let [calls (deref fetch-calls)]
        (reset! fetch-calls [])
        (Response. (JSON/stringify calls)
                   {:headers {"Content-Type" "application/json"}}))

      :else
      (tg/with-config {:token env.TELEGRAM_BOT_TOKEN
                       :chat_id env.TELEGRAM_CHAT_ID
                       :fetch mock-fetch}
        (fn []
          (handler/handle-request request env ctx))))))

(export-default
 {:fetch handle-request})
