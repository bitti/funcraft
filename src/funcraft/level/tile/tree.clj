(ns funcraft.level.tile.tree
  (:require [funcraft.entity.item-entity :as item-entity]
            [funcraft.entity.particle.smash :as particle.smash]
            [funcraft.entity.particle.text-particle :as particle.text-particle]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.item.item :refer [->Item]]
            [funcraft.level.level :as level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [<<]]
            [funcraft.level.tile.grass :refer [->Grass grass-color]])
  (:import funcraft.level.level.Level
           funcraft.level.tile.grass.ConnectsToGrass
           java.util.Random))

(def ^:const col (colors/index 10 30 151 grass-color))
(def ^:const bark-col1 (colors/index 10 30 430 grass-color)) ; Light bark color
(def ^:const bark-col2 (colors/index 10 30 320 grass-color)) ; Dark bark color

(def ^:const wood-resource (->Item (+ 1 128) (colors/index -1 200 531 430)))
(def ^:const acorn-resource (->Item (+ 3 128) (colors/index -1 100 531 320)))
(def ^:const apple-resource (->Item (+ 9 128) (colors/index -1 100 300 500)))

(def random (Random.))

(defprotocol Hurtable
  (hurt [this level level-id damage]))

(defn create-resource [resource x y]
  (item-entity/new
   (:sprite resource)
   (:color resource)
   (+ (<< x 4) (.nextInt random 10) 3)
   (+ (<< y 4) (.nextInt random 10) 3)))

(defrecord Tree [^int x ^int y ^int damage]
  ConnectsToGrass

  LevelRenderable
  (render [this screen level]
    (let [u  (instance? Tree (level/get-tile level x (dec y)))
          l  (instance? Tree (level/get-tile level (dec x) y))
          r  (instance? Tree (level/get-tile level (inc x) y))
          d  (instance? Tree (level/get-tile level x (inc y)))
          ul (instance? Tree (level/get-tile level (dec x) (dec y)))
          ur (instance? Tree (level/get-tile level (inc x) (dec y)))
          dl (instance? Tree (level/get-tile level (dec x) (inc y)))
          dr (instance? Tree (level/get-tile level (inc x) (inc y)))

          ;; Tile has map coordinates which need to be transformed into screen coordinates
          x (<< x 4)
          y (<< y 4)
          ]
      (if (and u ul l)
        (screen/render screen x y (+ 10 32) col)
        (screen/render screen x y 9 col))
      (if (and u ur r)
        (screen/render screen (+ x 8) y (+ 10 64) bark-col2)
        (screen/render screen (+ x 8) y 10 col))
      (if (and d dl l)
        (screen/render screen x (+ y 8) (+ 10 64) bark-col2)
        (screen/render screen x (+ y 8) 41 bark-col1))
      (if (and d dr r)
        (screen/render screen (+ x 8) (+ y 8) (+ 10 (* 1 32)) col)
        (screen/render screen (+ x 8) (+ y 8) (+ 10 (* 3 32)) bark-col2)))
    )

  Hurtable
  (hurt [this level level-id new-damage]
    (let [total-damage (+ new-damage damage)
          this (if (>= total-damage 20)
                 (->Grass x y)
                 (assoc this :damage total-damage))
          ]
      (concat
       (if (zero? (.nextInt random 10))
         (list [:add (create-resource apple-resource x y)]))
       (if (>= total-damage 20)
         (concat
          (repeatedly (inc (.nextInt random 2))
                      #(vector :add (create-resource wood-resource x y)))
          (repeatedly
           ;; Seems this nesting of two random ints
           ;; leads to these probabilities for the
           ;; number of acorns:
           ;;
           ;; 0 25/48
           ;; 1 13/48
           ;; 2 7/48
           ;; 3 1/16
           ;;
           ;; So the expected count is 3/4 acorns
           ;; per tree
           (.nextInt random (inc (.nextInt random 4)))
           #(vector :add (create-resource acorn-resource x y)))))
       (list
        [:update [level-id Level :tiles (+ x (* y 128))] this]
        [:add (particle.smash/new (+ (<< x 4) 8) (+ (<< y 4) 8))]
        [:add
         (particle.text-particle/new
          (str new-damage)
          (+ (<< x 4) 8) (+ (<< y 4) 8)
          (colors/index -1 500 500 500))]))))
  )

