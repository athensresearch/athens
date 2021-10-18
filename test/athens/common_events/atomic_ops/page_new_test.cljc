(ns athens.common-events.atomic-ops.page-new-test
  (:require
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest page-new-atomic-test
  (t/testing "page/new when page didn't exist yet"
    (let [test-title     "test page title"
          test-page-uid  "test-page-uid-1"
          page-new-event (atomic-graph-ops/make-page-new-op test-title
                                                            test-page-uid)
          page-new-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                  page-new-event)]
      (d/transact! @fixture/connection page-new-txs)
      (let [e-by-title (d/q '[:find ?e
                              :where [?e :node/title ?title]
                              :in $ ?title]
                            @@fixture/connection test-title)
            e-by-uid   (d/q '[:find ?e
                              :where [?e :block/uid ?uid]
                              :in $ ?uid]
                            @@fixture/connection test-page-uid)]
        (t/is (seq e-by-title))
        (t/is (= e-by-title e-by-uid))))))


(t/deftest page-new-composite-test
  (t/testing "that `:page/new` generates composite ops when page doesn't exist."
    (let [page-uid    "page-uid-1"
          page-title  "page 1 title"
          block-uid   "block-uid-1-1"
          page-new-op (graph-ops/build-page-new-op @@fixture/connection page-title page-uid block-uid)]
      (t/is (= :composite/consequence (:op/type page-new-op))))))
