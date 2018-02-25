(ns funcraft.engine.collision
  (:require [funcraft.engines :refer [->Engine]])
  (:import [funcraft.components Dimension Position]))

(defn detect-collision
  [engine itc [msg id dx dy]]
  (if (= msg :move)
    (if-let [entity (itc id)]
      (let [{{:keys [x y]} Position {:keys [xr yr]} Dimension} entity
            x (+ x dx)
            y (+ y dy)
            x0 (- x xr)
            y0 (- y yr)
            x1 (+ x xr)
            y1 (+ y yr)]
        (reduce (fn [collisions other-entity-id]
                  (let [other-entity (itc other-entity-id)
                        xor (get-in other-entity [Dimension :xr])
                        yor (get-in other-entity [Dimension :yr])
                        xo (get-in other-entity [Position :x])
                        yo (get-in other-entity [Position :y])
                        xo0 (- xo xor)
                        yo0 (- yo yor)
                        xo1 (+ xo xor)
                        yo1 (+ yo yor)]
                    (if (and (< x0 xo1) (< xo0 x1) (< y0 yo1) (< yo0 y1))
                      (conj collisions [:collision id other-entity-id])
                      collisions)))
                ()
                (disj (:ids engine) id)
                ))
      )))

(defn new [] (->Engine #{Position Dimension} #{} detect-collision))
