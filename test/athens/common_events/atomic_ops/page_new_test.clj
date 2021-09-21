(ns athens.common-events.atomic-ops.page-new-test
  (:require
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datahike.api                         :as d]))


(t/use-fixtures :each fixture/integration-test-fixture)


(t/deftest page-new-test
  (t/testing "Page new test"
    (let [test-title        "test page title"
          test-page-uid     "test-page-uid-1"
          test-block-uid    "test-block-uid-1"
          page-new-event   (atomic-graph-ops/make-page-new-op test-title
                                                              test-page-uid
                                                              test-block-uid)
          page-new-txs     (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                    page-new-event)]
      (d/transact @fixture/connection page-new-txs)
      (let [e-by-title (d/q '[:find ?e
                              :where [?e :node/title ?title]
                              :in $ ?title]
                            @@fixture/connection test-title)
            e-by-uid (d/q '[:find ?e
                            :where [?e :block/uid ?uid]
                            :in $ ?uid]
                          @@fixture/connection test-page-uid)]
        (t/is (seq e-by-title))
        (t/is (= e-by-title e-by-uid))))))
