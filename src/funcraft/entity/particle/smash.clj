(ns funcraft.entity.particle.smash
  (:require [funcraft.components :as components]
            [funcraft.gfx.colors :as color]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :refer [LevelRenderable]]
            [funcraft.protocols :refer [Tickable]])
  (:import funcraft.components.Position
           funcraft.entity.entity.Entity))

(def ^:const col (color/index -1 555 555 555))
(def ^:const sprite (+ 5 (* 12 32)))

(defn render [this screen _]
  (let [{:keys [x y]} (this Position)]
    (screen/render screen (- x 8) (- y 8) sprite col :mirror-y true)
    (screen/render screen (- x 0) (- y 8) sprite col [:mirror-x true :mirror-y true])
    (screen/render screen (- x 8) (- y 0) sprite col)
    (screen/render screen (- x 0) (- y 0) sprite col :mirror-x true))
  )

(defn new [x y]
  [(components/->Sprite render sprite col)
   (components/->Position x y)
   (components/->LifetimeLimit 10)])
