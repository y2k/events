(ns main-dev
  (:require [handler :as handler]))

(export-default
 {:fetch handler/handle-request})
