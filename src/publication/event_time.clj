(ns publication.event-time)

(defn local-now [now]
  (let [formatter (Intl.DateTimeFormat. "en-GB"
                    {:timeZone "Europe/Belgrade" :year "numeric" :month "2-digit" :day "2-digit"
                     :hour "2-digit" :minute "2-digit" :hourCycle "h23"})
        parts (Object/fromEntries (map (fn [part] [part.type part.value]) (.formatToParts formatter now)))]
    {:date (str (:year parts) "-" (:month parts) "-" (:day parts))
     :time (str (:hour parts) ":" (:minute parts))}))

(defn future? [event now]
  (let [local (local-now now)
        date-order (.localeCompare (:date event) (:date local))]
    (or (> date-order 0)
        (and (= date-order 0) (not (= (:time event) nil))
             (> (.localeCompare (:time event) (:time local)) 0)))))
