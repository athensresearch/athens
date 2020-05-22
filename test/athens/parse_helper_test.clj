(ns athens.parse-helper-test
  (:require
    [athens.parse-helper :as parse-helper]
    [clojure.test :refer [deftest is are]]))


(deftest combine-adjacent-strings-tests
  (are [x y] (= x (parse-helper/combine-adjacent-strings y))
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
