(ns funcraft.engine.message-monitor
  (:require [funcraft.engines :refer [->Engine]]))

(defn new []
  (->Engine #{} #{} (fn [engine itc msg]
                      (case (first msg)
                        (:move :update :collision :merge)
                        (println msg)
                        nil
                        ))))
