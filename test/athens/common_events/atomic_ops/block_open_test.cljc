(ns athens.common-events.atomic-ops.block-open-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events                 :as common-events]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common-events.resolver.undo   :as undo]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest close-opened-block
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        child-1-uid "child-1-1-1-uid"
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "close opened block testing page"
                      :block/children [{:block/uid      block-1-uid
                                        :block/string   ""
                                        :block/order    0
                                        :block/open     true
                                        :block/children [{:block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/open     false
                                                          :block/children []}]}]}]]
    (fixture/transact-with-middleware setup-txs)
    (let [atomic-close (atomic-graph-ops/make-block-open-op block-1-uid false)
          atomic-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-close)]
      (d/transact! @fixture/connection atomic-txs)
      (t/is (false? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open))))))


(t/deftest open-closed-block
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        child-1-uid "child-1-1-1-uid"
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "close opened block testing page"
                      :block/children [{:block/uid      block-1-uid
                                        :block/string   ""
                                        :block/order    0
                                        :block/open     false
                                        :block/children [{:block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/open     false
                                                          :block/children []}]}]}]]
    (fixture/transact-with-middleware setup-txs)
    (let [atomic-close (atomic-graph-ops/make-block-open-op block-1-uid true)
          atomic-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-close)]
      (d/transact! @fixture/connection atomic-txs)
      (t/is (true? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open))))))


(t/deftest open-opened-block
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        child-1-uid "child-1-1-1-uid"
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "close opened block testing page"
                      :block/children [{:block/uid      block-1-uid
                                        :block/string   ""
                                        :block/order    0
                                        :block/open     true
                                        :block/children [{:block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/open     false
                                                          :block/children []}]}]}]]
    (fixture/transact-with-middleware setup-txs)
    (let [atomic-close (atomic-graph-ops/make-block-open-op block-1-uid true)
          atomic-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-close)]
      (d/transact! @fixture/connection atomic-txs)
      (t/is (empty? atomic-txs))
      (t/is (true? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open))))))


(t/deftest close-closed-block
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        child-1-uid "child-1-1-1-uid"
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "close opened block testing page"
                      :block/children [{:block/uid      block-1-uid
                                        :block/string   ""
                                        :block/order    0
                                        :block/open     false
                                        :block/children [{:block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/open     false
                                                          :block/children []}]}]}]]
    (fixture/transact-with-middleware setup-txs)
    (let [atomic-close (atomic-graph-ops/make-block-open-op block-1-uid false)
          atomic-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-close)]
      (d/transact! @fixture/connection atomic-txs)
      (t/is (empty? atomic-txs))
      (t/is (false? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open))))))


(fixture/integration-test-fixture []
  (fn []
    (let [test-uid "test-uid"
          test-str "test-str"
          get-open (fn []
                     (->> [:block/uid test-uid]
                          (common-db/get-internal-representation @@fixture/connection)
                          :block/open?))
          setup!   (fn [open?]
                     (->> [{:page/title     "test-page"
                            :block/children [{:block/uid    test-uid
                                              :block/string test-str
                                              :block/open?   open?}]}]
                          (bfs/build-paste-op @@fixture/connection)
                          common-events/build-atomic-event
                          (atomic-resolver/resolve-transact! @fixture/connection)))
          save!    (fn [open?]
                     (let [db  @@fixture/connection
                           op  (atomic-graph-ops/make-block-open-op test-uid open?)
                           evt (common-events/build-atomic-event op)]
                       (atomic-resolver/resolve-transact! @fixture/connection evt)
                       [db evt]))
          undo!    (fn [evt-db evt]
                     (let [db       @@fixture/connection
                           undo-evt (undo/build-undo-event db evt-db evt)]
                       (atomic-resolver/resolve-transact! @fixture/connection undo-evt)
                       [db undo-evt]))]

      (t/testing "undo intializing block to open"
         (setup! true)
         (t/is (true? (get-open)) "Setup initialized block to open")
        (let [[db evt] (save! false)]
          (t/is (false? (get-open)) "Changed block to close")
          (undo! db evt)
          (t/is (true? (get-open)) "Undo block back to open")))

      (t/testing "undo intializing block to closed"
        (setup! false)
        (t/is (false? (get-open)) "Setup initialized block to closed")
        (let [[db evt] (save! true)]
           (t/is (true? (get-open)) "Changed block to open")
           (undo! db evt)
           (t/is (false? (get-open)) "Undo block back to closed")))

      (t/testing "redo"
        (setup! true)
        (t/is (true? (get-open)) "Setup initialized block to open")
        (let [[db evt] (save! false)]
          (t/is (false? (get-open)) "Changed block to close")
          (let [[db' evt'] (undo! db evt)]
            (t/is (true? (get-open)) "Undo block back to open")
            (undo! db' evt')
            (t/is (false? (get-open)) "Redo block back to closed")))))))




(t/deftest undo
  (let [test-uid "test-uid"
        test-str "test-str"
        get-open (fn []
                   (->> [:block/uid test-uid]
                        (common-db/get-internal-representation @@fixture/connection)
                        :block/open?))
        setup!   (fn [open?]
                   (->> [{:page/title     "test-page"
                          :block/children [{:block/uid    test-uid
                                            :block/string test-str
                                            :block/open   open?}]}]
                        (bfs/build-paste-op @@fixture/connection)
                        common-events/build-atomic-event
                        (atomic-resolver/resolve-transact! @fixture/connection)))
        save!    (fn [open?]
                   (let [db  @@fixture/connection
                         op  (atomic-graph-ops/make-block-open-op test-uid open?)
                         evt (common-events/build-atomic-event op)]
                     (atomic-resolver/resolve-transact! @fixture/connection evt)
                     [db evt]))
        undo!    (fn [evt-db evt]
                   (let [db       @@fixture/connection
                         undo-evt (undo/build-undo-event db evt-db evt)]
                     (atomic-resolver/resolve-transact! @fixture/connection undo-evt)
                     [db undo-evt]))]

    (t/testing "undo"
       (setup! true)
       (t/is (true? (get-open)) "Setup initialized block to open")
       (let [[db evt] (save! false)]
        (t/is (false? (get-open)) "Changed block to close")
        (undo! db evt)
        (t/is (true? (get-open)) "Undo block back to open")))


    (t/testing "undo"
       (setup! false)
       (t/is (false? (get-open)) "Setup initialized block to closed")
       (let [[db evt] (save! true)]
        (t/is (true? (get-open)) "Changed block to open")
        (undo! db evt)
        (t/is (false? (get-open)) "Undo block back to closed")))


    (t/testing "redo with interleaved edit"
      (setup! "one")
      (t/is (= "one" (get-str)) "Setup initialized string at one")
      (let [[db evt] (save! "two")]
        (t/is (= "two" (get-str)) "Changed string to two")
        (save! "three")
        (t/is (= "three" (get-str)) "Interleaved op changed string to three")
        (let [[db' evt'] (undo! db evt)]
          (t/is (= "one" (get-str)) "Undo string back to one")
          (undo! db' evt')
          (t/is (= "three" (get-str)) "Redo string back to three"))))))