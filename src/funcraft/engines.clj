(ns funcraft.engines
  (:require [funcraft.entity.mob :as mob]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.input-handler :as input-handler]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.macros :refer [>>]])
  (:import [funcraft.components Control Dimension Direction Position Sprite Walk Attack]
           java.util.Random))

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
  (when (= msg :move)
    (when ((:ids engine) id)
      (let [component (get-in itc [id Position])]
        [:update [id Position] (merge-with + component {:x dx :y dy})]))))

(def move-engine
  (->Engine #{Position Direction} #{} change-position-when-move))

(def render-sprite-engine
  (->Engine #{Position Sprite} #{}
            (fn [this itc [msg]]
              (if (= msg :render)
                (map (fn [components]
                       [:render (partial (get-in components [Sprite :render-fn]) components)])
                     (map itc (:ids this)))
                ))))

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

