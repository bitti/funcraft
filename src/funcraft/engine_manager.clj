(ns funcraft.engine-manager
  (:require [funcraft.engines :as engines]
            [funcraft.protocols :refer [Renderable Tickable]])
  (:import funcraft.components.Position
           funcraft.level.level.Level
           java.lang.AssertionError))

(declare send-message new-entity)

(defrecord EngineManager [engines itc]
  Tickable
  (tick
    [this]
    "Send a tick message to all engines. Returns the updated engine manager"
    (loop [messages '([:tick])
           updates ()
           merges ()
           removes ()
           adds ()
           loops 0]
      (if (> loops 10)
        (throw (AssertionError. "too many message loops"))
        (if (seq messages)
          (let [messages (concat (rest messages)
                                 (send-message this (first messages)))
                {new-messages :messages
                 new-updates :updates
                 new-merges :merges
                 new-removes :removes
                 new-adds :adds
                 }
                (group-by
                 (fn [message]
                   (case (first message)
                     :update :updates
                     :merge :merges
                     :remove :removes
                     :add :adds
                     :messages))
                 messages)
                ]
            (recur (concat new-messages)
                   (concat updates new-updates)
                   (concat merges new-merges)
                   (concat removes new-removes)
                   (concat adds new-adds)
                   (inc loops)))
          (let [itc (reduce #(apply assoc-in %1 (rest %2))
                            itc
                            updates)
                itc (reduce #(apply assoc-in %1 (rest %2))
                            itc
                            merges)
                remove-ids (map second removes)
                engines (map (fn [engine]
                               (reduce (fn [engine entity-id]
                                         (engines/remove-entity engine entity-id))
                                       engine
                                       remove-ids))
                             engines)
                itc (apply dissoc itc remove-ids)
                ]

            (reduce (fn [entity-manager [_ entity]] (new-entity this entity))
                    (assoc this
                           :engines engines
                           :itc itc)
                    adds)
            )))))

  Renderable
  (render
    [this screen]
    "Send a render message to all engines. Collect resulting render
    messages and use them to render to screen. Renderable entities
    must have at least a Position"
    (let [render-messages (send-message this [:render])
          sorted-by-y
          (reduce
           (fn [sorted [msg id render-fn]]
             (if (= msg :render-level)
               ;; Handle level rendering special till a better
               ;; solution is found. The sort key should ensure that
               ;; we render the level first
               (assoc sorted [##-Inf id] render-fn)
               (do
                 (assert (= :render msg))
                 ;; We sort by y coord and by id on a tie
                 (assoc sorted [(get-in itc [id Position :y]) id] render-fn))))
           (sorted-map)
           render-messages)
          [[_ level-id] level-render-fn] (first sorted-by-y)
          {level Level} (itc level-id)
          ]
      (level-render-fn screen)
      (doall
       (for [[[_ id] render-fn] (rest sorted-by-y)]
         (render-fn (itc id) screen level))))
    screen)
  )

(defn new [engines]
  (->EngineManager engines {}))

(defn send-message [^EngineManager this message]
  "Send a message to all engines and collect answer messages"
  (reduce
   (fn [answer-messages engine]
     (let [messages (engines/receive-msg engine (:itc this) message)]
       (if (seq messages)
         (if (seq? messages)
           (concat answer-messages messages)
           (conj answer-messages messages))
         answer-messages))
     )
   ()
   (:engines this)))

(defn new-entity [^EngineManager this components]
  (let [[itc engines] (engines/new-entity (.itc this) (.engines this) components)]
    (assoc this
           :engines engines
           :itc itc)))
