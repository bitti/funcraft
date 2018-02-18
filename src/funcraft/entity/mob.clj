(ns funcraft.entity.mob
  (:require [funcraft.level.level :as level]
            [funcraft.level.tile.water]
            [funcraft.level.macros :refer [>>]]
            )
  (:import funcraft.components.Position
           funcraft.level.level.Level
           funcraft.level.tile.water.Water))

(defn swimming? [^Position pos ^Level level]
  (instance? Water
             (level/get-tile level
                             (>> (:x pos) 4)
                             (>> (:y pos) 4))))
