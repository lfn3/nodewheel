 (ns nodewheel.figwheel
   (:require [figwheel-sidecar.repl-api :as ra]))

(defn start-figwheel-repl []
  (ra/start-figwheel!
    {:figwheel-options {}
     :build-ids ["server"]
     :all-builds
     [{:id "server"
       :figwheel true
       :source-paths ["src"]
       :compiler {:main "nodewheel.core"
                  :output-to "nodewheel.js"
                  :output-dir "out"
                  :target :nodejs
                  :optimizations :none
                  :verbose true}}]})
  (ra/cljs-repl))
