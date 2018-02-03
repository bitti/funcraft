(ns ld22.level.tile.tree
  (:require [ld22.gfx.colors :as colors]
            [ld22.gfx.screen :as screen]
            [ld22.level.macros :refer [>>]]
            [ld22.level.tile.grass :refer [grass-color]]
            [ld22.protocols :refer [Renderable]]))

(defrecord Tree [^int x ^int y ])

(def col (colors/index 10 30 151 grass-color))
(def bark-col1 (colors/index 10 30 430 grass-color)) ; Lighter bark color
(def bark-col2 (colors/index 10 30 320 grass-color)) ; Darker bark color

(extend-type Tree
  Renderable
  (render [this screen]
    (let [;; Tile has map coordinates which need to be transformed into screen coordinates
          x (>> (.x this) 4)
          y (>> (.y this) 4)]
      (screen/render screen x y 0 col)
      (screen/render screen (+ x 8) y 0 col)
      (screen/render screen x (+ y 8) 0 bark-col1)
      (screen/render screen (+ x 8) (+ y 8) 0 bark-col2))
    )
  )
