(ns main-test
  (:require [main :as m]
            [db :as db]))

(defn string->readable-stream [s]
  (let [encoder (js/TextEncoder.)]
    (js/ReadableStream.
     {:start (fn [controller]
               (.enqueue controller (.encode encoder s))
               (.close controller))})))

(defn readable-stream->string [stream]
  (-> (.getReader stream)
      (.read)
      (.then (fn [result]
               (let [decoder (js/TextDecoder.)]
                 (.decode decoder (.-value result)))))))

(->
 {:url "http://localhost/"
  :body (string->readable-stream "{\"test\": \"data\"}")}
 (m/handle-request {} {})
 (.-body)
 (readable-stream->string)
 (.then (fn [r] (println r))))
