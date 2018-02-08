(ns funcraft.protocols)

(defprotocol Renderable
  (render [this screen]))

(defprotocol Tickable
  (tick [this entities]))

(defprotocol Movable
  (move [this xa ya]))

(definterface MayPass)

