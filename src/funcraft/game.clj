(ns funcraft.game
  (:require [clojure.java.io :refer [resource]]
            [funcraft.entity.player :as player]
            [funcraft.gfx.colors :as colors]
            [funcraft.gfx.input-handler :as input-handler]
            [funcraft.gfx.screen :as screen]
            [funcraft.gfx.sprite-sheet :as sprite-sheet]
            [funcraft.gfx.text :as text]
            [funcraft.level.level :as level]
            [funcraft.level.level-gen :as level-gen]
            [funcraft.level.macros :refer [<< >>]]
            [funcraft.protocols :as protocols :refer [tick Tickable]])
  (:import funcraft.entity.entity.Entity
           funcraft.entity.mob.Mob
           funcraft.entity.player.Player
           funcraft.gfx.screen.Screen
           funcraft.level.tile.grass.Grass
           java.awt.BorderLayout
           [java.awt.image BufferedImage BufferStrategy]
           java.util.Random
           javax.imageio.ImageIO
           javax.swing.JFrame))

(def ^:const game-name "Funcraft")
(def ^:const height 200)
(def ^:const width (int (/ (* height 16) 9)))
(def ^:const nanos-per-tick (long (/ 1e9 60)))
(def ^:const scale 4)

(def ^BufferedImage image (BufferedImage. width height BufferedImage/TYPE_INT_RGB))
(def sprite-sheet (sprite-sheet/new-sheet (ImageIO/read (resource "icons.png"))))
(def ^Screen screen (screen/new width height sprite-sheet))
(def ^"[I" colors (int-array (colors/init)))

;; Not using the .. macro here preserves type hints
(def ^"[I" pixels (.getData ^java.awt.image.DataBufferInt
                            (.getDataBuffer (.getRaster image))))
(def running (atom true))

(def frame
  (delay (doto (new JFrame game-name)
           (.setDefaultCloseOperation

            ;; Misuse sytem property to detect if we got started in a REPL
            (if (System/getProperty "funcraft.version")
              JFrame/DISPOSE_ON_CLOSE
              JFrame/EXIT_ON_CLOSE))

           (.setVisible true)
           (.setSize (* scale width) (* scale height))
           (.setLayout (new BorderLayout))
           (.createBufferStrategy 3)
           (.setLocationRelativeTo nil)
           (.addKeyListener input-handler/key-listener)
           )))

(defrecord Game [state level]
  Tickable
  (tick [this level]
    (level/tick level)
    ))

(defn new-game []
  (let [level (level-gen/new-level)

        ;; Player start position is a random grass tile
        [px py] (some
                 (fn [[^int x ^int y]]
                   (if (instance? Grass (level/get-tile level x y))
                     [(<< x 4) (<< y 4)]))
                 (level-gen/even-map-distribution))]
    (Game.
     {:lt (System/nanoTime) ; time since last tick/render
      :nt (System/nanoTime) ; time since last FPS report
      :frames 0             ; Count of frames since last FPS report
      :sleeptime 0          ; idle time since last FPS report
      }
     (update level :entities conj (player/new-player px py))
     )))

(defn render [^BufferStrategy bs level]
  (let [^Player player (some #(if (instance? Player %) %) (:entities level))
        w (.w screen)
        h (.h screen)
        x (- (int (get-in player [:mob :entity :x])) (>> w))
        x (min (max 0 x) (- (* 16 128) w))
        y (- (int (.. ^Entity (. ^Mob (. ^Player player mob) entity) y)) (>> h))
        y (min (max 0 y) (- (* 16 128) h))
        screen (assoc screen
                      :x-offset x
                      :y-offset y)
        ]
    (protocols/render level screen))

  (let [w (:w screen)
        screen-pixels ^ints (:pixels screen)]
    (dotimes [y (:h screen)]
      (let [yoffs (* y ^int w)]
        (dotimes [x w]
          (->> (+ yoffs x)
               (aget screen-pixels)
               (aget colors)
               (aset pixels (+ yoffs x)))
          )
        )))

  (doto (.getDrawGraphics (or bs (.createBufferStrategy @frame 3)))
    (.drawImage image 0 0 (* scale width) (* scale height) nil)
    (.dispose))
  (.show bs))

(def bs (delay ^BufferStrategy (.getBufferStrategy ^JFrame @frame)))

(defn run [^Game {{:keys [^long lt
                          ^int nt
                          ^int frames
                          ^long sleeptime
                          ] :as state} :state
                  level :level :as game}]
  (if @running (send *agent* run))
  (let [now (System/nanoTime)
        unprocessed (- now lt)]
    (cond
      (< unprocessed nanos-per-tick)
      (do
        (Thread/sleep (int (/ (- nanos-per-tick unprocessed) 10e6)))
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
      (assoc game
             :state (assoc state
                           :lt now
                           :frames
                           (if (> unprocessed (+ nanos-per-tick 4e6))
                             ;; We drop a frame when we are 4ms late
                             frames
                             (do (render @bs level)
                                 (inc frames))
                             ))
             :level (tick game level))
        )))

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
