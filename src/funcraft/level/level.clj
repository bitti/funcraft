(ns funcraft.level.level
  (:require funcraft.gfx.screen
            clojure.pprint
            [funcraft.level.macros :refer [>>]]
            [funcraft.protocols :refer [Tickable]])
  (:import funcraft.gfx.screen.Screen
           java.io.Writer))

(def ticks (atom 0))

(defprotocol LevelRenderable
  (render [this ^Screen screen ^Level level]))

(defrecord Level [^int w ^int h colors tiles]
  Tickable
  (tick [this entities] (swap! ticks inc))
  )

;; Avoid spamming the REPL with long level outputs
(defmethod print-method Level [level ^Writer w]
  (.write w (str "Level of size "(:w level) "x" (:h level))))

(defmethod clojure.pprint/simple-dispatch Level [level]
  (print level))

(defn get-tile [^Level level ^long x ^long y]
  (if (and (< -1 x (:w level)) (< -1 y (:h level)))
    ((.tiles level) (+ x (* y (.w level))))
    nil))

(defn render-background [^Level level ^Screen screen]
  (let [xo (>> (.x-offset screen) 4)
        yo (>> (.y-offset screen) 4)
        sw (>> (+ (.w screen) 15) 4)
        sh (>> (+ (.h screen) 15) 4)
        w (.w level)
        h (.h level)
        ]
    (doall (for [x (range (max 0 xo) (min w (+ sw xo 1)))
                 y (range (max 0 yo) (min h (+ sh yo 1)))
                 ]
             (render (get-tile level x y) screen level)))
    ))
