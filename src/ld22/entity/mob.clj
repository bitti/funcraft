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

(defprotocol Tickable
  (tick [this entities]))

(extend-type Mob
  Movable
  (^Mob move [this xa ya]
   (assoc this
          :dir (cond
                 (> ya 0) 0 ; down
                 (< ya 0) 1 ; up
                 (< xa 0) 2 ; right
                 (> xa 0) 3 ; left
                 :else (:dir this)
                 )
          :walk-dist (if (or (not= xa 0) (not= ya 0))
                       (inc (:walk-dist this))
                       (:walk-dist this))
          :entity (entity/move (:entity this) xa ya)
          )))
