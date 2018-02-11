(ns funcraft.entity.player
  (:require [funcraft.entity.mob :as mob]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.input-handler :as input-handler]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :as level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [>>]]
            [funcraft.level.tile.tree :as tree :refer [Hurtable]]
            [funcraft.protocols :refer [move Tickable]])
  (:import funcraft.entity.mob.Mob
           java.util.Random))

(set! *unchecked-math* :warn-on-boxed)

(def ^:const col (colors/index -1 1 220 532))
(def ^:const water-color1 (colors/index -1 -1 115 335))
(def ^:const water-color2 (colors/index -1 -1 115 115))
(def ^:const bow-color (colors/index -1 555 555 555))
(def ^Random random (Random.))

(declare attack)

(defrecord Player [^Mob mob ^int stamina ^int attack-time]
  Tickable
  (tick [this level]
    (let [ya 0
          ya (if @input-handler/up (dec ya) ya)
          ya (if @input-handler/down (inc ya) ya)
          xa 0
          xa (if @input-handler/right (inc xa) xa)
          xa (if @input-handler/left (dec xa) xa)

          entities (disj (:entities level) this)
          this (update this :mob move level xa ya)
          this (assoc this
                      :attack-time
                      (if (pos? attack-time)
                        (dec attack-time)
                        (if @input-handler/attack 10 0)))
          level (assoc level
                       :entities (conj entities this))

          ]
      (if (= attack-time 10)
        (attack this level)
        level)))

  LevelRenderable
  (render [this screen level]
    (let [xo (- ^int (get-in mob [:entity :x]) 8)
          yo (- ^int (get-in mob [:entity :y])
                (if (mob/swimming? mob level) 7 11))
          walk-dist (.. mob walk-dist)
          dir (.dir mob)
          flip (case dir
                  (0 1) (pos? (bit-and walk-dist 8))
                  2 true
                  3 false)

          tile (+ (case dir
                    0 0
                    1 2
                    (2 3) (+ 4 (bit-and (>> walk-dist 2) 2)))
                  (* 14 32))
          ]

      (if (mob/swimming? mob level)

        ;; Render waves
        (let [water-color (if (zero? (bit-and ^int (:ticks level) 8))
                           water-color2
                           water-color1)
              yo (+ yo 3)
              tile (+ 5 (* 13 32))]
          (screen/render screen xo yo tile water-color)
          (screen/render screen (+ xo 8) yo tile water-color :mirror-x true))

        ;; Render feet
        (let [flip (case dir
                     (0 1) flip
                     (2 3) (pos? (bit-and walk-dist 16)))
              tile (+ tile 32)
              yo (+ 8 yo)]
          (screen/render screen (+ xo (if flip 8 0)) yo tile col :mirror-x flip)
          (screen/render screen (+ xo (if flip 0 8)) yo (inc tile) col :mirror-x flip)
          ))

      ;; Render body
      (screen/render screen (+ xo (if flip 8 0)) yo tile col :mirror-x flip)
      (screen/render screen (+ xo (if flip 0 8)) yo (inc tile) col :mirror-x flip)

      (if (pos? attack-time)

        ;; Render attack bow
        (let [^:const horiz-tile (+ 6 (* 13 32))
              ^:const vert-tile (+ 7 (* 13 32))
              [^int xo ^int yo tile flip]
              (case dir
                0 [xo (+ yo 12) horiz-tile [:mirror-y true]] ; down
                1 [xo (- yo 4) horiz-tile]                   ; up
                2 [(- xo 4) yo vert-tile [:mirror-x true]]   ; left
                3 [(+ xo 12) yo vert-tile])                  ; right
              ]
          (screen/render screen xo yo tile bow-color flip)
          (let [[^int xo ^int yo]
                (case dir
                  (0 1) [(+ xo 8) yo]
                  (2 3) [xo (+ yo 8)])
                flip
                (case dir
                  (0 2) [:mirror-x true :mirror-y true]
                  1 [:mirror-x true]
                  3 [:mirror-y true])
                ]
            (screen/render screen xo yo tile bow-color flip))))

      ))
  )

(defn attack [^Player player level]
  (let [{^int x :x ^int y :y} (.. player mob entity)
        r 20 ; Attack range
        yo 2 ; vertical offset

        [^int x ^int y]
        (case (.. player mob dir)
          0 [x (+ y r yo)]
          1 [x (+ (- y r) yo)]
          2 [(- x r) y]
          3 [(+ x r) y]
          )
        xt (>> x 4)
        yt (>> y 4)

        tile (level/get-tile level xt yt)
        ]
    (if (satisfies? Hurtable tile)
      (tree/hurt tile level (inc (.nextInt random 3)))
      level)
    ))

(defn new-player [x y]
  (->Player (mob/new x y) 10 0))
