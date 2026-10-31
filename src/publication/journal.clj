(ns publication.journal
  (:require ["node:assert/strict" :as check])
  (:require ["node:fs" :as fs])
  (:require ["node:path" :as path]))

(defn open! [directory]
  (let [events-directory (path/join directory "events")]
    (fs/mkdirSync events-directory {:recursive true :mode 448})
    events-directory))

(defn attempted? [directory id]
  (fs/existsSync (path/join directory (str id ".json"))))

(defn- sync-directory [directory]
  (let [fd (fs/openSync directory "r")]
    (-> (Promise/resolve nil)
        (.then (fn [] (fs/fsyncSync fd)))
        (.finally (fn [] (fs/closeSync fd))))))

(defn- write-record [directory record initial]
  (check/ok (.match (:event_id record) (RegExp. "^[0-9]+$" "i")) "invalid_journal_id")
  (let [file (path/join directory (str (:event_id record) ".json"))
        target (if initial file (str file ".tmp"))
        fd (fs/openSync target (if initial "wx" "w") 384)]
    (-> (Promise/resolve nil)
        (.then (fn []
                 (fs/writeFileSync fd (str (JSON/stringify record nil 2) "\n"))
                 (fs/fsyncSync fd)))
        (.finally (fn [] (fs/closeSync fd)))
        (.then (fn []
                 (if initial nil (fs/renameSync target file))
                 (sync-directory directory))))))

(defn start! [directory record]
  (write-record directory record true))

(defn save! [directory record]
  (write-record directory record false))
