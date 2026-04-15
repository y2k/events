(ns build (:require [make :as m]))

(deps {:make "0.5.0"})

(m/build-simple
 {:out ".wrangler/bin"
  :target "js"
  :deps [["xml" "0.3.0"]]})
