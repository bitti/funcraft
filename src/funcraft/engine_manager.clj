(ns funcraft.engine-manager
  (:require [funcraft.engines :as engines]
            [funcraft.protocols :refer [Renderable Tickable]])
  (:import java.lang.AssertionError))

(declare send-message)

(defrecord EngineManager [engines itc screen]
  Tickable
  (tick
    [this level]
    "Send a tick message to all engines. Return the updated entity id
    to component mapping"
    (loop [messages (send-message this [:tick])
           updates ()
           merges ()
           loops 0]
      (if (> loops 10)
        (throw (AssertionError. "too many message loops"))
        (if (seq messages)
          (let [messages (concat (rest messages)
                                 (send-message this (first messages)))
                {new-messages :messages
                 new-updates :updates
                 new-merges :merges}
                (group-by
                 (fn [message]
                   (case (first message)
                     :update :updates
                     :merge :merges
                     :messages))
                 messages)
                ]
            (recur (concat new-messages)
                   (concat updates new-updates)
                   (concat merges new-merges)
                   (inc loops)))
          (let [itc (reduce #(apply assoc-in %1 (rest %2))
                            itc
                            updates)]
            (reduce #(apply assoc-in %1 (rest %2))
                    itc
                    merges)
            )))))

  Renderable
  (render
    [this screen]
    "Send a render message to all engines. Collect resulting render
    messages and use them to render to screen"
    ))

(defn send-message [^EngineManager this message]
  "Send a message to all engines and collect answer messages"
  (reduce
   (fn [answer-messages engine]
     (let [messages (engines/receive-msg engine (:itc this) message)]
       (if (seq messages)
         (if (seq? messages)
           (concat answer-messages messages)
           (conj answer-messages messages))
         answer-messages)))
   ()
   (:engines this)))
