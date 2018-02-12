(ns funcraft.entity.particle.text-particle
  (:require [funcraft.entity.item-entity :as item-entity]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.text :as text]
            [funcraft.item.item :as item]
            [funcraft.level.level :refer [LevelRenderable]]
            [funcraft.level.macros :refer [<<]]
            [funcraft.protocols :as protocols :refer [Tickable]])
  (:import funcraft.entity.item_entity.ItemEntity))

(defrecord TextParticle
    [^ItemEntity item-entity
     ^String message
     ]

  Tickable
  (tick [this level]
    (let [item-entity
          (first (:entities
                  (protocols/tick item-entity (assoc level :entities #{}))))
          entities (disj (:entities level) this)]
      (assoc level
             :entities
             (if item-entity
               (conj entities (assoc this :item-entity item-entity))
               entities))))

  Comparable
  (compareTo [this other]
    (if (identical? this other)
      0
      (case (.compareTo ^Comparable other (get-in this [:item-entity :entity :y]))
        -1  1
        0 (if (satisfies? LevelRenderable other)
            (.compareTo (System/identityHashCode this) (System/identityHashCode other))
            0)
        1 -1
        ))
    ))

(defn new [msg x y col]
  (->TextParticle (update (assoc (item-entity/new (item/->Item 0 col) x y)
                                 :life-time 60)
                          :za inc)
                  msg))

(extend-type TextParticle
  LevelRenderable
  (render [this screen level]
    (let [x (- (get-in this [:item-entity :entity :x]) (<< (count (:message this)) 2))
          y (- (get-in this [:item-entity :entity :y]) (int (get-in this [:item-entity :zz])))
          ]
      (text/draw (:message this) screen (inc x) (inc y) (colors/index -1 0 0 0))
      (text/draw (:message this) screen x y (get-in this [:item-entity :item :color])))
    )
  )
