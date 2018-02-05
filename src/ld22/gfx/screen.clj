(ns ld22.gfx.screen
  (:require [ld22.level.macros :refer [<< >>]]))

(set! *unchecked-math* true)

(defprotocol Render
  (render
    [screen xp yp tile colors]
    [screen xp yp tile colors k v]
    [screen xp yp tile colors options])
  )

(defrecord Screen
    [^int x-offset
     ^int y-offset
     ^int w ^int h
     ^ints pixels
     sheet]

  Render
  (render [screen xp yp tile colors]
    (render screen xp yp tile colors []))
  (render [screen xp yp tile colors k v]
    (render screen xp yp tile colors [k v]))
  (render [screen xp yp tile colors options]
   (let [screen-pixels ^ints pixels
         sheet-pixels ^ints (sheet :pixels)
         x-offs x-offset
         y-offs y-offset
         xp (- ^int xp x-offs)
         yp (- ^int yp y-offs)
         x-tile (bit-and ^int tile 31)
         y-tile (>> ^int tile 5)
         t-offs (+ (<< x-tile 3)
                   (<< y-tile 11)) ; 8 bit for sheet width, 3 for tile width
         {:keys [mirror-x mirror-y]} (apply hash-map options)
         xss (if mirror-x 7 0)
         xd (if mirror-x -1 1)
         yss (if mirror-y 7 0)
         yd (if mirror-y -1 1)
         colors (int-array (list
                            (bit-and colors 0xff)
                            (bit-and (>> colors 8) 0xff)
                            (bit-and (>> colors 16) 0xff)
                            (bit-and (>> colors 24) 0xff))
                           )
         ]
     (loop [y 0
            ys yss
            x 0
            xs xss
            ]
       (cond
         ;; All rows done?
         (or (= y 8) (>= (+ y yp) h)) true

         ;; Next row?
         (or (= x 8)
             (neg? (+ y yp))
             (>= (+ x xp) w))
         (recur (inc y) (+ yd ys) 0 xss)

         ;; left crop
         (neg? (+ x xp))
         (recur y ys (inc x) (+ xd xs))
         :else

         ;; Map the color encoded in the first 2 bits to an index
         ;; into the 255 color palette by the given 'colors' parameter
         (let [col
               (aget colors (aget sheet-pixels
                                  (+ xs (bit-shift-left ys 8)
                                     t-offs)))
               ]
           (if (< col 255)
             (aset screen-pixels
                   (+ x xp (* (+ y yp) w)) col))
           (recur y ys (inc x) (+ xd xs)))
         ))
     )))

(defn new [^long w ^long h sheet]
  (Screen.
   0 0
   w h
   (int-array (* w h))
   sheet))

