(ns athens.bfs-test
  (:require
    [athens.common-db :as common-db]
    [athens.common-events.bfs :as bfs]
    [clojure.test :refer [deftest is] :as t]
    [datascript.core :as d]))


(def tree-with-page
  [{:page/title "Welcome"
    :block/properties
    {"key" #:block{:uid    "prop-1"
                   :string "value"}}
    :block/children
    [#:block{:uid    "block-1"
             :string "block with link to [[Welcome]]"
             :open?  false
             :properties
             {"another-key" #:block{:uid    "prop-2"
                                    :string "another-value"}}}]}])


(def tree-without-page
  [{:block/uid    "eaa4c9435"
    :block/string "block 1"
    :block/children
    [{:block/uid    "88c9ff662"
      :block/string "B1 C1"}
     {:block/uid    "7d11d532f"
      :block/string "B1 C2"
      :block/open?  false
      :block/children
      [{:block/uid    "db5fa9a43"
        :block/string "B1 C2 C1"}]}]}])


(deftest get-individual-blocks-from-tree-test
  (let [db common-db/empty-db]
    (is (= [#:op{:type :page/new, :atomic? true, :args {:page/title "Welcome"}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "block-1", :block/position {:page/title "Welcome", :relation :last}}}
            #:op{:type :block/open :atomic? true :args {:block/uid "block-1" :block/open? false}}
            #:op{:type :page/new, :atomic? true, :args {:page/title "another-key"}}
            #:op{:type :block/new, :atomic? true, :args #:block{:uid "prop-2", :position {:block/uid "block-1", :relation #:page{:title "another-key"}}}}
            #:op{:type :page/new, :atomic? true, :args {:page/title "key"}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "prop-1", :block/position {:page/title "Welcome", :relation {:page/title "key"}}}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "block-1", :block/string "block with link to [[Welcome]]"}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "prop-2", :block/string "another-value"}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "prop-1", :block/string "value"}}]
           (bfs/internal-representation->atomic-ops db tree-with-page nil)))

    (is (= [#:op{:type :page/new, :atomic? true, :args #:page{:title "title"}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "eaa4c9435", :block/position {:page/title "title", :relation :first}}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "88c9ff662", :block/position {:block/uid "eaa4c9435", :relation :last}}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "7d11d532f", :block/position {:block/uid "88c9ff662", :relation :after}}}
            #:op{:type :block/open :atomic? true :args {:block/uid "7d11d532f" :block/open? false}}
            #:op{:type :block/new, :atomic? true, :args {:block/uid "db5fa9a43", :block/position {:block/uid "7d11d532f", :relation :last}}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "eaa4c9435", :block/string "block 1"}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "88c9ff662", :block/string "B1 C1"}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "7d11d532f", :block/string "B1 C2"}}
            #:op{:type :block/save, :atomic? true, :args {:block/uid "db5fa9a43", :block/string "B1 C2 C1"}}]
           (bfs/internal-representation->atomic-ops db tree-without-page {:page/title "title" :relation :first})))))


(deftest get-internal-representation
  (let [db (-> common-db/empty-db
               (d/db-with [{:node/title "key"}
                           {:node/title "another-key"}
                           {:node/title   "Welcome"
                            :page/sidebar 0
                            :block/_property-of
                            [#:block{:uid    "prop-1"
                                     :string "value"
                                     :key    [:node/title "key"]}]
                            :block/children
                            [#:block{:uid    "block-1"
                                     :string "block with link to [[Welcome]]"
                                     :open   false
                                     :order  0
                                     :_property-of
                                     [#:block{:uid    "prop-2"
                                              :string "another-value"
                                              :key    [:node/title "another-key"]}]}]}
                           {:block/uid    "eaa4c9435",
                            :block/string "block 1",
                            :block/open   true,
                            :block/order  0
                            :block/children
                            [{:block/uid    "88c9ff662",
                              :block/string "B1 C1",
                              :block/order  0
                              :block/open   true}
                             {:block/uid    "7d11d532f",
                              :block/string "B1 C2",
                              :block/order  1
                              :block/open   false,
                              :block/children
                              [{:block/uid    "db5fa9a43",
                                :block/string "B1 C2 C1",
                                :block/order  0
                                :block/open   true}]}]}]))]
    (is (= (first tree-with-page) (common-db/get-internal-representation db (:db/id (d/entity db [:node/title "Welcome"])))))
    (is (= (first tree-without-page) (common-db/get-internal-representation db (:db/id (d/entity db [:block/uid "eaa4c9435"])))))))


(comment
  (binding [t/*stack-trace-depth* 5] (t/run-tests))

  (get-internal-representation)

  (bfs/internal-representation->atomic-ops common-db/empty-db tree-without-page {:page/title "title" :relation :first}))
