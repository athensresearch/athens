(ns athens.common-events.atomic-ops.shortcut-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))

(defn get-sidebar-count
  []
  (-> (common-db/get-sidebar-elements @@fixture/connection)
      (count)))

(t/deftest shortcut-add-test
  (let [setup-tx  [{:block/uid      "parent-uid"
                    :node/title     "Hello World!"
                    :block/children []}]]
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0 (get-sidebar-count)))
    (let [shortcut-new-op  (atomic-graph-ops/make-shortcut-new-op "Hello World!")
          shortcut-new-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                    shortcut-new-op)]
      (d/transact! @fixture/connection shortcut-new-txs)
      (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection))))))


(t/deftest shortcut-remove-test
  (let [setup-tx  [{:block/uid      "parent-uid"
                    :node/title     "Hello World!"
                    :block/children []}]]
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
    (let [shortcut-new-op     (atomic-graph-ops/make-shortcut-new-op "Hello World!")
          shortcut-new-txs    (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                       shortcut-new-op)
          shortcut-remove-op (atomic-graph-ops/make-shortcut-remove-op "Hello World!")
          shortcut-remove-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                         shortcut-remove-op)]

      (d/transact! @fixture/connection shortcut-new-txs)
      (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))

      (d/transact! @fixture/connection shortcut-remove-txs)
      (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection))))))


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
    (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 1")))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 2")))
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-new-op "page 3")))
    (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))

    ;; Test 1
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-move-op "page 3"
                                                                                                                      {:page/title "page 1"
                                                                                                                       :relation :before})))

    (let [page-1-loc  (common-db/find-order-from-title @@fixture/connection "page 1")
          page-2-loc  (common-db/find-order-from-title @@fixture/connection "page 2")
          page-3-loc  (common-db/find-order-from-title @@fixture/connection "page 3")]
      (t/is (= 0 page-3-loc))
      (t/is (= 1 page-1-loc))
      (t/is (= 2 page-2-loc)))


    ;; Test 2
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-move-op "page 3"
                                                                                                                      {:page/title "page 2"
                                                                                                                       :relation :before})))
    (let [page-1-loc  (common-db/find-order-from-title @@fixture/connection "page 1")
          page-2-loc  (common-db/find-order-from-title @@fixture/connection "page 2")
          page-3-loc  (common-db/find-order-from-title @@fixture/connection "page 3")]
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

    (let [page-1-loc  (common-db/find-order-from-title @@fixture/connection "page 1")
          page-2-loc  (common-db/find-order-from-title @@fixture/connection "page 2")
          page-3-loc  (common-db/find-order-from-title @@fixture/connection "page 3")]
      (t/is (= 0 page-1-loc))
      (t/is (= 1 page-3-loc))
      (t/is (= 2 page-2-loc)))


    ;; Test 2
    (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                              (atomic-graph-ops/make-shortcut-move-op "page 1"
                                                                                                                      {:page/title "page 2"
                                                                                                                       :relation :after})))
    (let [page-1-loc  (common-db/find-order-from-title @@fixture/connection "page 1")
          page-2-loc  (common-db/find-order-from-title @@fixture/connection "page 2")
          page-3-loc  (common-db/find-order-from-title @@fixture/connection "page 3")]
      (t/is (= 0 page-3-loc))
      (t/is (= 1 page-2-loc))
      (t/is (= 2 page-1-loc)))))





(t/deftest undo-shortcut-add)
(fixture/integration-test-fixture
  (fn []
    (let [setup-repr [{:page/title "Hello World!"}]
          save!      #(-> (atomic-graph-ops/make-shortcut-new-op "Hello World!")
                          (fixture/op-resolve-transact!))]
      (t/testing "undo"
        (fixture/setup! setup-repr)
        (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
        (let [[evt-db evt] (save!)]
          (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))
          (fixture/undo! evt-db evt)
          (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))))

      (t/testing "redo"
         (fixture/setup! setup-repr)
         (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
         (let [[evt-db evt] (save!)]
           (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))
           (let [[evt-db' evt'] (fixture/undo! evt-db evt)]
             (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
             (fixture/undo! evt-db' evt')
             (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))))))))


"
--Setup--
* Alice
* Bob
* Charlie

--Save--

* Alice
* Charlie


--Add--
* Alice
* Charlie
* Bob


--Undo--
* Alice
* Bob
* Charlie"

(t/deftest undo-shortcut-remove)
(fixture/integration-test-fixture
  (fn []
    (let [setup-tx [{:node/title "Alice" :block/uid "Alice" :page/sidebar 0}
                    {:node/title "Bob" :block/uid "Bob" :page/sidebar 1}
                    {:node/title "Charlie" :block/uid "Charlie" :page/sidebar 2}]
          setup!   #(fixture/transact-with-middleware setup-tx)
          save!    #(->> "Bob"
                         (atomic-graph-ops/make-shortcut-remove-op)
                         (fixture/op-resolve-transact!))]
      (t/testing "undo"
        (setup!)
        (let [sidebar-elem (common-db/get-sidebar-elements @@fixture/connection)]
          (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
          (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
          (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
          (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))
          (let [[evt-db evt] (save!)]
            (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
            (fixture/undo! evt-db evt)
            (let [sidebar-elem' (common-db/get-sidebar-elements @@fixture/connection)]
              (t/is (= sidebar-elem sidebar-elem'))))))
              ;;(t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
              ;;(t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
              ;;(t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
              ;;(t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))))

      #_(t/testing "redo"
          (fixture/setup! setup-repr)
          (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
          (let [[evt-db evt] (save!)]
            (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))
            (let [[evt-db' evt'] (fixture/undo! evt-db evt)]
              (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
              (fixture/undo! evt-db' evt')
              (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))))))))

"
--Setup--
* Alice <- target, :before
* Bob
* Charlie <- source

--Move--
* Charlie <- source
* Alice
* Bob <- target, :after

--Undo--
* Alice
* Bob
* Charlie"

(t/deftest undo-shortcut-move-before)
(fixture/integration-test-fixture
  (fn []
    (let [setup-tx [{:node/title "Alice" :block/uid "Alice" :page/sidebar 0}
                    {:node/title "Bob" :block/uid "Bob" :page/sidebar 1}
                    {:node/title "Charlie" :block/uid "Charlie" :page/sidebar 2}]
          setup!   #(fixture/transact-with-middleware setup-tx)
          move!    #(->> (atomic-graph-ops/make-shortcut-move-op "Charlie" {:page/title "Alice" :relation :before})
                         (fixture/op-resolve-transact!))]
      (t/testing "undo"
        (setup!)
        (let [sidebar-elem (common-db/get-sidebar-elements @@fixture/connection)]
          (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
          (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
          (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
          (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))
          (let [[evt-db evt] (move!)]
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Charlie")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Alice")))
            (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Bob")))
            (fixture/undo! evt-db evt)
            (let [sidebar-elem' (common-db/get-sidebar-elements @@fixture/connection)]
              (t/is (= sidebar-elem sidebar-elem'))
              (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
              (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
              (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie"))))))))))








(t/deftest undo-shortcut-move-after)
