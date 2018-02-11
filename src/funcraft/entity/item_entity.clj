(ns funcraft.entity.item-entity
  (:require [funcraft.entity.entity :refer [->Entity]]
            [funcraft.item.item]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.level :refer [LevelRenderable]]
            [funcraft.protocols :as entity :refer [Tickable]])
  (:import funcraft.entity.entity.Entity
           funcraft.item.item.Item
           java.util.Random))

(def ^Random random (Random.))

(declare compare-to)

(defrecord ItemEntity
    [^Item item
     ^Entity entity
     ^double xx
     ^double yy
     ^double zz
     ^double xa
     ^double ya
     ^double za
     ^int life-time
     ]

  Comparable
  (compareTo [this other]
    (compare-to this other))
  )

(defn compare-to [this other]
  (if (identical? this other)
    0
    (case (.compareTo other (get-in this [:entity :y]))
      -1  1
      0 (if (satisfies? LevelRenderable other)
          (.compareTo (System/identityHashCode this) (System/identityHashCode other))
          0)
      1 -1
      )))

(defn new [item x y]
  (->ItemEntity
   item
   (->Entity x y 3 3)
   x y 2
   (* (.nextGaussian random) 0.3)    ; x acceleration
   (* (.nextGaussian random) 0.2)    ; y acceleration
   (inc (* (.nextFloat random) 0.7)) ; z acceleration
   (+ 600 (.nextInt random 60))      ; lifetime in ticks
   ))

(extend-type ItemEntity
  Tickable
  (tick [this level]
    (let [life-time (:life-time this)
          entities (disj (:entities level) this)]
      (if (zero? life-time)
        (assoc level :entities entities)
        (let [{:keys [entity xa ya za xx yy zz]} this
              {:keys [x y]} entity
              xx (double (+ xa xx))
              yy (double (+ ya yy))
              zz (double (+ za zz))
              nx (int xx)
              ny (int yy)
              dnx (- nx x)
              dny (- ny y)
              ]
          (assoc level :entities
                 (conj entities
                       (-> this
                           (assoc :xx xx :yy yy :zz zz)

                           ;; Handle ground collision
                           ((fn [this]
                              (if (neg? zz)           ; Touched ground? Then...
                                (assoc this
                                       :zz 0
                                       :za (* -0.5 za) ; vertical reflection with 50% damping
                                       :xa (*  0.6 xa) ; 40% horizontal velocity damping
                                       :ya (*  0.6 ya))
                                this)))

                           ;; Next position
                           (update :za #(- % 0.15))       ; Gravity
                           (update :entity #(entity/move % level dnx dny))
                           (update :life-time dec)

                           ;; Adjust xx and yy according to actual movement
                           ((fn [this]
                              (update this :xx #(+ % (- (.. this entity x) nx)))
                              (update this :yy #(+ % (- (.. this entity y) ny)))))
                           )))))))

  LevelRenderable
  (render [this screen level]
    (let [{:keys [entity life-time item zz]} this
          {:keys [x y]} entity]
      (when-not (and (<= life-time 120) (even? (int (/ life-time 6))))
        (screen/render screen (- x 4) (- y 4) (:sprite item) (colors/index -1 0 0 0))
        (screen/render screen (- x 4) (- y 4 (int zz)) (:sprite item) (:color item))
        )))
  )
