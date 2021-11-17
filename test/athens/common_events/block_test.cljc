(ns athens.common-events.block-test
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.fixture :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test :as t]
    [datascript.core :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest split-block-tests
  (t/testing "Simple case, no page links or block refs"
    (let [parent-uid         "test-parent-2-uid"
          child-1-uid        "test-child-2-1-uid"
          child-1-init-value "we split this"
          child-2-uid        "test-child-2-2-uid"
          setup-txs          [{:db/id          -1
                               :node/title     "test page"
                               :block/uid      "page-uid"
                               :block/children {:db/id          -2
                                                :block/uid      parent-uid
                                                :block/string   ""
                                                :block/order    0
                                                :block/children {:db/id          -3
                                                                 :block/uid      child-1-uid
                                                                 :block/string   child-1-init-value
                                                                 :block/order    0
                                                                 :block/children []}}}]]
      (d/transact! @fixture/connection setup-txs)

      (let [parent-eid        (common-db/e-by-av @@fixture/connection
                                                 :block/uid parent-uid)
            child-1-eid       (common-db/e-by-av @@fixture/connection
                                                 :block/uid child-1-uid)
            child-1           (d/pull @@fixture/connection
                                      [:block/uid
                                       :block/order
                                       :block/string]
                                      child-1-eid)
            split-block-event (common-events/build-split-block-event child-1-uid child-1-init-value 2 child-2-uid)
            split-block-txs   (resolver/resolve-event-to-tx @@fixture/connection
                                                            split-block-event)
            query-children    '[:find ?child
                                :in $ ?eid
                                :where [?eid :block/children ?child]]]

        ;; before we add second child, check for 1st one
        (t/is (= #{[child-1-eid]} (d/q query-children @@fixture/connection parent-eid)))
        (t/is (= {:block/uid    child-1-uid
                  :block/order  0
                  :block/string child-1-init-value}
                 child-1))

        ;; split the block
        (d/transact! @fixture/connection split-block-txs)
        (let [child-2-eid (common-db/e-by-av @@fixture/connection
                                             :block/uid child-2-uid)
              children    (d/q query-children @@fixture/connection parent-eid)
              ;; query for child 1, it was updated with transact
              child-1     (d/pull @@fixture/connection
                                  [:block/uid
                                   :block/order
                                   :block/string]
                                  child-1-eid)
              child-2     (d/pull @@fixture/connection
                                  [:block/uid
                                   :block/order
                                   :block/string]
                                  child-2-eid)]
          (t/is (seq children))
          (t/is (= #{[child-2-eid] [child-1-eid]} children))
          (t/is (= {:block/uid    child-2-uid
                    :block/order  1
                    :block/string (subs child-1-init-value 2)}
                   child-2))
          (t/is (= {:block/uid    child-1-uid
                    :block/order  0
                    :block/string (subs child-1-init-value 0 2)}
                   child-1)))))))


