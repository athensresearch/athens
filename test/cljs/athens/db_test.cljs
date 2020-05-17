(ns athens.db-test
  (:require [athens.db :as db]
            [cljs.test :as t :include-macros true :refer [deftest is testing]]))

(def sample-roam-export-data-tuple "[ \"?e\",
  \"?a\",
  \"?v\",
  \"5276\",
  \":block/order\",
  \"0\",
  \"4992\",
  \":edit/email\",
  \"\\\"tangj1122@gmail.com\\\"\",
  \"3173\",
  \":block/string\",
  \"\\\"**Reitit** is great for a high-performance, industrial-strength, full-control router—the kind of thing you could build a company on.\\\"\",
  \"93\",
  \":block/string\",
  \"\\\"Classification\\\"\"
 ]")

(deftest json-str-to-vector
  (testing "Using the `sample-roam-export-data-tuple` above, we should be able to get a EDN-formatted representation."
    (let [-vector (db/json-str-to-vector sample-roam-export-data-tuple)]
      (is (vector? -vector))

      (testing "Notice, the first three items are structure indicators for the rest of the vector i.e. [?entity, ?attribute, ?value]"
        (is (= (take 3 -vector) (list "?e" "?a" "?v")))))))

(deftest parse-tuples
  (testing "The data format `sample-roam-export-data-tuple` is not a random string vector, it is a vector where every 3 itens should be grouped as (?e,?a,?v)."
    (let [-vector (db/json-str-to-vector sample-roam-export-data-tuple)
          ret (db/parse-tuples -vector)]
      (testing "Notice, the first triplet is dropped because it only indicated the described structure."
        (is (not= (count ret) (/ (count -vector) 3)))
        (is (= (count ret) 4)))

      (testing "All tuples must have a `:db/add` first item which will be the used to create the fact inside our database."
        (is (every? some? (map first ret)))))))
