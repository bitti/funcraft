(ns funcraft.level.level
  (:require clojure.pprint
            [funcraft.level.macros :refer [>>]]
            [funcraft.gfx.screen]
            [funcraft.protocols :as protocols :refer [Renderable Tickable]])
  (:import funcraft.gfx.screen.Screen
           java.io.Writer))

(defprotocol LevelRenderable
  (render [this ^Screen screen ^Level level]))

(defn tick [level]
  (-> level
      (update :ticks inc)
      (assoc :entities
             (for [entity (:entities level)]
               (protocols/tick entity level)))))

(declare get-tile)

(defrecord Level
    [^int w
     ^int h
     colors
     ^long ticks
     tiles
     entities]
  Renderable
  (render [level screen]
    (let [xo (>> (.x-offset ^Screen screen) 4)
          yo (>> (.y-offset ^Screen screen) 4)
          sw (>> (+ (.w ^Screen screen) 15) 4)
          sh (>> (+ (.h ^Screen screen) 15) 4)
          ]
      (doall (for [x (range (max 0 xo) (min w (+ sw xo 1)))
                   y (range (max 0 yo) (min h (+ sh yo 1)))]
               (render (get-tile level x y) screen level)))
      (doall
       (for [entity entities]
         (render entity screen level)))
      )))

(defn get-tile [^Level level ^long x ^long y]
  (if (and (< -1 x (:w level)) (< -1 y (:h level)))
    ((.tiles level) (+ x (* y (.w level))))
    nil))

;; Avoid spamming the REPL with long level outputs
(defmethod print-method Level [level ^Writer w]
  (.write w (str "Level of size "(:w level) "x" (:h level))))

(defmethod clojure.pprint/simple-dispatch Level [level]
  (print level))
