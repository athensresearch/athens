(ns athens.common-events.atomic-ops.block-remove-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common.logging :as log]
    [clojure.pprint :as pp]
    [clojure.test                         :as t]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(defn transact!
  [uid]
  (-> (graph-ops/build-block-remove-op @@fixture/connection uid)
      fixture/op-resolve-transact!))


(t/deftest block-remove-onlychild-test
  (let [page-uid   "page-1-uid"
        parent-uid "parent-1-uid"
        child-uid  "child-1-uid"
        setup-txs  [{:block/uid      page-uid
                     :node/title     "test page 1"
                     :block/children {:block/uid      parent-uid
                                      :block/string   ""
                                      :block/order    0
                                      :block/children [{:block/uid      child-uid
                                                        :block/string   ""
                                                        :block/order    0
                                                        :block/children []}]}}]]
    (fixture/transact-with-middleware setup-txs)
    (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-uid])
                   :block/children
                   count))
          "Page should have only 1 child block after setup.")
    (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid parent-uid])
                   :block/children
                   count))
          "Parent should have only 1 child block after setup.")
    (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-uid])
                   :block/children
                   count)))
    (transact! child-uid)
    (let [page   (common-db/get-block @@fixture/connection [:block/uid page-uid])
          parent (common-db/get-block @@fixture/connection [:block/uid parent-uid])
          child  (common-db/e-by-av @@fixture/connection :block/uid child-uid)]
      (log/debug "block-remove-onlychild-test:"
                 "\npage:" (with-out-str
                             (pp/pprint page))
                 "\nparent:" (with-out-str
                               (pp/pprint parent))
                 "\nchild:" (with-out-str
                              (pp/pprint child)))
      (t/is (not child)
            "After `:block/remove` block should be gone for good")
      (t/is (= 1 (-> page :block/children count))
            "Page should have 1 child after block split")
      (t/is (= 0 (-> parent :block/children count))
            "Parent should not have children after `:block/remove`"))))


