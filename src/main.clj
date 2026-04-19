(ns main
  (:require [handler :as handler]
            [fetch :as fetch]
            [telegram :as tg]))

(defn- handle-request [request env ctx]
  (fetch/with-fetch js/fetch
    (fn []
      (tg/with-config {:token env.TELEGRAM_BOT_TOKEN
                       :chat_id env.TELEGRAM_CHAT_ID}
        (fn []
          (handler/handle-request request env ctx))))))

(export-default
 {:fetch handle-request})
