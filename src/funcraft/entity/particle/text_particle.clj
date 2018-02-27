(ns funcraft.entity.particle.text-particle
  (:require [funcraft.components :as components]
            [funcraft.engines :as engines]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.text :as text]
            [funcraft.level.macros :refer [<<]])
  (:import [funcraft.components LifetimeLimit Message Position Velocity]))

(defn render-text-particle [this screen _]
  (let [message (get-in this [Message :message])
        x (- (get-in this [Position :x])
             (<< (count message) 2))
        y (- (get-in this [Position :y])
             (int (get-in this [Velocity :zz])))
        ]
    (text/draw message screen (inc x) (inc y) (colors/index -1 0 0 0))
    (text/draw message screen x y (get-in this [Message :color])))
  )

(defn new [msg x y col]
  [(components/->Position x y)
   (components/->LifetimeLimit 60)
   (components/->Message msg col)
   (components/->Sprite render-text-particle -1 col)
   (update (components/new-velocity x y) :zv inc)
   ])
