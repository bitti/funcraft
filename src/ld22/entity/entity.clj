(ns ld22.entity.entity
  (:require [ld22.protocols :refer [Movable]]))

(defrecord Entity [^int x ^int y ^int xr ^int yr]
  Movable
  (move [this xa ya]
    #_ {:pre [(or (zero? xa) (zero? ya))]}
    (assoc this
           :x (+ ^int xa x)
           :y (+ ^int ya y)))
  )

(defn intersects [^Entity e x0 y0 x1 y1]
  (not (or
        (< (+ (.x e) (.xr e)) ^long x0)
        (< (+ (.y e) (.yr e)) ^long y0)
        (> (- (.x e) (.xr e)) ^long x1)
        (> (- (.y e) (.yr e)) ^long y1))))
