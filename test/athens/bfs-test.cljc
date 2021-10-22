(ns athens.bfs-test
  (:require
    [athens.common-events.bfs :as bfs]
    [clojure.test :refer [deftest is]]))

(def tree-with-pages
  [{:block/uid      "0"
    :node/title     "Welcome"
    :page/sidebar   0
    :block/children [#:block{:uid      "block-1"
                             :string   "block with link to [[Welcome]]"
                             :order    0
                             :open     false
                             :children []}]}])


(def tree-without-page
  [{:block/uid "eaa4c9435",
    :block/string "block 1",
    :block/open true,
    :block/order 0,
    :block/children
    [{:block/uid "88c9ff662",
      :block/string "B1 C1",
      :block/open true,
      :block/order 0}
     {:block/uid "7d11d532f",
      :block/string "B1 C2",
      :block/open true,
      :block/order 1,
      :block/children
      [{:block/uid "db5fa9a43",
        :block/string "B1 C2 C1",
        :block/open true,
        :block/order 0}]}]}])


(deftest get-individual-blocks-from-tree-test
  (is (= [[{:block/uid "0", :block/order nil, :block/open nil, :block/string nil, :node/title "Welcome"}
           {:block/uid "block-1",
            :block/order 0,
            :block/open false,
            :block/string "block with link to [[Welcome]]",
            :node/title nil}]
          {"block-1" "0"}]
         (bfs/get-individual-blocks tree-with-pages)))

  (is (= [[{:block/uid "eaa4c9435", :block/order 0, :block/open true, :block/string "block 1", :node/title nil}
           {:block/uid "88c9ff662", :block/order 0, :block/open true, :block/string "B1 C1", :node/title nil}
           {:block/uid "7d11d532f", :block/order 1, :block/open true, :block/string "B1 C2", :node/title nil}
           {:block/uid "db5fa9a43", :block/order 0, :block/open true, :block/string "B1 C2 C1", :node/title nil}]
          {"88c9ff662" "eaa4c9435", "7d11d532f" "eaa4c9435", "db5fa9a43" "7d11d532f"}]
         (bfs/get-individual-blocks tree-without-page))))