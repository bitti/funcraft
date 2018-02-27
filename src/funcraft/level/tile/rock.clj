(ns funcraft.level.tile.rock
  (:require [funcraft.entity.particle.smash :as particle.smash]
            [funcraft.entity.particle.text-particle :as particle.text-particle]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.item.item :refer [->Item]]
            [funcraft.level.level :as level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [<<]]
            [funcraft.level.tile.dirt :refer [->Dirt]]
            [funcraft.level.tile.tree :refer [create-resource Hurtable]])
  (:import funcraft.level.level.Level
           java.util.Random))

(def ^:const main-color 444)
(def ^:const dark-color 111)
(def ^:const col (colors/index* main-color main-color (- main-color 111) (- main-color 111)))
(def ^:const flip [:mirror-x true :mirror-y true])
(def ^:const stone-resource (->Item (+ 2 128) (colors/index -1 111 333 555)))

(def random (Random.))

(defrecord Rock [^int x ^int y ^int damage]
  LevelRenderable
  (render [this screen level]
    (let [transition-color
          (colors/index* dark-color main-color (+ main-color 111) (get-in level [:colors :dirt-color]))

          u  (instance? Rock (level/get-tile level x (dec y)))
          l  (instance? Rock (level/get-tile level (dec x) y))
          r  (instance? Rock (level/get-tile level (inc x) y))
          d  (instance? Rock (level/get-tile level x (inc y)))
          ul (instance? Rock (level/get-tile level (dec x) (dec y)))
          ur (instance? Rock (level/get-tile level (inc x) (dec y)))
          dl (instance? Rock (level/get-tile level (dec x) (inc y)))
          dr (instance? Rock (level/get-tile level (inc x) (inc y)))

          x (<< x 4)
          y (<< y 4)]
      (if (and u l)
        (if ul
          (screen/render screen x y 0 col)
          (screen/render screen x y 7 transition-color flip))
        (screen/render screen x y (+ (if l 5 6) (if u 32 64)) transition-color flip))
      (if (and u r)
        (if ur
          (screen/render screen (+ x 8) y 1 col)
          (screen/render screen (+ x 8) y 8 transition-color flip))
        (screen/render screen (+ x 8) y (+ (if r 5 4) (if u 32 64)) transition-color flip))
      (if (and d l)
        (if dl
          (screen/render screen x (+ y 8) 2 col)
          (screen/render screen x (+ y 8) (+ 7 32) transition-color flip))
        (screen/render screen x (+ y 8) (+ (if l 5 6) (if d 32 0)) transition-color flip))
      (if (and d r)
        (if dr
          (screen/render screen (+ x 8) (+ y 8) 3 col)
          (screen/render screen (+ x 8) (+ y 8) (+ 8 32) transition-color flip))
        (screen/render screen (+ x 8) (+ y 8) (+ (if r 5 4) (if d 32 0)) transition-color flip)
        )
      ))

  Hurtable
  (hurt [this level level-id new-damage]
    (let [total-damage (+ new-damage damage)
          this (if (>= total-damage 50)
                 (->Dirt x y)
                 (assoc this :damage total-damage))
          ]
      (concat
       (if (>= total-damage 50)
         (concat
          (repeatedly (inc (.nextInt random 4))
                      #(vector :add (create-resource stone-resource x y)))
          ))
       (list
        [:update [level-id Level :tiles (+ x (* y 128))] this]
        [:add (particle.smash/new (+ (<< x 4) 8) (+ (<< y 4) 8))]
        [:add
         (particle.text-particle/new
          (str new-damage)
          (+ (<< x 4) 8) (+ (<< y 4) 8)
          (colors/index -1 500 500 500))]))))
  )
