(ns athens.common-events.atomic-ops.block-new-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [clojure.test                         :as t])
  #?(:clj
     (:import
       (clojure.lang
         ExceptionInfo))))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest concurrency-simulations-block-new-v2-1
  (t/testing "just 2 events starting from the same point, but with new concurrency compatible model, wooh!"
    (let [page-1-uid    "page-4-uid"
          block-1-uid   "block-4-1-uid"
          block-2-uid   "block-4-2-uid"
          block-3-1-uid "block-4-3-1-uid"
          block-3-2-uid "block-4-3-2-uid"
          setup-txs     [{:block/uid      page-1-uid
                          :node/title     "test page 4"
                          :block/children [{:block/uid      block-1-uid
                                            :block/string   ""
                                            :block/order    0
                                            :block/children []}
                                           {:block/uid      block-2-uid
                                            :block/string   ""
                                            :block/order    1
                                            :block/children []}]}]]
      (fixture/transact-with-middleware setup-txs)
      ;; we want children to be:
      ;; 0: block-1-uid
      ;; 1: block-3-1-uid
      ;; 2: block-2-uid
      ;; 3: block-3-2-uid
      ;; now, there is no order to resolve and apply these events so we have what was intended
      (t/testing "event-1 before event-2"
        (let [;; intention: add block after `block-1-uid`
              event-1 (atomic-graph-ops/make-block-new-op block-3-1-uid {:block/uid block-1-uid :relation :after})
              ;; intention: add block after `block-2-uid`
              event-2 (atomic-graph-ops/make-block-new-op block-3-2-uid {:block/uid block-2-uid :relation :after})]
          (-> event-1 fixture/op-resolve-transact!)
          (-> event-2 fixture/op-resolve-transact!)
          (t/is (= 1 (:block/order (common-db/get-block @@fixture/connection
                                                        [:block/uid block-3-1-uid]))))
          (t/is (= 3 (:block/order (common-db/get-block @@fixture/connection
                                                        [:block/uid block-3-2-uid]))))))))

  (t/testing "just 2 events starting from the same point, but with new concurrency compatible model, wooh!"
    (let [page-1-uid         "page-5-uid"
          block-1-uid        "block-5-1-uid"
          block-2-uid        "block-5-2-uid"
          block-2-order-prev 1
          block-3-1-uid      "block-5-3-1-uid"
          block-3-2-uid      "block-5-3-2-uid"
          setup-txs          [{:block/uid      page-1-uid
                               :node/title     "test page 5"
                               :block/children [{:block/uid      block-1-uid
                                                 :block/string   ""
                                                 :block/order    0
                                                 :block/children []}
                                                {:block/uid      block-2-uid
                                                 :block/string   ""
                                                 :block/order    block-2-order-prev
                                                 :block/children []}]}]]
      (fixture/transact-with-middleware setup-txs)
      ;; we want children to be at the end:
      ;; 0: block-1-uid
      ;; 1: block-3-1-uid
      ;; 2: block-2-uid
      ;; 3: block-3-2-uid
      ;; now, there is no order to resolve and apply these events so we have what was intended
      (t/testing "event-1 after event-2"

        (let [;; intention: add block after `block-1-uid`
              event-1     (atomic-graph-ops/make-block-new-op block-3-1-uid {:block/uid block-1-uid :relation :after})
              ;; intention: add block after `block-2-uid`
              event-2     (atomic-graph-ops/make-block-new-op block-3-2-uid {:block/uid block-2-uid :relation :after})]
          (-> event-2 fixture/op-resolve-transact!)
          (let [block-2   (common-db/get-block @@fixture/connection
                                               [:block/uid block-2-uid])
                block-3-2 (common-db/get-block @@fixture/connection
                                               [:block/uid block-3-2-uid])]
            (t/is (= block-2-order-prev (:block/order block-2)))
            (t/is (= 2 (:block/order block-3-2))))
          (-> event-1 fixture/op-resolve-transact!)
          (let [block-3-1 (common-db/get-block @@fixture/connection
                                               [:block/uid block-3-1-uid])
                block-3-2 (common-db/get-block @@fixture/connection
                                               [:block/uid block-3-2-uid])]
            (t/is (= 1 (:block/order block-3-1)))
            (t/is (= 3 (:block/order block-3-2)))))))))


