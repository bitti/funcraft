(ns funcraft.level.tile.cactus
  (:require [funcraft.entity.item-entity :as item-entity]
            [funcraft.entity.particle.smash :as particle.smash]
            [funcraft.entity.particle.text-particle :as particle.text-particle]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.item.item :refer [->Item]]
            [funcraft.level.level :as level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [<<]]
            [funcraft.level.tile.sand :refer [->Sand]]
            [funcraft.level.tile.tree :refer [Hurtable]])
  (:import funcraft.level.level.Level
           funcraft.level.tile.sand.ConnectsToSand
           java.util.Random))

(def ^:const cactus-flower-resource (->Item (+ 4 128) (colors/index -1 10 40 50)))

(def random (Random.))

(defn create-resource [resource x y]
  (item-entity/new
   (:sprite resource)
   (:color resource)
   (+ (<< x 4) (.nextInt random 10) 3)
   (+ (<< y 4) (.nextInt random 10) 3)))

(defrecord Cactus [^int x ^int y ^int damage]
  ConnectsToSand

  LevelRenderable
  (render [this screen level]
    (let [col (colors/index* 20 40 50 (get-in level [:colors :sand-color]))
          x (<< x 4)
          y (<< y 4)
          ]
      (screen/render screen x y (+ 8 64) col)
      (screen/render screen (+ x 8) y (+ 9 64) col)
      (screen/render screen x (+ y 8) (+ 8 96) col)
      (screen/render screen (+ x 8) (+ y 8) (+ 9 96) col)))

  Hurtable
  (hurt [this level level-id new-damage]
    (let [total-damage (+ new-damage damage)
          this (if (>= total-damage 10)
                 (->Sand x y)
                 (assoc this :damage total-damage))
          ]
      (concat
       (if (>= total-damage 10)
         (repeatedly (inc (.nextInt random 2))
                     #(vector :add (create-resource cactus-flower-resource x y))))
       (list
        [:update [level-id Level :tiles (+ x (* y 128))] this]
        [:add (particle.smash/new (+ (<< x 4) 8) (+ (<< y 4) 8))]
        [:add
         (particle.text-particle/new
          (str new-damage)
          (+ (<< x 4) 8) (+ (<< y 4) 8)
          (colors/index -1 500 500 500))]))))
  )

