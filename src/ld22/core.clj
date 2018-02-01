(ns ld22.core
  (:gen-class)
  (:require [ld22.game :refer [start]]))

(defn -main
  "Start a game"
  [& args]
  (start))
