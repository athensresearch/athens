(ns athens.parser-test
  (:require
    [athens.parser :refer [parse-to-ast combine-adjacent-strings]]
    [clojure.test :refer [deftest is are]]))


(deftest block-parser-tests
  (are [x y] (= x (parse-to-ast y))
    [:block]
    , ""
    [:block "OK? Yes."]
    , "OK? Yes."
    [:block [:page-link "link"]]
    , "[[link]]"
    [:block "A " [:page-link "link"] "."]
    , "A [[link]]."
    [:block "[[text"]
    , "[[text"
    [:block [:url-link {:url "https://example.com/"} "an example"]]
    , "[an example](https://example.com/)"))


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
