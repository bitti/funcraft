(ns ld22.perfgraph
  (:import [java.awt Color Graphics Graphics2D RenderingHints]
           [javax.swing JFrame JPanel]))

;;; Alternative? https://github.com/sjl/metrics-clojure


(def data (atom []))
(def pad 20)

(def plot-test
  (proxy [JPanel] []
    (paintComponent [^Graphics g]
      (proxy-super paintComponent g)
      (.setRenderingHint ^Graphics2D g
                         RenderingHints/KEY_ANTIALIASING
                         RenderingHints/VALUE_ANTIALIAS_ON)

      (let [w (proxy-super getWidth)
            h (proxy-super getHeight)
            x-scale (/ (- w (* 2 pad)) (inc (count @data)))
            max-value 100.0
            y-scale (/ (- h (* 2 pad)) max-value)
            x0 pad
            y0 (- h pad)
            ]
        (.drawLine g pad pad pad (- h pad))
        (.drawLine g pad (- h pad) (- w pad) (- h pad))
        (.setPaint g Color/red)
        (dotimes [j (count @data)]
          (let [x (+ x0 (int (* x-scale (inc j))))
                y (- y0 (int (* y-scale (@data j))))]
;            (println "fill " x y)
            (.fillOval g (- x 2) (- y 2) 8 8))
          )
        )
      )))


(def f (let [jf (new JFrame)]
         (.add (.getContentPane jf) plot-test)
         (doto jf
           (.setSize 400 400)
           (.setLocation 200 200)
           (.setVisible true)
           )
         jf))

;(.add (.getContentPane f) plot-test)

(defn repaint [s t]
  (when t
    (swap! data #(conj (vec (drop (- (count %) 599) %)) t)))
  (if (and s (= (mod s 30) 0))
    (.repaint plot-test))
  (if s (inc s) 0))

(def a (agent nil))
(send a repaint nil)
