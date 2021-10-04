(ns athens.copy-paste-test
  (:require
    [athens.views.blocks.content :as content]
    [clojure.test :refer [deftest is]]))


(deftest test-update-strings-with-new-uids
  (is (= "This is a test string with ((uid1)) and ((more_uids1)) and ((some_more_uids1)) ((rast1)))) ((rsrsta1)))) ((arst))a)) ((rst1))rst)) invalid uids (()) (( a)) ((a )) (( a )) ((a b)) s((a1))")
      (content/update-strings-with-new-uids "This is a test string with ((uid)) and ((more_uids)) and ((some_more_uids)) ((((rast)))) ((rs((rsta)))) ((a((rst))a)) ((((rst))rst)) invalid uids (()) (( a)) ((a )) (( a )) ((a b)) s((a))"
                                            {"uid"            "uid1"
                                             "more_uids"      "more_uids1"
                                             "some_more_uids" "some_more_uids1"
                                             "((rast"         "rast1"
                                             "rs((rsta"       "rsrsta1"
                                             "a((rst"         "arst"
                                             "((rst"          "rst1"
                                             "a"              "a1"})))


(deftest test-walk-tree-and-update-string-with-new-uids
  (is (= [#:block{:uid "df27e0c38", :string "((111))", :open true, :order 0}
          #:block{:uid "b6c3d65a7", :string "((222))", :open true, :order 1}])
      (content/walk-tree-to-replace [{:block/uid "df27e0c38",
                                      :block/string "((df27e0c38))"
                                      :block/open true,
                                      :block/order 0}
                                     {:block/uid "b6c3d65a7",
                                      :block/string "((b6c3d65a7))",,
                                      :block/open true,}]
                                    {"df27e0c38" "111"
                                     "b6c3d65a7" "222"}
                                    :block/string)))


(deftest test-walk-tree-and-update-uids-with-new-uids
  (is (= [#:block{:uid "111", :string "((df27e0c38))", :open true, :order 0}
          #:block{:uid "222", :string "((b6c3d65a7))", :open true, :order 1}])
      (content/walk-tree-to-replace [{:block/uid "df27e0c38",
                                      :block/string "((df27e0c38))"
                                      :block/open true,
                                      :block/order 0}
                                     {:block/uid "b6c3d65a7",
                                      :block/string "((b6c3d65a7))",,
                                      :block/open true,
                                      :block/order 1}]
                                    {"df27e0c38" "111"
                                     "b6c3d65a7" "222"}
                                    :block/string)))


(deftest test-walk-tree-and-update-uids-with-new-uids
  (is (= [#:block{:uid "111", :string "((111))", :open true, :order 0}
          #:block{:uid "222", :string "((222))", :open true, :order 1}])
      (content/update-uids [{:block/uid "df27e0c38",
                             :block/string "((df27e0c38))"
                             :block/open true,
                             :block/order 0}
                            {:block/uid "b6c3d65a7",
                             :block/string "((b6c3d65a7))",,
                             :block/open true,
                             :block/order 1}]
                           {"df27e0c38" "111"
                            "b6c3d65a7" "222"})))
