(ns athens.common-events.atomic-ops.page-remove-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datascript.core                      :as d])
  #?(:clj
     (:import
       (clojure.lang
         ExceptionInfo))))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest page-remove-test
  (t/testing "Removing page with no references"
    (let [test-uid        "test-page-uid-1"
          test-block-uid  "test-block-uid-1"
          test-title      "test page title 1"
          create-page-txs [{:block/uid      test-uid
                            :node/title     test-title
                            :block/children [{:block/uid      test-block-uid
                                              :block/order    0
                                              :block/string   ""
                                              :block/children []}]}]]
      (fixture/transact-with-middleware create-page-txs)
      (let [e-by-title (d/q '[:find ?e
                              :where [?e :node/title ?title]
                              :in $ ?title]
                            @@fixture/connection test-title)
            e-by-uid   (d/q '[:find ?e
                              :where [?e :block/uid ?uid]
                              :in $ ?uid]
                            @@fixture/connection test-uid)]
        (t/is (seq e-by-title))
        (t/is (= e-by-title e-by-uid)))

      (let [remove-page-op  (atomic-graph-ops/make-page-remove-op test-title)
            remove-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection remove-page-op)]

        (d/transact! @fixture/connection remove-page-txs)
        (let [e-by-title (d/q '[:find ?e
                                :where [?e :node/title ?title]
                                :in $ ?title]
                              @@fixture/connection test-title)
              e-by-uid   (d/q '[:find ?e
                                :where [?e :block/uid ?uid]
                                :in $ ?uid]
                              @@fixture/connection test-uid)]
          (t/is (empty? e-by-title))
          (t/is (= e-by-title e-by-uid))))))

  (t/testing "Remove page with references"
    (let [test-page-1-title "test page 1 title"
          test-page-1-uid   "test-page-1-uid"
          test-block-1-text "test block 1 uid"
          test-block-1-uid  "test-block-1-uid"
          test-page-2-title "test page 2 title"
          test-page-2-uid   "test-page-2-uid"
          page-ref-text     (str "[[" test-page-1-title "]]")
          block-ref-text    (str "((" test-block-1-uid "))")
          page-ref-uid      "page-ref-uid"
          block-ref-uid     "block-ref-uid"
          setup-txs         [{:db/id          -1
                              :node/title     test-page-1-title
                              :block/uid      test-page-1-uid
                              :block/children [{:db/id          -2
                                                :block/uid      test-block-1-uid
                                                :block/string   test-block-1-text
                                                :block/children []}]}
                             {:db/id          -3
                              :node/title     test-page-2-title
                              :block/uid      test-page-2-uid
                              :block/children [{:db/id        -4
                                                :block/uid    page-ref-uid
                                                :block/string page-ref-text}
                                               {:db/id        -5
                                                :block/uid    block-ref-uid
                                                :block/string block-ref-text}]}]
          query             '[:find ?text
                              :where
                              [?e :block/string ?text]
                              [?e :block/uid ?uid]
                              :in $ ?uid]]
      (fixture/transact-with-middleware setup-txs)
      (t/is (= #{[page-ref-text]}
               (d/q query
                    @@fixture/connection
                    page-ref-uid)))
      (t/is (= #{[block-ref-text]}
               (d/q query
                    @@fixture/connection
                    block-ref-uid)))

      ;; remove page 1
      (d/transact! @fixture/connection
                   (->> test-page-1-title
                        (atomic-graph-ops/make-page-remove-op)
                        (atomic-resolver/resolve-to-tx @@fixture/connection)))
      ;; check if page and block references were cleaned
      (t/is (= #{[test-page-1-title]}
               (d/q query
                    @@fixture/connection
                    page-ref-uid)))
      (t/is (= #{[test-block-1-text]}
               (d/q query
                    @@fixture/connection
                    block-ref-uid))))))


