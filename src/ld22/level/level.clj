(ns ld22.level.level
  (:require [ld22.gfx.colors :as colors]
            [ld22.gfx.screen :as screen]))

(def >> bit-shift-right)
(def << bit-shift-left)
(def div unchecked-divide-int)

(def ^:const grass-color 141)
(def ^:const dirt-color 322)
(def ^:const sand-color 550)

(def w 128)
(def h 128)
(def level-size (* w h))
(def tiles (byte-array level-size))
(def data (byte-array level-size))

(defn set-tile
  [x y t d]
  {:pre [(and (< -1 x w) (< -1 y h))]}
  (aset-byte tiles (+ x (* y w)) (t :id))
  (aset-byte data (+ x (* y w)) d))

(defn get-tile [x y]
  {:pre [(and (< -1 x w) (< -1 y h))]}

  )

(defn render-background [screen ^long x-scroll ^long y-scroll]
  (let [xo (>> x-scroll 4)
        yo (>> y-scroll 4)
        w (>> (+ (screen :w) 15) 4)
        h (>> (+ (screen :h) 15) 4)
        ]
    (for [y (range yo (+ h yo 1))
          x (range xo (+ w xo 1))]
      (screen/render screen x y 0 (colors/index 444 333 222 111)))
    ))
