(ns ld22.protocols)

(defprotocol Renderable
  (render [this screen]))

(defprotocol Tickable
  (tick [this entities]))

(defprotocol Movable
  (move [this xa ya]))
