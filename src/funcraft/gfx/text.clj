(ns funcraft.gfx.text
  (:require [clojure.string :as string]
            [funcraft.gfx.screen :as screen]
            [funcraft.level.macros :refer [<<]])
  (:import funcraft.gfx.screen.Screen))

(def ^:const char->idx
  (->> (str "ABCDEFGHIJKLMNOPQRSTUVWXYZ      "
            "0123456789.,!?'\"-+=/\\%()<>:;")
       (map-indexed #(list %2 %1))
       flatten
       (apply array-map)))

(defn draw [msg ^Screen screen x y col]
  (doall
   (map-indexed
    (fn [i c]
      (if-let [ix (char->idx c)]
        (screen/render screen (+ x (<< i 3)) y (+ ix (* 30 32)) col)))
    (string/upper-case msg))))
