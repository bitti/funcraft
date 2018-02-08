(ns ld22.level.level-gen
  (:require [ld22.level.tile grass rock sand tree water]
            [ld22.gfx.colors :as colors]
            [ld22.level.macros :refer [>>]])
  (:import java.awt.Image
           java.awt.image.BufferedImage
           java.lang.Math
           java.util.Random
           [javax.swing ImageIcon JOptionPane]
           ld22.level.level.Level
           ld22.level.tile.grass.Grass
           ld22.level.tile.rock.Rock
           ld22.level.tile.sand.Sand
           ld22.level.tile.tree.Tree
           ld22.level.tile.water.Water))

(def ^Random random (Random.))

(def ^:const grass-color 141)
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

        ;; Then interpolate original values (a, b, e) and new
        ;; midpoints (f, d, e) for missing mitpoints (here g, h) to
        ;; get a grid again for the next iteration (with half distance
        ;; between points)
        ;;
        ;;       . e .
        ;;     . . . . .
        ;;   . a .[h] . b
        ;; . . . . . . .
        ;; f .[g]. d .
        ;; . . . . .
        ;;   . e .
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

(defn create-top-map []
  (let [^doubles mnoise1 (noise-map 16)
        ^doubles mnoise2 (noise-map 16)
        ^doubles mnoise3 (noise-map 16)

        ^doubles noise1 (noise-map 32)
        ^doubles noise2 (noise-map 32)

        map (vec
             (for [y (range 0 h)
                   x (range 0 w)
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
               (if (< val -0.5)
                 (Water. x y
                         (colors/index 3 5 (- dirt-color 111) dirt-color)
                         (colors/index 3 5 (- sand-color 110) sand-color))
                 (if (and (> val 0.5) (< mval -1.5))
                   (Rock. x y)
                   (Grass. x y
                           (colors/index (- grass-color 111) grass-color
                                         (+ grass-color 111)
                                         dirt-color))))))
        ]
    (apply assoc map
           (flatten
            (for [i (range 0 (int (/ (* w h) 2800)))
                  :let [xs (.nextInt random w)
                        ys (.nextInt random h)]]
              (for [k (range 0 10)
                    :let [x (+ xs (.nextInt random 21) -10)
                          y (+ ys (.nextInt random 21) -10)]]
                (for [j (range 0 100)
                      :let [xo (+ x (.nextInt random 5) (- (.nextInt random 5)))
                            yo (+ y (.nextInt random 5) (- (.nextInt random 5)))]]
                  (for [yy (range (dec yo) (+ yo 2))
                        xx (range (dec xo) (+ xo 2))
                        :let [i (+ xx (* yy w))]
                        :when (and (< -1 xx w) (< -1 yy h)
                                   (instance? Grass (map i)))
                        ]
                    [i (Sand. xx yy)]
                    )
                  )))))))

(defn inspect-map []
  (let [img (BufferedImage. w h BufferedImage/TYPE_INT_RGB)
        pixels (int-array (* w h))]

    (loop []
        (let [map (create-top-map)]
          (dotimes [i (* w h)]
            (aset pixels i
                  (condp instance? (map i)
                    Grass 0x208020
                    Water 0x000080
                    Rock 0xa0a0a0
                    Tree 0x003000
                    Sand 0xa0a040
                    0x000000))))
        (.setRGB img 0 0 w h pixels 0 w)
        (if
            (zero?
             (JOptionPane/showOptionDialog
              nil                            ; parent Component
              nil                            ; message
              "Map Generator"                ; Title
              JOptionPane/YES_NO_OPTION
              JOptionPane/QUESTION_MESSAGE
              (ImageIcon. (.getScaledInstance img (* w 8) (* h 8) Image/SCALE_AREA_AVERAGING))
              (into-array String ["Another" "Quit"]) ; Button options
              nil                                    ; start value
              ))
          (recur)
          )))

(defn generate [^long w ^long h]
    (vec
     (for [j (range h)
           i (range w)
           ]
       (condp > (.nextDouble random)
         0.3 (Water. i j
                     (colors/index* 3 5 (- dirt-color 111) dirt-color)
                     (colors/index* 3 5 (- sand-color 110) sand-color))
         0.5 (Sand. i j)
         (Grass. i j
                 (colors/index* (- grass-color 111) grass-color (+ grass-color 111)
                                dirt-color))
         )))
    ))

(defn new-level []
  (Level. w h
          {:dirt-color dirt-color
           :sand-color sand-color
           }
          (create-top-map)))
