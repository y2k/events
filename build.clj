(ns build (:require [make :as m]))

(deps {:make "0.5.0"})

(m/build-simple
 {:out ".wrangler/bin"
  :target "js"
  :deps [["effect_fetch" "0.1.0/js"]
         ["effect" "0.1.0/js"]
         ["test-cloudflare-worker" "0.2.0/js"]
         ["xml" "0.3.0"]]})
