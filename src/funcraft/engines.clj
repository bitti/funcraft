(ns funcraft.engines
  (:require [funcraft.protocols :as protocols]
            [funcraft.components]
            [funcraft.level.level])
  (:import [funcraft.components Control Direction Position Sprite Walk]
           funcraft.level.level.Level))

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

(defn render-by-level-fn [this itc [msg]]
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
