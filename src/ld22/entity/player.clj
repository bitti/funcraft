(ns ld22.entity.player
  (:require [ld22.entity.mob :as mob]
            [ld22.gfx.colors :as colors]
            [ld22.gfx.input-handler :as input-handler]
            [ld22.gfx.screen :as screen]
            [ld22.level.level :as level :refer [LevelRenderable]]
            [ld22.level.macros :refer [>>]]
            [ld22.protocols :refer [move Tickable]])
  (:import ld22.entity.mob.Mob))

(set! *unchecked-math* :warn-on-boxed)

(def ^:const col (colors/index -1 1 220 532))
(def ^:const water-color1 (colors/index -1 -1 115 335))
(def ^:const water-color2 (colors/index -1 -1 115 115))

(defrecord Player [^Mob mob ^int stamina]
  Tickable
  (tick [this entities]
    (let [ya 0
          ya (if @input-handler/up (dec ya) ya)
          ya (if @input-handler/down (inc ya) ya)
          xa 0
          xa (if @input-handler/right (inc xa) xa)
          xa (if @input-handler/left (dec xa) xa)
          ]
      (update this :mob move xa ya)))

  LevelRenderable
  (render [this screen level]
    (let [xo (- ^int (get-in mob [:entity :x]) 8) ; offset
          yo (- ^int (get-in mob [:entity :y])
                (if (mob/swimming? mob level) 7 11))
          walk-dist (.. mob walk-dist)
          dir (.dir mob)
          flip1 (case dir
                  (0 1) (bit-and (>> walk-dist 3) 1)
                  2 1
                  3 0)
          flip2 (case dir
                  (0 1) flip1
                  (2 3) (bit-and (>> walk-dist 4) 1))

          ;; tile coords
          xt (case dir
               0 0
               1 2
               2 (+ 4 (bit-and (>> walk-dist 2) 2))
               3 (+ 4 (bit-and (>> walk-dist 2) 2))
               )
          ^:const yt 14
          ]

      (if (mob/swimming? mob level)
        (let [water-color (if (zero? (bit-and ^int @level/ticks 8))
                           water-color2
                           water-color1)]
          (screen/render screen xo (+ yo 3) (+ 5 (* 13 32)) water-color)
          (screen/render screen (+ xo 8) (+ yo 3) (+ 5 (* 13 32)) water-color :mirror-x true))
        (do
          (screen/render screen (+ xo (* 8 flip2)) (+ 8 yo) (+ xt (* (inc yt) 32)) col
                         :mirror-x (= flip2 1))
          (screen/render screen (+ 8 xo (* -8 flip2)) (+ 8 yo) (+ xt 1 (* (inc yt) 32)) col
                         :mirror-x (= flip2 1)))
        )
      (screen/render screen (+ xo (* 8 flip1)) yo (+ xt (* yt 32)) col
                     :mirror-x (= flip1 1))
      (screen/render screen (+ 8 xo (* -8 flip1)) yo (+ xt 1 (* yt 32)) col
                     :mirror-x (= flip1 1))
      )))

(defn new-player [x y]
  (Player. (mob/new x y) 10))
