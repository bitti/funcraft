(defproject funcraft "0.1.0-SNAPSHOT"
  :description "Funcraft - a port of Notch's famous Minicraft into Clojure"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :main ^:skip-aot funcraft.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
