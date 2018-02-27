(ns funcraft.engine.item
  (:require [funcraft.engines :as engines])
  (:import [funcraft.components Position Velocity]))

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
    nil)
  )

(defn new []
  (engines/->Engine
   #{Position
     Velocity}
   #{}
   message-handler
   ))
