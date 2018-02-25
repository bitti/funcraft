(ns funcraft.engine.move
  (:require [funcraft.engines :refer [->Engine]])
  (:import funcraft.components.Position))

(defn change-position-when-move
  [engine itc [msg id dx dy]]
  (if (= msg :move)
    (if ((:ids engine) id)
      (let [component (get-in itc [id Position])]
        [:update [id Position] (merge-with + component {:x dx :y dy})]))))

(defn new []
  (->Engine #{Position} #{} change-position-when-move))
