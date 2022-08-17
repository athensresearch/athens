(ns athens.common-events.atomic-ops.shortcut-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [clojure.test                         :as t]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest shortcut-add-test
  (let [setup-tx [{:block/uid  "parent-uid"
                   :node/title "Hello World!"}]
        add! #(->> (atomic-graph-ops/make-shortcut-new-op "Hello World!")
                   fixture/op-resolve-transact!)]


    ;; setup
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
    ;; add shortcut
    (add!)
    (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))))


(t/deftest shortcut-remove-test
  (t/testing "last"
    (let [setup-tx [{:block/uid    "parent-uid"
                     :node/title   "Hello World!"
                     :page/sidebar 0}]
          remove!  #(->> (atomic-graph-ops/make-shortcut-remove-op "Hello World!")
                         fixture/op-resolve-transact!)]
      ;; setup
      (fixture/transact-with-middleware setup-tx)
      (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))
      ;; remove shortcut
      (remove!)
      (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))))

  (t/testing "middle"
    (let [setup-tx [{:block/uid    "page-uid-1"
                     :node/title   "page 1"
                     :page/sidebar 0}
                    {:block/uid    "page-uid-2"
                     :node/title   "page 2"
                     :page/sidebar 1}
                    {:block/uid    "page-uid-3"
                     :node/title   "page 3"
                     :page/sidebar 2}]
          remove!  #(->> (atomic-graph-ops/make-shortcut-remove-op "page 2")
                         fixture/op-resolve-transact!)]
      ;; setup
      (fixture/transact-with-middleware setup-tx)
      (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
      ;; remove shortcut
      (remove!)
      (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
      (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "page 1")))
      (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "page 3"))))))


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
    - end ->
      page 1
      page 3
      page 2"
  (let [setup-tx [{:block/uid    "page-1-uid"
                   :node/title   "page 1"
                   :page/sidebar 0}
                  {:block/uid    "page-2-uid"
                   :node/title   "page 2"
                   :page/sidebar 1}
                  {:block/uid    "page-3-uid"
                   :node/title   "page 3"
                   :page/sidebar 2}]
        move-1!  #(->> (atomic-graph-ops/make-shortcut-move-op "page 3" {:page/title "page 1" :relation :before})
                       fixture/op-resolve-transact!)
        move-2!  #(->> (atomic-graph-ops/make-shortcut-move-op "page 3" {:page/title "page 2" :relation :before})
                       fixture/op-resolve-transact!)]
    ;; setup
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
    ;; Test 1
    (move-1!)
    (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "page 3")))
    (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "page 1")))
    (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "page 2")))

    ;; Test 2
    (move-2!)
    (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "page 1")))
    (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "page 3")))
    (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "page 2")))))


(t/deftest shortcut-move-after-test
  "Test 1 :  Note this case is not possible through UI this action is registered as
             move page 3 before 2
    - start ->
      page 1
      page 2  ; <- before this
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
    - end ->
      page 3
      page 2
      page 1"
  (let [setup-tx [{:block/uid    "page-1-uid"
                   :node/title   "page 1"
                   :page/sidebar 0}
                  {:block/uid    "page-2-uid"
                   :node/title   "page 2"
                   :page/sidebar 1}
                  {:block/uid    "page-3-uid"
                   :node/title   "page 3"
                   :page/sidebar 2}]
        test-1! #(->> (atomic-graph-ops/make-shortcut-move-op "page 3" {:page/title "page 2" :relation   :before})
                      fixture/op-resolve-transact!)
        test-2! #(->> (atomic-graph-ops/make-shortcut-move-op "page 1" {:page/title "page 2" :relation   :after})
                      fixture/op-resolve-transact!)]
    ;; setup
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))

    ;; Test 1
    (test-1!)
    (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "page 1")))
    (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "page 3")))
    (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "page 2")))


    ;; Test 2
    (test-2!)
    (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "page 3")))
    (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "page 2")))
    (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "page 1")))))


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
    - end ->
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
    (-> (atomic-graph-ops/make-shortcut-new-op "page 1") fixture/op-resolve-transact!)
    (-> (atomic-graph-ops/make-shortcut-new-op "page 2") fixture/op-resolve-transact!)
    (-> (atomic-graph-ops/make-shortcut-new-op "page 3") fixture/op-resolve-transact!)

    (t/is (= 3
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))

    (-> (atomic-graph-ops/make-shortcut-move-op "page 3"
                                                {:page/title "page 1"
                                                 :relation :after})
        fixture/op-resolve-transact!)

    (let [page-1-loc  (common-db/find-order-from-title @@fixture/connection "page 1")
          page-2-loc  (common-db/find-order-from-title @@fixture/connection "page 2")
          page-3-loc  (common-db/find-order-from-title @@fixture/connection "page 3")]
      (t/is (= 0 page-1-loc))
      (t/is (= 1 page-3-loc))
      (t/is (= 2 page-2-loc)))


    ;; Test 2
    (-> (atomic-graph-ops/make-shortcut-move-op "page 1"
                                                {:page/title "page 2"
                                                 :relation :after})
        fixture/op-resolve-transact!)

    (let [page-1-loc  (common-db/find-order-from-title @@fixture/connection "page 1")
          page-2-loc  (common-db/find-order-from-title @@fixture/connection "page 2")
          page-3-loc  (common-db/find-order-from-title @@fixture/connection "page 3")]
      (t/is (= 0 page-3-loc))
      (t/is (= 1 page-2-loc))
      (t/is (= 2 page-1-loc)))))


