(ns funcraft.engine.control
  (:require [funcraft.engines :refer [->Engine]])
  (:import funcraft.components.Control))

(defn new []
  (->Engine #{Control} #{}
            (fn [this itc [msg]]
              (if (= msg :tick)
                (mapcat (fn [id]
                          ((get-in (itc id) [Control :input-handler-fn]) id))
                        (:ids this))
                ))))
