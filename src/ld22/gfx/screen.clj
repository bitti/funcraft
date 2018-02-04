(ns ld22.gfx.screen
  (:require [ld22.level.macros :refer [<< >>]]))

(set! *unchecked-math* true)

(defrecord Screen
    [^int x-offset
     ^int y-offset
     ^int w ^int h
     ^ints pixels
     sheet])

(defn new [^long w ^long h sheet]
  (Screen.
   0 0
   w h
   (int-array (* w h))
   sheet))

(defn render
  "Render tile from sprite sheet based on screen coordinates,
  tile number, colors and mirror options"
  [^Screen screen xp yp tile colors & options]
  (let [sheet (:sheet screen)
        screen-pixels ^ints (:pixels screen)
        sheet-pixels ^ints (sheet :pixels)
;        sheet-width ^int (sheet :width)
        x-offs (:x-offset screen)
        y-offs (:y-offset screen)
        xp (- ^int xp (int x-offs))
        yp (- ^int yp (int y-offs))
        x-tile (bit-and ^int tile 31)
        y-tile (>> ^int tile 5)
        t-offs (+ (<< x-tile 3)
                  (<< y-tile 11)) ; 8 bit for sheet width, 3 for tile width
        {:keys [mirror-x mirror-y]} (apply hash-map options)
        xss (int (if mirror-x 7 0))
        xd (if mirror-x dec inc)
        yss ^int (if mirror-y 7 0)
        yd (if mirror-y dec inc)
        h (:h screen)
        w (:w screen)
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
        (or (= y 8) (>= (+ y yp) ^int h)) true

        ;; Next row?
        (or (= x 8)
            (neg? (+ y yp))
            (>= (+ x xp) ^int w))
        (recur (inc y) (yd ys) 0 xss)

        ;; left crop
        (neg? (+ x xp))
        (recur y ys (inc x) (xd xs))
        :else

        ;; Map the color encoded in the first 2 bits to an index
        ;; into the 255 color palette by the given 'colors' parameter
        (let [col
              (aget colors (aget sheet-pixels
                            (+ (int xs) (bit-shift-left (int ys) 8)
                               t-offs)))
              ]
          (if (< col 255)
            (aset screen-pixels
                  (+ x xp (* (+ y yp) ^int w)) col))
          (recur y ys (inc x) (xd xs)))
        ))
    ))
