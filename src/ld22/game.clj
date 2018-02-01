(ns ld22.game
  (:require [clojure.java.io :refer [resource]]
            [ld22.gfx.colors :as colors]
            [ld22.gfx.screen :as screen]
            [ld22.gfx.sprite-sheet :as sprite-sheet]
            [ld22.level.level :refer [grass-color]]
            [ld22.perfgraph :as pergraph])
  (:import java.awt.BorderLayout
           [java.awt.image BufferedImage BufferStrategy]
           java.util.Random
           javax.imageio.ImageIO
           javax.swing.JFrame))

(def ^:const game-name "Minicraft")
(def ^:const height 240)
(def ^:const width (int (/ (* height 16) 9)))
(def ^:const nanos-per-tick (long (/ 1e9 60)))
(def ^:const scale 3)

(def image ^BufferedImage (new BufferedImage width height BufferedImage/TYPE_INT_RGB))
(def sprite-sheet (sprite-sheet/new-sheet (ImageIO/read (resource "icons.png"))))
(def screen (screen/new width height sprite-sheet))
(def colors (int-array (colors/init)))

;; Not using a macro here preserves type hints
(def pixels ^ints (.getData ^java.awt.image.DataBufferInt
                            (.getDataBuffer (.getRaster ^BufferedImage image))))
(def running (atom true))

(def c ^long (atom 0))

(def gr (Random.))

(defn render [bs]
  (swap! c #(mod (dec ^int %) (* 2 height)))
  (dotimes [y 32]
    (let [yoffs (* y 8)
          tile-yoffs (* 32 y)
          cc @c
          ]
      (dotimes [x 32]
        (screen/render screen (bit-shift-left x 3) yoffs
                       (+ x tile-yoffs)
                       0 cc
                       (colors/index 000 grass-color 404 555)
                       ))))

  (let [w (screen :w)
        screen-pixels ^ints (screen :pixels)]
    (dotimes [y (screen :h)]
      (let [yoffs (* y ^int w)]
        (dotimes [x w]
          (->> (+ yoffs x)
               (aget screen-pixels)
               (aget ^ints colors)
               (aset ^ints pixels (+ yoffs x)))
          )
        )))

  (doto (.getDrawGraphics ^BufferStrategy bs)
    (.drawImage image 0 0 (* scale width) (* scale height) nil)
    (.dispose))
  (.show ^BufferStrategy bs))

;(def game (delay (doto (new Canvas))))

(def frame
  (delay (doto (new JFrame game-name)
           (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
           (.setVisible true)
           (.setSize (* scale width) (* scale height))
           (.setLayout (new BorderLayout))
           (.createBufferStrategy 3)
           (.setLocationRelativeTo nil)
           )))

(def bs (delay ^BufferStrategy (.getBufferStrategy ^JFrame @frame)))

(defn run [{:keys [lt              ; Time since last tick/render
                   nt              ; Time since last FPS report
                   frames          ; Count of frames since last FPS report
                   sleeptime       ; Idle time since last FPS report
                   ] :as state}]
  (if @running (send *agent* run))
  (let [now (System/nanoTime)
        unprocessed (- now ^long lt)]
    (cond
      (< unprocessed nanos-per-tick)
      (do
        (Thread/sleep (int (/ (- nanos-per-tick unprocessed) 1e6)))
        (assoc state :sleeptime (+ sleeptime (- (System/nanoTime) now))))
      
      (> (- now nt) 9.99e8)
      (do
        (printf " %d FPS Capacity %.2f\n" frames (double (/ sleeptime (- now nt))))
        (flush)
        (assoc state
               :nt now
               :frames 0
               :sleeptime 0))
      
      :else
      (do
        (render @bs)
        (send pergraph/a pergraph/repaint (int (/ (- (System/nanoTime) now) 1e6)))
        (assoc state :lt now :frames (inc frames))
        ))))

(def game-loop (delay (agent {:lt (System/nanoTime) :nt (System/nanoTime) :frames 0 :sleeptime 0})))

(defn start []
  (reset! running true)
  (send @game-loop run))

(defn stop []
  (reset! running false))

