(ns funcraft.level.tile.tree
  (:require [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :as level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [<<]]
            [funcraft.level.tile.grass :refer [grass-color]])
  (:import funcraft.level.tile.grass.ConnectsToGrass))

(def col (colors/index 10 30 151 grass-color))
(def bark-col1 (colors/index 10 30 430 grass-color)) ; Light bark color
(def bark-col2 (colors/index 10 30 320 grass-color)) ; Dark bark color

(defrecord Tree [^int x ^int y]
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
