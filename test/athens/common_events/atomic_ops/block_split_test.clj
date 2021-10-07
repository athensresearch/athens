(ns athens.common-events.atomic-ops.block-split-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging                :as log]
    [clojure.test                         :as t]
    [datahike.api                         :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture [] (fixture/random-tmp-folder-config)))


(defn- transact-with-middleware
  [txs]
  (let [processed-txs (->> txs
                           (common-db/linkmaker @@fixture/connection)
                           (common-db/orderkeeper @@fixture/connection))]
    (log/debug "\n\nmiddleware\n"
               "\nfrom:" (pr-str txs)
               "\nto:" (pr-str processed-txs)
               "\n")
    (d/transact @fixture/connection processed-txs)))


(t/deftest block-split-tests

  (t/testing "Complex `:block/save` needed."
    (let [page-1-uid     "page-1-uid"
          child-1-uid    "child-1-1-uid"
          child-2-uid    "child-1-2-uid"
          start-str      "a-o-k"
          new-page       "o-k"
          new-tmp-string (str "a-[[" new-page "]]")
          setup-txs      [{:block/uid      page-1-uid
                           :node/title     "test page 1"
                           :block/children {:block/uid      child-1-uid
                                            :block/string   start-str
                                            :block/order    0
                                            :block/children []}}]]
      (transact-with-middleware setup-txs)
      (t/is (nil? (common-db/e-by-av @@fixture/connection
                                     :block/uid
                                     child-2-uid))
            "Should not have child 2 before block split.")
      (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-1-uid])
                     :block/children
                     count))
            "Page should have only 1 child block after setup.")
      (let [block-split-op (graph-ops/build-block-split-op @@fixture/connection
                                                           {:parent-uid      page-1-uid
                                                            :old-block-uid   child-1-uid
                                                            :new-block-uid   child-2-uid
                                                            :new-block-order 1
                                                            :old-string      start-str
                                                            :new-string      new-tmp-string
                                                            :index           2})
            block-split-tx (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-split-op)]
        (transact-with-middleware block-split-tx)
        (let [page         (common-db/get-block @@fixture/connection [:block/uid page-1-uid])
              old-block    (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
              new-block    (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
              new-page-eid (common-db/e-by-av @@fixture/connection :node/title new-page)]
          (t/is (= 2 (-> page :block/children count))
                "Page should have 2 children after block split")
          ;; `:block/string` tests
          (t/is (= "a-" (-> old-block :block/string)))
          (t/is (= "[[o-k]]" (-> new-block :block/string)))
          ;; `:block/order' tests`
          (t/is (= 0 (-> old-block :block/order)))
          (t/is (= 1 (-> new-block :block/order)))
          ;; new page created
          (t/is (pos-int? new-page-eid))))))

  (t/testing "`:block/split` with re-indexing 🪄"
    (let [page-1-uid  "page-2-uid"
          child-1-uid "child-2-1-uid"
          child-2-uid "child-2-2-uid"
          child-3-uid "child-2-3-uid"
          start-str   "a-o-k"
          new-str     "o-k"
          setup-txs   [{:block/uid      page-1-uid
                        :node/title     "test page 2"
                        :block/children [{:block/uid      child-1-uid
                                          :block/string   start-str
                                          :block/order    0
                                          :block/children []}
                                         {:block/uid      child-2-uid
                                          :block/string   ""
                                          :block/order    1
                                          :block/children []}]}]]
      (transact-with-middleware setup-txs)
      (let [page (common-db/get-block @@fixture/connection [:block/uid page-1-uid])]
        (t/is (nil? (common-db/e-by-av @@fixture/connection
                                       :block/uid
                                       child-3-uid))
              "Should not have 3rd child before block split.")
        (t/is (= 2 (-> page
                       :block/children
                       count))
              "Page should have only 2 children block after setup.")
        (let [block-split-op (graph-ops/build-block-split-op @@fixture/connection
                                                             {:parent-uid      page-1-uid
                                                              :old-block-uid   child-1-uid
                                                              :new-block-uid   child-3-uid
                                                              :new-block-order 1
                                                              :old-string      start-str
                                                              :new-string      start-str
                                                              :index           2})
              block-split-tx (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-split-op)]
          (transact-with-middleware block-split-tx)
          (let [page        (common-db/get-block @@fixture/connection [:block/uid page-1-uid])
                old-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
                old-2-block (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
                new-block   (common-db/get-block @@fixture/connection [:block/uid child-3-uid])]
            (t/is (= 3 (-> page :block/children count))
                  "Page should have 3 children after block split")
            ;; `:block/string` tests
            (t/is (= "a-" (-> old-1-block :block/string)))
            (t/is (= "" (-> old-2-block :block/string)))
            (t/is (= new-str (-> new-block :block/string)))
            ;; `:block/order' tests`
            (t/is (= 0 (-> old-1-block :block/order)))
            (t/is (= 1 (-> new-block :block/order)))
            (t/is (= 2 (-> old-2-block :block/order)))))))))


