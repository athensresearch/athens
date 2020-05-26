(ns athens.parse-transform-helper-test
  (:require
    [athens.parse-transform-helper :refer [combine-adjacent-strings]]
    [clojure.test :refer [deftest is are]]))


(deftest combine-adjacent-strings-tests
  (are [x y] (= x (combine-adjacent-strings y))
    []
    , []
    ["some text"]
    , ["some" " " "text"]
    ["some text" [:link] "around a link"]
    , ["some" " " "text" [:link] "around " "a link"]
    [{:something nil} "more text" [:link] "between elements" 39]
    , [{:something nil} "more" " " "text" [:link] "between" " " "elements" 39]
    [{:a 1 :b 2} 3 ["leave" "intact"]]
    , [{:a 1 :b 2} 3 ["leave" "intact"]]))
