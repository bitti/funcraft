(ns funcraft.entity.text-particle-test
  (:require [clojure.test :refer [deftest is testing]]
            [funcraft.engine-manager :as engine-manager]
            [funcraft.engines :as engines]
            [funcraft.entity.particle.text-particle :as sut]
            [funcraft.protocols :as protocols])
  (:import [funcraft.components LifetimeLimit Velocity]))

(deftest particle-lifetime-and-movement
  (let [engines [engines/move-engine
                 sut/text-particle-engine
                 sut/lifetime-limit-engine]
        text-particle (sut/new "23" 10 20 134)
        [itc engines] (engines/new-entity {} engines text-particle)
        [id components] (first itc)
        engine-manager (engine-manager/->EngineManager engines itc)
        ]

    (testing "particle in itc and components"
      (is (itc id))
      (is (every? #((:ids %) id) engines)))

    (testing "Height and lifetime after 10 ticks"
      (let [orig-zv (get-in itc [id Velocity :zv])
            {itc :itc} (last (take 11 (iterate protocols/tick engine-manager)))
            zv (get-in itc [id Velocity :zv])
            ]
        (is (= (get-in itc [id LifetimeLimit :lifetime]) 50))
        (is (> 1e-14 (Math/abs (- orig-zv zv 1.5)))))
      )

    (testing "Removal after 60 ticks"
      (let [{:keys [engines itc]} (last (take 62 (iterate protocols/tick engine-manager)))]
        (is (nil? (itc id)))
        (is (every? #(nil? ((:ids %) id)) engines))
        ))))
