(ns funcraft.core
  (:gen-class)
  (:require [funcraft.game :refer [start stop]]))

(defn -main
  "Start a game"
  [& args]
  (start))
