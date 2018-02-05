(ns ld22.level.level-gen
  (:require [ld22.level.tile grass rock sand tree])
  (:import java.util.Random
           ld22.level.level.Level
           ld22.level.tile.grass.Grass
           ld22.level.tile.sand.Sand
           ld22.level.tile.rock.Rock
           ld22.level.tile.tree.Tree))

(def random (Random.))

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
         0.2 (Tree. i j)
         0.5 (Sand. i j)
         (Grass. i j)
       )))
  )

(defn new-level [w h]
  (Level. w h
          {:dirt-color dirt-color}
          (generate w h)))

