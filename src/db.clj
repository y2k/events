(ns db)

(defn query! [db sql args]
  (->
   db
   (.prepare sql)
   (.bind args)
   (.all)
   (.then (fn [result] result.results))))

(defn first! [db sql args]
  (->
   db
   (.prepare sql)
   (.bind args)
   (.first)))

(defn execute! [db sql args]
  (->
   db
   (.prepare sql)
   (.bind args)
   (.run)))
