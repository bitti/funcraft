(ns funcraft.engine.attack
  (:require [funcraft.engines :refer [->Engine]])
  (:import [funcraft.components Attack Walk]))

;; Attack time is used for bow animation but the start attack time can
;; indicate an actual attack
(def ^:const attack-start-time 10)

(defn new []
  (->Engine #{Attack} #{}
            (fn [this itc [msg id]]
                 (case msg
                   :tick
                   (for [entity-id (:ids this)
                         :let [attack-time (get-in itc [entity-id Attack :attack-time])]
                         :when (pos? attack-time)]
                     [:update [entity-id Attack :attack-time] (dec attack-time)])

                   :attack
                   (concat
                    (if-let [walk (get-in itc [id Walk])]
                      ;; Attack animation
                      (list [:update [id Walk :distance] (+ (:distance walk) 8)]))
                    (list [:update [id Attack :attack-time] attack-start-time]))

                   nil))))
