(ns ld22.entity.entity)

(defrecord Entity [^int x ^int y ^int xr ^int yr])

(defn intersects [^Entity e x0 y0 x1 y1]
  (not (or
        (< (+ (.x e) (.xr e)) ^long x0)
        (< (+ (.y e) (.yr e)) ^long y0)
        (> (- (.x e) (.xr e)) ^long x1)
        (> (- (.y e) (.yr e)) ^long y1))))

(defprotocol Movable
  (move [^Entity this xa ya]))

(extend-type Entity
  Movable
  (move [this xa ya]
                                        ;    {:pre [(or (zero? xa) (zero? ya))]}
    (assoc this
           :x (+ xa (:x this))
           :y (+ ya (:y this))))
  )
