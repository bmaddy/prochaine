(defproject prochaine "0.2.0"
  :description "Om Next technical study"
  :url "https://github.com/tmarble/prochaine"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 ;; [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ;; [cljs-http "0.1.35"]
                 [datascript "0.11.6"]
                 ;;[org.omcljs/om "0.9.0"]
                 ]

  :git-dependencies [["https://github.com/omcljs/om.git" "a3bf9ac50305fb91cf8b980fab22a1bfc450225e"]]

  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-figwheel "0.3.9" :exclusions [org.clojure/clojure]]
            [lein-git-deps "0.0.2-SNAPSHOT"]]

  :hooks [leiningen.cljsbuild]

  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888}

  :source-paths ["src/main/clj"] ;; NOTE: not used

  ;; clean generated JavaScript
  :clean-targets ^{:protect false}
  ["resources/public/js/compiled" :target-path :compile-path]

  :profiles
  {:dev {:cljsbuild
         {:builds
          {:app
           {:source-paths ["src/main/cljs"]
            :figwheel {:websocket-host "localhost"}
            :compiler {:main prochaine.client
                       :output-dir "resources/public/js/compiled"
                       :output-to  "resources/public/js/compiled/app.js"
                       :asset-path "js/compiled"
                       :source-map true
                       :source-map-timestamp true
                       :verbose true
                       :cache-analysis true
                       :optimizations :none
                       :pretty-print false
                       :warnings {:single-segment-namespace false}
                       }}}}}})
