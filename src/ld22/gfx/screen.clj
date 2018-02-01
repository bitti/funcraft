(ns ld22.gfx.screen)

(defstruct screen-struct
  :xOffset :yOffset
  :w :h
  :pixels
  :sheet)

(defn new [^long w ^long h sheet]
  (struct screen-struct
          0 0
          w h
          (int-array (* w h))
          sheet))

(defn render
  "Render a tile from the sprite sheet based on screen coordinates,
  tile number, colors and mirror options"
  [screen xp yp tile x-offs y-offs colors & options]

  (let [sheet (screen :sheet)
        screen-pixels ^ints (screen :pixels)
        sheet-pixels ^ints (sheet :pixels)
        sheet-width ^int (sheet :width)
        xp (- ^int xp (int x-offs))
        yp (- ^int yp (int y-offs))
        x-tile (bit-and ^int tile 31)
        y-tile (bit-shift-right ^int tile 5)

        ;; t-offs = x-tile * 8 + y-tile * 8 * sheet.width
        t-offs (+ (bit-shift-left x-tile 3)
                  (* (bit-shift-left y-tile 3) ^int (sheet :width)))
        mirror-x ((set options) :mirror-x)
        mirror-y ((set options) :mirror-y)
        xss (if mirror-x 7 0)
        xd (if mirror-x dec inc)
        yss (if mirror-y 7 0)
        yd (if mirror-y dec inc)
        h (screen :h)
        w (screen :w)
        ]
    (loop [y 0
           ys yss
           x 0
           xs xss
           ]
      (cond
        (or (= y 8) (>= (+ y yp) ^int h)) true

        (or (= x 8)
            (< (+ y yp) 0)
            (>= (+ x xp) ^int w))
        (recur (inc y) (yd ys) 0 xss)

        (< (+ x xp) 0)
        (recur y ys (inc x) (xd xs))
        :else

        ;; Map the color encoded in the first 2 bits to an index
        ;; into the 255 color palette by the given 'colors' parameter
        (let [col (-> ^int colors
                      (bit-shift-right
                       (bit-shift-left
                        (aget sheet-pixels
                              (+ (int xs) (* (int ys) ^int sheet-width)
                                 t-offs))
                        3))
                      (bit-and 0xff))
              ]
          (aset screen-pixels
                (+ x xp (* (+ y yp) ^int w)) col)
          (recur y ys (inc x) (xd xs)))
        ))
    ))
