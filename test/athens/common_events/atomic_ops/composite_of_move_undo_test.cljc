(ns athens.common-events.atomic-ops.composite-of-move-undo-test
  (:require
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-ops]
    [athens.common-events.graph.composite :as composite]
    [clojure.test                         :as t]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(let [from-title   "from-title"
      target-title "target-title"
      setup-repr   [{:page/title     from-title
                     :block/children [{:block/uid    "one"
                                       :block/string ""}
                                      {:block/uid    "two"
                                       :block/string ""}
                                      {:block/uid    "three"
                                       :block/string ""}]}
                    {:page/title target-title}]
      exp-repr     [{:page/title     target-title
                     :block/children [{:block/uid    "one"
                                       :block/string ""}
                                      {:block/uid    "two"
                                       :block/string ""}
                                      {:block/uid    "three"
                                       :block/string ""}]}]
      move!        #(->> [(atomic-ops/make-block-move-op "one" {:relation :last :page/title target-title})
                          (atomic-ops/make-block-move-op "two" {:relation :last :page/title target-title})
                          (atomic-ops/make-block-move-op "three" {:relation :last :page/title target-title})]
                         (composite/make-consequence-op {:op/type :multi-move})
                         fixture/op-resolve-transact!)]

  (t/deftest undo-move-composite
    (fixture/setup! setup-repr)

    (let [[evt-db evt] (move!)]
      (t/is (= [(fixture/get-repr [:node/title target-title])] exp-repr)
            "Blocks were moved in order")
      (fixture/undo! evt-db evt)
      (t/is (= setup-repr [(fixture/get-repr [:node/title from-title])
                           (fixture/get-repr [:node/title target-title])])
            "Undo restored to the original state")))

  (t/deftest redo-move-composite
    (fixture/setup! setup-repr)

    (let [[evt-db evt] (move!)
          [evt-db' evt'] (fixture/undo! evt-db evt)]
      (fixture/undo! evt-db' evt')
      (t/is (= [(fixture/get-repr [:node/title target-title])] exp-repr)
            "Blocks were moved in order"))))
