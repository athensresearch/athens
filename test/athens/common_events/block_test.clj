(ns athens.common-events.block-test
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as t]
    [datahike.api                  :as d]))


(t/use-fixtures :each fixture/integration-test-fixture)


(t/deftest block-save-test
  (t/testing "Saving block string"
    (let [block-uid   "test-block-uid"
          string-init "start test string"
          string-new  "new test string"
          setup-tx    [{:db/id          -1
                        :block/uid      block-uid
                        :block/string   string-init
                        :block/order    0
                        :block/children []}]]
      (d/transact @fixture/connection setup-tx)
      (let [block-save-event             (common-events/build-block-save-event -1
                                                                               block-uid
                                                                               string-new
                                                                               false)
            block-save-txs               (resolver/resolve-event-to-tx @@fixture/connection
                                                                       block-save-event)
            {block-string :block/string} (common-db/get-block @@fixture/connection
                                                              [:block/uid  block-uid])]
        (t/is (= string-init block-string))
        (d/transact @fixture/connection block-save-txs)
        (let [{new-block-string :block/string} (common-db/get-block @@fixture/connection
                                                                    [:block/uid  block-uid])]
          (t/is (= string-new new-block-string)))))))


(t/deftest block-open-test
  (t/testing "Open a block with children"
    (let [block-uid   "test-block-uid"
          child-1-uid "child-1-1-uid"
          string-init "start test string"
          setup-tx    [{:block/uid      block-uid
                        :block/string   string-init
                        :block/order    0
                        :block/open     false
                        :block/children {:block/uid      child-1-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children []}}]]
      (d/transact @fixture/connection setup-tx)
      (let [block-open-event   (common-events/build-block-open-event -1
                                                                     block-uid
                                                                     true)
            block-open-txs     (resolver/resolve-event-to-tx @@fixture/connection
                                                             block-open-event)
            current-open-state (:block/open (common-db/get-block @@fixture/connection [:block/uid block-uid]))]

        (t/is (= false current-open-state))
        (d/transact @fixture/connection block-open-txs)

        (let [current-open-state (:block/open (common-db/get-block @@fixture/connection [:block/uid block-uid]))]
          (t/is (= true current-open-state)))))))


(t/deftest block-close-test
  (t/testing "Close a block with children"
    (let [block-uid   "test-block-uid"
          child-1-uid "child-1-1-uid"
          string-init "start test string"
          setup-tx    [{:block/uid      block-uid
                        :block/string   string-init
                        :block/order    0
                        :block/open     true
                        :block/children {:block/uid      child-1-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children []}}]]
      (d/transact @fixture/connection setup-tx)
      (let [block-open-event   (common-events/build-block-open-event -1
                                                                     block-uid
                                                                     false)
            block-open-txs     (resolver/resolve-event-to-tx @@fixture/connection
                                                             block-open-event)
            current-open-state (:block/open (common-db/get-block @@fixture/connection [:block/uid block-uid]))]

        (t/is (= true current-open-state))
        (d/transact @fixture/connection block-open-txs)

        (let [current-open-state (:block/open (common-db/get-block @@fixture/connection [:block/uid block-uid]))]
          (t/is (= false current-open-state)))))))


