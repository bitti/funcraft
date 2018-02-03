(ns ld22.entity.mob
  (:require [ld22.protocols :refer [move Movable]]
            [ld22.entity.entity :as entity])
  (:import ld22.entity.entity.Entity))

(def ^:const max-health 10)

(defrecord Mob
    [^Entity entity
     ^int dir
     ^int health
     ^int walk-dist]
  Movable
  (move [this xa ya]
    (assoc this
           :dir (cond
                  (pos? ya) 0 ; down
                  (neg? ya) 1 ; up
                  (neg? xa) 2 ; right
                  (pos? xa) 3 ; left
                  :else dir)

           :walk-dist (if (= 0 xa ya)
                        walk-dist
                        (inc walk-dist))

           :entity (move entity xa ya)
           ))
  )

(defn new-mob [x y]
  (Mob. (Entity. x y 4 3) 0 max-health 0))
