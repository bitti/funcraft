(ns funcraft.entity.level-test
  (:require [clojure.test :refer :all]
            [funcraft.engine-manager :as engine-manager]
            [funcraft.engines :as engines]
            [funcraft.level.level :as level]
            [funcraft.protocols :as protocols])
  (:import funcraft.level.level.Level))

(deftest tick-increases-tick-counter
  (let [[itc engines] (engines/new-entity
                       {}
                       [engines/render-level-engine]
                       [(level/->Level 10 10 {} 0 [])])
        [level-id {level Level}] (first itc)
        ]
    (is (= (:ticks level) 0))
    (is (= (engines/send-message engines itc [:tick])
           (list [:update [level-id Level :ticks] 1])))
    (is (= (get-in (protocols/tick (engine-manager/->EngineManager engines itc))
                   [level-id Level :ticks])
           1))
    ))
