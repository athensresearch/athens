(ns athens.common-events.atomic-ops.composite-of-composites-undo-test
  (:require
    [athens.common-db               :as common-db]
    [athens.common-events.fixture   :as fixture]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common.logging          :as log]
    [clojure.pprint                 :as pp]
    [clojure.test                   :as t]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest composite-of-composites-undo
  (let [block-1-uid    "block-1-uid"
        block-2-uid    "block-2-uid"
        start-str      "aa"
        new-page-title "new test page"
        setup-repr     [{:page/title     "composite-of-composites-undo test page"
                         :block/children [{:block/uid    block-1-uid
                                           :block/string start-str}]}]
        get-page       #(->> [:node/title %]
                             (common-db/get-block @@fixture/connection))]
    (fixture/setup! setup-repr)
    (t/is (nil? (get-page new-page-title)))
    (let [block-split-op (graph-ops/build-block-split-op @@fixture/connection {:old-block-uid block-1-uid
                                                                               :new-block-uid block-2-uid
                                                                               :string        (str start-str " [[" new-page-title "]]")
                                                                               :index         (inc (count start-str))
                                                                               :relation      :after})
          block-split-atomics (graph-ops/extract-atomics block-split-op)
          [before-split-db split-event] (fixture/op-resolve-transact! block-split-op)]
      (log/info "block-split-op\n"
                (with-out-str
                  (pp/pprint block-split-op))
                "\nblock-split-atomics\n"
                (with-out-str
                  (pp/pprint block-split-atomics)))
      (t/is (not (nil? (get-page new-page-title))))
      (let [[_ undo-op] (fixture/undo! before-split-db split-event)]
        (log/info "undo-op\n"
                  (with-out-str
                    (pp/pprint undo-op)))
        (t/is (nil? (get-page new-page-title)))))))


