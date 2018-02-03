(ns ld22.gfx.colors
  (:require [ld22.level.macros :refer [<<]]))

(def div unchecked-divide-int)

(defn init
  "Initialize a 216 color palette"
  []
  (for [r (range 6)
        g (range 6)
        b (range 6)]
    (let [
          rr (int (/ (* r 255) 5))
          gg (int (/ (* g 255) 5))
          bb (int (/ (* b 255) 5))

          ;; Use luma coefficients to move colors towards gray,
          ;; therefore lowering 'saturation', which gives an effect
          ;; of distance
          mid (div (+ (* rr 30) (* gg 59) (* bb 11)) 100)
          r1 (+ (div (* (+ rr mid) 115) 255) 10)
          g1 (+ (div (* (+ gg mid) 115) 255) 10)
          b1 (+ (div (* (+ bb mid) 115) 255) 10)
          ]
      (bit-or (<< r1 16) (<< g1 8) b1)
      )))

(defn index*
  ([^long a ^long b ^long c ^long d]
   (reduce (fn [s c] (+ (<< s 8) (index* c))) 0 [d c b a]))
  ([^long d]
   (if (neg? d)
     255
     (let [r (mod (div d 100) 10)
           g (mod (div d 10) 10)
           b (mod d 10)]
       (+ (* r 36) (* g 6) b)))))

(defmacro index
  "Map each given color to an index of the color palette.

  Each color is given as a decimal where each digit maps to a
  corresponding rgb component. The allowed range for each digit is 0
  to 5, giving 216 possible colors. Furthermore you should avoid
  leading zeros to avoid octal interpretation.

  Since a color index needs one byte this macro returns an int for all
  4 colors"
  [a b c d]
  (apply index* (map eval [a b c d])))
