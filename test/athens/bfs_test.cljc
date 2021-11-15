(ns athens.bfs-test
  (:require
    [athens.common-db :as common-db]
    [athens.common-events.bfs :as bfs]
    [clojure.test :refer [deftest is] :as t]
    [datascript.core :as d]))


(def tree-with-pages
  [{:node/title     "Welcome"
    :block/children [#:block{:uid    "block-1"
                             :string "block with link to [[Welcome]]"
                             :open   false}]}])


(def tree-without-page
  [{:block/uid "eaa4c9435"
    :block/string "block 1"
    :block/children
    [{:block/uid "88c9ff662"
      :block/string "B1 C1"}
     {:block/uid "7d11d532f"
      :block/string "B1 C2"
      :block/open false
      :block/children
      [{:block/uid "db5fa9a43"
        :block/string "B1 C2 C1"}]}]}])


(deftest get-individual-blocks-from-tree-test
  (let [db (d/empty-db common-db/schema)]
    (is (= [#:op{:type :page/new, :atomic? true, :args {:page/title "Welcome"}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "block-1", :block/position {:page/title "Welcome", :relation :last}}}
            #:op{:type :composite/consequence,
                 :atomic? false,
                 :trigger #:op{:type :block/save},
                 :consequences
                 [#:op{:type :page/new, :atomic? true, :args {:page/title "Welcome"}}
                  #:op{:type :block/save, :atomic? true, :args {:block/uid "block-1", :block/string "block with link to [[Welcome]]"}}]}
            #:op{:type :block/open :atomic? true :args {:block/uid "block-1" :block/open? false}}]
           (bfs/internal-representation->atomic-ops db tree-with-pages nil)))

    (is (= [#:op{:type :block/new, :atomic? true, :args {:block/uid "eaa4c9435", :block/position {:page/title "title", :relation :first}}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "eaa4c9435", :block/string "block 1"}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "88c9ff662", :block/position {:block/uid "eaa4c9435", :relation :last}}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "88c9ff662", :block/string "B1 C1"}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "7d11d532f", :block/position {:block/uid "88c9ff662", :relation :after}}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "7d11d532f", :block/string "B1 C2"}}
            #:op{:type :block/open :atomic? true :args {:block/uid "7d11d532f" :block/open? false}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "db5fa9a43", :block/position {:block/uid "7d11d532f", :relation :last}}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "db5fa9a43", :block/string "B1 C2 C1"}}]
           (bfs/internal-representation->atomic-ops db tree-without-page {:page/title "title" :relation :first})))))


(comment
  (binding [t/*stack-trace-depth* 5] (t/run-tests))

  (bfs/internal-representation->atomic-ops (d/empty-db common-db/schema) tree-without-page {:page/title "title" :relation :first}))
