(ns funcraft.level.level-gen
  (:require [funcraft.gfx.colors :as colors]
            [funcraft.level.macros :refer [<< >>]]
            funcraft.level.tile.grass
            funcraft.level.tile.rock
            funcraft.level.tile.sand
            funcraft.level.tile.tree
            funcraft.level.tile.cactus
            funcraft.level.tile.water
            )
  (:import funcraft.level.level.Level
           funcraft.level.tile.grass.Grass
           funcraft.level.tile.rock.Rock
           funcraft.level.tile.sand.Sand
           funcraft.level.tile.tree.Tree
           funcraft.level.tile.cactus.Cactus
           funcraft.level.tile.water.Water
           java.awt.Image
           java.awt.image.BufferedImage
           java.lang.Math
           java.util.Random
           [javax.swing ImageIcon JOptionPane]))

(set! *unchecked-math* :warn-on-boxed)

(def ^Random random (Random.))

(def ^:const dirt-color 322)
(def ^:const sand-color 550)
(def ^:const w 128)
(def ^:const h 128)

(definline rnd-1to1 []
  `(dec (* (.nextFloat random) 2)))

(defn set-sample [^doubles values ^long x ^long y ^double value]
  (aset values (+ (bit-and x (dec w))
                  (* (bit-and y (dec h)) w))
        value))

(defn sample ^double [^doubles values ^long x ^long y]
  (aget values (+ (bit-and x (dec w))
                  (* (bit-and y (dec h)) w))))

(defn noise-map [feature-size]
  (let [values (double-array (* w h))]

    ;; First set a grid of values in feature size distance
    (doall
     (for [y (range 0 h feature-size)
           x (range 0 w feature-size)]
       (set-sample values x y (rnd-1to1))))

    (loop [^double scale (/ w)
           scale-mod 1.0
           ^int step-size feature-size
           half-step (>> step-size)]
      (when (> step-size 1)
        ;; Then interpolate 4 neighbours to set midpoint
        ;;
        ;; a . . . b
        ;; . . . . .
        ;; . .[e]. .
        ;; . . . . .
        ;; c . . . d
        (doall
         (for [^int y (range 0 h step-size)
               ^int x (range 0 w step-size)
               :let [
                     a (sample values x y)
                     b (sample values (+ x step-size) y)
                     c (sample values x (+ y step-size))
                     d (sample values (+ x step-size) (+ y step-size))

                     e (+ (/ (+ a b c d) 4.0) (* (rnd-1to1) step-size scale))
                     ]]
           (set-sample values (+ x half-step) (+ y half-step) e)))

        ;; Then interpolate original values (a, b, c) and new
        ;; midpoints (f, d, e) for missing midpoints (here g, h) to
        ;; get a grid again for the next iteration (with half distance
        ;; between points)
        ;;
        ;;       . e .
        ;;     . . . . .
        ;;   . a .[h] . b
        ;; . . . . . . .
        ;; f .[g]. d .
        ;; . . . . .
        ;;   . c .
        (doall
         (for [^int y (range 0 h step-size)
               ^int x (range 0 w step-size)
               :let [
                     a (sample values x y)
                     b (sample values (+ x step-size) y)
                     c (sample values x (+ y step-size))

                     d (sample values (+ x half-step) (+ y half-step))
                     e (sample values (+ x half-step) (- y half-step))
                     f (sample values (- x half-step) (+ y half-step))

                     h (+ (/ (+ a b d e) 4.0) (* (rnd-1to1) step-size (/ scale 2)))
                     g (+ (/ (+ a c d f) 4.0) (* (rnd-1to1) step-size (/ scale 2)))
                     ]]
           (do
             (set-sample values (+ x half-step) y h)
             (set-sample values x (+ y half-step) g))))
        (recur (* scale (* scale (+ scale-mod 0.8)))
               (* scale-mod 0.3)
               (>> step-size)
               (>> half-step))))
    values))

(defn- base-top-map []
  (let [^doubles mnoise1 (noise-map 16)
        ^doubles mnoise2 (noise-map 16)
        ^doubles mnoise3 (noise-map 16)

        ^doubles noise1 (noise-map 32)
        ^doubles noise2 (noise-map 32)
        ]
    (vec
     (for [^int y (range 0 h)
           ^int x (range 0 w)
           :let [i (+ x (* y w))
                 val (-> (aget noise1 i)
                         (- (aget noise2 i))
                         (Math/abs)
                         (* 3)
                         (- 2))
                 mval (-> (aget mnoise1 i)
                          (- (aget mnoise2 i))
                          (Math/abs)
                          (- (aget mnoise3 i))
                          (Math/abs)
                          (* 3)
                          (- 2))
                 dist (max
                       (Math/abs (dec (* (/ x (- w 1.0)) 2)))
                       (Math/abs (dec (* (/ y (- h 1.0)) 2))))
                 dist (Math/pow dist 16)
                 val (+ val 1 (* dist -20))
                 ]]
       (cond
         (< val -0.5) (Water. x y
                              (colors/index 3 5 (- dirt-color 111) dirt-color)
                              (colors/index 3 5 (- sand-color 110) sand-color))
         (and (> val 0.5) (< mval -1.5)) (Rock. x y 0)
         :else (Grass. x y))))))

(defn even-map-distribution []
  "Lazy seq of evenly distributed random map positions"
  (repeatedly #(vector (.nextInt random w) (.nextInt random h))))

(defn- even-distribution
  "Evenly distributed random integers around [x y] with a manhatten
  distance of d"
  [^long x ^long y ^long d]
  (repeatedly
   #(vector
     (+ x (.nextInt random (inc (<< d))) (- d))
     (+ y (.nextInt random (inc (<< d))) (- d))
     )))

(defn- triangular-distribution [^long x ^long d]
  (+ x (.nextInt random d) (- (.nextInt random d))))

(defn- pyramid-distribution
  "Pyramidial distributed random integers around [x y] with a manhatten
  distance of d"
  [^long x ^long y ^long d]
  (repeatedly
   #(vector (triangular-distribution x d)
            (triangular-distribution y d))))

(defn- sand-for-map [m]
  (apply assoc! m
         (flatten
          (for [[xs ys] (take (int (/ (* w h) 2800)) (even-map-distribution))]
            (for [[x y] (take 10 (even-distribution xs ys 10))]
              (for [[^int xo ^int yo] (take 100 (pyramid-distribution x y 15))]
                (for [^int yy (range (dec yo) (+ yo 2))
                      ^int xx (range (dec xo) (+ xo 2))
                      :let [i (+ xx (* yy w))]
                      :when (and (< -1 xx w) (< -1 yy h)
                                 (instance? Grass (m i)))
                      ]
                  [i (Sand. xx yy)]
                  )
                ))))))

(defn- trees-for-map [m]
  (apply assoc! m
         (flatten
          (for [[x y] (take (int (/ (* w h) 400)) (even-map-distribution))]
            (for [[^int xx ^int yy] (take 200 (pyramid-distribution x y 15))
                  :let [i (+ xx (* yy w))]
                  :when (and (< -1 xx w) (< -1 yy h)
                             (instance? Grass (m i)))]
              [i (Tree. xx yy 0)])))))

(defn- cactuses-for-map [m]
  (apply assoc! m
         (flatten
          (for [[^int x ^int y] (take (int (/ (* w h) 100)) (even-map-distribution))
                :let [i (+ x (* y w))]
                :when (instance? Sand (m i))]
            [i (Cactus. x y 0)]))))

(defn create-top-map []
  (-> (transient (base-top-map))
      sand-for-map
      trees-for-map
      cactuses-for-map
      persistent!))

(defn inspect-map []
  (let [img (BufferedImage. w h BufferedImage/TYPE_INT_RGB)
        pixels (int-array (* w h))]
    (loop []
      (doall
       (map-indexed
        (fn [i tile]
          (aset pixels i
                (condp instance? tile
                  Grass 0x208020
                  Water 0x000080
                  Rock 0xa0a0a0
                  Tree 0x003000
                  Sand 0xa0a040
                  0x000000)))
        (create-top-map)))
      (.setRGB img 0 0 w h pixels 0 w)
      (if (zero?
           (JOptionPane/showOptionDialog
            nil                         ; parent Component
            nil                         ; message
            "Map Generator"             ; Title
            JOptionPane/YES_NO_OPTION
            JOptionPane/QUESTION_MESSAGE
            (ImageIcon. (.getScaledInstance img (* w 8) (* h 8) Image/SCALE_AREA_AVERAGING))
            (into-array String ["Another" "Quit"]) ; Button options
            nil                                    ; start value
            ))
          (recur)
          ))))

(defn new-level []
  (Level. w h
          {:dirt-color dirt-color
           :sand-color sand-color
           }
          0                             ; Initial ticks 0
          (create-top-map)
          ))
