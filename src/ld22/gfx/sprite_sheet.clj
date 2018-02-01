(ns ld22.gfx.sprite-sheet
  (:import java.awt.image.BufferedImage))

(defstruct sheet
  :width :height :pixels)

(defn new-sheet [^BufferedImage image]
  (let [width (.getWidth image)
        height (.getHeight image)
        pixels (.getRGB image 0 0 width height nil 0 width)]
    (dotimes [i (count pixels)]
      ;; Only 4 colors per sprite are supported, therefore we extract
      ;; bit 6 and 7 of the sheet
      (aset ^ints pixels i (-> (aget ^ints pixels i)
                               (bit-and 0xff)
                               (bit-shift-right 6))))  
    (struct sheet width height pixels)))
