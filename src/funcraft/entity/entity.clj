(ns funcraft.entity.entity
  (:require [funcraft.level.level :as level]
            [funcraft.level.macros :refer [>>]]
            [funcraft.protocols :refer [Movable]])
  (:import funcraft.protocols.MayPass))

(defn move* [entity level xa ya ]
  {:pre [(or (zero? xa) (zero? ya))]}
  (let [{:keys [x y xr yr]} entity
        xr0 (- x xr)
        yr0 (- y yr)
        xr1 (+ x xr)
        yr1 (+ y yr)

        xto0 (>> xr0 4)
        yto0 (>> yr0 4)
        xto1 (>> xr1 4)
        yto1 (>> yr1 4)

        xt0 (>> (+ xr0 xa) 4)
        yt0 (>> (+ yr0 ya) 4)
        xt1 (>> (+ xr1 xa) 4)
        yt1 (>> (+ yr1 ya) 4)
        ]
    (every? #(instance? MayPass %)
            (for [yt (range yt0 (inc yt1))
                  xt (range xt0 (inc xt1))
                  :when
                  (not (and (<= xto0 xt xto1)
                            (<= yto0 yt yto1)))
                  ]
              (level/get-tile level xt yt)))
    ))

(defrecord Entity [^int x ^int y ^int xr ^int yr]
  Movable
  (move [this level xa ya]
    (assoc this
           :x (+ ^int
                 (if (and (not= xa 0)
                          (move* this level xa 0))
                   xa 0) x)
           :y (+ ^int
                 (if (and (not= ya 0)
                          (move* this level 0 ya))
                   ya 0) y)))

  Comparable
  (compareTo [this other]
    (if (identical? this other)
      0
      (- (.compareTo other y))))
  )

(defn intersects [^Entity e x0 y0 x1 y1]
  (not (or
        (< (+ (.x e) (.xr e)) ^long x0)
        (< (+ (.y e) (.yr e)) ^long y0)
        (> (- (.x e) (.xr e)) ^long x1)
        (> (- (.y e) (.yr e)) ^long y1))))
