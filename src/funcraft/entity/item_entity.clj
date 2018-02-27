(ns funcraft.entity.item-entity
  (:require [funcraft.components
             :as
             components
             :refer
             [->Dimension ->LifetimeLimit ->Position ->Sprite]]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :refer [LevelRenderable]]
            [funcraft.entity.entity]
            [funcraft.protocols :as entity :refer [Tickable]])
  (:import [funcraft.components LifetimeLimit Position Sprite Velocity]
           funcraft.entity.entity.Entity
           funcraft.item.item.Item
           java.util.Random))

(def ^Random random (Random.))

(defn render [this screen _]
  (let [{{:keys [sprite color]} Sprite
         {:keys [x y]} Position
         {:keys [lifetime]} LifetimeLimit
         {:keys [zz]} Velocity} this]
    (when-not (and (<= lifetime 120)
                   (even? (int (/ lifetime 6))))
      (screen/render screen (- x 4) (- y 4) sprite (colors/index -1 0 0 0))
      (screen/render screen (- x 4) (- y 4 (int zz)) sprite color)
      )))

(defn new [sprite color x y]
  [(->Position x y)
   (->Dimension 3 3)                    ; Radius 3
   (->Sprite render sprite color)
   (->LifetimeLimit (+ 600 (.nextInt random 60))) ; lifetime in ticks
   (components/new-velocity x y)])
