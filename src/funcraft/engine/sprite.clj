(ns funcraft.engine.sprite
  (:require [funcraft.engines :refer [->Engine]])
  (:import [funcraft.components Position Sprite]))

(defn render-by-sprite-fn [this itc [msg]]
  (if (= msg :render)
    (map (fn [id]
           [:render id (get-in itc [id Sprite :render-fn])])
         (:ids this))
    ))

(defn new []
  (->Engine #{Position Sprite} #{} render-by-sprite-fn))

