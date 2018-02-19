(ns funcraft.engine-manager-test
  (:require [clojure.test :refer [deftest is testing]]
            [funcraft.engine-manager :as sut]
            [funcraft.engines :as engines]
            [funcraft.entity.player :as player]
            [funcraft.protocols :as protocols])
  (:import [funcraft.components Control Dimension Position]))

(defn go-down-when-tick
  [engine itc [msg]]
  (when (= msg :tick)
    (map (fn [id]
           (let [pos (get-in itc [id Position])]
             [:move id 1 1]))
         (:ids engine))))

(defn block-y-110
  [engine itc [msg id dx dy]]
  (assert (not (nil? msg)))
  (when (and (= msg :move) (get-in engine [:ids id]))
    (let [pos (get-in itc [id Position])]
      (if (> (+ dy (:y pos)) 110)
        [:merge [id Position :y] 110]
        ))))

(def control-engine
  (engines/->Engine #{Control} #{} go-down-when-tick))

(def collision-engine
  (engines/->Engine #{Position Dimension} #{} block-y-110))

(deftest engine-manager-tick
  (let [[itc engines] (engines/new-entity
                       {}
                       [engines/move-engine collision-engine control-engine]
                       (player/new 100 100))
        player-id (first (keys itc))
        engine-manager (sut/->EngineManager engines itc)]

    (testing "player and engine setup"
      (is (= (get-in itc [player-id Position])
             (Position. 100 100)))
      (is (= (sut/send-message engine-manager [:tick])
             (list [:move player-id 1 1]))))

    (testing "One tick moves one position"
      (let [itc (protocols/tick engine-manager)]
        (is (= (get-in itc [player-id Position]) (Position. 101 101)))))

    (testing "10 ticks move 10 positions"
      (let [{itc :itc}
            (last (take 11 (iterate #(assoc % :itc (protocols/tick %)) engine-manager)))]
        (is (= (get-in itc [player-id Position]) (Position. 110 110)))))

    (testing "20 ticks move 20 positions in x but only 10 in y direction"
      (let [{itc :itc}
            (last (take 21 (iterate #(assoc % :itc (protocols/tick %)) engine-manager)))]
        (is (= (get-in itc [player-id Position]) (Position. 120 110)))))
    ))