(t/deftest block-remove-childless-kids-test
  (t/testing "removing 1st child"
    (let [page-uid    "page-1-uid"
          parent-uid  "parent-1-uid"
          child-1-uid "child-1-1-uid"
          child-2-uid "child-1-2-uid"
          setup-txs   [{:block/uid      page-uid
                        :node/title     "test page - remove childless kids"
                        :block/children {:block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children [{:block/uid      child-1-uid
                                                           :block/string   ""
                                                           :block/order    0
                                                           :block/children []}
                                                          {:block/uid      child-2-uid
                                                           :block/string   ""
                                                           :block/order    1
                                                           :block/children []}]}}]]
      (fixture/transact-with-middleware setup-txs)
      (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-uid])
                     :block/children
                     count))
            "Page should have only 1 child block after setup.")
      (t/is (= 2 (-> (common-db/get-block @@fixture/connection [:block/uid parent-uid])
                     :block/children
                     count))
            "Parent should have only 2 child block after setup.")
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
                     :block/children
                     count)))
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
                     :block/children
                     count)))
      (transact! child-1-uid)
      (let [page         (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent       (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            first-child  (common-db/e-by-av @@fixture/connection :block/uid child-1-uid)
            second-child (common-db/get-block @@fixture/connection [:block/uid child-2-uid])]
        (log/debug "block-remove-childless-kids-test:"
                   "\npage:" (with-out-str
                               (pp/pprint page))
                   "\nparent:" (with-out-str
                                 (pp/pprint parent))
                   "\nfirst-child:" (with-out-str
                                      (pp/pprint first-child))
                   "\nsecond-child:" (with-out-str
                                       (pp/pprint second-child)))
        (t/is (not first-child)
              "After `:block/remove` block should be gone for good")
        (t/is (= 1 (-> page :block/children count))
              "Page should have 1 child after block split")
        (t/is (= 1 (-> parent :block/children count))
              "Parent should not have 1 child after `:block/remove`")
        (t/is (= 0 (-> second-child :block/order))))))

  (t/testing "removing last child"
    (let [page-uid    "page-2-uid"
          parent-uid  "parent-2-uid"
          child-1-uid "child-2-1-uid"
          child-2-uid "child-2-2-uid"
          setup-txs   [{:block/uid      page-uid
                        :node/title     "test page 1"
                        :block/children {:block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children [{:block/uid      child-1-uid
                                                           :block/string   ""
                                                           :block/order    0
                                                           :block/children []}
                                                          {:block/uid      child-2-uid
                                                           :block/string   ""
                                                           :block/order    1
                                                           :block/children []}]}}]]
      (fixture/transact-with-middleware setup-txs)
      (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-uid])
                     :block/children
                     count))
            "Page should have only 1 child block after setup.")
      (t/is (= 2 (-> (common-db/get-block @@fixture/connection [:block/uid parent-uid])
                     :block/children
                     count))
            "Parent should have only 2 child block after setup.")
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
                     :block/children
                     count)))
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
                     :block/children
                     count)))
      (transact! child-2-uid)
      (let [page          (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent        (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
            first-child   (common-db/get-block @@fixture/connection [:block/uid child-1-uid])]
        (t/is (not child-exists?)
              "After `:block/remove` block should be gone for good")
        (t/is (= 1 (-> page :block/children count))
              "Page should have 1 child after block split")
        (t/is (= 1 (-> parent :block/children count))
              "Parent should not have 1 child after `:block/remove`")
        (t/is (= 0 (-> first-child :block/order))))))

  (t/testing "removing middle child"
    (let [page-uid    "page-3-uid"
          parent-uid  "parent-3-uid"
          child-1-uid "child-3-1-uid"
          child-2-uid "child-3-2-uid"
          child-3-uid "child-3-3-uid"
          setup-txs   [{:block/uid      page-uid
                        :node/title     "test page removing middle child"
                        :block/children {:block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children [{:block/uid      child-1-uid
                                                           :block/string   ""
                                                           :block/order    0
                                                           :block/children []}
                                                          {:block/uid      child-2-uid
                                                           :block/string   ""
                                                           :block/order    1
                                                           :block/children []}
                                                          {:block/uid      child-3-uid
                                                           :block/string   ""
                                                           :block/order    2
                                                           :block/children []}]}}]]
      (fixture/transact-with-middleware setup-txs)
      (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-uid])
                     :block/children
                     count))
            "Page should have only 1 child block after setup.")
      (t/is (= 3 (-> (common-db/get-block @@fixture/connection [:block/uid parent-uid])
                     :block/children
                     count))
            "Parent should have only 3 child block after setup.")
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
                     :block/children
                     count)))
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
                     :block/children
                     count)))
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-3-uid])
                     :block/children
                     count)))
      (transact! child-2-uid)
      (let [page          (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent        (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
            first-child   (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            last-child    (common-db/get-block @@fixture/connection [:block/uid child-3-uid])]
        (t/is (not child-exists?)
              "After `:block/remove` block should be gone for good")
        (t/is (= 1 (-> page :block/children count))
              "Page should have 1 child after block split")
        (t/is (= 2 (-> parent :block/children count))
              "Parent should have 2 child after `:block/remove`")
        (t/is (= 0 (-> first-child :block/order)))
        (t/is (= 1 (-> last-child :block/order)))))))


