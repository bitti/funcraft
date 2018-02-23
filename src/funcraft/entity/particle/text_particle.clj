(ns funcraft.entity.particle.text-particle
  (:require [funcraft.components :as components]
            [funcraft.engines :as engines]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.text :as text]
            [funcraft.level.macros :refer [<<]])
  (:import [funcraft.components LifetimeLimit Message Position Velocity]))

(defn new [msg x y col]
  [(components/->Position x y)
   (components/->LifetimeLimit 60)
   (components/->Message msg col)
   (update (components/new-velocity x y) :zv inc)
   ])

(defn decrease-lifetime-on-tick
  [this itc [msg]]
  (if (= msg :tick)
    (map #(let [lifetime-limit (get-in itc [% LifetimeLimit :lifetime])]
            (if (zero? lifetime-limit)
              [:remove %]
              [:update [% LifetimeLimit :lifetime] (dec lifetime-limit)]))
         (:ids this)
         )))

(def lifetime-limit-engine
  (engines/->Engine
   #{LifetimeLimit}
   #{}
   decrease-lifetime-on-tick))

(defn- render-text-particle [this screen]
  (let [message (get-in this [Message :message])
        x (- (get-in this [Position :x]) (<< (count message) 2))
        y (- (get-in this [Position :y]) (int (get-in this [Velocity :zz])))
        ]
    (text/draw message screen (inc x) (inc y) (colors/index -1 0 0 0))
    (text/draw message screen x y (get-in this [Message :color])))
  )

(defn message-handler [this itc [msg]]
  (case msg
    :tick
    (reduce
     (fn [messages id]
       (let [{{:keys [xx yy zz xv yv zv] :as velocity} Velocity
              {:keys [x y]} Position} (itc id)
             xx (+ xv xx)
             yy (+ yv yy)
             zz (+ zv zz)
             velocity (assoc velocity
                             :xx xx
                             :yy yy
                             :zz zz
                             :zv (- zv 0.15)  ; Gravity
                             )
             velocity (if (neg? zz) ; Touched ground? Then...
                        (assoc velocity
                               :zz 0
                               :zv (* -0.5 zv) ; vertical reflection with 50% damping
                               :xv (*  0.6 xv) ; 40% horizontal velocity damping
                               :yv (*  0.6 yv))
                        velocity)
             ]
         (conj messages
               [:update [id Velocity] velocity]
               [:move id (- (int xx) x) (- (int yy) y)])))
     ()                         ; Start with an empty list of messages
     (:ids this))

    :render
    (map #([:render % render-text-particle]) (:ids this))
    nil)
  )

(def text-particle-engine
  (engines/->Engine
   #{Position
     Message
     Velocity}
   #{}
   message-handler
   ))

