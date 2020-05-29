(ns athens.parser-test
  (:require
    [athens.parser :refer [parse-to-ast]]
    [clojure.test :refer [deftest is]]))


(deftest block-parser-tests
  (is (= [:block] (parse-to-ast "")))
  (is (= [:block "OK? Yes."] (parse-to-ast "OK? Yes.")))
  (is (= [:block [:block-link "link"]] (parse-to-ast "[[link]]")))
  (is (= [:block "[[text"] (parse-to-ast "[[text")))
  ;; Not including tests for every type of syntax because I expect the trees they are parsed to to change soon.
  ;; For now, additional tests would probably be more annoying than useful.
  )
