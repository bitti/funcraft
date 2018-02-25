(ns funcraft.engine.walk-animation
  (:require [funcraft.engines :refer [->Engine]])
  (:import [funcraft.components Direction Walk]))

(defn change-walk-direction-and-distance-when-move
  [engine itc [msg id dx dy]]
  (if (= msg :move)
    (if ((:ids engine) id)
      (list [:update [id Walk :distance] (inc (get-in itc [id Walk :distance]))]
            [:update [id Direction :direction]
             (cond
               (pos? ^int dy) 0 ; down
               (neg? ^int dy) 1 ; up
               (neg? ^int dx) 2 ; right
               (pos? ^int dx) 3 ; left
               )
             ])
        )))

(defn new []
  (->Engine #{Direction Walk} #{} change-walk-direction-and-distance-when-move))