(t/deftest undo-shortcut-new
  (let [setup-repr [{:page/title "Hello World!"}]
        save!      #(-> (atomic-graph-ops/make-shortcut-new-op "Hello World!")
                        (fixture/op-resolve-transact!))]
    (t/testing "undo"
      ;; setup
      (fixture/setup! setup-repr)
      (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
      (let [[evt-db evt] (save!)]
        (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))
        ;; undo
        (fixture/undo! evt-db evt)
        (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))))

    (t/testing "redo"
      ;; setup
      (fixture/setup! setup-repr)
      (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
      (let [[evt-db evt] (save!)]
        (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))
        ;; undo (remove)
        (let [[evt-db' evt'] (fixture/undo! evt-db evt)]
          (t/is (= 0 (common-db/get-sidebar-count @@fixture/connection)))
          ;; redo (add)
          (fixture/undo! evt-db' evt')
          (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection))))))))


(t/deftest undo-shortcut-remove-top
  (let [setup-tx    [{:node/title "Alice" :block/uid "Alice" :page/sidebar 0}
                     {:node/title "Bob" :block/uid "Bob" :page/sidebar 1}]
        setup!      #(fixture/transact-with-middleware setup-tx)
        remove-top! #(->> "Alice"
                          (atomic-graph-ops/make-shortcut-remove-op)
                          (fixture/op-resolve-transact!))]

    (t/testing "undo remove top"
      (setup!)
      (let [sidebar-elem (common-db/get-sidebar-elements @@fixture/connection)]
        (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
        (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
        (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
        (let [[evt-db evt] (remove-top!)]
          (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))
          (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Bob")))
          (fixture/undo! evt-db evt)
          (let [sidebar-elem' (common-db/get-sidebar-elements @@fixture/connection)]
            (t/is (= sidebar-elem sidebar-elem'))
            (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))))))))


(t/deftest undo-shortcut-remove-bottom
  (let [setup-tx    [{:node/title "Bob" :block/uid "Bob" :page/sidebar 0}
                     {:node/title "Charlie" :block/uid "Charlie" :page/sidebar 1}]
        setup!      #(fixture/transact-with-middleware setup-tx)
        remove-bottom! #(->> "Charlie"
                             (atomic-graph-ops/make-shortcut-remove-op)
                             (fixture/op-resolve-transact!))]

    (t/testing "undo remove bottom"
      (setup!)
      (let [sidebar-elem (common-db/get-sidebar-elements @@fixture/connection)]
        (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
        (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Bob")))
        (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Charlie")))
        (let [[evt-db evt] (remove-bottom!)]
          (t/is (= 1 (common-db/get-sidebar-count @@fixture/connection)))
          (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Bob")))
          (fixture/undo! evt-db evt)
          (let [sidebar-elem' (common-db/get-sidebar-elements @@fixture/connection)]
            (t/is (= sidebar-elem sidebar-elem'))
            (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Bob")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Charlie")))))))))


(t/deftest undo-shortcut-remove-middle
  (let [setup-tx       [{:node/title "Alice" :block/uid "Alice" :page/sidebar 0}
                        {:node/title "Bob" :block/uid "Bob" :page/sidebar 1}
                        {:node/title "Charlie" :block/uid "Charlie" :page/sidebar 2}]
        setup!         #(fixture/transact-with-middleware setup-tx)
        remove-middle! #(->> "Bob"
                             (atomic-graph-ops/make-shortcut-remove-op)
                             (fixture/op-resolve-transact!))]

    (t/testing "undo shortcut/remove middle Bob"
      ;; setup
      (setup!)
      (let [sidebar-elem (common-db/get-sidebar-elements @@fixture/connection)]
        (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
        (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
        (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
        (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))
        ;; remove Bob
        (let [[evt-db evt] (remove-middle!)]
          (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
          (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
          (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Charlie")))
          ;; undo (add)
          (fixture/undo! evt-db evt)
          (let [sidebar-elem'' (common-db/get-sidebar-elements @@fixture/connection)]
            (t/is (= sidebar-elem sidebar-elem''))
            (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
            (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))))))

    (t/testing "redo"
      ;; setup
      (setup!)
      (let [sidebar-elem (common-db/get-sidebar-elements @@fixture/connection)]
        (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
        (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
        (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
        (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))
        ;; remove
        (let [[evt-db evt] (remove-middle!)]
          (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
          (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
          (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Charlie")))
          ;; undo (add)
          (let [[evt-db' evt'] (fixture/undo! evt-db evt)
                sidebar-elem' (common-db/get-sidebar-elements @@fixture/connection)]
            (t/is (= sidebar-elem sidebar-elem'))
            (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
            (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))
            ;; redo (remove)
            (fixture/undo! evt-db' evt')
            (t/is (= 2 (common-db/get-sidebar-count @@fixture/connection)))
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Charlie")))))))))


(t/deftest undo-shortcut-move-before
  (let [setup-tx [{:node/title "Alice" :block/uid "Alice" :page/sidebar 0}
                  {:node/title "Bob" :block/uid "Bob" :page/sidebar 1}
                  {:node/title "Charlie" :block/uid "Charlie" :page/sidebar 2}]
        setup!   #(fixture/transact-with-middleware setup-tx)
        save!    #(->> (atomic-graph-ops/make-shortcut-move-op "Charlie" {:page/title "Alice" :relation :before})
                       (fixture/op-resolve-transact!))]
    (t/testing "undo"
      ;; setup
      (setup!)
      (let [sidebar-elem (common-db/get-sidebar-elements @@fixture/connection)]
        (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
        (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
        (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
        (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))
        ;; move
        (let [[evt-db evt] (save!)]
          (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Charlie")))
          (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Alice")))
          (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Bob")))
          ;; undo (move)
          (fixture/undo! evt-db evt)
          (let [sidebar-elem' (common-db/get-sidebar-elements @@fixture/connection)]
            (t/is (= sidebar-elem sidebar-elem'))
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
            (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))))))

    (t/testing "redo"
      ;; setup
      (setup!)
      (let [sidebar-elem (common-db/get-sidebar-elements @@fixture/connection)]
        (t/is (= 3 (common-db/get-sidebar-count @@fixture/connection)))
        (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
        (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
        (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))
        ;; move
        (let [[evt-db evt] (save!)]
          (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Charlie")))
          (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Alice")))
          (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Bob")))
          ;; undo (move)
          (let [[evt-db' evt'] (fixture/undo! evt-db evt)
                sidebar-elem' (common-db/get-sidebar-elements @@fixture/connection)]
            (t/is (= sidebar-elem sidebar-elem'))
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Alice")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Bob")))
            (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Charlie")))
            ;; redo (move)
            (fixture/undo! evt-db' evt')
            (t/is (= 0 (common-db/find-order-from-title @@fixture/connection "Charlie")))
            (t/is (= 1 (common-db/find-order-from-title @@fixture/connection "Alice")))
            (t/is (= 2 (common-db/find-order-from-title @@fixture/connection "Bob")))))))))
