(ns ahungry.overdexer-test
  (:require [clojure.test :refer :all]
            [ahungry.overdexer :refer :all]
            [ahungry.overdexer.entity.itm :as itm]))

(deftest test-item-header-test
  (testing "See if I can pull out some useful item info"
    (let [item (itm/parse-item "sw1h04.itm")]
      (is (= 0 (:enchantment (:header item)))))))
