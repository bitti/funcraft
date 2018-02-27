(ns funcraft.engine.level
  (:require [funcraft.engines :refer [->Engine]]
            [funcraft.level.level :as level :refer [LevelTickable]]
            [funcraft.level.level-gen :refer [even-map-distribution]]
            [funcraft.level.macros :refer [>>]]
            [funcraft.level.tile.tree :as tree :refer [Hurtable]]
            [funcraft.protocols :as protocols])
  (:import [funcraft.components Attack Dimension Direction Position]
           funcraft.level.level.Level
           funcraft.level.tile.water.MaySwim
           funcraft.protocols.MayPass
           java.util.Random))

(def ^Random random (Random.))

(defn can-move-in-dir? [level x y xr yr xa ya]
  (let [x0 (>> (- x xr) 4)
        y0 (>> (- y yr) 4)
        x1 (>> (+ x xr) 4)
        y1 (>> (+ y yr) 4)
        x (+ x xa)
        y (+ y ya)
        xt0 (>> (- x xr) 4)
        yt0 (>> (- y yr) 4)
        xt1 (>> (+ x xr) 4)
        yt1 (>> (+ y yr) 4)
        ]
    (every? #(instance? MayPass %)
            (for [yt (range yt0 (inc yt1))
                  xt (range xt0 (inc xt1))
                  :when
                  (not (and (<= x0 xt x1)
                            (<= y0 yt y1)))
                  ]
              (level/get-tile level xt yt)))))

(defn level-message-handler [this itc [msg & args]]
  ;; Currently only one level is supported
  (case msg
    :tick
    (let [id (first (:ids this))
          level (get-in itc [id Level])]

      (concat

       ;; Give 50 random tiles a chance to tick
       (for [[x y] (take 50 (even-map-distribution))
             :let [tile (level/get-tile level x y)
                   update (and (satisfies? LevelTickable tile)
                               (level/tick tile level id))]
             :when update]
         update)

       ;; Increase tick counter which is used for animations
       (list [:update [id Level :ticks] (inc (get-in itc [id Level :ticks]))])))

    :render
    (let [id (first (:ids this))]
      [:render-level id (fn [screen]
                          (protocols/render ^Level (get-in itc [id Level]) screen))])

    :move
    (let [level-id (first (:ids this))
          level (get-in itc [level-id Level])
          [entity-id xa ya] args
          entity (itc entity-id)
          {{:keys [x y]} Position {:keys [xr yr]} Dimension} entity
          ]
      (if xr
        (concat
         (if-not (can-move-in-dir? level x y xr yr xa 0)
           (list [:merge [entity-id Position :x] x]))
         (if-not (can-move-in-dir? level x y xr yr 0 ya)
           (list [:merge [entity-id Position :y] y])))))

    :collision
    (let [id (first (:ids this))
          [entity-id level-id] args]
      (if (= id level-id)
        (let [tile (level/get-tile (get-in itc [id Level])
                                   (>> (get-in itc [entity-id Position :x]) 4)
                                   (>> (get-in itc [entity-id Position :y]) 4))]
          ;; When swimming, cut speed in half
          (if (and (instance? MaySwim tile) (even? (get-in itc [id Level :ticks])))
            [:merge [entity-id Position] (get-in itc [entity-id Position])]
            ))))

    :attack
    (let [level-id (first (:ids this))
          level (get-in itc [level-id Level])
          [entity-id] args
          {{x :x y :y} Position
           {dir :direction} Direction
           {r :range} Attack} (itc entity-id)
          yo 2 ; vertical offset

          [^int x ^int y]
          (case dir
            0 [x (+ y r yo)]
            1 [x (+ (- y r) yo)]
            2 [(- x r) y]
            3 [(+ x r) y]
            )
          xt (>> x 4)
          yt (>> y 4)
          tile (level/get-tile level xt yt)]
      (when (satisfies? Hurtable tile)
        (tree/hurt tile level level-id (inc (.nextInt random 3)))))
    nil))

(defn new []
  (->Engine #{Level} #{} level-message-handler))
