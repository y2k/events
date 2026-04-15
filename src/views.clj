(ns views)

(def- head
  [:head {}
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:title {} "Рекомендовать событие"]
   [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css"}]
   [:script {:src "https://unpkg.com/htmx.org@2.0.4"}]])

(defn home-page []
  [:html {}
   head
   [:body {}
    [:main {:class "container"}
     [:fieldset {}
      [:label {} "Ссылка на событие (link to event)"
       [:input {:name :link_to_event
                :placeholder "https://"
                :type :url}]]]
     [:input {:type :submit :value "Советую сходить!"}]]]])
