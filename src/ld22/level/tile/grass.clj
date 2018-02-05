(ns ld22.level.tile.grass
  (:require [ld22.gfx.colors :as colors]
            [ld22.gfx.screen :as screen]
            [ld22.level.level :as level :refer [LevelRenderable]]
            [ld22.level.macros :refer [<<]]))

(def ^:const grass-color 141)
(def ^:const col (colors/index grass-color
                               grass-color
                               (+ grass-color 111)
                               (+ grass-color 111)))
(def transition-color (colors/index (- grass-color 111)
                                    grass-color
                                    (+ grass-color 111)
                                    422
                                        ;(get-in level [:colors :dirt-color])
                                    ))

(definterface ConnectsToGrass)
(definterface MayPass)

(defrecord Grass [^int x ^int y]
  ConnectsToGrass
  MayPass)

(extend-type Grass
  LevelRenderable
  (render [this screen level]
    (let [
          x (:x this)
          y (:y this)

          u  (instance? ConnectsToGrass (level/get-tile level x (dec y)))
          l  (instance? ConnectsToGrass (level/get-tile level (dec x) y))
          r  (instance? ConnectsToGrass (level/get-tile level (inc x) y))
          d  (instance? ConnectsToGrass (level/get-tile level x (inc y)))

          ;; Tile has map coordinates which need to be transformed
          ;; to screen coordinates
          x (<< x 4)
          y (<< y 4)]

      (if (and u l)
        (screen/render screen x y 0 col)
        (screen/render screen x y (+ (if l 12 11) (if u 32 0)) transition-color))
      (if (and u r)
        (screen/render screen (+ x 8) y 1 col)
        (screen/render screen (+ x 8) y (+ (if r 12 13) (if u 32 0)) transition-color))
      (if (and d l)
        (screen/render screen x (+ y 8) 2 col)
        (screen/render screen x (+ y 8) (+ (if l 12 11) (if d 32 64)) transition-color))
      (if (and d r)
        (screen/render screen (+ x 8) (+ y 8) 3 col)
        (screen/render screen (+ x 8) (+ y 8) (+ (if r 12 13) (if d 32 64)) transition-color))
      )))
