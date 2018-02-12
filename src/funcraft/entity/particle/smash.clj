(ns funcraft.entity.particle.smash
  (:require [funcraft.gfx.colors :as color]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :refer [LevelRenderable]]
            [funcraft.protocols :refer [Tickable]])
  (:import funcraft.entity.entity.Entity))

(def ^:const col (color/index -1 555 555 555))
(def ^:const sprite (+ 5 (* 12 32)))

(defrecord Smash
    [^Entity entity
     ^int time]

  LevelRenderable
  (render [this screen _]
    (let [{:keys [x y]} entity]
      (screen/render screen (- x 8) (- y 8) sprite col :mirror-y true)
      (screen/render screen (- x 0) (- y 8) sprite col [:mirror-x true :mirror-y true])
      (screen/render screen (- x 8) (- y 0) sprite col)
      (screen/render screen (- x 0) (- y 0) sprite col :mirror-x true))
    )

  Comparable
  (compareTo [this other]
    (if (= this other)
      0
      (case (.compareTo other (.y entity))
        -1 1
        0 (if (satisfies? LevelRenderable other)
            (.compareTo (System/identityHashCode this) (System/identityHashCode other))
            0)
        1 -1)
      )))

(defn new [x y]
  (Smash. (Entity. x y 0 0) 0))

(extend-type Smash

  Tickable
  (tick [this level]
    (let [level (update level :entities disj this)]
      (if (< (.time this) 10)
        (update level :entities conj (update this :time inc))
        level)))

  )
