(ns main
  (:require [handler :as handler]
            [fetch :as fetch]
            [telegram :as tg]))

(export-default
 {:fetch (fn [request env ctx]
           (fetch/with-fetch js/fetch
             (fn []
               (tg/with-config {:token env.TELEGRAM_BOT_TOKEN
                                :chat_id env.TELEGRAM_CHAT_ID}
                 (fn []
                   (handler/handle-request request env ctx))))))})
