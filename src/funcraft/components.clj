(ns funcraft.components)

(defrecord Position [^int x ^int y])

(defrecord Dimension [^int xr ^int yr])

(defrecord Direction [^int dir])

(defrecord Health [^int health])

(defrecord Walk [^int distance])

(defrecord Sprite [render-fn])

(defrecord Control [input-handler-fn])

(defrecord Attack [^int attack-time])
