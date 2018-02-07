(ns ld22.level.level-gen
  (:require [ld22.level.tile grass rock sand tree water]
            [ld22.gfx.colors :as colors])
  (:import java.util.Random
           ld22.level.level.Level
           ld22.level.tile.grass.Grass
           ld22.level.tile.sand.Sand
           ld22.level.tile.rock.Rock
           ld22.level.tile.tree.Tree
           ld22.level.tile.water.Water
           ))

(def ^Random random (Random.))

(def ^:const grass-color 141)
(def ^:const dirt-color 322)
(def ^:const sand-color 550)
(def ^:const w 128)
(def ^:const h 128)

(defn generate [^long w ^long h]
  (vec
   (for [j (range h)
         i (range w)
         ]
     (condp > (.nextDouble random)
       0.3 (Water. i j
                   (colors/index* 3 5 (- dirt-color 111) dirt-color)
                   (colors/index* 3 5 (- sand-color 110) sand-color))
       0.5 (Sand. i j)
       (Grass. i j
               (colors/index* (- grass-color 111) grass-color (+ grass-color 111)
                              dirt-color))
       )))
  )

(defn new-level [w h]
  (Level. w h
          {:dirt-color dirt-color
           :sand-color sand-color
           }
          (generate w h)))

