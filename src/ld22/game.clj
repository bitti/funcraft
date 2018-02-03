(ns ld22.game
  (:require [clojure.java.io :refer [resource]]
            [ld22.entity.player :as player]
            [ld22.gfx.colors :as colors]
            [ld22.gfx.input-handler :as input-handler]
            [ld22.gfx.screen :as screen]
            [ld22.gfx.sprite-sheet :as sprite-sheet]
            [ld22.level.level :as level]
            [ld22.level.tile.grass :refer [grass-color]]
            [ld22.protocols :as protocols :refer [tick Tickable]])
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
(def gr (Random.))

(defrecord Game [state entities])

(extend-type Game
  Tickable
  (tick [^Game this entities]
    (assoc entities
           :player (tick (:player entities) entities))))

(defn new-game []
  (Game.
   {:lt (System/nanoTime) ; time since last tick/render
    :nt (System/nanoTime) ; time since last FPS report
    :frames 0             ; Count of frames since last FPS report
    :sleeptime 0}         ; Idle time since last FPS report
   {:player (player/new-player (/ width 2) (/ height 2))}))

(defn render [bs entities]
  (let [player (:player entities)
        x (- (.. player mob entity x) (/ width 2))
        y (- (.. player mob entity y) (/ height 2))
        ]
    (dotimes [yt 32]
      (let [yoffs (bit-shift-left yt 3)
            tile-yoffs (* 32 yt)]
        (dotimes [xt 32]
          (screen/render screen (bit-shift-left xt 3) yoffs
                         (+ xt tile-yoffs)
                         x y
                         (colors/index 000 grass-color 404 555)
                         ))))
    (protocols/render player screen)
    )

  (let [w (:w screen)
        screen-pixels ^ints (:pixels screen)]
    (dotimes [y (:h screen)]
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

(def frame
  (delay (doto (new JFrame game-name)
           (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
           (.setVisible true)
           (.setSize (* scale width) (* scale height))
           (.setLayout (new BorderLayout))
           (.createBufferStrategy 3)
           (.setLocationRelativeTo nil)
           (.addKeyListener input-handler/key-listener)
           )))

(def bs (delay ^BufferStrategy (.getBufferStrategy ^JFrame @frame)))

(defn run [^Game {{:keys [ lt
                    ^int nt
                    ^int frames
                    sleeptime
                    ] :as state} :state
            entities :entities :as game}]
  (if @running (send *agent* run))
  (let [now (System/nanoTime)
        unprocessed (- now lt)]
    (cond
      (< unprocessed nanos-per-tick)
      (do
        (Thread/sleep (int (/ (- nanos-per-tick unprocessed) 1e6)))
        (assoc game
               :state (assoc state :sleeptime (+ sleeptime (- (System/nanoTime) now)))))

      (> (- now nt) 9.99e8)
      (do
        (printf " %d FPS Capacity %.2f\n" frames (double (/ sleeptime (- now nt))))
        (flush)
        (assoc game
               :state (assoc state
                             :nt now
                             :frames 0
                             :sleeptime 0)))
      :else
      (do
        (render @bs entities)
        (assoc game
               :state (assoc state :lt now :frames (inc frames))
               :entities (tick game entities))
        ))))

(defn throw-error [a e]
  (throw e))

(def game-loop
  (delay
   (agent (new-game)
          :erro-handler throw-error
          :error-mode :fail
    )))

(defn start []
  (reset! running true)
  (send @game-loop run))

(defn stop []
  (reset! running false))
