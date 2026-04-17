(ns main
  (:require [handler :as handler]
            [telegram :as tg]))

(defn- handle-request [request env ctx]
  (tg/with-config {:token env.TELEGRAM_BOT_TOKEN
                   :chat_id env.TELEGRAM_CHAT_ID
                   :fetch js/fetch}
    (fn []
      (handler/handle-request request env ctx))))

(export-default
 {:fetch handle-request})
