(ns ld22.level.tile.grass
  (:require [ld22.gfx.colors :as colors]
            [ld22.gfx.screen :as screen]
            [ld22.level.macros :refer [<<]]
            [ld22.protocols :refer [Renderable]]))

(def ^:const grass-color 141)
(def ^:const col (colors/index grass-color
                               grass-color
                               (+ grass-color 111)
                               (+ grass-color 111)))

(defrecord Grass
    [^int x ^int y])

(extend-type Grass
  Renderable
  (render [this screen]
    (let [
          ;; Tile has map coordinates which need to be transformed into screen coordinates
          x (<< (.x this) 4)
          y (<< (.y this) 4)]
      (screen/render screen x y 0 col)
      (screen/render screen (+ x 8) y 0 col)
      (screen/render screen x (+ y 8) 0 col)
      (screen/render screen (+ x 8) (+ y 8) 0 col))
    )
  )
