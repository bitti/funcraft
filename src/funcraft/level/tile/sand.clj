(ns funcraft.level.tile.sand
  (:require [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :as level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [<<]])
  (:import funcraft.protocols.MayPass))

(definterface ConnectsToSand)

(defrecord Sand [^int x ^int y]
  ConnectsToSand
  MayPass

  LevelRenderable
  (render [this screen level]
    (let [^int sand-color (get-in level [:colors :sand-color])
          col (colors/index* (+ sand-color 2) sand-color (- sand-color 110) (- sand-color 110))
          transition-color (colors/index* (- sand-color 110) sand-color (- sand-color 110)
                                          (get-in level [:colors :dirt-color]))

          u  (instance? ConnectsToSand (level/get-tile level x (dec y)))
          l  (instance? ConnectsToSand (level/get-tile level (dec x) y))
          r  (instance? ConnectsToSand (level/get-tile level (inc x) y))
          d  (instance? ConnectsToSand (level/get-tile level x (inc y)))

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
