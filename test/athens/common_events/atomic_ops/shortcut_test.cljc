(ns athens.common-events.atomic-ops.shortcut-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest shortcut-add-test
  (let [setup-tx  [{:block/uid      "parent-uid"
                    :node/title     "Hello World!"
                    :block/children []}]]
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))
    (let [shortcut-new-op  (atomic-graph-ops/make-shortcut-new-op "Hello World!")
          shortcut-new-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                    shortcut-new-op)]
      (d/transact! @fixture/connection shortcut-new-txs)
      (t/is (= 1
               (-> (common-db/get-sidebar-elements @@fixture/connection)
                   (count)))))))


(t/deftest shortcut-remove-test
  (let [setup-tx  [{:block/uid      "parent-uid"
                    :node/title     "Hello World!"
                    :block/children []}]]
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))
    (let [shortcut-new-op     (atomic-graph-ops/make-shortcut-new-op "Hello World!")
          shortcut-new-txs    (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                       shortcut-new-op)
          shortcut-remove-op (atomic-graph-ops/make-shortcut-remove-op "Hello World!")
          shortcut-remove-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                         shortcut-remove-op)]

      (d/transact! @fixture/connection shortcut-new-txs)
      (t/is (= 1
               (-> (common-db/get-sidebar-elements @@fixture/connection)
                   (count))))

      (d/transact! @fixture/connection shortcut-remove-txs)
      (t/is (= 0
               (-> (common-db/get-sidebar-elements @@fixture/connection)
                   (count)))))))


(t/deftest shortcut-move-before-test
  "Test 1 :
    - start ->
      page 1  ; <- before this
      page 2
      page 3  ; <- move this
    - end ->
      page 3
      page 1
      page 2
   Test 2
    - start ->
      page 3  ; <- move this
      page 1
      page 2  ; <- before this
    -end ->
      page 1
      page 3
      page 2"
  (let [setup-tx  [{:block/uid      "page-1-uid"
                    :node/title     "page 1"
                    :block/children []}
                   {:block/uid      "page-2-uid"
                    :node/title     "page 2"
                    :block/children []}
                   {:block/uid      "page-3-uid"
                    :node/title     "page 3"
                    :block/children []}]]
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 1")))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 2")))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 3")))
    (t/is (= 3
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))

    ;; Test 1
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-move-op "page 3"
                                                                                                                      {:page/title "page 1"
                                                                                                                       :relation :before})))

    (let [sidebar-els (common-db/get-sidebar-elements @@fixture/connection)
          page-1-loc  (common-db/find-order-from-title sidebar-els "page 1")
          page-2-loc  (common-db/find-order-from-title sidebar-els "page 2")
          page-3-loc  (common-db/find-order-from-title sidebar-els "page 3")]
      (t/is (= 0 page-3-loc))
      (t/is (= 1 page-1-loc))
      (t/is (= 2 page-2-loc)))


    ;; Test 2
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-move-op "page 3"
                                                                                                                      {:page/title "page 2"
                                                                                                                       :relation :before})))
    (let [sidebar-els (common-db/get-sidebar-elements @@fixture/connection)
          page-1-loc  (common-db/find-order-from-title sidebar-els "page 1")
          page-2-loc  (common-db/find-order-from-title sidebar-els "page 2")
          page-3-loc  (common-db/find-order-from-title sidebar-els "page 3")]
      (t/is (= 0 page-1-loc))
      (t/is (= 1 page-3-loc))
      (t/is (= 2 page-2-loc)))))


(t/deftest shortcut-move-after-test
  "Test 1 :  Note this case is not possible through UI this action is registered as
             move page 3 before 2
    - start ->
      page 1  ; <- after this
      page 2
      page 3  ; <- move this
    - end ->
      page 1
      page 3
      page 2
   Test 2
    - start ->
      page 1  ; <- move this
      page 3
      page 2  ; <- after this
    -end ->
      page 3
      page 2
      page 1"
  (let [setup-tx  [{:block/uid      "page-1-uid"
                    :node/title     "page 1"
                    :block/children []}
                   {:block/uid      "page-2-uid"
                    :node/title     "page 2"
                    :block/children []}
                   {:block/uid      "page-3-uid"
                    :node/title     "page 3"
                    :block/children []}]]
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 1")))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 2")))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 3")))
    (t/is (= 3
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))

    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-move-op "page 3"
                                                                                                                      {:page/title "page 2"
                                                                                                                       :relation :before})))

    (let [sidebar-els (common-db/get-sidebar-elements @@fixture/connection)
          page-1-loc  (common-db/find-order-from-title sidebar-els "page 1")
          page-2-loc  (common-db/find-order-from-title sidebar-els "page 2")
          page-3-loc  (common-db/find-order-from-title sidebar-els "page 3")]
      (t/is (= 0 page-1-loc))
      (t/is (= 1 page-3-loc))
      (t/is (= 2 page-2-loc)))


    ;; Test 2
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-move-op "page 1"
                                                                                                                      {:page/title "page 2"
                                                                                                                       :relation :after})))
    (let [sidebar-els (common-db/get-sidebar-elements @@fixture/connection)
          page-1-loc  (common-db/find-order-from-title sidebar-els "page 1")
          page-2-loc  (common-db/find-order-from-title sidebar-els "page 2")
          page-3-loc  (common-db/find-order-from-title sidebar-els "page 3")]
      (t/is (= 0 page-3-loc))
      (t/is (= 1 page-2-loc))
      (t/is (= 2 page-1-loc)))))


(fixture/integration-test-fixture
  (fn []
    (let [setup-repr [{:page/title     "Hello World!"}]
          save!      #(-> (atomic-graph-ops/make-shortcut-new-op "Hello World!")
                          (fixture/op-resolve-transact!))]
      (t/testing "undo")
      (fixture/setup! setup-repr)
      (t/is (= 0
               (-> (common-db/get-sidebar-elements @@fixture/connection)
                   (count))))
      (let [[evt-db evt] (save!)]
        (t/is (= 1
                 (-> (common-db/get-sidebar-elements @@fixture/connection)
                     (count))))
        (cljs.pprint/pprint evt)
        (fixture/undo! evt-db evt)
        (t/is (= 0
                  (-> (common-db/get-sidebar-elements @@fixture/connection)
                      (count))))))))


(t/deftest undo-shortcut-add)


(t/deftest undo-shortcut-remove)


(t/deftest undo-shortcut-move-before)
(t/deftest undo-shortcut-move-after)
