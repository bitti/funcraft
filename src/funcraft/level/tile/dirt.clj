(ns funcraft.level.tile.dirt
  (:require [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :as level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [<<]])
  (:import funcraft.level.level.Level
           funcraft.protocols.MayPass))

(defrecord Dirt [^int x ^int y]
  MayPass

  LevelRenderable
  (render [this screen level]
    (let [dirt-color (get-in level [:colors :dirt-color])
          col (colors/index* dirt-color dirt-color (- dirt-color 111) (- dirt-color 111))

          ;; Tile has map coordinates which need to be transformed
          ;; to screen coordinates
          x (<< x 4)
          y (<< y 4)]
      (screen/render screen x y 0 col)
      (screen/render screen (+ x 8) y 1 col)
      (screen/render screen x (+ y 8) 2 col)
      (screen/render screen (+ x 8) (+ y 8) 3 col)
      ))
  )
