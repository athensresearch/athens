(ns athens.common-events.linkmaker-test
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as t]
    [datahike.api                  :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest string->lookup-refs
  (t/testing "Doesn't find refs if none are there"
    (t/are [x] (empty? (common-db/string->lookup-refs x))
      ""
      "one"
      "[one]"
      "[ [one]]"
      "[[one"
      "one]]"))
  (t/testing "Finds page refs"
    (t/are [x y] (= (common-db/string->lookup-refs x) y)
      "[[one]]"            #{[:node/title "one"]}
      "[[one]] two"        #{[:node/title "one"]}
      "one [[two three]]"  #{[:node/title "two three"]}
      "#one"               #{[:node/title "one"]}
      "#[[one]]"           #{[:node/title "one"]}
      "one #[[two three]]" #{[:node/title "two three"]}
      "[[one]] #two [[three four]] #[[five six]]"
      #{[:node/title "one"] [:node/title "two"] [:node/title "three four"] [:node/title "five six"]}))
  (t/testing "Finds block refs"
    (t/are [x y] (= (common-db/string->lookup-refs x) y)
      "((one))"            #{[:block/uid "one"]}
      "one ((two))"        #{[:block/uid "two"]}
      "((one)) two"        #{[:block/uid "one"]}
      "((one)) ((two))"    #{[:block/uid "one"] [:block/uid "two"]}))
  (t/testing "Finds mixed page and block refs"
    (t/is (= (common-db/string->lookup-refs "((one)) [[two]] ((three)) #[[four]]")
             #{[:block/uid "one"] [:node/title "two"] [:block/uid "three"] [:node/title "four"]})))
  ;; broken, need improved parser
  (t/testing "Finds nested refs inside page refs"
    (t/are [x y] (= (common-db/string->lookup-refs x) y)
      "[[one [[two]]]]"     #{[:node/title "one [[two]]"] [:node/title "two"]}
      ;; broken on the parser
      #_#_"#[[one #two three]]" #{[:node/title "one #two #three"] [:node/title "two"] [:node/title "three"]}
      #_#_"one [[#two #[[three four]]]]" #{[:node/title "#two #three"] [:node/title "two"] [:node/title "three four"]}
      "[[truly [[madly [[deeply [[nested]]]]]]]]"
      #{[:node/title "truly [[madly [[deeply [[nested]]]]]]"]
        [:node/title "madly [[deeply [[nested]]]]"]
        [:node/title "deeply [[nested]]"]
        [:node/title "nested"]})))


(t/deftest eid->lookup-ref
  (let [page      [{:db/id 101 :block/uid "page" :node/title "the page"}]
        block     [{:db/id 102 :block/uid "block" :block/string "the block"}]
        pageblock [{:db/id 103 :block/uid "pageblock" :node/title "the pageblock" :block/string "the pageblock"}]
        neither   [{:db/id 104 :create/time 1}]
        _         (d/transact @fixture/connection (concat page block pageblock neither))
        db        @@fixture/connection]
    (t/testing "Returns nil if the entity doesn't exist"
      (t/is (nil? (common-db/eid->lookup-ref db 200))))
    (t/testing "Returns nil if the entity doesn't have :node/title or :block/uid"
      (t/is (nil? (common-db/eid->lookup-ref db 104))))
    (t/testing "Returns :block/uid lookup ref for pages"
      (t/is (= [:block/uid "page"] (common-db/eid->lookup-ref db 101))))
    (t/testing "Returns :block/uid lookup ref for blocks"
      (t/is (= [:block/uid "block"] (common-db/eid->lookup-ref db 102))))
    (t/testing "Returns :block/uid lookup ref even if both :node/title and :block/uid are present"
      (t/is (= [:block/uid "pageblock"] (common-db/eid->lookup-ref db 103))))))


(t/deftest update-refs-tx
  (let [ref       [:block/uid "uid"]
        block-foo [:block/uid "foo"]
        page-foo  [:page/title "foo"]
        block-bar [:block/uid "bar"]
        page-bar  [:page/title "bar"]
        add-ref   (fn [x] [:db/add ref :block/refs x])
        rm-ref    (fn [x] [:db/retract ref :block/refs x])]
    (t/testing "Returns empty tx if the sets are empty"
      (t/is (empty? (common-db/update-refs-tx ref #{} #{}))))
    (t/testing "Returns empty tx if the nothing changes between the sets"
      (t/is (empty? (common-db/update-refs-tx ref #{block-foo page-bar} #{block-foo page-bar}))))
    (t/testing "Adds refs"
      (t/is (= #{(add-ref block-foo) (add-ref block-bar)}
               (common-db/update-refs-tx ref #{page-foo page-bar} #{page-foo page-bar block-foo block-bar}))))
    (t/testing "Removes refs"
      (t/is (= #{(rm-ref block-foo) (rm-ref page-foo)}
               (common-db/update-refs-tx ref #{page-bar page-foo block-foo} #{page-bar}))))
    (t/testing "Adds and removes refs in the same tx"
      (t/is (= #{(rm-ref block-foo) (rm-ref page-foo) (add-ref page-bar)}
               (common-db/update-refs-tx ref #{block-bar page-foo block-foo} #{block-bar page-bar}))))))


(t/deftest block-refs-as-lookup-refs
  (let [page      [{:db/id 101 :block/uid "page" :node/title "the page"}]
        block     [{:db/id 102 :block/uid "block" :block/string "the block"}]
        refblock  [{:db/id 103 :block/uid "refblock" :block/string "the refblock"
                    :block/refs [{:db/id 101} {:db/id 102} {:db/id 103}]}]
        _         (d/transact @fixture/connection (concat page block refblock))
        db        @@fixture/connection]
    (t/testing "Returns empty if the entity doesn't have refs"
      (t/is (empty? (common-db/block-refs-as-lookup-refs db 101))))
    (t/testing "Returns ref lookups for each ref"
      (t/is (= #{[:block/uid "page"] [:block/uid "block"] [:block/uid "refblock"]} (common-db/block-refs-as-lookup-refs db 103))))))


;; See doc/adr/004-lan-party-linkmaker.md for requirements that led to these tests.
;; Redundant scenarios are not tested.

(defn transact-with-linkmaker [tx-data]
  (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection tx-data)))

(defn get-block [uid]
  (common-db/get-block @@fixture/connection [:block/uid uid]))

(defn get-page [uid]
  (common-db/get-page-document @@fixture/connection [:block/uid uid]))

(t/deftest p1-page-create
  (t/testing "New page, with refs on page title"
    (let [target-page-uid   "target-page-1-1-uid"
          target-page-title "Target Page Title 1 1"
          target-block-uid  "target-block-1-1-uid"
          setup-tx          [{:node/title     target-page-title
                              :block/uid      target-page-uid
                              :block/children [{:block/uid    target-block-uid
                                                :block/string ""
                                                :block/order  0}]}]]
      (transact-with-linkmaker setup-tx)
      (let [test-page-title (str "test page [[" target-page-title "]] and ((" target-block-uid "))")
            test-page-uid   "test-page-uid"
            add-link-tx     [{:block/uid    test-page-uid
                              :block/string test-page-title}]]

        (transact-with-linkmaker add-link-tx)
        (let [{testing-block-eid :db/id
               block-refs        :block/refs} (get-page test-page-uid)
              {target-block-eid :db/id
               block-backrefs   :block/_refs} (get-block target-block-uid)
              {target-page-eid :db/id
               page-backrefs   :block/_refs}  (get-page target-page-uid)]
          ;; Assert that we do have new refs.
          (t/is (seq page-backrefs))
          (t/is (seq block-backrefs))
          (t/is (seq block-refs))
          (t/is (= [{:db/id testing-block-eid}] page-backrefs))
          (t/is (= [{:db/id testing-block-eid}] block-backrefs))
          (t/is (= #{{:db/id target-page-eid} {:db/id target-block-eid}} (set block-refs))))))))


(t/deftest p2-page-delete
  (t/testing "Page delete, with refs to page"
    (let [target-page-title "Target page"
          target-page-uid   "target-page-uid"
          test-block-uid    "test-block-uid"
          test-page-uid     "test-page-uid"
          ref-str           (str "ref to [[" target-page-title "]]")
          setup-tx          [{:node/title target-page-title
                              :block/uid  target-page-uid}
                             {:block/uid      test-page-uid
                              :node/title     ref-str
                              :block/children [{:block/uid    test-block-uid
                                                :block/string ref-str
                                                :block/order  0}]}]]

      (transact-with-linkmaker setup-tx)
      (let [delete-page-event (common-events/build-page-delete-event -1 target-page-uid)
            delete-page-txs   (resolver/resolve-event-to-tx @@fixture/connection delete-page-event)]

        (transact-with-linkmaker delete-page-txs)
        (let [{block-refs :block/refs} (get-block test-block-uid)
              {page-refs :block/refs}  (get-page test-page-uid)]
          ;; Assert that we don't have any refs.
          ;; Linkmaker doesn't actually do anything in this case, but the datalog database should remove them.
          (t/is (empty? page-refs))
          (t/is (empty? block-refs)))))))


(t/deftest p3-page-rename
  (t/testing "Page rename, with refs to page"
    (let [target-page-title "Target page"
          target-page-uid   "target-page-uid"
          test-block-uid    "test-block-uid"
          test-page-uid     "test-page-uid"
          ref-str           (str "ref to [[" target-page-title "]]")
          setup-tx          [{:node/title target-page-title
                              :block/uid  target-page-uid}
                             {:block/uid      test-page-uid
                              :node/title     ref-str
                              :block/children [{:block/uid    test-block-uid
                                                :block/string ref-str
                                                :block/order  0}]}]]

      (transact-with-linkmaker setup-tx)
      (let [{original-block-refs :block/refs} (get-block test-block-uid)
            {original-page-refs :block/refs}  (get-page test-page-uid)
            target-page-new-title             "Target page new"
            rename-page-event                 (common-events/build-page-rename-event -1 target-page-uid target-page-title target-page-new-title)
            rename-page-txs                   (resolver/resolve-event-to-tx @@fixture/connection rename-page-event)]

        ;; Page should have refs to it.
        (t/is (seq original-block-refs))
        (t/is (seq original-page-refs))

        (transact-with-linkmaker rename-page-txs)
        (let [{block-refs :block/refs} (get-block test-block-uid)
              {page-refs :block/refs}  (get-page test-page-uid)]
          ;; Refs should be the same as before the rename.
          (t/is (= original-block-refs block-refs))
          (t/is (= original-page-refs page-refs))))))

  (t/testing "Page rename, with refs on page title"
    (let [test-block-uid    "test-block-uid"
          test-page-uid     "test-page-uid"
          test-page-title   "Test page"
          target-page-uid   "target-page-uid"
          target-page-title (str "ref to [[" test-page-title "]]")
          setup-tx          [{:block/uid      test-page-uid
                              :node/title     test-page-title
                              :block/children [{:block/uid    test-block-uid
                                                :block/string "test block"
                                                :block/order  0}]}
                             {:node/title target-page-title
                              :block/uid  target-page-uid}]]

      (transact-with-linkmaker setup-tx)
      (let [{block-backrefs :block/_refs} (get-block test-block-uid)
            {page-backrefs :block/_refs}  (get-page test-page-uid)
            target-page-new-title             (str "ref to ((" test-block-uid "))")
            rename-page-event                 (common-events/build-page-rename-event -1 target-page-uid target-page-title target-page-new-title)
            rename-page-txs                   (resolver/resolve-event-to-tx @@fixture/connection rename-page-event)]

        ;; Page should have ref to block, but not to page.
        (t/is (not (seq block-backrefs)))
        (t/is (seq page-backrefs))

        (transact-with-linkmaker rename-page-txs)
        (let [{block-backrefs :block/_refs} (get-block test-block-uid)
              {page-backrefs :block/_refs}  (get-page test-page-uid)]
          ;; Page should have ref to page, but not to block.
          (t/is (seq block-backrefs))
          (t/is (not (seq page-backrefs))))))))


(t/deftest b2-block-edit
  (t/testing "Block edit, with refs on block string"
    (let [target-page-uid   "target-page-1-1-uid"
          target-page-title "Target Page Title 1 1"
          target-block-uid  "target-block-1-1-uid"
          source-page-uid   "source-page-1-1-uid"
          source-page-title "Source Page Title 1 1"
          testing-block-uid "testing-block-1-1-uid"
          setup-tx          [{:node/title     target-page-title
                              :block/uid      target-page-uid
                              :block/children [{:block/uid    target-block-uid
                                                :block/string ""
                                                :block/order  0}]}
                             {:node/title     source-page-title
                              :block/uid      source-page-uid
                              :block/children [{:block/uid    testing-block-uid
                                                :block/string ""
                                                :block/order  0}]}]]
      (transact-with-linkmaker setup-tx)
      ;; Assert that target page and block has no `:block/refs` to start with.
      (let [target-page  (get-page target-page-uid)
            target-block (get-block target-block-uid)
            add-link-tx  [{:db/id        [:block/uid testing-block-uid]
                           :block/string (str "[[" target-page-title "]] and ((" target-block-uid "))")}]]
        (t/is (empty? (:block/_refs target-page)))
        (t/is (empty? (:block/_refs target-block)))

        (transact-with-linkmaker add-link-tx)
        (let [{testing-block-eid :db/id
               block-refs        :block/refs} (get-block testing-block-uid)
              {target-block-eid :db/id
               block-backrefs   :block/_refs} (get-block target-block-uid)
              {target-page-eid :db/id
               page-backrefs   :block/_refs}  (get-page target-page-uid)]
          ;; Assert that we do have new refs.
          (t/is (seq page-backrefs))
          (t/is (seq block-backrefs))
          (t/is (seq block-refs))
          (t/is (= [{:db/id testing-block-eid}] page-backrefs))
          (t/is (= [{:db/id testing-block-eid}] block-backrefs))
          (t/is (= #{{:db/id target-page-eid} {:db/id target-block-eid}} (set block-refs))))))

    (t/testing "Block split, 1st Page link stays in 1st block, and 2nd Page link goes to a new block"
      (let [target-page-1-uid   "target-page-3-1-uid"
            target-page-1-title "Target Page Title 3 1"
            target-page-2-uid   "target-page-3-2-uid"
            target-page-2-title "Target Page Title 3 2"

            source-page-uid        "source-page-3-1-uid"
            source-page-title      "Source Page Title 3 1"
            testing-block-1-uid    "testing-block-3-1-uid"
            testing-block-1-string (str "[[" target-page-1-title "]]"
                                        "[[" target-page-2-title "]]")
            split-index            (count (str "[[" target-page-1-title "]]"))
            testing-block-2-uid    "testing-block-3-2-uid"
            setup-tx               [{:node/title     target-page-1-title
                                     :block/uid      target-page-1-uid
                                     :block/children [{:block/uid    "irrelevant-1"
                                                       :block/string ""
                                                       :block/order  0}]}
                                    {:node/title     target-page-2-title
                                     :block/uid      target-page-2-uid
                                     :block/children [{:block/uid    "irrelevant-2"
                                                       :block/string ""
                                                       :block/order  0}]}
                                    {:node/title     source-page-title
                                     :block/uid      source-page-uid
                                     :block/children [{:block/uid    testing-block-1-uid
                                                       :block/string testing-block-1-string
                                                       :block/order  0}]}]]

        (transact-with-linkmaker setup-tx)

        (let [{testing-block-1-eid :db/id}      (get-block testing-block-1-uid)
              {target-page-1-refs :block/_refs} (get-page target-page-1-uid)
              {target-page-2-refs :block/_refs} (get-page target-page-2-uid)
              split-block-event                 (common-events/build-split-block-event -1
                                                                                       testing-block-1-uid
                                                                                       testing-block-1-string
                                                                                       split-index
                                                                                       testing-block-2-uid)
              split-block-tx                    (resolver/resolve-event-to-tx @@fixture/connection split-block-event)]
          ;; assert that target pages has no `:block/refs` to start with
          (t/is (= [{:db/id testing-block-1-eid}] target-page-1-refs))
          (t/is (= [{:db/id testing-block-1-eid}] target-page-2-refs))

          ;; apply split-block
          (transact-with-linkmaker split-block-tx)
          (let [{testing-block-2-eid :db/id}      (get-block testing-block-2-uid)
                {target-page-1-refs :block/_refs} (get-page target-page-1-uid)
                {target-page-2-refs :block/_refs} (get-page target-page-2-uid)]
            ;; assert that we do have new ref
            (t/is (= [{:db/id testing-block-1-eid}] target-page-1-refs))
            (t/is (= [{:db/id testing-block-2-eid}] target-page-2-refs))))))))


(t/deftest b3-block-delete
  (t/testing "Block deleted, with refs to block"
    ;; The block delete event is not yet migrated to common events.
    #_(let [target-block-string "Target block"
          target-block-uid   "target-block-uid"
          test-block-uid    "test-block-uid"
          test-page-uid     "test-page-uid"
         ref-str           (str "ref to ((" target-block-uid "))")
         setup-tx          [{:block/uid  target-block-uid
                             :block/string target-block-string}
                             {:block/uid      test-page-uid
                              :node/title     ref-str
                              :block/children [{:block/uid    test-block-uid
                                                :block/string ref-str
                                                :block/order  0}]}]]

      (transact-with-linkmaker setup-tx)
      (let [delete-page-event (common-events/build-block-delete-event -1 target-block-uid)
            delete-page-txs   (resolver/resolve-event-to-tx @@fixture/connection delete-page-event)]

        (transact-with-linkmaker delete-page-txs)
        (let [{block-refs :block/refs} (get-block test-block-uid)
              {page-refs :block/refs}  (get-page test-page-uid)]
          ;; Assert that we don't have any refs.
          ;; Linkmaker doesn't actually do anything in this case, but the datalog database should remove them.
          (t/is (empty? page-refs))
          (t/is (empty? block-refs)))))))


(t/deftest m3-unresolved-refs
  (t/testing "Block and page edit, with unresolved refs"
    (let [test-page-uid "test-page-uid"
          test-block-uid  "test-block-uid"
          setup-tx          [{:node/title     "refs to [[missing page]] and ((missing-block))"
                              :block/uid      test-page-uid
                              :block/children [{:block/uid    test-block-uid
                                                :block/string "refs to [[another missing page]] and ((another missing block))"
                                                :block/order  0}]}]]
      (transact-with-linkmaker setup-tx)
      ;; Assert that target page and block has no `:block/refs`.
      (let [test-page  (get-page test-page-uid)
            test-block (get-block test-block-uid)]
        (t/is (empty? (:block/_refs test-page)))
        (t/is (empty? (:block/_refs test-block)))))))

(t/deftest m4-fix-db
  (t/testing "Broken/missing refs in db"
    (let [target-page-uid      "target-page-1-1-uid"
          target-page-title    "Target Page Title 1 1"
          target-block-uid     "target-block-1-1-uid"
          source-page-uid      "source-page-1-1-uid"
          source-page-title    "Source Page Title 1 1"
          testing-block-uid    "testing-block-1-1-uid"
          testing-block-string (str "[[" target-page-title "]] and ((" target-block-uid "))")
          setup-tx             [{:node/title     target-page-title
                                 :block/uid      target-page-uid
                                 :block/children [{:block/uid    target-block-uid
                                                   :block/string ""
                                                   :block/order  0}]}
                                {:node/title     source-page-title
                                 :block/uid      source-page-uid
                                 :block/children [{:block/uid    testing-block-uid
                                                   :block/string testing-block-string
                                                   :block/order  0}]}]]
      ;; Transact without linkmaker.
      (d/transact @fixture/connection setup-tx)
      ;; Assert that target page and block has no `:block/refs` to start with.
      (let [target-page  (get-page target-page-uid)
            target-block (get-block target-block-uid)
            add-links-tx  (common-db/linkmaker @@fixture/connection)]
        (t/is (empty? (:block/_refs target-page)))
        (t/is (empty? (:block/_refs target-block)))

        (d/transact @fixture/connection add-links-tx)
        (let [{testing-block-eid :db/id
               block-refs        :block/refs} (get-block testing-block-uid)
              {target-block-eid :db/id
               block-backrefs   :block/_refs} (get-block target-block-uid)
              {target-page-eid :db/id
               page-backrefs   :block/_refs}  (get-page target-page-uid)]
          ;; Assert that we do have new refs.
          (t/is (seq page-backrefs))
          (t/is (seq block-backrefs))
          (t/is (seq block-refs))
          (t/is (= [{:db/id testing-block-eid}] page-backrefs))
          (t/is (= [{:db/id testing-block-eid}] block-backrefs))
          (t/is (= #{{:db/id target-page-eid} {:db/id target-block-eid}} (set block-refs))))))))


(comment
  (string->lookup-refs)
  (t/test-vars [#'athens.common-events.linkmaker-test/eid->lookup-ref])
  (update-refs-tx)
  (t/test-vars [#'athens.common-events.linkmaker-test/block-refs-as-lookup-refs])
  (t/test-vars [#'athens.common-events.linkmaker-test/p1-page-create])
  (t/test-vars [#'athens.common-events.linkmaker-test/p2-page-delete])
  (t/test-vars [#'athens.common-events.linkmaker-test/p3-page-rename])
  (t/test-vars [#'athens.common-events.linkmaker-test/b2-block-edit])
  (t/test-vars [#'athens.common-events.linkmaker-test/b3-block-delete])
  (t/test-vars [#'athens.common-events.linkmaker-test/m3-unresolved-refs])
  (t/test-vars [#'athens.common-events.linkmaker-test/m4-fix-db]))
