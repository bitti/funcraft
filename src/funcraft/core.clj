(ns funcraft.core
  (:gen-class)
  (:require [funcraft.game :refer [start]]))

(defn -main
  "Start a game"
  [& args]
  (def a (start)))
