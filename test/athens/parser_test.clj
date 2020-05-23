(ns athens.parser-test
  (:require
    [athens.parser :refer [block-parser]]
    [clojure.test :refer [deftest is]]))


(deftest block-parser-tests
  (is (= [:block] (block-parser "")))
  (is (= [:block "O" "K" "?" " " "Y" "e" "s" "."] (block-parser "OK? Yes.")))
  (is (= [:block [:block-link [:any-chars "l" "i" "n" "k"]]] (block-parser "[[link]]")))
  (is (= [:block "[" "[" "t" "e" "x" "t"] (block-parser "[[text")))
  ;; Not including tests for every type of syntax because I expect the trees they are parsed to to change soon.
  ;; For now, additional tests would probably be more annoying than useful.
  )
