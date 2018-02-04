(ns ld22.level.level
  (:require [ld22.level.macros :refer [>>]]
            [ld22.level.tile.grass]
            [ld22.level.tile.tree]
            [ld22.protocols :as protocols])
  (:import java.io.Writer
           java.util.Random
           ld22.level.tile.grass.Grass
           ld22.level.tile.tree.Tree))

(def div unchecked-divide-int)

(def ^:const dirt-color 322)
(def ^:const sand-color 550)

(def w 128)
(def h 128)
(def level-size (* w h))
(def tiles (byte-array level-size))
(def data (byte-array level-size))
(def random (Random.))

(defrecord Level [^int w ^int h tiles])

(defmethod print-method Level [level ^Writer w]
  (.write w (str (:w level) "x" (:h level))))

(defn new-level [w h]
  (Level. w h
          (vec
           (for [j (range h)
                 i (range w)
                 ]
             (if (> 0.05 (.nextDouble random))
               (Tree. i j)
               (Grass. i j)
               )))))

(defn set-tile
  [x y t d]
  {:pre [(and (< -1 x w) (< -1 y h))]}
  (aset-byte tiles (+ x (* y w)) (t :id))
  (aset-byte data (+ x (* y w)) d))

(defn get-tile [level x y]
  {:pre [(and (< -1 x w) (< -1 y h))]}
  ((.tiles level) (+ x (* y w))))

(defn render-background [level screen x-scroll y-scroll]
  (let [xo (>> x-scroll 4)
        yo (>> y-scroll 4)
        sw (>> (+ (:w screen) 15) 4)
        sh (>> (+ (:h screen) 15) 4)
        ]
    (doall (for [x (range (max 0 xo) (min w (+ sw xo 1)))
                 y (range (max 0 yo) (min h (+ sh yo 1)))
                 ]
             (protocols/render (get-tile level x y) screen)))
    ))
