(ns ld22.level.tile.water
  (:require [ld22.gfx.colors :as colors]
            [ld22.gfx.screen :as screen]
            [ld22.level.level :as level :refer [LevelRenderable]]
            [ld22.level.macros :refer [<< >>]])
  (:import java.util.Random
           ld22.level.tile.sand.ConnectsToSand))

(set! *unchecked-math* true)

(def ^:const col (colors/index 5 5 115 115))
(def ^:const nanos-per-tick (long (/ 1e9 60)))
(def ^Random random (Random.))

(definterface ConnectsToWater)
(definterface MaySwim)

(defn random-flip []
  (let [i (.nextInt random 4)]
    [:mirror-x (zero? (bit-and i 1))
     :mirror-y (zero? (bit-and i 2))]))

(defrecord Water [^int x ^int y]
  ConnectsToSand
  MaySwim

  LevelRenderable
  (render [this screen level]
    (let [
          ^int dirt-color (get-in level [:colors :dirt-color])
          ^int sand-color (get-in level [:colors :sand-color])
          transition-color1 (colors/index* 3 5 (- dirt-color 111) dirt-color)
          transition-color2 (colors/index* 3 5 (- sand-color 110) sand-color)

          u (instance? Water (level/get-tile level x (dec y)))
          l (instance? Water (level/get-tile level (dec x) y))
          r (instance? Water (level/get-tile level (inc x) y))
          d (instance? Water (level/get-tile level x (inc y)))

          su (instance? ConnectsToSand (level/get-tile level x (dec y)))
          sl (instance? ConnectsToSand (level/get-tile level (dec x) y))
          sr (instance? ConnectsToSand (level/get-tile level (inc x) y))
          sd (instance? ConnectsToSand (level/get-tile level x (inc y)))

          x (<< x 4)
          y (<< y 4)
          ^long tick-count (/ (System/nanoTime) nanos-per-tick)
          ]

      (.setSeed random (+ (long (* (int (/ ^int (* (+ tick-count (* (- (>> x) y) 4311)))
                                           10))
                                   54687121))
                          (* x 3271612)
                          (* y 3412987161)y))

      (if (and u l)
        (screen/render screen x y (.nextInt random 4) col (random-flip))
        (screen/render screen x y (+ (if l 15 14) (if u 32 0))
                       (if (or su sl) transition-color2  transition-color1)))
      (if (and u r)
        (screen/render screen (+ x 8) y (.nextInt random 4) col (random-flip))
        (screen/render screen (+ x 8) y (+ (if r 15 16) (if u 32 0))
                       (if (or su sr) transition-color2  transition-color1)
                       ))
      (if (and d l)
        (screen/render screen x (+ y 8) (.nextInt random 4) col (random-flip))
        (screen/render screen x (+ y 8) (+ (if l 15 14) (if d 32 64))
                       (if (or sd sl) transition-color2  transition-color1)
                       ))
      (if (and d r)
        (screen/render screen (+ x 8) (+ y 8) (.nextInt random 4) col (random-flip))
        (screen/render screen (+ x 8) (+ y 8) (+ (if r 15 16) (if d 32 64))
                       (if (or sd sr) transition-color2  transition-color1)
                       ))
      )
    )
  )
