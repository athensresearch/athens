(ns athens.db-test
  (:require
    [athens.db :as db]
    [clojure.pprint :refer [pprint]]
    [clojure.test :refer [deftest is are]]
    [datascript.core :as d]))


(enable-console-print!)


(defn make-nodes-with-titles
  "Returns empty nodes (pages) with the given titles."
  [titles]
  (map-indexed
    (fn [i title]
      {:node/title title
       :block/uid (str "uid" (inc i))})
    titles))


(deftest search-in-node-title-test
  ; Given that database contains nodes with titles node-titles and we search
  ; for query, check that expected-titles were returned by the search.
  (are [node-titles query expected-titles]
       (with-redefs
         [db/dsdb (d/create-conn db/schema)]
         (d/transact! db/dsdb (make-nodes-with-titles node-titles))
         (let
           [search-results (db/search-in-node-title query)
            actual-titles (map :node/title search-results)]
           (is (= actual-titles expected-titles))))

    ; Exact string match
    ["Foo", "Bar"] "Foo" ["Foo"]

    ; Case-insensitive substring match
    ["Page foo 1", "Page bar 2"] "FOO" ["Page foo 1"]

    ; TODO(agentydragon): "kiwi recipe" should match "[[Banana]] - [[Kiwi]] smoothie recipe"
    ))
