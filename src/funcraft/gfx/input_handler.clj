(ns funcraft.gfx.input-handler
  (:require [clojure.string :as string])
  (:import [java.awt.event KeyEvent KeyListener]))

(declare toggle)

(def key-listener
  (proxy [KeyListener] []
    (keyPressed [^KeyEvent ke] (toggle ke :pressed))
    (keyReleased [^KeyEvent ke] (toggle ke :released))
    (keyTyped [ke])
    ))

(def up (atom false))
(def down (atom false))
(def left (atom false))
(def right (atom false))
(def attack (atom false))

(defn toggle-key [state key]
  (if key
    (reset! key (= state :pressed))))

(defmacro jumptable [code]
  `(case ~code
     (~KeyEvent/VK_UP ~KeyEvent/VK_W) up
     (~KeyEvent/VK_DOWN ~KeyEvent/VK_S) down
     (~KeyEvent/VK_LEFT ~KeyEvent/VK_A) left
     (~KeyEvent/VK_RIGHT ~KeyEvent/VK_D) right
     (~KeyEvent/VK_SPACE ~KeyEvent/VK_CONTROL
      ~KeyEvent/VK_INSERT ~KeyEvent/VK_ENTER
      ~KeyEvent/VK_C ~KeyEvent/VK_NUMPAD0) attack
    nil))

(def ^:const vk-left KeyEvent/VK_LEFT)

(defn toggle [^KeyEvent ke state]
  (let [code (.getKeyCode ke)]
    (toggle-key state (jumptable code))))