(t/deftest new-block-tests
  (t/testing "Adding new block to new page"
    (let [page-1-uid  "page-1-uid"
          child-1-uid "child-1-1-uid"
          child-2-uid "child-1-2-uid"
          setup-txs   [{:db/id          -1
                        :block/uid      page-1-uid
                        :node/title     "test page 1"
                        :block/children {:db/id          -2
                                         :block/uid      child-1-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children []}}]]
      (d/transact @fixture/connection setup-txs)
      (let [page-1-eid      (common-db/e-by-av @@fixture/connection
                                               :block/uid page-1-uid)
            child-1-eid     (common-db/e-by-av @@fixture/connection
                                               :block/uid child-1-uid)
            new-block-event (common-events/build-new-block-event -1
                                                                 page-1-eid
                                                                 0
                                                                 child-2-uid)
            new-block-txs   (resolver/resolve-event-to-tx @@fixture/connection
                                                          new-block-event)
            query-children  '[:find ?child
                              :in $ ?eid
                              :where [?eid :block/children ?child]]]
        (t/is (= #{[child-1-eid]} (d/q query-children @@fixture/connection page-1-eid)))
        (d/transact @fixture/connection new-block-txs)
        (let [child-2-eid (common-db/e-by-av @@fixture/connection
                                             :block/uid child-2-uid)
              children (d/q query-children @@fixture/connection page-1-eid)]
          (t/is (seq children))
          (t/is (= #{[child-1-eid] [child-2-eid]} children)))))))


;; TODO more test cases for `:datascript/new-block` event



(t/deftest split-block-tests
  (t/testing "Simple case, no page links or block refs"
    (let [parent-uid         "test-parent-2-uid"
          child-1-uid        "test-child-2-1-uid"
          child-1-init-value "we split this"
          child-2-uid        "test-child-2-2-uid"
          setup-txs          [{:db/id          -1
                               :node/title     "test page"
                               :block/uid      "page-uid"
                               :block/children {:db/id          -2
                                                :block/uid      parent-uid
                                                :block/string   ""
                                                :block/order    0
                                                :block/children {:db/id          -3
                                                                 :block/uid      child-1-uid
                                                                 :block/string   child-1-init-value
                                                                 :block/order    0
                                                                 :block/children []}}}]]
      (d/transact @fixture/connection setup-txs)

      (let [parent-eid        (common-db/e-by-av @@fixture/connection
                                                 :block/uid parent-uid)
            child-1-eid       (common-db/e-by-av @@fixture/connection
                                                 :block/uid child-1-uid)
            child-1           (d/pull @@fixture/connection
                                      [:block/uid
                                       :block/order
                                       :block/string]
                                      child-1-eid)
            split-block-event (common-events/build-split-block-event -1
                                                                     child-1-uid
                                                                     child-1-init-value
                                                                     2
                                                                     child-2-uid)
            split-block-txs   (resolver/resolve-event-to-tx @@fixture/connection
                                                            split-block-event)
            query-children    '[:find ?child
                                :in $ ?eid
                                :where [?eid :block/children ?child]]]

        ;; before we add second child, check for 1st one
        (t/is (= #{[child-1-eid]} (d/q query-children @@fixture/connection parent-eid)))
        (t/is (= {:block/uid    child-1-uid
                  :block/order  0
                  :block/string child-1-init-value}
                 child-1))

        ;; split the block
        (d/transact @fixture/connection split-block-txs)
        (let [child-2-eid (common-db/e-by-av @@fixture/connection
                                             :block/uid child-2-uid)
              children    (d/q query-children @@fixture/connection parent-eid)
              ;; query for child 1, it was updated with transact
              child-1     (d/pull @@fixture/connection
                                  [:block/uid
                                   :block/order
                                   :block/string]
                                  child-1-eid)
              child-2     (d/pull @@fixture/connection
                                  [:block/uid
                                   :block/order
                                   :block/string]
                                  child-2-eid)]
          (t/is (seq children))
          (t/is (= #{[child-2-eid] [child-1-eid]} children))
          (t/is (= {:block/uid    child-2-uid
                    :block/order  1
                    :block/string (subs child-1-init-value 2)}
                   child-2))
          (t/is (= {:block/uid    child-1-uid
                    :block/order  0
                    :block/string (subs child-1-init-value 0 2)}
                   child-1)))))))


;; TODO: test case of moving page links and block refs



(t/deftest add-child-tests
  (t/testing "Adding 1st child"
    (let [parent-1-uid "test-parent-1-uid"
          child-1-uid  "test-child-1-1-uid"
          setup-txs    [{:db/id          -1
                         :node/title     "test page"
                         :block/uid      "page-uid"
                         :block/children {:db/id          -2
                                          :block/uid      parent-1-uid
                                          :block/string   ""
                                          :block/order    0
                                          :block/children []}}]]
      (d/transact @fixture/connection setup-txs)
      (let [add-child-event (common-events/build-add-child-event -1 parent-1-uid child-1-uid false)
            txs             (resolver/resolve-event-to-tx @@fixture/connection
                                                          add-child-event)
            query-children  '[:find ?children
                              :in $ ?eid
                              :where [?eid :block/children ?children]]]
        (t/is (= #{} (d/q query-children @@fixture/connection [:block/uid parent-1-uid])))
        (d/transact @fixture/connection txs)
        (let [child-eid (common-db/e-by-av @@fixture/connection
                                           :block/uid child-1-uid)
              children  (d/q query-children @@fixture/connection [:block/uid parent-1-uid])]
          (t/is (seq children))
          (t/is (= #{[child-eid]} children))))))

  (t/testing "Adding 2nd child"
    (let [parent-uid  "test-parent-2-uid"
          child-1-uid "test-child-2-1-uid"
          child-2-uid "test-child-2-2-uid"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children {:db/id          -2
                                         :block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children {:db/id          -3
                                                          :block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/children []}}}]]
      (d/transact @fixture/connection setup-txs)

      (let [child-1-eid     (common-db/e-by-av @@fixture/connection
                                               :block/uid child-1-uid)
            child-1         (d/pull @@fixture/connection
                                    [:block/uid :block/order]
                                    child-1-eid)
            add-child-event (common-events/build-add-child-event -1 parent-uid child-2-uid false)
            add-child-txs   (resolver/resolve-event-to-tx @@fixture/connection
                                                          add-child-event)
            query-children  '[:find ?child
                              :in $ ?eid
                              :where [?eid :block/children ?child]]]

        ;; before we add second child, check for 1st one
        (t/is (= #{[child-1-eid]} (d/q query-children @@fixture/connection [:block/uid parent-uid])))
        (t/is (= {:block/uid   child-1-uid
                  :block/order 0}
                 child-1))

        ;; add second child
        (d/transact @fixture/connection add-child-txs)
        (let [child-2-eid (common-db/e-by-av @@fixture/connection
                                             :block/uid child-2-uid)
              children    (d/q query-children @@fixture/connection [:block/uid parent-uid])
              child-1     (d/pull @@fixture/connection
                                  [:block/uid :block/order]
                                  child-1-eid)
              child-2     (d/pull @@fixture/connection
                                  [:block/uid :block/order]
                                  child-2-eid)]
          (t/is (seq children))
          (t/is (= #{[child-2-eid] [child-1-eid]} children))
          (t/is (= {:block/uid   child-2-uid
                    :block/order 0}
                   child-2))
          (t/is (= {:block/uid   child-1-uid
                    :block/order 1}
                   child-1)))))))


(t/deftest split-block-to-children-test
  (t/testing "Just splitting text, no link management involved"
    (let [parent-uid  "test-parent-1-uid"
          child-1-uid "test-child-1-1-uid"
          child-2-uid "test-child-1-2-uid"
          parent-text "abc123"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children {:db/id          -2
                                         :block/uid      parent-uid
                                         :block/string   parent-text
                                         :block/order    0
                                         :block/children {:db/id          -3
                                                          :block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/children []}}}]]
      (d/transact @fixture/connection setup-txs)
      (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            split-event   (common-events/build-split-block-to-children-event -1
                                                                             parent-uid
                                                                             parent-text
                                                                             3
                                                                             child-2-uid)
            split-txs     (resolver/resolve-event-to-tx @@fixture/connection split-event)]
        (t/is (= 1 (-> parent-block :block/children count)))
        (t/is (= [(select-keys child-1-block [:block/uid :block/order])]
                 (:block/children parent-block)))

        (d/transact @fixture/connection split-txs)
        (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
              child-2-block (common-db/get-block @@fixture/connection [:block/uid child-2-uid])]
          (t/is (= "abc" (:block/string parent-block)))
          (t/is (= "123" (:block/string child-2-block))))))))


;; TODO reference maintaining test "[[abc]]|[[def]]" -> "[[abc]]", "[[def]]" (and similar)


(t/deftest unindent-test
  (t/testing "Just unindent already"
    (let [parent-uid  "test-parent-1-uid"
          child-1-uid "test-child-1-1-uid"
          child-text  "abc123"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children {:db/id          -2
                                         :block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children {:db/id          -3
                                                          :block/uid      child-1-uid
                                                          :block/string   child-text
                                                          :block/order    0
                                                          :block/children []}}}]]
      (d/transact @fixture/connection setup-txs)
      (let [parent-block   (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-block  (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            unindent-event (common-events/build-unindent-event -1
                                                               child-1-uid
                                                               child-text)
            unindent-txs   (resolver/resolve-event-to-tx @@fixture/connection unindent-event)]
        (t/is (= 1 (-> parent-block :block/children count)))
        (t/is (= [(select-keys child-1-block [:block/uid :block/order])]
                 (:block/children parent-block)))

        (d/transact @fixture/connection unindent-txs)
        (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])]
          (t/is (= 0 (-> parent-block :block/children count)))
          (t/is (= 1 (:block/order child-1-block))))))))


(t/deftest unindent-multi-test
  (t/testing "Just unindent multiple blocks already"
    (let [parent-uid  "test-parent-1-uid"
          child-1-uid "test-child-1-1-uid"
          child-2-uid "test-child-1-2-uid"
          child-1-text  "1abc123"
          child-2-text  "2abc123"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children {:db/id          -2
                                         :block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children [{:db/id          -3
                                                           :block/uid      child-1-uid
                                                           :block/string   child-1-text
                                                           :block/order    0
                                                           :block/children []}
                                                          {:db/id          -4
                                                           :block/uid      child-2-uid
                                                           :block/string   child-2-text
                                                           :block/order    1
                                                           :block/children []}]}}]]
      (d/transact @fixture/connection setup-txs)
      (let [parent-block   (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-block  (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            child-2-block  (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
            uids           [child-1-uid child-2-uid]
            unindent-multi-event (common-events/build-unindent-multi-event -1
                                                                           uids)
            unindent-multi-txs   (resolver/resolve-event-to-tx @@fixture/connection unindent-multi-event)]
        (t/is (= 2 (-> parent-block :block/children count)))
        (t/is (= [(select-keys child-1-block [:block/uid :block/order])
                  (select-keys child-2-block [:block/uid :block/order])]
                 (:block/children parent-block)))

        (d/transact @fixture/connection unindent-multi-txs)
        (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
              child-2-block (common-db/get-block @@fixture/connection [:block/uid child-2-uid])]
          (t/is (= 0 (-> parent-block :block/children count)))
          (t/is (= 1 (:block/order child-1-block)))
          (t/is (= 2 (:block/order child-2-block))))))))


;; TODO More cases with nested blocks inside nested block


(t/deftest indent-test
  (t/testing "Just indent already"
    (let [parent-uid  "test-parent-1-uid"
          child-1-uid "test-child-1-1-uid"
          child-text  "abc123"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children [{:db/id          -2
                                          :block/uid      parent-uid
                                          :block/string   ""
                                          :block/order    0
                                          :block/children []}
                                         {:db/id          -3
                                          :block/uid      child-1-uid
                                          :block/string   child-text
                                          :block/order    1
                                          :block/children []}]}]]
      (d/transact @fixture/connection setup-txs)
      (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            indent-event  (common-events/build-indent-event -1
                                                            child-1-uid
                                                            child-text)
            indent-txs    (resolver/resolve-event-to-tx @@fixture/connection indent-event)]
        (t/is (= 0 (-> parent-block :block/children count)))
        (t/is (= 1 (:block/order child-1-block)))


        (d/transact @fixture/connection indent-txs)
        (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])]
          (t/is (= 1 (-> parent-block :block/children count)))
          (t/is (= [(select-keys child-1-block [:block/uid :block/order])]
                   (:block/children parent-block))))))))


(t/deftest indent-multi-test
  (t/testing "Just indent multiple blocks already"
    (let [parent-uid  "test-parent-1-uid"
          child-1-uid "test-child-1-1-uid"
          child-2-uid "test-child-1-2-uid"
          child-1-text  "1abc123"
          child-2-text  "2abc123"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children [{:db/id          -2
                                          :block/uid      parent-uid
                                          :block/string   ""
                                          :block/order    0
                                          :block/children []}
                                         {:db/id          -3
                                          :block/uid      child-1-uid
                                          :block/string   child-1-text
                                          :block/order    1
                                          :block/children []}
                                         {:db/id          -4
                                          :block/uid      child-2-uid
                                          :block/string   child-2-text
                                          :block/order    2
                                          :block/children []}]}]]
      (d/transact @fixture/connection setup-txs)
      (let [parent-block         (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-block        (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            child-2-block        (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
            uids                 [child-1-uid child-2-uid]
            indent-multi-event   (common-events/build-indent-multi-event -1
                                                                         uids)

            indent-multi-txs   (resolver/resolve-event-to-tx @@fixture/connection indent-multi-event)]
        (t/is (= 0 (-> parent-block :block/children count)))
        (t/is (= 1 (:block/order child-1-block)))
        (t/is (= 2 (:block/order child-2-block)))

        (d/transact @fixture/connection indent-multi-txs)
        (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
              child-2-block (common-db/get-block @@fixture/connection [:block/uid child-2-uid])]
          (t/is (= 2 (-> parent-block :block/children count)))
          (t/is (= [(select-keys child-1-block [:block/uid :block/order])
                    (select-keys child-2-block [:block/uid :block/order])]
                   (:block/children parent-block))))))))


(t/deftest bump-up-test
  (t/testing "Testing bump up simple case"
    (let [parent-uid   "test-parent-1-uid"
          child-1-uid  "test-child-1-uid"
          child-2-uid  "test-child-2-uid"
          child-1-text "testing 123"
          setup-txs    [{:db/id          -1
                         :node/title     "test page"
                         :block/uid      "page-uid"
                         :block/children {:db/id          -2
                                          :block/uid      parent-uid
                                          :block/string   ""
                                          :block/order    0
                                          :block/children {:db/id          -3
                                                           :block/uid      child-1-uid
                                                           :block/string   child-1-text
                                                           :block/order    0
                                                           :block/children []}}}]]
      (d/transact @fixture/connection setup-txs)
      (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
            bump-up-event (common-events/build-bump-up-event -1
                                                             child-1-uid
                                                             child-2-uid)
            bump-up-txs   (resolver/resolve-event-to-tx @@fixture/connection
                                                        bump-up-event)]
        ;; before -> parent has 1 child
        (t/is (= 1 (-> parent-block :block/children count)))
        (t/is (= child-1-text (:block/string child-1-block)))
        (d/transact @fixture/connection bump-up-txs)
        (let [parent-block  (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              kids          (:block/children parent-block)
              child-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
              child-2-block (common-db/get-block @@fixture/connection [:block/uid child-2-uid])]
          ;; after bump-up
          (t/is (= 2 (count kids)))
          (t/is (= [(select-keys child-1-block
                                 [:block/uid :block/order])
                    (select-keys child-2-block
                                 [:block/uid :block/order])]
                   kids))
          (t/is (= child-1-text (:block/string child-1-block)))
          (t/is (= 1 (:block/order child-1-block)))
          (t/is (= "" (:block/string child-2-block)))
          (t/is (= 0 (:block/order child-2-block))))))))


(t/deftest drop-child-test
  "Basic Case:
    Start with :
      -a
      -b
    End:
      -a
        -b"
  (t/testing "Drop block as first child test"
    (let [target-uid  "test-target-uid"
          target-text "a"
          source-uid  "test-source-uid"
          source-text "b"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children [{:db/id          -2
                                          :block/uid      target-uid
                                          :block/string   target-text
                                          :block/order    0
                                          :block/children []}
                                         {:db/id          -3
                                          :block/uid      source-uid
                                          :block/string   source-text
                                          :block/order    1
                                          :block/children []}]}]]


      (d/transact @fixture/connection setup-txs)
      (let [source-block (common-db/get-block @@fixture/connection [:block/uid source-uid])
            target-block (common-db/get-block @@fixture/connection [:block/uid target-uid])
            drop-child-event     (common-events/build-drop-child-event -1
                                                                       source-uid
                                                                       target-uid)
            drop-child-txs   (resolver/resolve-event-to-tx @@fixture/connection drop-child-event)]
        (t/is (= 0 (-> target-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 1 (:block/order source-block)))

        (d/transact @fixture/connection drop-child-txs)
        (let [source-block (common-db/get-block @@fixture/connection [:block/uid source-uid])
              target-block (common-db/get-block @@fixture/connection [:block/uid target-uid])]
          (t/is (= 1 (-> target-block :block/children count)))
          (t/is (= [(select-keys source-block [:block/uid :block/order])]
                   (:block/children target-block))))))))


(t/deftest drop-multi-child-test
  "Basic Case:
       -a
       -b
       -c
     End:
       -a
         -b
         -c"
  (t/testing "Drop multiple blocks as the first child test"
    (let [target-uid    "test-target-uid"
          target-text   "a"
          source-1-uid  "test-source-1-uid"
          source-1-text "b"
          source-2-uid  "test-source-2-uid"
          source-2-text "c"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children [{:db/id          -2
                                          :block/uid      target-uid
                                          :block/string   target-text
                                          :block/order    0
                                          :block/children []}
                                         {:db/id          -3
                                          :block/uid      source-1-uid
                                          :block/string   source-1-text
                                          :block/order    1
                                          :block/children []}
                                         {:db/id          -4
                                          :block/uid      source-2-uid
                                          :block/string   source-2-text
                                          :block/order    2
                                          :block/children []}]}]]

      (d/transact @fixture/connection setup-txs)
      (let [target-block             (common-db/get-block @@fixture/connection [:block/uid target-uid])
            source-1-block           (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
            source-2-block           (common-db/get-block @@fixture/connection [:block/uid source-2-uid])
            source-uids              [source-1-uid source-2-uid]
            drop-multi-child-event   (common-events/build-drop-multi-child-event -1
                                                                                 source-uids
                                                                                 target-uid)
            drop-child-txs   (resolver/resolve-event-to-tx @@fixture/connection drop-multi-child-event)]
        (t/is (= 0 (-> target-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 1 (:block/order source-1-block)))
        (t/is (= 2 (:block/order source-2-block)))


        (d/transact @fixture/connection drop-child-txs)
        (let [target-block             (common-db/get-block @@fixture/connection [:block/uid target-uid])
              source-1-block           (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
              source-2-block           (common-db/get-block @@fixture/connection [:block/uid source-2-uid])]

          (t/is (= 2 (-> target-block :block/children count)))
          (t/is (= [(select-keys source-1-block [:block/uid :block/order])
                    (select-keys source-2-block [:block/uid :block/order])]
                   (:block/children target-block))))))))


(t/deftest drop-link-child-test
  "Basic Case:
       -a
       -b
     End:
       -a
         -b
       -b"
  (t/testing "Drop block reference as the first child test"
    (let [target-uid    "test-target-uid"
          target-text   "a"
          source-1-uid  "test-source-1-uid"
          source-1-text "b"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children [{:db/id          -2
                                          :block/uid      target-uid
                                          :block/string   target-text
                                          :block/order    0
                                          :block/children []}
                                         {:db/id          -3
                                          :block/uid      source-1-uid
                                          :block/string   source-1-text
                                          :block/order    1
                                          :block/children []}]}]]
      (d/transact @fixture/connection setup-txs)
      (let [target-block            (common-db/get-block @@fixture/connection [:block/uid target-uid])
            source-1-block          (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
            drop-link-child-event   (common-events/build-drop-link-child-event -1
                                                                               source-1-uid
                                                                               target-uid)
            drop-link-child-txs   (resolver/resolve-event-to-tx @@fixture/connection drop-link-child-event)]
        (t/is (= 0 (-> target-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 1 (:block/order source-1-block)))

        (d/transact @fixture/connection drop-link-child-txs)
        (let [target-block       (common-db/get-block @@fixture/connection [:block/uid target-uid])
              source-1-ref-str   (str "((" source-1-uid "))")
              linked-child-1-uid (last (common-db/get-children-uids-recursively @@fixture/connection target-uid))
              linked-child-1-str (:block/string (common-db/get-block @@fixture/connection [:block/uid linked-child-1-uid]))]
          (t/is (= 1 (-> target-block :block/children count)))
          (t/is (= source-1-ref-str
                   linked-child-1-str)))))))


(t/deftest drop-diff-parent-test
  "Basic Case:
     Start with :
       -a
         -b
       -c
     End:
       -a
         -b
         -c"

  (t/testing "Drop block under different parent test"
    (let [target-parent-uid "target-parent-uid"
          target-parent-str "a"
          target-uid        "test-target-uid"
          target-text       "b"
          source-uid        "test-source-uid"
          source-text       "c"
          setup-txs         [{:db/id          -1
                              :node/title     "test page"
                              :block/uid      "page-uid"
                              :block/children [{:db/id          -2
                                                :block/uid      target-parent-uid
                                                :block/string   target-parent-str
                                                :block/order    0
                                                :block/children {:db/id          -3
                                                                 :block/uid      target-uid
                                                                 :block/string   target-text
                                                                 :block/order    0
                                                                 :block/children []}}
                                               {:db/id          -4
                                                :block/uid      source-uid
                                                :block/string   source-text
                                                :block/order    1
                                                :block/children []}]}]]


      (d/transact @fixture/connection setup-txs)
      (let [source-block            (common-db/get-block @@fixture/connection [:block/uid source-uid])
            target-block            (common-db/get-block @@fixture/connection [:block/uid target-uid])
            target-parent-block     (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
            drop-diff-parent-event  (common-events/build-drop-diff-parent-event -1
                                                                                :below
                                                                                source-uid
                                                                                target-uid)
            drop-diff-parent-txs    (resolver/resolve-event-to-tx @@fixture/connection drop-diff-parent-event)]
        (t/is (= 1 (-> target-parent-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 1 (:block/order source-block)))

        (d/transact @fixture/connection drop-diff-parent-txs)
        (let [source-block         (common-db/get-block @@fixture/connection [:block/uid source-uid])
              target-block         (common-db/get-block @@fixture/connection [:block/uid target-uid])
              target-parent-block  (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])]
          (t/is (= 2 (-> target-parent-block :block/children count)))
          (t/is (= [(select-keys target-block [:block/uid :block/order])
                    (select-keys source-block [:block/uid :block/order])]
                   (:block/children target-parent-block))))))))


(t/deftest drop-multi-diff-source-diff-parents-test
  "Basic Case:
     Start with :
       -a
         -b
       -c
       -d
         -e
     End:
       -a
       -d
         -e
         -b
         -c"

  (t/testing "Drop blocks under different parent test"
    (let [source-1-parent-uid  "source-1-parent-uid"
          source-1-parent-text "a"
          source-1-uid         "test-source-1-uid"
          source-1-text        "b"
          source-2-uid         "test-source-2-uid"
          source-2-text        "c"
          target-parent-uid    "target-parent-uid"
          target-parent-text    "d"
          target-uid           "test-target-uid"
          target-text          "e"
          setup-txs            [{:node/title     "test page"
                                 :block/uid      "page-uid"
                                 :block/children [{:block/uid      source-1-parent-uid
                                                   :block/string   source-1-parent-text
                                                   :block/order    0
                                                   :block/children {:block/uid      source-1-uid
                                                                    :block/string   source-1-text
                                                                    :block/order    0
                                                                    :block/children []}}
                                                  {:block/uid      source-2-uid
                                                   :block/string   source-2-text
                                                   :block/order    1
                                                   :block/children []}
                                                  {:block/uid      target-parent-uid
                                                   :block/string   target-parent-text
                                                   :block/order    2
                                                   :block/children {:block/uid      target-uid
                                                                    :block/string   target-text
                                                                    :block/order    0
                                                                    :block/children []}}]}]]


      (d/transact @fixture/connection setup-txs)
      (let [target-parent-block                      (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
            source-1-parent-block                    (common-db/get-block @@fixture/connection [:block/uid source-1-parent-uid])
            source-uids                              [source-1-uid source-2-uid]
            drop-multi-diff-source-diff-parents-event (common-events/build-drop-multi-diff-source-diff-parents-event -1
                                                                                                                     :below
                                                                                                                     source-uids
                                                                                                                     target-uid)
            drop-multi-diff-source-diff-parents-txs    (resolver/resolve-event-to-tx @@fixture/connection drop-multi-diff-source-diff-parents-event)]
        (t/is (= 1  (-> target-parent-block :block/children count)))
        (t/is (= 1  (-> source-1-parent-block :block/children count)))

        (d/transact @fixture/connection drop-multi-diff-source-diff-parents-txs)
        (let [source-1-block        (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
              source-2-block        (common-db/get-block @@fixture/connection [:block/uid source-2-uid])
              source-1-parent-block (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
              target-block          (common-db/get-block @@fixture/connection [:block/uid target-uid])
              target-parent-block   (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])]
          (t/is (= {} (:block/children target-parent-block)))
          (t/is (= 3 (-> target-parent-block   :block/children count)))
          (t/is (= 0 (-> source-1-parent-block :block/children count)))
          (t/is (= [(select-keys target-block [:block/uid :block/order])
                    (select-keys source-1-block [:block/uid :block/order])
                    (select-keys source-2-block [:block/uid :block/order])]
                   (:block/children target-parent-block))))))))



(t/deftest drop-multi-diff-source-same-parents-test
  "Basic Case:
     Start with :
       -a
         -b
       -c
       -d

     End:
       -a
       -d
       -b
       -c"

  (t/testing "Drop blocks under different parent test"
    (let [source-1-parent-uid  "source-1-parent-uid"
          source-1-parent-text "a"
          source-1-uid         "test-source-1-uid"
          source-1-text        "b"
          source-2-uid         "test-source-2-uid"
          source-2-text        "c"
          target-uid           "test-target-uid"
          target-text          "d"
          setup-txs            [{:node/title     "test page"
                                 :block/uid      "page-uid"
                                 :block/children [{:block/uid      source-1-parent-uid
                                                   :block/string   source-1-parent-text
                                                   :block/order    0
                                                   :block/children {:block/uid      source-1-uid
                                                                    :block/string   source-1-text
                                                                    :block/order    0
                                                                    :block/children []}}
                                                  {:block/uid      source-2-uid
                                                   :block/string   source-2-text
                                                   :block/order    1
                                                   :block/children []}
                                                  {:block/uid      target-uid
                                                   :block/string   target-text
                                                   :block/order    2
                                                   :block/children []}]}]]

      (d/transact @fixture/connection setup-txs)
      (let [target-parent-block                       (common-db/get-block @@fixture/connection [:block/uid "page-uid"])
            source-1-parent-block                     (common-db/get-block @@fixture/connection [:block/uid source-1-parent-uid])
            source-uids                               [source-1-uid source-2-uid]
            drop-multi-diff-source-same-parents-event (common-events/build-drop-multi-diff-source-same-parents-event -1
                                                                                                                     :below
                                                                                                                     source-uids
                                                                                                                     target-uid)
            drop-multi-diff-source-same-parents-txs    (resolver/resolve-event-to-tx @@fixture/connection drop-multi-diff-source-same-parents-event)]
        (t/is (= 3 (-> target-parent-block :block/children count)))
        (t/is (= 1 (-> source-1-parent-block :block/children count)))

        (d/transact @fixture/connection drop-multi-diff-source-same-parents-txs)
        (let [source-1-block        (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
              source-2-block        (common-db/get-block @@fixture/connection [:block/uid source-2-uid])
              source-1-parent-block (common-db/get-block @@fixture/connection [:block/uid source-1-parent-uid])
              target-block          (common-db/get-block @@fixture/connection [:block/uid target-uid])
              target-parent-block   (common-db/get-block @@fixture/connection [:block/uid "page-uid"])]
          (t/is (= 4 (-> target-parent-block   :block/children count)))
          (t/is (= 0 (-> source-1-parent-block :block/children count)))
          (t/is (= [(select-keys source-1-parent-block [:block/uid :block/order])
                    (select-keys target-block [:block/uid :block/order])
                    (select-keys source-1-block [:block/uid :block/order])
                    (select-keys source-2-block [:block/uid :block/order])]
                   (:block/children target-parent-block))))))))



(t/deftest drop-link-diff-parent-test
  "Basic Case:
     Start with :
       -a
         -b
       -c
     End:
       -a
         -b
         -c
       -c"

  (t/testing "Drop a block reference under different parent test"
    (let [target-parent-uid "target-parent-uid"
          target-parent-str "a"
          target-uid        "test-target-uid"
          target-text       "b"
          source-uid        "test-source-uid"
          source-text       "c"
          setup-txs         [{:db/id          -1
                              :node/title     "test page"
                              :block/uid      "page-uid"
                              :block/children [{:db/id          -2
                                                :block/uid      target-parent-uid
                                                :block/string   target-parent-str
                                                :block/order    0
                                                :block/children {:db/id          -3
                                                                 :block/uid      target-uid
                                                                 :block/string   target-text
                                                                 :block/order    0
                                                                 :block/children []}}
                                               {:db/id          -4
                                                :block/uid      source-uid
                                                :block/string   source-text
                                                :block/order    1
                                                :block/children []}]}]]


      (d/transact @fixture/connection setup-txs)
      (let [source-block                 (common-db/get-block @@fixture/connection [:block/uid source-uid])
            target-block                 (common-db/get-block @@fixture/connection [:block/uid target-uid])
            target-parent-block          (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
            drop-link-diff-parent-event  (common-events/build-drop-link-diff-parent-event -1
                                                                                          :below
                                                                                          source-uid
                                                                                          target-uid)
            drop-link-diff-parent-txs    (resolver/resolve-event-to-tx @@fixture/connection drop-link-diff-parent-event)]
        (t/is (= 1 (-> target-parent-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 1 (:block/order source-block)))

        (d/transact @fixture/connection drop-link-diff-parent-txs)
        ;; The idea here is to find the values of all the block's string under target parent then compare it after adding
        ;; the reference link. Comparision here is done by making a set containing the target parent's block's string and
        ;; the expected set of strings, we then find if after joining both sets the len of this set is same as the previous set.
        (let [source-ref-str       (str "((" source-uid "))")
              target-block-str     (:block/string (common-db/get-block @@fixture/connection [:block/uid target-uid]))
              expected-set         #{source-ref-str target-block-str}
              linked-ref-uid       (last (common-db/get-children-uids-recursively @@fixture/connection target-parent-uid))
              linked-ref-str       (:block/string (common-db/get-block @@fixture/connection [:block/uid linked-ref-uid]))
              childrens-str-set    #{linked-ref-str target-block-str}
              union-set            (clojure.set/union expected-set childrens-str-set)
              target-parent-block  (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])]

          (t/is (= 2 (-> target-parent-block :block/children count)))
          (t/is (= 2 (count union-set))))))))


(t/deftest drop-same-test
  "Basic Case:
     Start with :
       -a
         -b
         -c
     End:
       -a
         -c
         -b"

  (t/testing "Drop block under same parent test"
    (let [target-parent-uid "target-parent-uid"
          target-parent-str "a"
          target-uid        "test-target-uid"
          target-text       "b"
          source-uid        "test-source-uid"
          source-text       "c"
          setup-txs         [{:db/id          -1
                              :node/title     "test page"
                              :block/uid      "page-uid"
                              :block/children [{:db/id          -2
                                                :block/uid      target-parent-uid
                                                :block/string   target-parent-str
                                                :block/order    0
                                                :block/children [{:db/id          -3
                                                                  :block/uid      target-uid
                                                                  :block/string   target-text
                                                                  :block/order    0
                                                                  :block/children []}
                                                                 {:db/id          -4
                                                                  :block/uid      source-uid
                                                                  :block/string   source-text
                                                                  :block/order    1
                                                                  :block/children []}]}]}]]


      (d/transact @fixture/connection setup-txs)
      (let [source-block            (common-db/get-block @@fixture/connection [:block/uid source-uid])
            target-block            (common-db/get-block @@fixture/connection [:block/uid target-uid])
            target-parent-block     (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
            drop-same-parent-event  (common-events/build-drop-same-event -1
                                                                         :above
                                                                         source-uid
                                                                         target-uid)
            drop-same-parent-txs    (resolver/resolve-event-to-tx @@fixture/connection drop-same-parent-event)]
        (t/is (= 2 (-> target-parent-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 1 (:block/order source-block)))

        (d/transact @fixture/connection drop-same-parent-txs)
        (let [source-block         (common-db/get-block @@fixture/connection [:block/uid source-uid])
              target-block         (common-db/get-block @@fixture/connection [:block/uid target-uid])
              target-parent-block  (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])]
          (t/is (= 2 (-> target-parent-block :block/children count)))
          (t/is (= 1 (:block/order target-block)))
          (t/is (= 0 (:block/order source-block))))))))


(t/deftest drop-multi-same-all-test
  "Basic Case:
       -a
         -b
         -c
         -d
     End:
       -a
         -c
         -d
         -a"
  (t/testing "Drop multiple blocks inside the same source parent "
    (let [target-parent-uid  "test-target-parent-uid"
          target-parent-text "a"
          target-uid    "test-target-uid"
          target-text   "b"
          source-1-uid  "test-source-1-uid"
          source-1-text "c"
          source-2-uid  "test-source-2-uid"
          source-2-text "d"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children [{:db/id          -2
                                          :block/uid      target-parent-uid
                                          :block/string   target-parent-text
                                          :block/order    0
                                          :block/children [{:db/id          -3
                                                            :block/uid      target-uid
                                                            :block/string   target-text
                                                            :block/order    0
                                                            :block/children []}
                                                           {:db/id          -4
                                                            :block/uid      source-1-uid
                                                            :block/string   source-1-text
                                                            :block/order    1
                                                            :block/children []}
                                                           {:db/id          -5
                                                            :block/uid      source-2-uid
                                                            :block/string   source-2-text
                                                            :block/order    2
                                                            :block/children []}]}]}]]

      (d/transact @fixture/connection setup-txs)
      (let [target-parent-block       (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
            target-block              (common-db/get-block @@fixture/connection [:block/uid target-uid])
            source-1-block            (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
            source-2-block            (common-db/get-block @@fixture/connection [:block/uid source-2-uid])
            source-uids               [source-1-uid source-2-uid]
            drop-multi-same-all-event (common-events/build-drop-multi-same-all-event -1
                                                                                     :above
                                                                                     source-uids
                                                                                     target-uid)
            drop-same-all-txs   (resolver/resolve-event-to-tx @@fixture/connection drop-multi-same-all-event)]
        (t/is (= 3 (-> target-parent-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 1 (:block/order source-1-block)))
        (t/is (= 2 (:block/order source-2-block)))


        (d/transact @fixture/connection drop-same-all-txs)
        (let [target-parent-block       (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
              target-block             (common-db/get-block @@fixture/connection [:block/uid target-uid])
              source-1-block           (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
              source-2-block           (common-db/get-block @@fixture/connection [:block/uid source-2-uid])]

          (t/is (= 3 (-> target-parent-block :block/children count)))
          (t/is (= 2 (:block/order target-block)))
          (t/is (= 0 (:block/order source-1-block)))
          (t/is (= 1 (:block/order source-2-block))))))))


(t/deftest drop-multi-same-source-test
  "Basic Case:
       -a
         -b
       -c
         -d
         -e
     End:
       -a
         -b
         -d
         -e
       -c"
  (t/testing "Drop multiple blocks selected from under same parent inside differnt block "
    (let [target-parent-uid  "test-target-parent-uid"
          target-parent-text "a"
          target-uid         "test-target-uid"
          target-text        "b"
          source-parent-uid  "test-source-parent-uid"
          source-parent-text "c"
          source-1-uid       "test-source-1-uid"
          source-1-text      "d"
          source-2-uid       "test-source-2-uid"
          source-2-text      "e"
          setup-txs         [{:db/id          -1
                              :node/title     "test page"
                              :block/uid      "page-uid"
                              :block/children [{:db/id          -2
                                                :block/uid      target-parent-uid
                                                :block/string   target-parent-text
                                                :block/order    0
                                                :block/children {:db/id          -3
                                                                 :block/uid      target-uid
                                                                 :block/string   target-text
                                                                 :block/order    0
                                                                 :block/children []}}
                                               {:db/id          -4
                                                :block/uid      source-parent-uid
                                                :block/string   source-parent-text
                                                :block/order    1
                                                :block/children [{:db/id          -5
                                                                  :block/uid      source-1-uid
                                                                  :block/string   source-1-text
                                                                  :block/order    0
                                                                  :block/children []}
                                                                 {:db/id          -6
                                                                  :block/uid      source-2-uid
                                                                  :block/string   source-2-text
                                                                  :block/order    1
                                                                  :block/children []}]}]}]]

      (d/transact @fixture/connection setup-txs)
      (let [target-parent-block       (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
            target-block              (common-db/get-block @@fixture/connection [:block/uid target-uid])
            source-parent-block       (common-db/get-block @@fixture/connection [:block/uid source-parent-uid])
            source-1-block            (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
            source-2-block            (common-db/get-block @@fixture/connection [:block/uid source-2-uid])
            source-uids               [source-1-uid source-2-uid]
            drop-multi-same-source-event (common-events/build-drop-multi-same-source-event -1
                                                                                           :below
                                                                                           source-uids
                                                                                           target-uid)
            drop-same-source-txs   (resolver/resolve-event-to-tx @@fixture/connection drop-multi-same-source-event)]
        (t/is (= 1 (-> target-parent-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 2 (-> source-parent-block :block/children count)))
        (t/is (= 0 (:block/order source-1-block)))
        (t/is (= 1 (:block/order source-2-block)))


        (d/transact @fixture/connection drop-same-source-txs)
        (let [target-parent-block       (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
              target-block              (common-db/get-block @@fixture/connection [:block/uid target-uid])
              source-parent-block       (common-db/get-block @@fixture/connection [:block/uid source-parent-uid])
              source-1-block            (common-db/get-block @@fixture/connection [:block/uid source-1-uid])
              source-2-block            (common-db/get-block @@fixture/connection [:block/uid source-2-uid])]

          (t/is (= 3 (-> target-parent-block :block/children count)))
          (t/is (= 0 (-> source-parent-block :block/children count)))
          (t/is (= 0 (:block/order target-block)))
          (t/is (= 1 (:block/order source-1-block)))
          (t/is (= 2 (:block/order source-2-block))))))))


(t/deftest drop-link-same-test
  "Basic Case:
     Start with :
       -a
         -b
         -c
     End:
       -a
         -b
         -c
         -c
       "

  (t/testing "Drop a block reference under different parent test"
    (let [target-parent-uid "target-parent-uid"
          target-parent-str "a"
          target-uid        "test-target-uid"
          target-text       "b"
          source-uid        "test-source-uid"
          source-text       "c"
          setup-txs         [{:db/id          -1
                              :node/title     "test page"
                              :block/uid      "page-uid"
                              :block/children [{:db/id          -2
                                                :block/uid      target-parent-uid
                                                :block/string   target-parent-str
                                                :block/order    0
                                                :block/children [{:db/id          -3
                                                                  :block/uid      target-uid
                                                                  :block/string   target-text
                                                                  :block/order    0
                                                                  :block/children []}
                                                                 {:db/id          -4
                                                                  :block/uid      source-uid
                                                                  :block/string   source-text
                                                                  :block/order    1
                                                                  :block/children []}]}]}]]


      (d/transact @fixture/connection setup-txs)
      (let [source-block          (common-db/get-block @@fixture/connection [:block/uid source-uid])
            target-block          (common-db/get-block @@fixture/connection [:block/uid target-uid])
            target-parent-block   (common-db/get-block @@fixture/connection [:block/uid target-parent-uid])
            drop-link-same-parent-event  (common-events/build-drop-link-same-parent-event -1
                                                                                          :below
                                                                                          source-uid
                                                                                          target-uid)
            drop-link-same-parent-txs    (resolver/resolve-event-to-tx @@fixture/connection drop-link-same-parent-event)]
        (t/is (= 2 (-> target-parent-block :block/children count)))
        (t/is (= 0 (:block/order target-block)))
        (t/is (= 1 (:block/order source-block)))


        (d/transact @fixture/connection drop-link-same-parent-txs)
        ;; The idea here is to find the values of all the block's string under target parent then compare it after adding
        ;; the reference link. Comparision here is done by making a set containing the target parent's block's string and
        ;; the expected set of strings, we then find if after joining both sets the len of this set is same as the previous set.
        (let [source-ref-str    (str "((" source-uid "))")
              target-block-str  (:block/string (common-db/get-block @@fixture/connection [:block/uid target-uid]))
              expected-set      #{source-ref-str target-block-str}
              linked-ref-uid    (last (common-db/get-children-uids-recursively @@fixture/connection target-parent-uid))
              linked-ref-str    (:block/string (common-db/get-block @@fixture/connection [:block/uid linked-ref-uid]))
              childrens-str-set #{linked-ref-str target-block-str}
              union-set         (clojure.set/union expected-set childrens-str-set)]

          (t/is (= 2 (-> target-parent-block :block/children count)))
          (t/is (= 2 (count union-set))))))))


(t/deftest selected-delete-test
  "Basic Case:
     Start with :
       -a
         -b
       -c
       -d
     End:
       -a
       -d"

  (t/testing "Delete some blocks"
    (let [block-1-uid    "block-1-uid"
          block-1-text   "a"
          block-2-uid    "block-2-uid"
          block-2-text   "b"
          block-3-uid    "block-3-uid"
          block-3-text   "c"
          block-4-uid    "block-4-uid"
          block-4-text   "d"
          setup-txs         [{:node/title     "test page"
                              :block/uid      "page-uid"
                              :block/children [{:block/uid      block-1-uid
                                                :block/string   block-1-text
                                                :block/order    0
                                                :block/children {:block/uid      block-2-uid
                                                                 :block/string   block-2-text
                                                                 :block/order    0
                                                                 :block/children []}}
                                               {:block/uid      block-3-uid
                                                :block/string   block-3-text
                                                :block/order    1
                                                :block/children []}
                                               {:block/uid      block-4-uid
                                                :block/string   block-4-text
                                                :block/order    2
                                                :block/children []}]}]]


      (d/transact @fixture/connection setup-txs)
      (let [uids                   [block-2-uid block-3-uid]
            parent-block           (common-db/get-block @@fixture/connection [:block/uid "page-uid"])
            block-1                (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
            block-4                (common-db/get-block @@fixture/connection [:block/uid block-4-uid])
            selected-delete-event  (common-events/build-selected-delete-event -1
                                                                              uids)
            selected-delete-txs    (resolver/resolve-event-to-tx @@fixture/connection selected-delete-event)]
        (t/is (= 3 (-> parent-block :block/children count)))
        (t/is (= 0 (:block/order block-1)))
        (t/is (= 2 (:block/order block-4)))

        (d/transact @fixture/connection selected-delete-txs)
        (let [parent-block           (common-db/get-block @@fixture/connection [:block/uid "page-uid"])
              block-1                (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
              block-4                (common-db/get-block @@fixture/connection [:block/uid block-4-uid])]
          (t/is (= 2 (-> parent-block :block/children count)))
          (t/is (= 0 (:block/order block-1)))
          (t/is (= 1 (:block/order block-4))))))))
