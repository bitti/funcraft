(ns funcraft.protocols)

(defprotocol Renderable
  (render [this screen]))

(defprotocol Tickable
  (tick [this level]))

(defprotocol Movable
  (move [this level xa ya]))

(definterface MayPass)

