(ns funcraft.engines
  (:require [clojure.set :as set]
            [funcraft.level.level :as level]
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
  (empty?
   (set/difference
    (:component-types engine)
    (set (map type components)))))

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
