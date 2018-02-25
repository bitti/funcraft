(ns funcraft.entity.player-test
  (:require [clojure.test :refer [deftest is testing]]
            [funcraft.engine.control :as engine.control]
            [funcraft.engine.move :as engine.move]
            [funcraft.engine.sprite :as engine.sprite]
            [funcraft.engines :as engines]
            [funcraft.entity.player :as sut])
  (:import funcraft.components.Position))

(deftest move-updates-player-position
  (let [engines [(engine.move/new)
                 (engine.control/new)
                 (engine.sprite/new)
                 ]
        player (sut/new 100 100)
        [itc engines] (engines/new-entity {} engines player)
        move-engine (first engines)
        [player-id player-components] (first itc)
        ]

    (testing "player eligible for move engine"
      (is (engines/eligible-entity? move-engine player)))

    (testing "player-id added to move-engine"
      (is (= (:ids move-engine) #{player-id})))

    (testing "new-entity adds player to entiy->components map"
      (is (= (get-in itc [player-id Position]) (Position. 100 100))))

    (testing "player move message to move-engine creates update message for player position"
      (is (= (engines/receive-msg move-engine itc [:move player-id 13 -11])
             [:update [player-id Position] (Position. 113 89)])))

    (testing "player move message creates player position update message"
      (is (= (engines/send-message engines itc [:move player-id 1 -1])
             (list [:update [player-id Position] (Position. 101 99)]))))

    ))
