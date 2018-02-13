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
  (:import funcraft.level.tile.grass.ConnectsToGrass
           java.util.Random))

(def ^:const col (colors/index 10 30 151 grass-color))
(def ^:const bark-col1 (colors/index 10 30 430 grass-color)) ; Light bark color
(def ^:const bark-col2 (colors/index 10 30 320 grass-color)) ; Dark bark color

(def ^:const wood-resource (->Item (+ 1 128) (colors/index -1 200 531 430)))
(def ^:const acorn-resource (->Item (+ 3 128) (colors/index -1 100 531 320)))
(def ^:const apple-resource (->Item (+ 9 128) (colors/index -1 100 300 500)))

(def random (Random.))

(defprotocol Hurtable
  (hurt [this level damage]))

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
    ))

(defn create-resource [resource x y]
  (item-entity/new
   resource
   (+ (<< x 4) (.nextInt random 10) 3)
   (+ (<< y 4) (.nextInt random 10) 3)))

(extend-type Tree
  Hurtable
  (hurt [this level damage]
    (let [{:keys [x y]} this
          total-damage (+ damage (:damage this))
          tiles (if (>= total-damage 20)
                  (level/set-tile level (->Grass x y))
                  (level/set-tile level (assoc this :damage total-damage)))
          entities (:entities level)
          entities (if (zero? (.nextInt random 10))
                     (conj entities (create-resource apple-resource x y))
                     entities)
          entities (if (>= total-damage 20)
                     (into entities
                           (into
                            (repeatedly
                             (inc (.nextInt random 2))
                             #(create-resource wood-resource x y))
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
                             #(create-resource acorn-resource x y))
                            ))
                     entities)
          ]
      (assoc level
             :tiles tiles
             :entities
             (conj entities
                   (particle.smash/new (+ (<< x 4) 8) (+ (<< y 4) 8))
                   (particle.text-particle/new
                    (str damage)
                    (+ (<< x 4) 8) (+ (<< y 4) 8)
                    (colors/index -1 500 500 500))
                   )
             )))
  )
