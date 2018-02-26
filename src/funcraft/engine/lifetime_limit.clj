(ns funcraft.engine.lifetime-limit
  (:require [funcraft.engines :as engines])
  (:import funcraft.components.LifetimeLimit))

(defn decrease-lifetime-on-tick
  [this itc [msg]]
  (if (= msg :tick)
    (map #(let [lifetime-limit (get-in itc [% LifetimeLimit :lifetime])]
            (if (zero? lifetime-limit)
              [:remove %]
              [:update [% LifetimeLimit :lifetime] (dec lifetime-limit)]))
         (:ids this)
         )))

(defn new []
  (engines/->Engine
   #{LifetimeLimit}
   #{}
   decrease-lifetime-on-tick))
