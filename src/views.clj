(ns views)

(defn- layout [title content]
  [:html {}
   [:head {}
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title {} title]
    [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css"}]]
   [:body {}
    [:main {:class "container"}
      content]]])

(defn home-page []
  (layout "Рекомендовать событие"
   [:form {:method "POST" :action "/submit"}
    [:fieldset {}
     [:label {} "Ссылка на событие (link to event)"
      [:input {:name "link_to_event"
                :placeholder "https://"
                :type "url"
                :required true}]]]
    [:input {:type "submit" :value "Советую сходить!"}]]))

(defn submit-result [link]
  (layout "Рекомендовать событие"
   [:article {}
    [:p {} "Спасибо! Ссылка получена: " link]
    [:a {:href "/"} "Вернуться"]]))
