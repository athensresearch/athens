(ns athens.block-test
  (:require [clojure.test :refer [deftest is]]
            [athens.blocks :as blocks]))


(deftest sort-block-test
  (is (= {:block/children [{:block/order 1
                            :block/children [{:block/order 3 }
                                             {:block/order 4}
                                             {:block/order 6}]}
                           {:block/order 2
                            :block/children [{:block/order 3 }
                                             {:block/order 4}
                                             {:block/order 5}]}]}
         (blocks/sort-block {:block/children [{:block/order 2
                                               :block/children [{:block/order 4}
                                                                {:block/order 3 }
                                                                {:block/order 5}]}
                                              {:block/order 1
                                               :block/children [{:block/order 4}
                                                                {:block/order 3 }
                                                                {:block/order 6}]}]}))))
