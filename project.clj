(defproject funcraft "0.1.0-SNAPSHOT"
  :description "Funcraft - a port of Notch's famous Minicraft into Clojure"
  :url "https://github.com/bitti/funcraft"
  :license {:name "GNU General Public License Version 3"
            :url "https://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :main ^:skip-aot funcraft.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
