(ns funcraft.entity.text-particle-test
  (:require [clojure.test :refer [deftest is testing]]
            [funcraft.engine-manager :as engine-manager]
            [funcraft.engine.lifetime-limit :as engine.lifetime-limit]
            [funcraft.engine.move :as engine.move]
            [funcraft.engines :as engines]
            [funcraft.entity.particle.text-particle :as sut]
            [funcraft.protocols :as protocols])
  (:import [funcraft.components LifetimeLimit Message Position Velocity]))

(deftest particle-lifetime-and-movement
  (let [engines [(engine.move/new)
                 sut/text-particle-engine
                 (engine.lifetime-limit/new)]
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

(def text-particle-creation-engine
  (engines/->Engine #{} #{}
                    (fn [this itc [msg]]
                      (if (= msg :tick)
                        [:add (sut/new "23" 10 20 134)]))))

(deftest particle-creation
  (let [engines [text-particle-creation-engine
                 (engine.move/new)
                 sut/text-particle-engine
                 (engine.lifetime-limit/new)]
        {itc :itc [control-engine & particle-engines] :engines}
        (protocols/tick
         (engine-manager/->EngineManager engines {}))
        [particle-id particle] (first itc)
        ]

    (testing "particle in itc and corresponding components"
      (is (not (nil? particle-id)))
      (is (empty?
           (disj (set (keys particle))
                 Position
                 LifetimeLimit
                 Message
                 Velocity)
             ))
      (is (every? #((:ids %) particle-id) particle-engines))
      )))
