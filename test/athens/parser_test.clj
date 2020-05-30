(ns athens.parser-test
  (:require
    [athens.parser :refer [parse-to-ast combine-adjacent-strings]]
    [clojure.test :refer [deftest is are]]))


(deftest block-parser-tests
  (is (= [:block] (parse-to-ast "")))
  (is (= [:block "OK? Yes."] (parse-to-ast "OK? Yes.")))
  (is (= [:block [:block-link "link"]] (parse-to-ast "[[link]]")))
  (is (= [:block "[[text"] (parse-to-ast "[[text")))
  ;; Not including tests for every type of syntax because I expect the trees they are parsed to to change soon.
  ;; For now, additional tests would probably be more annoying than useful.
  )


(deftest combine-adjacent-strings-tests
  (are [x y] (= x (combine-adjacent-strings y))
    []
    []
    ["some text"]
    ["some" " " "text"]
    ["some text" [:link] "around a link"]
    ["some" " " "text" [:link] "around " "a link"]
    [{:something nil} "more text" [:link] "between elements" 39]
    [{:something nil} "more" " " "text" [:link] "between" " " "elements" 39]
    [{:a 1 :b 2} 3 ["leave" "intact"]]
    [{:a 1 :b 2} 3 ["leave" "intact"]]))
