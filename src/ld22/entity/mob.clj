(ns ld22.entity.mob
  (:require [ld22.entity.entity :as entity :refer [Movable]])
  (:import ld22.entity.entity.Entity))

(def ^:const max-health 10)

(defrecord Mob
    [^Entity entity
     ^int dir
     ^int health
     ^int walk-dist])

(defn new-mob [x y]
  (Mob. (Entity. x y 4 3) 0 max-health 0))

(extend-type Mob
  Movable
  (^Mob move [this xa ya]
   (assoc this
          :dir (cond
                 (pos? ya) 0 ; down
                 (neg? ya) 1 ; up
                 (neg? xa) 2 ; right
                 (pos? xa) 3 ; left
                 :else (:dir this)
                 )
          :walk-dist (if (or (not= xa 0) (not= ya 0))
                       (inc (:walk-dist this))
                       (:walk-dist this))
          :entity (entity/move (:entity this) xa ya)
          )))
