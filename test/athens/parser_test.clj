(ns athens.parser-test
  (:require [clojure.test :refer :all]
            [athens.parser :refer :all]))

(deftest parser-test
  (is (= (parser "Link") [:S "Link"]))
  (is (= (parser "[[Link]]") [:S [:link "Link"]]))
  (is (= (parser "#c") [:S [:hash "c"]]))
  (is (= (parser "[[ Link1 [[Link2]] ]]") [:S [:link [:S " Link1 "] [:link "Link2"] [:S " "]]]))
  )
