(ns publish
  (:require [publication.workflow :as pub])
  (:require [publication.analysis :as analysis])
  (:require ["node:os" :as os])
  (:require ["node:path" :as path])
  (:require ["node:timers/promises" :as timers]))

(-> (pub/run-publication
      (path/join (os/homedir) ".local/state/events")
      {:token process.env.TELEGRAM_BOT_TOKEN :chat_id process.env.TELEGRAM_CHAT_ID}
      {:now (fn [] (Date.))
       :sleep (fn [ms] (timers/setTimeout ms))
       :analyze analysis/analyze
       :fetch (fn [url props]
                (fetch url (Object/assign {} props {:signal (AbortSignal/timeout 30000) :redirect "error"})))})
    (.catch (fn [] (Object/assign process {:exitCode 1}))))
