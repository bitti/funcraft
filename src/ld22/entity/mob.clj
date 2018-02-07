(ns ld22.entity.mob
  (:require [ld22.level.level :as level]
            [ld22.level.macros :refer [>>]]
            [ld22.level.tile.water]
            [ld22.entity.entity]
            [ld22.protocols :refer [Movable move]])
  (:import ld22.entity.entity.Entity
           ld22.level.level.Level
           ld22.level.tile.water.Water))

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
                  (pos? ^int ya) 0 ; down
                  (neg? ^int ya) 1 ; up
                  (neg? ^int xa) 2 ; right
                  (pos? ^int xa) 3 ; left
                  :else dir)

           :walk-dist (if (= 0 xa ya)
                        walk-dist
                        (inc walk-dist))

           :entity (move entity xa ya)
           ))
  )

(defn new [x y]
  (Mob. (Entity. x y 4 3) 0 max-health 0))

(defn swimming? [^Mob mob ^Level level]
  (instance? Water
             (level/get-tile level
                             (>> (get-in mob [:entity :x]) 4)
                             (>> (get-in mob [:entity :y]) 4))))
