(defproject nodewheel "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [figwheel-sidecar "0.5.2"]
                 [com.cemerick/piggieback "0.2.1"]
                 [org.clojure/clojurescript  "1.9.225"]
                 [io.nervous/eulalie "0.7.0-SNAPSHOT"]
                 [org.clojure/core.async "0.2.385"]]

  :npm {:dependencies [[ws "1.0.1"]
                       [source-map-support "0.4.0"]
                       [jszip              "3.0.0"]]}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-npm "0.6.2"]]

  :cljsbuild
  {:builds [{:id "main"
             :source-paths ["src"]
             :compiler {:main          "nodewheel.core"
                        :output-to     "nodewheelDeployable.js"
                        :target        :nodejs
                        :optimizations :simple}}]}
  :figwheel {})
