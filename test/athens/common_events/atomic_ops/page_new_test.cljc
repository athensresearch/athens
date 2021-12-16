(ns athens.common-events.atomic-ops.page-new-test
  (:require
    [athens.common-db                     :as common-db]
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
          page-new-event (atomic-graph-ops/make-page-new-op test-title)
          page-new-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                  page-new-event)]
      (d/transact! @fixture/connection page-new-txs)
      (let [e-by-title (d/q '[:find ?e
                              :where [?e :node/title ?title]
                              :in $ ?title]
                            @@fixture/connection test-title)]
        (t/is (seq e-by-title)))))

  (t/testing "page/new daily page resolves to special uid"
    (let [title "October 22, 2021"
          uid   "10-22-2021"]
      (->> (atomic-graph-ops/make-page-new-op title)
           (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection)
           (d/transact! @fixture/connection))
      (t/is (= uid (common-db/get-page-uid @@fixture/connection title))))))


(t/deftest page-new-composite-test
  (t/testing "that `:page/new` with block uid generates composite ops when page doesn't exist."
    (let [page-title  "page 1 title"
          page-new-op (graph-ops/build-page-new-op @@fixture/connection page-title "uid")]
      (t/is (= :composite/consequence (:op/type page-new-op))))))

