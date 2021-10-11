(ns athens.db-test
  (:require
    [athens.db :as db]
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
  ;; Given that database contains nodes with titles node-titles and we search
  ;; for query, check that expected-titles were returned by the search.
  (are [node-titles query expected-titles]
       (with-redefs
         [db/dsdb (d/create-conn db/schema)]
         (d/transact! db/dsdb (make-nodes-with-titles node-titles))
         (let
           [search-results (db/search-in-node-title query)
            actual-titles (map :node/title search-results)]
           (is (= actual-titles expected-titles))))

    ;; Exact string match
    ["Foo", "Bar"] "Foo" ["Foo"]

    ;; Case-insensitive substring match
    ["Page foo 1", "Page bar 2"] "FOO" ["Page foo 1"]

    ;; TODO(agentydragon): "kiwi recipe" should match "[[Banana]] - [[Kiwi]] smoothie recipe"
    ))


(deftest update-legacy-to-latest-test
  (let [graph-conf {:hlt-link-levels 4}
        expected   (assoc db/default-athens-persist
                          :theme/dark true
                          :graph-conf graph-conf
                          :settings {:email       "id@example.com"
                                     :username    "foo"
                                     :monitoring  false
                                     :backup-time 30})]
    (js/localStorage.setItem "auth/email" "id@example.com")
    (js/localStorage.setItem "user/name" "foo")
    (js/localStorage.setItem "debounce-save-time" "30")
    (js/localStorage.setItem "monitoring" "false")
    (js/localStorage.setItem "theme/dark" "true")
    (js/localStorage.setItem "graph-conf" "{:hlt-link-levels 4}")
    (is (= (db/update-legacy-to-latest db/default-athens-persist) expected))))
