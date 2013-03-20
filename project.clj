(defproject marchgame "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.2"]
                 [domina "1.0.2-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "0.3.0"]
            [lein-ring "0.8.3"]]
  :ring {:handler marchgame.core/handler}
  :cljsbuild {:builds
              {:dev {:source-paths ["src/brepl" "src/cljs"]
                     :compiler {:output-to "resources/public/js/rogue.js"
                                :optimizations :whitespace
                                :pretty-print true}}
               :prod {:source-paths ["src/cljs"]
                      :compiler {:output-to "resources/public/js/rogue-min.js"
                                 :optimizations :advanced}}}})