(t/deftest block-new-v2-test
  (t/testing "`:block/new-v2` block creation tests"

    (t/testing "rel `:before`"
      (t/testing "inserts into 1st position"
        (let [parent-block-uid "1-before-test-parent-uid"
              block-1-uid      "1-before-test-block-1-uid"
              block-2-uid      "1-before-test-block-2-uid"
              setup-txs        [{:block/uid      parent-block-uid
                                 :block/string   ""
                                 :block/order    0
                                 :block/children [{:block/uid    block-1-uid
                                                   :block/string ""
                                                   :block/order  0}]}]]
          (fixture/transact-with-middleware setup-txs)
          (-> (atomic-graph-ops/make-block-new-op block-2-uid {:block/uid block-1-uid :relation :before})
              fixture/op-resolve-transact!)
          (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])]
            (t/is (= 2 (-> parent :block/children count)))
            (t/is (= 0 (-> block-2 :block/order)))
            (t/is (= 1 (-> block-1 :block/order))))))

      (t/testing "inserts between 2 blocks"
        (let [parent-block-uid "2-before-test-parent-uid"
              block-1-uid      "2-before-test-block-1-uid"
              block-2-uid      "2-before-test-block-2-uid"
              block-3-uid      "2-before-test-block-3-uid"
              setup-txs        [{:block/uid      parent-block-uid
                                 :block/string   ""
                                 :block/order    0
                                 :block/children [{:block/uid    block-1-uid
                                                   :block/string ""
                                                   :block/order  0}
                                                  {:block/uid    block-2-uid
                                                   :block/string ""
                                                   :block/order  1}]}]]
          (fixture/transact-with-middleware setup-txs)
          (-> (atomic-graph-ops/make-block-new-op block-3-uid {:block/uid block-2-uid :relation :before})
              fixture/op-resolve-transact!)
          (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])
                block-3 (common-db/get-block @@fixture/connection [:block/uid block-3-uid])]
            (t/is (= 3 (-> parent :block/children count)))
            (t/is (= 0 (-> block-1 :block/order)))
            (t/is (= 1 (-> block-3 :block/order)))
            (t/is (= 2 (-> block-2 :block/order)))))))

    (t/testing "rel `:after`"
      (t/testing "inserts at last position"
        (let [parent-block-uid "1-after-test-parent-uid"
              block-1-uid      "1-after-test-block-1-uid"
              block-2-uid      "1-after-test-block-2-uid"
              setup-txs        [{:block/uid      parent-block-uid
                                 :block/string   ""
                                 :block/order    0
                                 :block/children [{:block/uid    block-1-uid
                                                   :block/string ""
                                                   :block/order  0}]}]]
          (fixture/transact-with-middleware setup-txs)
          (-> (atomic-graph-ops/make-block-new-op block-2-uid {:block/uid block-1-uid :relation :after})
              fixture/op-resolve-transact!)
          (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])]
            (t/is (= 2 (-> parent :block/children count)))
            (t/is (= 0 (-> block-1 :block/order)))
            (t/is (= 1 (-> block-2 :block/order))))))

      (t/testing "inserts between 2 blocks"
        (let [parent-block-uid "2-after-test-parent-uid"
              block-1-uid      "2-after-test-block-1-uid"
              block-2-uid      "2-after-test-block-2-uid"
              block-3-uid      "2-after-test-block-3-uid"
              setup-txs        [{:block/uid      parent-block-uid
                                 :block/string   ""
                                 :block/order    0
                                 :block/children [{:block/uid    block-1-uid
                                                   :block/string ""
                                                   :block/order  0}
                                                  {:block/uid    block-2-uid
                                                   :block/string ""
                                                   :block/order  1}]}]]
          (fixture/transact-with-middleware setup-txs)
          (-> (atomic-graph-ops/make-block-new-op block-3-uid {:block/uid block-1-uid :relation :after})
              fixture/op-resolve-transact!)
          (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])
                block-3 (common-db/get-block @@fixture/connection [:block/uid block-3-uid])]
            (t/is (= 3 (-> parent :block/children count)))
            (t/is (= 0 (-> block-1 :block/order)))
            (t/is (= 1 (-> block-3 :block/order)))
            (t/is (= 2 (-> block-2 :block/order)))))))

    (t/testing "rel `:first`"
      (let [parent-block-uid "1-first-test-parent-uid"
            block-1-uid      "1-first-test-block-1-uid"
            block-2-uid      "1-first-test-block-2-uid"
            setup-txs        [{:block/uid      parent-block-uid
                               :block/string   ""
                               :block/order    0
                               :block/children [{:block/uid    block-1-uid
                                                 :block/string ""
                                                 :block/order  0}]}]]
        (fixture/transact-with-middleware setup-txs)
        (-> (atomic-graph-ops/make-block-new-op block-2-uid {:block/uid parent-block-uid :relation :first})
            fixture/op-resolve-transact!)
        (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
              block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
              block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])]
          (t/is (= 2 (-> parent :block/children count)))
          (t/is (= 0 (-> block-2 :block/order)))
          (t/is (= 1 (-> block-1 :block/order))))))

    (t/testing "rel `:last`"
      (let [parent-block-uid "1-last-test-parent-uid"
            block-1-uid      "1-last-test-block-1-uid"
            block-2-uid      "1-last-test-block-2-uid"
            setup-txs        [{:block/uid      parent-block-uid
                               :block/string   ""
                               :block/order    0
                               :block/children [{:block/uid    block-1-uid
                                                 :block/string ""
                                                 :block/order  0}]}]]
        (fixture/transact-with-middleware setup-txs)
        (-> (atomic-graph-ops/make-block-new-op block-2-uid {:block/uid parent-block-uid :relation :last})
            fixture/op-resolve-transact!)
        (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
              block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
              block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])]
          (t/is (= 2 (-> parent :block/children count)))
          (t/is (= 0 (-> block-1 :block/order)))
          (t/is (= 1 (-> block-2 :block/order))))))

    (t/testing "rel absolute ordering, please don't use it"
      (let [parent-block-uid "1-abs-test-parent-uid"
            block-1-uid      "1-abs-test-block-1-uid"
            block-2-uid      "1-abs-test-block-2-uid"
            block-3-uid      "1-abs-test-block-3-uid"
            setup-txs        [{:block/uid      parent-block-uid
                               :block/string   ""
                               :block/order    0
                               :block/children [{:block/uid    block-1-uid
                                                 :block/string ""
                                                 :block/order  0}
                                                {:block/uid    block-2-uid
                                                 :block/string ""
                                                 :block/order  1}]}]]
        (fixture/transact-with-middleware setup-txs)
        (let [position        (common-db/compat-position @@fixture/connection {:block/uid parent-block-uid :relation 1})
              block-new-v2-op (atomic-graph-ops/make-block-new-op block-3-uid position)]
          (-> block-new-v2-op fixture/op-resolve-transact!)
          (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])
                block-3 (common-db/get-block @@fixture/connection [:block/uid block-3-uid])]
            (t/is (= 3 (-> parent :block/children count)))
            (t/is (= 0 (-> block-1 :block/order)))
            (t/is (= 1 (-> block-3 :block/order)))
            (t/is (= 2 (-> block-2 :block/order)))))))


    (t/testing "missing ref"
      (let [parent-block-uid "missing-test-parent-uid"
            block-uid        "missing-test-block-uid"
            block-new-v2-op  (atomic-graph-ops/make-block-new-op block-uid {:block/uid parent-block-uid :relation :last})]
        (t/is (thrown-with-msg? #?(:cljs js/Error
                                   :clj ExceptionInfo)
                                #"Location uid does not exist"
                (fixture/op-resolve-transact! block-new-v2-op)))))))