(t/deftest undo-page-remove-with-reference
  (let [test-page-1-title "test page 1 title"
        test-page-1-uid   "test-page-1-uid"
        test-page-2-title "test page 2 title"
        test-page-2-uid   "test-page-2-uid"
        block-text        (str "[[" test-page-1-title "]]")
        block-uid         "test-block-uid"
        setup-repr        [{:node/title     test-page-1-title
                            :block/uid      test-page-1-uid
                            :block/children [{:block/uid      "test-block-1-uid"
                                              :block/string   ""
                                              :block/children []}]}
                           {:node/title     test-page-2-title
                            :block/uid      test-page-2-uid
                            :block/children [{:block/uid    block-uid
                                              :block/string block-text}]}]
        get-str          #(->> [:block/uid %]
                               fixture/get-repr
                               :block/string)
        remove!          #(-> (atomic-graph-ops/make-page-remove-op %)
                              fixture/op-resolve-transact!)]

    (t/testing "undo page remove with refs"
      (fixture/transact-with-middleware setup-repr)
      (t/is (= block-text (get-str block-uid)) "see if test-page-1-title is referenced in test-page-2")
      (let [[evt-db db] (remove! test-page-1-title)]
        (remove! test-page-1-title)
        (t/is (= test-page-1-title (get-str block-uid)) "see if the referenced page got removed from the block string")
        (fixture/undo! evt-db db)
        (t/is (= block-text (get-str block-uid)) "After undo see if test-page-1-title is referenced in test-page-2")))))


(t/deftest undo-page-remove-without-reference
  (let [test-page-1-title "test page 1 title"
        test-page-1-uid   "test-page-1-uid"
        setup-repr        [{:node/title     test-page-1-title
                            :block/uid      test-page-1-uid
                            :block/children [{:block/uid      "test-block-1-uid"
                                              :block/string   ""
                                              :block/children []}]}]

        get-page-by-title #(common-db/get-page-document @@fixture/connection [:node/title test-page-1-title])
        remove!           #(-> (atomic-graph-ops/make-page-remove-op %)
                               fixture/op-resolve-transact!)]

    (t/testing "undo page remove with refs"
      (fixture/transact-with-middleware setup-repr)
      (t/is (seq (get-page-by-title)) "Check if the page is created")
      (let [[evt-db db] (remove! test-page-1-title)]
        (t/is (empty? (get-page-by-title)) "Check if the page is removed")
        (fixture/undo! evt-db db)
        (t/is (seq (get-page-by-title)) "After Undo Check if the page is created")))))


(t/deftest undo-page-remove-in-sidebar
  (let [test-page-1-title "test page 1 title"
        test-page-1-uid   "test-page-1-uid"
        setup-repr        [{:node/title     test-page-1-title
                            :block/uid      test-page-1-uid
                            :page/sidebar   0
                            :block/children [{:block/uid      "test-block-1-uid"
                                              :block/string   ""
                                              :block/children []}]}]
        remove!           #(-> (atomic-graph-ops/make-page-remove-op %)
                               fixture/op-resolve-transact!)]

    (t/testing "undo page remove with refs"
      (fixture/transact-with-middleware setup-repr)
      (let [[evt-db db] (remove! test-page-1-title)]
        (fixture/undo! evt-db db)
        (t/is (= (common-db/get-sidebar-titles @@fixture/connection)
                 [test-page-1-title])
              "Undo restored shortcuts")))))


(t/deftest nested-page-link-delete-snafu
  (let [page-title       "page title"
        page-link        (str "[[" page-title "]]")
        page-double-link (str "[[" page-link "]]")
        block-1-uid      "block-uid-1-1"
        block-1-txt      "## header2"
        block-2-uid      "block-uid-1-2"
        block-3-uid      "block-uid-1-3"
        setup-repr       [{:page/title     page-title
                           :block/children [{:block/uid    "random-block"
                                             :block/string "with text"}]}
                          {:page/title     page-link
                           :block/children [{:block/uid    "block-uid-1"
                                             :block/string "some meaningless text"}]}
                          {:page/title     "some other page"
                           :block/children [{:block/uid    block-1-uid
                                             :block/string block-1-txt}
                                            {:block/uid    block-2-uid
                                             :block/string page-double-link}
                                            {:block/uid    block-3-uid
                                             :block/string page-link}]}]]
    (t/testing "remove the page with nested page link"
      (fixture/setup! setup-repr)
      (fixture/op-resolve-transact! (atomic-graph-ops/make-page-remove-op page-link))
      ;; we didn't modify random shit
      (t/is (= block-1-txt (:block/string (common-db/get-block @@fixture/connection [:block/uid block-1-uid]))))
      ;; normal link stays intact
      (t/is page-link (:block/string (common-db/get-block @@fixture/connection [:block/uid block-3-uid])))
      ;; wrong link was stripped of `[[]]`
      (t/is page-link (:block/string (common-db/get-block @@fixture/connection [:block/uid block-2-uid])))
      (fixture/teardown! setup-repr))))


(t/deftest remove-prop-k
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key" #:block{:uid    "uid"
                                   :string ""}}}])
  (fixture/op-resolve-transact! (atomic-graph-ops/make-page-remove-op "key"))
  (fixture/is #{{:page/title "title"}}))
