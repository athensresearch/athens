(ns athens.common-events.atomic-ops.block-open-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [clojure.test                         :as t]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(defn transact!
  [uid closed?]
  (-> (atomic-graph-ops/make-block-open-op uid closed?)
      fixture/op-resolve-transact!))


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
    (transact! block-1-uid false)
    (t/is (false? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open)))))


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
    (transact! block-1-uid true)
    (t/is (true? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open)))))


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
    (transact! block-1-uid true)
    (t/is (true? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open)))))


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
    (transact! block-1-uid false)
    (t/is (false? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open)))))


(let [test-uid "test-uid"
      setup-repr (fn [open?]
                   [{:page/title     "test-page"
                     :block/children [(merge
                                        {:block/uid    test-uid
                                         :block/string "test-str"}
                                        ;; NB: internal representation does not contain
                                        ;; a key for :block/open? if it's true, since
                                        ;; that's the default.
                                        (when (not open?)
                                          {:block/open? open?}))]}])
      get-open #(->> [:block/uid test-uid]
                     (common-db/get-internal-representation @@fixture/connection)
                     :block/open?)
      save! (partial transact! test-uid)]


  (t/deftest undo-init-open
    (fixture/setup! (setup-repr true))
    (t/is (nil? (get-open)) "Setup initialized block to open")
    (let [[db evt] (save! false)]
      (t/is (false? (get-open)) "Changed block to close")
      (fixture/undo! db evt)
      (t/is (nil? (get-open)) "Undo block back to open")))

  (t/deftest undo-init-close
    (fixture/setup! (setup-repr false))
    (t/is (false? (get-open)) "Setup initialized block to closed")
    (let [[db evt] (save! true)]
      (t/is (nil? (get-open)) "Changed block to open")
      (fixture/undo! db evt)
      (t/is (false? (get-open)) "Undo block back to closed")))

  (t/deftest undo-redo
    (fixture/setup! (setup-repr true))
    (t/is (nil? (get-open)) "Setup initialized block to open")
    (let [[db evt] (save! false)]
      (t/is (false? (get-open)) "Changed block to close")
      (let [[db' evt'] (fixture/undo! db evt)]
        (t/is (nil? (get-open)) "Undo block back to open")
        (fixture/undo! db' evt')
        (t/is (false? (get-open)) "Redo block back to closed")))))
