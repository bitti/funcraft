(ns ld22.game
  (:require [clojure.java.io :refer [resource]]
            [ld22.entity.player :as player]
            [ld22.gfx.colors :as colors]
            [ld22.gfx.input-handler :as input-handler]
            [ld22.gfx.screen :as screen]
            [ld22.gfx.sprite-sheet :as sprite-sheet]
            [ld22.level.level :as level]
            [ld22.level.macros :refer [>>]]
            [ld22.protocols :as protocols :refer [tick Tickable]])
  (:import java.awt.BorderLayout
           [java.awt.image BufferedImage BufferStrategy]
           java.util.Random
           javax.imageio.ImageIO
           javax.swing.JFrame
           ld22.entity.player.Player))

(def ^:const game-name "Minicraft")
(def ^:const height 200)
(def ^:const width 267 #_(int (/ (* height 16) 9)))
(def nanos-per-tick (long (/ 1e9 60)))
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

(defrecord Game [state entities]
  Tickable
  (tick [^Game this entities]
    (update entities :player tick entities)))

(defn new-game []
  (Game.
   {:lt (System/nanoTime) ; time since last tick/render
    :nt (System/nanoTime) ; time since last FPS report
    :frames 0             ; Count of frames since last FPS report
    :sleeptime 0          ; idle time since last FPS report
    }
   {:level (level/new-level 128 128)
    :player (player/new-player (.nextInt gr (* 128 16)) (.nextInt gr (* 128 16)))}))

(defn render [bs entities]
  (let [player (:player entities)
        w (:w screen)
        h (:h screen)
        x (- (int (get-in player [:mob :entity :x])) (>> (int w)))
        x (min (max 0 x) (- (* 16 128) w))
        y (- (int (.. player mob entity y)) (>> (int h)))
        y (min (max 0 y) (- (* 16 128) h))
        screen (assoc screen
                      :x-offset x
                      :y-offset y)
        ]
    (level/render-background (:level entities) screen x y)
    (protocols/render player screen))
    

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
