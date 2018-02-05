(ns ld22.level.tile.rock
  (:require [ld22.gfx.colors :as colors]
            [ld22.gfx.screen :as screen]
            [ld22.level.level :as level :refer [LevelRenderable]]
            [ld22.level.macros :refer [<<]]))

(def ^:const main-color 444)
(def ^:const dark-color 111)
(def ^:const col (colors/index* main-color main-color (- main-color 111) (- main-color 111)))
(def ^:const flip [:mirror-x true :mirror-y true])

(defrecord Rock [^int x ^int y]
  LevelRenderable
  (render [this screen level]
    (let [transition-color
          (colors/index* dark-color main-color (+ main-color 111) (get-in level [:colors :dirt-color]))

          u  (instance? Rock (level/get-tile level x (dec y)))
          l  (instance? Rock (level/get-tile level (dec x) y))
          r  (instance? Rock (level/get-tile level (inc x) y))
          d  (instance? Rock (level/get-tile level x (inc y)))
          ul (instance? Rock (level/get-tile level (dec x) (dec y)))
          ur (instance? Rock (level/get-tile level (inc x) (dec y)))
          dl (instance? Rock (level/get-tile level (dec x) (inc y)))
          dr (instance? Rock (level/get-tile level (inc x) (inc y)))

          x (<< x 4)
          y (<< y 4)]
      (if (and u l)
        (if ul
          (screen/render screen x y 0 col)
          (screen/render screen x y 7 transition-color flip))
        (screen/render screen x y (+ (if l 5 6) (if u 32 64)) transition-color flip))
      (if (and u r)
        (if ur
          (screen/render screen (+ x 8) y 1 col)
          (screen/render screen (+ x 8) y 8 transition-color flip))
        (screen/render screen (+ x 8) y (+ (if r 5 4) (if u 32 64)) transition-color flip))
      (if (and d l)
        (if dl
          (screen/render screen x (+ y 8) 2 col)
          (screen/render screen x (+ y 8) (+ 7 32) transition-color flip))
        (screen/render screen x (+ y 8) (+ (if l 5 6) (if d 32 0)) transition-color flip))
      (if (and d r)
        (if dr
          (screen/render screen (+ x 8) (+ y 8) 3 col)
          (screen/render screen (+ x 8) (+ y 8) (+ 8 32) transition-color flip))
        (screen/render screen (+ x 8) (+ y 8) (+ (if r 5 4) (if d 32 0)) transition-color flip)
        )
      )))