(t/deftest block-remove-subtree
  (t/testing "Make sure we remove subtree"
    (let [page-uid    "page-1-uid"
          parent-uid  "parent-1-uid"
          child-1-uid "child-1-1-uid"
          child-2-uid "child-1-1-1-uid"
          child-3-uid "child-1-1-1-1-uid"
          child-4-uid "child-1-2-uid"
          setup-txs   [{:block/uid      page-uid
                        :node/title     "test page - removing subtree"
                        :block/children [{:block/uid      parent-uid
                                          :block/string   ""
                                          :block/order    0
                                          :block/children [{:block/uid      child-1-uid
                                                            :block/string   ""
                                                            :block/order    0
                                                            :block/children [{:block/uid      child-2-uid
                                                                              :block/string   ""
                                                                              :block/order    0
                                                                              :block/children []}
                                                                             {:block/uid      child-3-uid
                                                                              :block/string   ""
                                                                              :block/order    0
                                                                              :block/children []}]}
                                                           {:block/uid      child-4-uid
                                                            :block/string   ""
                                                            :block/order    1
                                                            :block/children []}]}]}]]
      (fixture/transact-with-middleware setup-txs)
      (transact! child-1-uid)
      (let [page            (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent          (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-exists? (common-db/e-by-av @@fixture/connection :block/uid child-1-uid)
            child-2-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
            child-3-exists? (common-db/e-by-av @@fixture/connection :block/uid child-3-uid)
            child-4         (common-db/get-block @@fixture/connection [:block/uid child-4-uid])]
        (t/is (not child-1-exists?))
        (t/is (not child-2-exists?))
        (t/is (not child-3-exists?))
        (t/is (= 1 (-> parent :block/children count)))
        (t/is (= 0 (:block/order child-4)))
        (t/is (= 1 (-> page :block/children count))))))

  (t/testing "Make sure we remove subtree, event if there was a block ref below"
    (let [page-uid    "page-2-uid"
          parent-uid  "parent-2-uid"
          child-1-uid "child-2-1-uid"
          child-2-uid "child-2-1-1-uid"
          child-3-uid "child-2-1-1-1-uid"
          child-4-uid "child-2-2-uid"
          setup-txs   [{:block/uid      page-uid
                        :node/title     "test page - removing subtree even with block refs below"
                        :block/children [{:block/uid      parent-uid
                                          :block/string   ""
                                          :block/order    0
                                          :block/children [{:block/uid      child-1-uid
                                                            :block/string   ""
                                                            :block/order    0
                                                            :block/children [{:block/uid      child-2-uid
                                                                              :block/string   (str "((" child-1-uid "))")
                                                                              :block/order    0
                                                                              :block/children [{:block/uid      child-3-uid
                                                                                                :block/string   ""
                                                                                                :block/order    0
                                                                                                :block/children []}]}]}
                                                           {:block/uid      child-4-uid
                                                            :block/string   ""
                                                            :block/order    1
                                                            :block/children []}]}]}]]
      (fixture/transact-with-middleware setup-txs)
      (transact! child-1-uid)
      (let [page            (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent          (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-exists? (common-db/e-by-av @@fixture/connection :block/uid child-1-uid)
            child-2-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
            child-3-exists? (common-db/e-by-av @@fixture/connection :block/uid child-3-uid)
            child-4         (common-db/get-block @@fixture/connection [:block/uid child-4-uid])]
        (t/is (not child-1-exists?))
        (t/is (not child-2-exists?))
        (t/is (not child-3-exists?))
        (t/is (= 1 (-> parent :block/children count)))
        (t/is (= 0 (:block/order child-4)))
        (t/is (= 1 (-> page :block/children count)))))))


(t/deftest block-remove-with-block-refs-involved
  (t/testing "Make sure we modify block string of containing block-refs to removed block"
    (let [page-uid     "page-1-uid"
          parent-uid   "parent-1-uid"
          child-1-uid  "child-1-1-uid"
          child-1-text "this text is godly"
          child-2-uid  "child-1-1-1-uid"
          child-3-uid  "child-1-2-uid"
          setup-txs    [{:block/uid      page-uid
                         :node/title     "test page - removing subtree"
                         :block/children [{:block/uid      parent-uid
                                           :block/string   ""
                                           :block/order    0
                                           :block/children [{:block/uid      child-1-uid
                                                             :block/string   child-1-text
                                                             :block/order    0
                                                             :block/children [{:block/uid      child-2-uid
                                                                               :block/string   ""
                                                                               :block/order    0
                                                                               :block/children []}]}
                                                            {:block/uid      child-3-uid
                                                             :block/string   (str "((" child-1-uid "))")
                                                             :block/order    1
                                                             :block/children []}]}]}]]
      (fixture/transact-with-middleware setup-txs)
      (transact! child-1-uid)
      (let [page            (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent          (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-exists? (common-db/e-by-av @@fixture/connection :block/uid child-1-uid)
            child-2-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
            child-3         (common-db/get-block @@fixture/connection [:block/uid child-3-uid])]
        (t/is (= 1 (-> page :block/children count)))
        (t/is (= 1 (-> parent :block/children count)))
        (t/is (not child-1-exists?))
        (t/is (not child-2-exists?))
        (t/is (= 0 (:block/order child-3)))
        (t/is (= child-1-text (:block/string child-3))))))

  (t/testing "Make sure we don't modify block string of blocks that are deleted but did have block-ref"
    (let [page-uid     "page-2-uid"
          parent-uid   "parent-2-uid"
          child-1-uid  "child-2-1-uid"
          child-1-text "this text is 1337"
          child-2-uid  "child-2-1-1-uid"
          child-3-uid  "child-2-2-uid"
          setup-txs    [{:block/uid      page-uid
                         :node/title     "test page - removing subtree with block ref"
                         :block/children [{:block/uid      parent-uid
                                           :block/string   ""
                                           :block/order    0
                                           :block/children [{:block/uid      child-1-uid
                                                             :block/string   child-1-text
                                                             :block/order    0
                                                             :block/children [{:block/uid      child-2-uid
                                                                               :block/string   (str "((" child-1-uid "))")
                                                                               :block/order    0
                                                                               :block/children []}]}
                                                            {:block/uid      child-3-uid
                                                             :block/string   (str "((" child-1-uid "))")
                                                             :block/order    1
                                                             :block/children []}]}]}]]
      (fixture/transact-with-middleware setup-txs)
      (transact! child-1-uid)
      (let [page            (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent          (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-exists? (common-db/e-by-av @@fixture/connection :block/uid child-1-uid)
            child-2-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
            child-3         (common-db/get-block @@fixture/connection [:block/uid child-3-uid])]
        (t/is (= 1 (-> page :block/children count))
              (str "Should be 1 child:" (pr-str (:block/children page))))
        (t/is (= 1 (-> parent :block/children count)))
        (t/is (not child-1-exists?))
        (t/is (not child-2-exists?))
        (t/is (= 0 (:block/order child-3)))
        (t/is (= child-1-text (:block/string child-3)))))))


(t/deftest block-delete-and-merge
  (t/testing "Merge and delete with update"
    (let [page-uid             "page-1-uid"
          parent-uid           "parent-1-uid"
          child-1-uid          "child-1-1-uid"
          child-1-text         "this text is godly"
          child-1-updated-text "new text for child 1"
          child-2-uid          "child-1-1-1-uid"
          child-2-text         "this text is unholy"
          child-3-uid          "child-1-2-uid"
          setup-txs            [{:block/uid      page-uid
                                 :node/title     "test page - merge and delete with update"
                                 :block/children [{:block/uid      parent-uid
                                                   :block/string   ""
                                                   :block/order    0
                                                   :block/children [{:block/uid      child-1-uid
                                                                     :block/string   child-1-text
                                                                     :block/order    0
                                                                     :block/children []}
                                                                    {:block/uid      child-2-uid
                                                                     :block/string   child-2-text
                                                                     :block/order    1
                                                                     :block/children [{:block/uid      child-3-uid
                                                                                       :block/string   ""
                                                                                       :block/order    0
                                                                                       :block/children []}]}]}]}]]
      (fixture/transact-with-middleware setup-txs)
      (-> (graph-ops/build-block-merge-with-updated-op @@fixture/connection
                                                       child-2-uid
                                                       child-1-uid
                                                       child-2-text
                                                       child-1-updated-text)
          fixture/op-resolve-transact!)
      (let [page            (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent          (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1         (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            child-2-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
            child-3         (common-db/get-block @@fixture/connection [:block/uid child-3-uid])]
        (t/is (= 1 (-> page :block/children count)))
        (t/is (= 1 (-> parent :block/children count)))
        (t/is (seq child-1))
        (t/is (not child-2-exists?))
        (t/is (seq child-3))
        (t/is (= 0 (:block/order child-3)))
        (t/is (= (str child-1-updated-text child-2-text)
                 (:block/string child-1))))))

  (t/testing "Merge and delete without update"
    (let [page-uid             "page-2-uid"
          parent-uid           "parent-2-uid"
          child-1-uid          "child-2-1-uid"
          child-1-text         "this text is godly"
          child-2-uid          "child-2-1-1-uid"
          child-2-text         "this text is unholy"
          child-3-uid          "child-2-2-uid"
          setup-txs            [{:block/uid      page-uid
                                 :node/title     "test page - merge and delete without update"
                                 :block/children [{:block/uid      parent-uid
                                                   :block/string   ""
                                                   :block/order    0
                                                   :block/children [{:block/uid      child-1-uid
                                                                     :block/string   child-1-text
                                                                     :block/order    0
                                                                     :block/children []}
                                                                    {:block/uid      child-2-uid
                                                                     :block/string   child-2-text
                                                                     :block/order    1
                                                                     :block/children [{:block/uid      child-3-uid
                                                                                       :block/string   ""
                                                                                       :block/order    0
                                                                                       :block/children []}]}]}]}]]
      (fixture/transact-with-middleware setup-txs)
      (-> (graph-ops/build-block-remove-merge-op @@fixture/connection child-2-uid child-1-uid child-2-text)
          fixture/op-resolve-transact!)
      (let [page            (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent          (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1         (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            child-2-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
            child-3         (common-db/get-block @@fixture/connection [:block/uid child-3-uid])]
        (t/is (= 1 (-> page :block/children count)) (str "Page should have 1 element:" (pr-str (:block/children page))))
        (t/is (= 1 (-> parent :block/children count)))
        (t/is (seq child-1))
        (t/is (not child-2-exists?))
        (t/is (seq child-3))
        (t/is (= 0 (:block/order child-3)))
        (t/is (= (str child-1-text child-2-text)
                 (:block/string child-1)))))))


(t/deftest undo
  (let [test-uid   "test-uid"
        setup-repr [{:page/title     "test-page"
                     :block/children [{:block/uid    "reffer-uid"
                                       ;; The string displays as:
                                       ;;   "yields falsehood when preceded by its quotation" yields falsehood when preceded by its quotation.
                                       ;; This is a quine: https://en.wikipedia.org/wiki/Quine_(computing)
                                       ;; It's interesting in and of itself but it's also especially relevant
                                       ;; for this test, as it's an example of a reference in plain text.
                                       ;; Undoing a remove must restore the referencing string to its previous
                                       ;; state instead of trying to do clever things with regexes, since there
                                       ;; isn't enough information in the string after the reference was removed
                                       ;; to determine what parts need to go back to being a ref.
                                       :block/string (str "\"((" test-uid "))\" ((" test-uid ")).")}
                                      {:block/uid      test-uid
                                       :block/string   "yields falsehood when preceded by its quotation"
                                       :block/children [{:block/uid    "123-uid"
                                                         :block/string "123"}]}]}]
        exp-repr   [{:page/title     "test-page"
                     :block/children [{:block/uid    "reffer-uid"
                                       :block/string "\"yields falsehood when preceded by its quotation\" yields falsehood when preceded by its quotation."}]}]
        lookup     [:node/title "test-page"]
        remove!    #(-> (atomic-graph-ops/make-block-remove-op test-uid)
                        fixture/op-resolve-transact!)]

    (t/testing "undo"
      (fixture/setup! setup-repr)
      (let [[evt-db evt] (remove!)]
        (t/is (= [(fixture/get-repr lookup)] exp-repr)
              "Removed block and children, and replaced ref with text")
        (fixture/undo! evt-db evt)
        (t/is (= setup-repr [(fixture/get-repr [:node/title "test-page"])]) "Undo restored to the original state"))
      (fixture/teardown! setup-repr))


    (t/testing "redo"
      (fixture/setup! setup-repr)
      (let [[evt-db evt]   (remove!)
            [evt-db' evt'] (fixture/undo! evt-db evt)]
        (fixture/undo! evt-db' evt')
        (t/is (= [(fixture/get-repr lookup)] exp-repr)
              "Redo removed block and children, and replaced ref with text"))
      (fixture/teardown! setup-repr))))


(t/deftest remove-prop
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key" #:block{:uid    "uid"
                                   :string ""}}}])
  (fixture/op-resolve-transact! (atomic-graph-ops/make-block-remove-op "uid"))
  (fixture/is #{{:page/title "key"}
                {:page/title "title"}}))


(t/deftest remove-prop-parent
  (fixture/setup! [{:page/title "title"
                    :block/children
                    [#:block{:uid    "parent-uid"
                             :string ""
                             :properties
                             {"key" #:block{:uid    "uid"
                                            :string ""}}}]}])
  (fixture/op-resolve-transact! (atomic-graph-ops/make-block-remove-op "parent-uid"))
  (fixture/is #{{:page/title "key"}
                {:page/title "title"}}))


(t/deftest remove-prop-parent-deep
  (fixture/setup! [{:page/title "title"
                    :block/children
                    [#:block{:uid    "parent-uid"
                             :string ""
                             :properties
                             {"comments/thread"
                              #:block{:uid    "thread-uid"
                                      :string ""
                                      :children
                                      [#:block{:uid    "deep-1"
                                               :string ""
                                               :properties
                                               {"deep-prop" #:block{:uid    "deep-1-1"
                                                                    :string ""}}}
                                       #:block{:uid    "deep-2"
                                               :string ""
                                               :properties
                                               {"deep-prop" #:block{:uid    "deep-2-1"
                                                                    :string ""}}}]}}}]}])
  (fixture/op-resolve-transact! (atomic-graph-ops/make-block-remove-op "parent-uid"))
  (fixture/is #{{:page/title "comments/thread"}
                {:page/title "deep-prop"}
                {:page/title "title"}}))