(t/deftest undo
  (let [test-uid     "test-uid"
        new-test-uid "test-new-uid"
        setup-repr   [{:page/title     "test-undo-block-new-page"
                       :block/children [{:block/uid    test-uid
                                         :block/string ""}]}]
        get-children #(->> [:block/uid test-uid]
                           (common-db/get-block @@fixture/connection)
                           :block/children)
        new-child!   #(->> (atomic-graph-ops/make-block-new-op %
                                                               {:block/uid test-uid
                                                                :relation  :first})
                           fixture/op-resolve-transact!)]
    (t/testing "undo"
      (fixture/setup! setup-repr)
      (t/is (empty? (get-children)))
      (let [[event-db event] (new-child! new-test-uid)]
        (t/is (= [#:block{:uid   new-test-uid
                          :order 0}] (get-children)))
        (let [undo-event (fixture/undo-resulting-ops event-db event)]
          (t/is (= #:op{:type         :composite/consequence,
                        :atomic?      false,
                        :trigger      #:op{:undo (:event/id event)},
                        :consequences [#:op{:type    :block/remove,
                                            :atomic? true,
                                            :args    #:block{:uid new-test-uid}}]}
                   (:event/op undo-event))))
        (fixture/undo! event-db event)
        (t/is (empty? (get-children))))
      (fixture/teardown! setup-repr))))


