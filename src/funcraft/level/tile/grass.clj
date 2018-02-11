(ns funcraft.level.tile.grass
  (:require [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :as level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [<<]])
  (:import funcraft.level.level.Level
           funcraft.protocols.MayPass))

(def ^:const grass-color 141)
(def ^:const col (colors/index grass-color
                               grass-color
                               (+ grass-color 111)
                               (+ grass-color 111)))
(def transition-color
  (partial colors/index* (- grass-color 111) grass-color (+ grass-color 111)))

(definterface ConnectsToGrass)

(defrecord Grass [^int x ^int y]
  ConnectsToGrass
  MayPass

  LevelRenderable
  (render [this screen level]
    (let [u  (instance? ConnectsToGrass (level/get-tile level x (dec y)))
          l  (instance? ConnectsToGrass (level/get-tile level (dec x) y))
          r  (instance? ConnectsToGrass (level/get-tile level (inc x) y))
          d  (instance? ConnectsToGrass (level/get-tile level x (inc y)))

          transition-color (transition-color (get-in level [:colors :dirt-color]))
          
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
      ))
  )
