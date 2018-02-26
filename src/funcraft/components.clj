(ns funcraft.components
  (:import java.util.Random))

(defrecord Position [^int x ^int y])

(defrecord Dimension [^int xr ^int yr])

(defrecord Direction [^int direction])

(defrecord Health [^int health])

(defrecord Walk [^int distance])

(defrecord Sprite [render-fn])

(defrecord Control [input-handler-fn])

(defrecord Attack [^int attack-time ^int range])

(defrecord LifetimeLimit [^int lifetime])

(defrecord Message [^String message ^int color])

(defrecord Velocity [^double xx ^double yy ^double zz
                     ^double xv ^double yv ^double zv])

(def random (Random.))

(defn new-velocity [x y]
  (->Velocity x y 2
              (* (.nextGaussian random) 0.3)    ; x velocity
              (* (.nextGaussian random) 0.2)    ; y velocity
              (inc (* (.nextFloat random) 0.7)) ; z velocity
              ))
