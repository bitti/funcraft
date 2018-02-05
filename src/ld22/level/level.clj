(ns ld22.level.level
  (:require [ld22.level.macros :refer [>>]])
  (:import java.io.Writer))

(defprotocol LevelRenderable
  (render [this screen level]))

(defrecord Level [^int w ^int h colors tiles])

(defmethod print-method Level [level ^Writer w]
  (.write w (str (:w level) "x" (:h level))))

(defn get-tile [level x y]
  (if (and (< -1 x (:w level)) (< -1 y (:h level)))
    ((.tiles level) (+ x (* y (:w level))))
    nil))

(defn render-background [level screen x-scroll y-scroll]
  (let [xo (>> x-scroll 4)
        yo (>> y-scroll 4)
        sw (>> (+ (:w screen) 15) 4)
        sh (>> (+ (:h screen) 15) 4)
        w (:w level)
        h (:h level)
        ]
    (doall (for [x (range (max 0 xo) (min w (+ sw xo 1)))
                 y (range (max 0 yo) (min h (+ sh yo 1)))
                 ]
             (render (get-tile level x y) screen level)))
    ))
