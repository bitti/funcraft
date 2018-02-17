(ns funcraft.entity.player
  (:require [funcraft.components
             :refer
             [->Attack
              ->Control
              ->Dimension
              ->Direction
              ->Health
              ->Position
              ->Sprite
              ->Walk]]
            [funcraft.entity.mob :as mob]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.input-handler :as input-handler]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.macros :refer [>>]])
  (:import [funcraft.components Attack Direction Position Walk]
           java.util.Random))

(def ^:const col (colors/index -1 1 220 532))
(def ^:const water-color1 (colors/index -1 -1 115 335))
(def ^:const water-color2 (colors/index -1 -1 115 115))
(def ^:const bow-color (colors/index -1 555 555 555))
(def ^Random random (Random.))

(declare player-render input-handler)

(defn new [x y]
  [(->Position x y)
   (->Health 10)
   (->Direction 0)
   (->Dimension 4 3)
   (->Walk 0)
   (->Sprite player-render)
   (->Control input-handler)
   (->Attack 0)
   ])

(defn input-handler [id]
  (let [ya 0
        ya (if @input-handler/up (dec ya) ya)
        ya (if @input-handler/down (inc ya) ya)
        xa 0
        xa (if @input-handler/right (inc xa) xa)
        xa (if @input-handler/left (dec xa) xa)]
    (if-not (= ya xa 0)
      [:move id xa ya])))

(defn player-render [{[{^int x :x ^int y :y} :as pos] Position
                      {^int walk-dist :distance} Walk
                      {^int dir :direction} Direction
                      {^int attack-time :attack-time} Attack
                      }
                     screen level]
  (let [swimming? (mob/swimming? pos level)
        xo (- x 8)
        yo (- y (if swimming? 7 11))
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

    (if (swimming?)

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

