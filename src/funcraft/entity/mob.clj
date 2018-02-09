(ns funcraft.entity.mob
  (:require funcraft.entity.entity
            [funcraft.level.macros :refer [>>]]
            funcraft.level.tile.water
            [funcraft.protocols :refer [Movable move]]
                        [funcraft.level.level :as level]
            )
  (:import funcraft.entity.entity.Entity
           funcraft.level.level.Level
           funcraft.level.tile.water.Water))

(def ^:const max-health 10)

(defrecord Mob
    [^Entity entity
     ^int dir
     ^int health
     ^int walk-dist]
  Movable
  (move [this level xa ya]
    (assoc this
           :dir (cond
                  (pos? ^int ya) 0 ; down
                  (neg? ^int ya) 1 ; up
                  (neg? ^int xa) 2 ; right
                  (pos? ^int xa) 3 ; left
                  :else dir)

           :walk-dist (if (or (= 0 xa ya))
                        walk-dist
                        (inc walk-dist))

           :entity (move entity level xa ya)
           ))
  )

(defn swimming? [^Mob mob ^Level level]
  (instance? Water
             (level/get-tile level
                             (>> (get-in mob [:entity :x]) 4)
                             (>> (get-in mob [:entity :y]) 4))))

(defn new [x y]
  (Mob. (Entity. x y 4 3) 0 max-health 0))
