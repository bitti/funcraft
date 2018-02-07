(ns ld22.level.level
  (:require [ld22.level.macros :refer [>>]]
            [ld22.gfx.screen]
            [ld22.protocols :refer [Tickable]]
            )
  (:import java.io.Writer
           ld22.gfx.screen.Screen))

(def ticks (atom 0))

(defprotocol LevelRenderable
  (render [this ^Screen screen ^Level level]))

(defrecord Level [^int w ^int h colors tiles]
  Tickable
  (tick [this entities] (swap! ticks inc)))

(defmethod print-method Level [level ^Writer w]
  (.write w (str (:w level) "x" (:h level))))

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
