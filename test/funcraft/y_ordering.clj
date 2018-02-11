(ns funcraft.y-ordering
  (:require [clojure.test :refer :all]
            [funcraft.entity.item-entity :as item-entity]
            [funcraft.entity.particle.smash :as particle.smash]
            [funcraft.entity.particle.text-particle :as particle.text-particle]
            [funcraft.entity.player :as player]
            [funcraft.gfx.colors :as colors]
            [funcraft.item.item :refer [->Item]]))

(deftest compare-contract-for-renderable-entities
  (let [p1 (player/new-player 10 10)
        p2 (player/new-player 10 10)
        i1 (item-entity/new
            (->Item (+ 9 128) (colors/index -1 100 300 500))
            123
            10)
        i2 (item-entity/new
            (->Item (+ 9 128) (colors/index -1 100 300 500))
            123
            10)
        s1 (particle.smash/new 43 10)
        s2 (particle.smash/new 43 10)
        ]

    (testing "Reflexivity and antisymmetry"
      (doall
       (for [e1 [p1 p2 i1 i2 s1 s2]
             e2 [p1 p2 i1 i2 s1 s2]
             ]
         (when-not
             (if (identical? e1 e2)
               (is (zero? (compare e1 e2))
                   (format "e1: %s%n e2: %s" e1 e2))
               (is (= (compare e1 e2) (- (compare e2 e1)))
                   (format "e1: %s%ne2: %s" e1 e2)))
           )
         )))

    (testing "Transitivity"
      (doall
       (for [e1 [p1 p2 i1 i2 s1 s2]
             e2 [p1 p2 i1 i2 s1 s2]
             e3 [p1 p2 i1 i2 s1 s2]
             :when (and (not= e1 e2)
                        (not= e2 e3)
                        (not= e1 e3))
             ]
         (if (and (= (compare e1 e2) 1)
                  (= (compare e2 e3) 1))
           (is (compare e1 e3) 1)))))))
