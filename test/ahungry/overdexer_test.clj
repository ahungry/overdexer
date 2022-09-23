(ns ahungry.overdexer-test
  (:require [clojure.test :refer :all]
            [ahungry.overdexer :refer :all]
            [ahungry.overdexer.entity.itm :as itm]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))

(deftest test-item-header-test
  (testing "See if I can pull out some useful item info"
    (let [item (itm/test-item-header "sw1h04.itm")]
      (is (= 0 (:enchantment item))))))
