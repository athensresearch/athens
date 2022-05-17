(ns athens.copy-paste-test
  (:require
    [athens.common-db :as common-db]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.fixture :as fixture]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.views.blocks.internal-representation :as internal-representation]
    [cljs.test :as t]
    [clojure.test :refer [deftest is]]
    [datascript.core :as d]))


(deftest test-update-strings-with-new-uids
  (is (= "This is a test string with ((uid1)) and ((more_uids1)) and ((some_more_uids1)) ((rast1)))) ((rsrsta1)))) ((arst))a)) ((rst1))rst)) invalid uids (()) (( a)) ((a )) (( a )) ((a b)) s((a1))"
         (internal-representation/update-strings-with-new-uids "This is a test string with ((uid)) and ((more_uids)) and ((some_more_uids)) ((((rast)))) ((rs((rsta)))) ((a((rst))a)) ((((rst))rst)) invalid uids (()) (( a)) ((a )) (( a )) ((a b)) s((a))"
                                                               {"uid"            "uid1"
                                                                "more_uids"      "more_uids1"
                                                                "some_more_uids" "some_more_uids1"
                                                                "((rast"         "rast1"
                                                                "rs((rsta"       "rsrsta1"
                                                                "a((rst"         "arst"
                                                                "((rst"          "rst1"
                                                                "a"              "a1"}))))


(def blocks-for-testing
  [{:block/uid    "df27e0c38",
    :block/string "((df27e0c38))"
    :block/open   true,
    :block/order  0}
   {:block/uid    "b6c3d65a7",
    :block/string "((b6c3d65a7))",,
    :block/open   true,
    :block/order  1}])


(def replace-uids-for-testing
  {"df27e0c38" "111"
   "b6c3d65a7" "222"})


(deftest test-walk-tree-and-update-string-with-new-uids
  (is (= [#:block{:uid "df27e0c38", :string "((111))", :open true, :order 0}
          #:block{:uid "b6c3d65a7", :string "((222))", :open true, :order 1}]
         (internal-representation/walk-tree-to-replace blocks-for-testing
                                                       replace-uids-for-testing
                                                       :block/string))))


(deftest test-walk-tree-and-update-uids-with-new-uids
  (is (= [#:block{:uid "111", :string "((df27e0c38))", :open true, :order 0}
          #:block{:uid "222", :string "((b6c3d65a7))", :open true, :order 1}]
         (internal-representation/walk-tree-to-replace blocks-for-testing
                                                       replace-uids-for-testing
                                                       :block/uid))))


(deftest test-update-all-uids
  (is (= [#:block{:uid "111", :string "((111))", :open true, :order 0}
          #:block{:uid "222", :string "((222))", :open true, :order 1}]
         (internal-representation/update-uids blocks-for-testing
                                              replace-uids-for-testing))))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(deftest paste-internal-event
  (let [block-1-uid         "test-block-1-uid"
        block-2-uid         "test-block-2-uid"
        block-1-str         "block 1 string"
        paste-uid           "test-block-uid"
        paste-string        "Paste me"
        page-title          "test page"
        setup-repr          [{:page/title     page-title
                              :block/children [#:block {:uid    block-1-uid
                                                        :string block-1-str}
                                               #:block {:uid    block-2-uid
                                                        :string ""}]}]
        exp-repr            [{:page/title     page-title
                              :block/children [#:block {:uid    block-1-uid
                                                        :string block-1-str}
                                               #:block {:uid    paste-uid
                                                        :string paste-string}
                                               #:block {:uid    block-2-uid
                                                        :string ""}]}]
        paste-internal-repr [{:block/uid    paste-uid,
                              :block/string paste-string,
                              :block/open   true,
                              :block/order  5}]
        run!                #(->> (bfs/build-paste-op @@fixture/connection block-1-uid block-1-str paste-internal-repr)
                                  fixture/op-resolve-transact!)]
    (fixture/setup! setup-repr)
    (is (= setup-repr
           [(fixture/get-repr [:node/title page-title])]))
    (run!)
    (is (= exp-repr
           [(fixture/get-repr [:node/title page-title])]))))


(deftest paste-with-open-block-children-test
  (let [page-title          "test page"
        block-1-uid         "test-block-1-uid"
        block-1-str         "block 1 string"
        block-2-uid         "test-block-2-uid"
        paste-uid           "paste-uid"
        paste-string        "Paste me"
        setup-repr          [{:page/title     page-title
                              :block/children [#:block {:uid      block-1-uid
                                                        :string   block-1-str
                                                        :children [#:block {:uid    block-2-uid
                                                                            :string ""}]}]}]
        exp-repr            [{:page/title     page-title
                              :block/children [#:block {:uid      block-1-uid
                                                        :string   block-1-str
                                                        :children [#:block {:uid    paste-uid
                                                                            :string paste-string}
                                                                   #:block {:uid    block-2-uid
                                                                            :string ""}]}]}]
        paste-internal-repr [{:block/uid    paste-uid,
                              :block/string paste-string,
                              :block/open   true,
                              :block/order  5}]
        run!                #(->> (bfs/build-paste-op @@fixture/connection block-1-uid block-1-str paste-internal-repr)
                                  fixture/op-resolve-transact!)]
    (fixture/setup! setup-repr)
    (is (= setup-repr
           [(fixture/get-repr [:node/title page-title])]))
    (run!)
    (is (= exp-repr
           [(fixture/get-repr [:node/title page-title])]))))


(deftest paste-with-temporary-string-event
  (let [block-1-uid         "test-block-1-uid"
        block-2-uid         "test-block-2-uid"
        block-1-str-start   "block 1 string"
        block-1-str-end     (str block-1-str-start " and more text")
        paste-uid           "test-block-uid"
        paste-string        "Paste me"
        page-title          "test page"
        setup-repr          [{:page/title     page-title
                              :block/children [#:block {:uid    block-1-uid
                                                        :string block-1-str-start}
                                               #:block {:uid    block-2-uid
                                                        :string ""}]}]
        exp-repr            [{:page/title     page-title
                              :block/children [#:block {:uid    block-1-uid
                                                        :string block-1-str-end}
                                               #:block {:uid    paste-uid
                                                        :string paste-string}
                                               #:block {:uid    block-2-uid
                                                        :string ""}]}]
        paste-internal-repr [{:block/uid    paste-uid,
                              :block/string paste-string,
                              :block/open   true,
                              :block/order  5}]
        run!                #(->> (bfs/build-paste-op @@fixture/connection block-1-uid block-1-str-end paste-internal-repr)
                                  fixture/op-resolve-transact!)]
    (fixture/setup! setup-repr)
    (is (= setup-repr
           [(fixture/get-repr [:node/title page-title])]))
    (run!)
    (is (= exp-repr
           [(fixture/get-repr [:node/title page-title])]))))