(t/deftest undo-2
  (let [test-uid     "test-uid"
        new-test-uid "test-new-uid"
        setup-repr   [{:page/title     "test-undo-block-new-page"
                       :block/children [{:block/uid    test-uid
                                         :block/string ""}]}]
        get-children #(->> [:block/uid test-uid]
                           (common-db/get-block @@fixture/connection)
                           :block/children)
        new-child!   #(->> (atomic-graph-ops/make-block-new-op %
                                                               {:block/uid test-uid
                                                                :relation  :first})
                           fixture/op-resolve-transact!)]
    (t/testing "redo"
      (fixture/setup! setup-repr)
      (t/is (empty? (get-children)))
      (let [[event-db new-child-event] (new-child! new-test-uid)]
        (t/is (= [#:block{:uid   new-test-uid
                          :order 0}] (get-children)))
        (let [[undo-event-db undo-event] (fixture/undo! event-db new-child-event)]
          (t/is (= #:op{:type         :composite/consequence,
                        :atomic?      false,
                        :trigger      #:op{:undo (:event/id new-child-event)},
                        :consequences [#:op{:type    :block/remove,
                                            :atomic? true,
                                            :args    #:block{:uid new-test-uid}}]}
                   (:event/op undo-event)))
          (t/is (empty? (get-children)))
          (let [[_redo-event-db redo-event] (fixture/undo! undo-event-db undo-event)]
            (t/is (= #:op{:type         :composite/consequence
                          :atomic?      false
                          :trigger      #:op{:undo (:event/id undo-event)}
                          :consequences [#:op{:type    :block/new
                                              :atomic? true
                                              :args    #:block{:uid      new-test-uid
                                                               :position {:relation  :first
                                                                          :block/uid test-uid}}}
                                         #:op{:type    :block/save
                                              :atomic? true
                                              :args    #:block{:uid    new-test-uid
                                                               :string ""}}]}
                     (:event/op redo-event)))
            (t/is (= [#:block{:uid   new-test-uid
                              :order 0}] (get-children))))))
      (fixture/teardown! setup-repr))))


(t/deftest rel-property
  (fixture/setup! [{:page/title "title"}])
  (fixture/op-resolve-transact!
    (graph-ops/build-block-new-op @@fixture/connection "uid" {:page/title "title"
                                                              :relation   {:page/title "key"}}))
  (fixture/is #{{:page/title "key"}
                {:page/title "title"
                 :block/properties
                 {"key" #:block{:uid    "uid"
                                :string ""}}}}))


(t/deftest rel-property-repeat-fail
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key" #:block{:uid    "uid"
                                   :string ""}}}])
  (t/is (thrown-with-msg? #?(:cljs js/Error
                             :clj ExceptionInfo)
                          #"Location already contains key"
          (fixture/op-resolve-transact!
            (graph-ops/build-block-new-op @@fixture/connection "uid" {:page/title "title"
                                                                      :relation   {:page/title "key"}})))))


(t/deftest double-new
  (fixture/setup! [{:page/title "title"}])
  (-> (graph-ops/build-block-new-op @@fixture/connection "uid" {:page/title "title"
                                                                :relation   {:page/title "key"}})
      fixture/op-resolve-transact!)
  (-> (graph-ops/build-block-new-op @@fixture/connection "uid" {:page/title "title"
                                                                :relation   :first})
      fixture/op-resolve-transact!)
  (fixture/is #{{:page/title "key"}
                {:page/title "title"
                 :block/children
                 [#:block{:uid    "uid"
                          :string ""}]}}))
