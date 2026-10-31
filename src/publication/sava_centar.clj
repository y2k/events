(ns publication.sava-centar
  (:require ["node:assert/strict" :as check]))

(def listing-url "https://savacentar.rs/en/events-in-the-blue-hall/")
(defn- match [pattern text]
  (.match text (RegExp. pattern "i")))
(defn- field [pattern text]
  (or (get (match pattern text) 1) ""))
(defn- nonblank? [value]
  (and (= (.call Object.prototype.toString value) "[object String]")
       (.test (RegExp. "\\S") value)))

(defn- decode [html]
  (.replace html (RegExp. "&(#x[0-9a-f]+|#[0-9]+|[a-z]+);" "gi")
    (fn [entity name]
      (if (.startsWith name "#")
        (String/fromCodePoint
          (if (= (.toLowerCase (.slice name 0 2)) "#x")
            (parseInt (.slice name 2) 16)
            (parseInt (.slice name 1) 10)))
        (or (get {:amp "&" :quot "\"" :apos "'" :lt "<" :gt ">" :nbsp " "
                  :ndash "–" :mdash "—" :rsquo "’" :lsquo "‘" :rdquo "”" :ldquo "“" :hellip "…"} name)
            entity)))))

(defn- plain [html]
  (-> html
      (.replace (RegExp. "<(script|style)\\b[^>]*>[\\s\\S]*?</\\1>" "gi") " ")
      (.replace (RegExp. "<!--[^]*?-->" "g") " ")
      (.replace (RegExp. "<[^>]*>" "g") " ")
      (decode)
      (.replace (RegExp. "\\s+" "g") " ")
      (.trim)))

(defn- source-url [value]
  (let [url (URL. (decode value) listing-url)]
    (check/ok (and (= url.protocol "https:") (= url.hostname "savacentar.rs")
                   (= url.port "") (= url.username "") (= url.password "")
                   (.startsWith url.pathname "/en/events/")) "invalid_source_url")
    url.href))

(defn read-list [html]
  ; ponytail: fixed Elementor cards; fail closed if the source changes its markup.
  (let [blocks (drop 1 (.split html (RegExp. "<div\\b[^>]*data-elementor-type=[\"']loop-item[\"']" "i")))
        seen (Map.)]
    (check/ok (> (count blocks) 0) "unrecognized_listing")
    (.filter
      (map (fn [block]
             (let [id (field "\\be-loop-item-([0-9]+)\\b" (get (.split block ">") 0))
                   anchor (match "<a\\b[^>]*href=[\"']([^\"']+)[\"'][^>]*>([\\s\\S]*?)</a>" block)
                   card-html (or (get anchor 2) "")
                   url (source-url (or (get anchor 1) ""))
                   listing-date (plain (field "^([\\s\\S]*?)<h2\\b" card-html))
                   previous (.get seen id)]
               (check/ok (nonblank? id) "missing_source_id")
               (check/ok (or (not previous) (= (:url previous) url)) "ambiguous_source_id")
               (check/ok (or (not previous) (= (:listing_date previous) listing-date)) "conflicting_duplicate_date")
               (.set seen id {:url url :listing_date listing-date})
               (if previous nil
                 {:event_id id :url url :listing_text (plain card-html)
                  :listing_date listing-date}))) blocks)
      (fn [card] (not (= card nil))))))

(defn- parse-date [text]
  (let [parts (match "^\\s*([0-9]{1,2})\\.([0-9]{1,2})\\.([0-9]{4})\\.?(?:\\s+([0-9]{1,2})\\s*:\\s*([0-9]{2}))?\\s*$" text)]
    (check/ok parts "missing_or_ambiguous_date")
    (let [day (.padStart (get parts 1) 2 "0")
          month (.padStart (get parts 2) 2 "0")
          year (get parts 3)
          date (str year "-" month "-" day)
          hour (get parts 4)
          minute (get parts 5)
          time (if hour (str (.padStart hour 2 "0") ":" minute) nil)
          instant (Date. (str date "T00:00:00Z"))]
      (check/ok (and (>= (Number year) 2000) (<= (Number year) 2199)
                     (not (Number/isNaN (.getTime instant)))
                     (= (.slice (.toISOString instant) 0 10) date)) "invalid_date")
      (check/ok (or (not hour) (and (< (Number hour) 24) (< (Number minute) 60))) "invalid_time")
      {:date date :time time :display_date (str day "." month "." year)})))

(defn read-event [card html]
  (let [body (field "(<div\\b[^>]*data-elementor-type=[\"']single-post[\"'][\\s\\S]*?)(?:<footer\\b|$)" html)
        title (plain (field "<h1\\b[^>]*>([\\s\\S]*?)</h1>" body))
        heading (plain (field "<h2\\b[^>]*>([\\s\\S]*?)</h2>" body))
        dates (parse-date heading)
        listed (match "([0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{4}\\.?(?:\\s+[0-9]{1,2}\\s*:\\s*[0-9]{2})?)" (:listing_date card))
        listed-date (parse-date (or (get listed 1) ""))
        description (plain (field "data-widget_type=[\"']theme-post-content.default[\"'][^>]*>([\\s\\S]*?)</div>\\s*</div>" body))
        ticket (field "<a\\b[^>]*href=[\"'](https://(?:tickets\\.rs/event/|efinity\\.rs/event-details/)[^\"']+)[\"']" body)]
    (check/ok (match (str "\\bpost-" (:event_id card) "\\b") body) "wrong_event_page")
    (check/ok (and (nonblank? title) (not (match "&[a-z]+;" title))) "missing_title")
    (check/ok (and (match "\\bBlue Hall\\b" (:listing_text card))
                   (match "Sava Cent(?:er|ar)" (plain body))) "missing_venue")
    (check/ok (nonblank? description) "missing_description")
    (check/ok (and (= (:date dates) (:date listed-date))
                   (or (not (:time listed-date)) (= (:time dates) (:time listed-date)))) "conflicting_date")
    (Object/assign {} card dates
      {:title title :venue "Sava Centar, Blue Hall, Belgrade"
       :description description :date_evidence heading
       :ticket_url (if (nonblank? ticket) (decode ticket) nil)})))
