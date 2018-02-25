(ns funcraft.engines
  (:require [funcraft.level.level :as level]
            [funcraft.level.macros :refer [>>]]
            [funcraft.level.tile.water]
            [funcraft.protocols :as protocols])
  (:import [funcraft.components Control Dimension Direction Position Sprite Walk]
           funcraft.level.level.Level
           funcraft.level.tile.water.MaySwim
           funcraft.protocols.MayPass))

(defprotocol EngineProtocol
  (add-entity [this id components])
  (remove-entity [this id])
  (receive-msg [this itc message])
  )

(defn eligible-entity? [engine components]
  (not
   (seq
    (clojure.set/difference
     (:component-types engine)
     (set (map type components))))))

(defrecord Engine [component-types ids receiver]
  EngineProtocol
  (add-entity [this id components]
    (if (eligible-entity? this components)
      (update this :ids conj id)
      this))

  (remove-entity [this id]
    (update this :ids disj id))

  (receive-msg [this itc message]
    (receiver this itc message)))

(defn send-message [engines itc message]
  (reduce (fn [new-messages engine]
            (if-let [messages (receive-msg engine itc message)]
              (if (list? messages)
                (concat new-messages messages)
                (conj new-messages messages))
              new-messages))
          ()
          engines))

(defn change-position-when-move
  [engine itc [msg id dx dy]]
  (if (= msg :move)
    (if ((:ids engine) id)
      (let [component (get-in itc [id Position])]
        [:update [id Position] (merge-with + component {:x dx :y dy})]))))

(def move-engine
  (->Engine #{Position} #{} change-position-when-move))

(defn change-walk-direction-and-distance-when-move
  [engine itc [msg id dx dy]]
  (if (= msg :move)
    (if ((:ids engine) id)
      (list [:update [id Walk :distance] (inc (get-in itc [id Walk :distance]))]
            [:update [id Direction :direction]
             (cond
               (pos? ^int dy) 0 ; down
               (neg? ^int dy) 1 ; up
               (neg? ^int dx) 2 ; right
               (pos? ^int dx) 3 ; left
               )
             ])
        )))

(def walk-animation-engine
  (->Engine #{Direction Walk} #{} change-walk-direction-and-distance-when-move))

(defn render-by-sprite-fn [this itc [msg]]
  (if (= msg :render)
    (map (fn [id]
           [:render id (get-in itc [id Sprite :render-fn])])
         (:ids this))
    ))

(def render-sprite-engine
  (->Engine #{Position Sprite} #{} render-by-sprite-fn))

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

(defn render-by-level-fn [this itc [msg & args]]
  ;; Currently only one level is supported
  (case msg
    :tick
    (let [id (first (:ids this))]
      ;; Increase tick counter which is used for animations
      [:update [id Level :ticks] (inc (get-in itc [id Level :ticks]))])

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
      (concat
         (if-not (can-move-in-dir? level x y xr yr xa 0)
           (list [:merge [entity-id Position :x] (get-in entity [Position :x])]))
         (if-not (can-move-in-dir? level x y xr yr 0 ya)
           (list [:merge [entity-id Position :y] (get-in entity [Position :y])]))))

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
    nil))

(def render-level-engine
  (->Engine #{Level} #{} render-by-level-fn))

(def control-engine
  (->Engine #{Control} #{}
            (fn [this itc [msg]]
              (if (= msg :tick)
                (map (fn [id]
                       ((get-in (itc id) [Control :input-handler-fn]) id))
                     (:ids this))
                ))))

(def id-count (atom 0))

(defn new-entity [id->type->component engines components]
  (let [id (swap! id-count inc)
        type->component
        (reduce (fn [type->component component]
                  (assoc type->component (type component) component))
                {} components)]
    [(assoc id->type->component id type->component)
     (map (fn [^Engine engine]
            (add-entity engine id components))
          engines)]))
