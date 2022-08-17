(ns athens.patterns-test
  (:require
    [athens.patterns :as patterns]
    [clojure.string :as string]
    [clojure.test :refer [deftest is are]]))


(deftest contains-unlinked?
  ;; matches
  (are [x] (patterns/contains-unlinked? "foo bar" x)
    "foo bar"
    "Foo Bar"
    "foo bar baz"
    "baz foo bar")

  ;; does not match
  (are [x] (not (patterns/contains-unlinked? "foo bar" x))
    "[[foo bar]]"
    "[[Foo Bar]]"
    "#[[foo bar]]"
    "#foo bar"))


(deftest replace-roam-date
  ;; matches
  (are [x y] (= (patterns/replace-roam-date x) y)
    "February 1st, 2021"  "February 1, 2021"
    "February 2nd, 2021"  "February 2, 2021"
    "February 3rd, 2021"  "February 3, 2021"
    "February 4th, 2021"  "February 4, 2021"
    "February 5th, 2021"  "February 5, 2021"
    "February 6th, 2021"  "February 6, 2021"
    "February 7th, 2021"  "February 7, 2021"
    "February 8th, 2021"  "February 8, 2021"
    "February 9th, 2021"  "February 9, 2021"
    "January 10th, 2020"  "January 10, 2020"
    "February 11th, 2020"  "February 11, 2020"
    "March 12th, 2020"     "March 12, 2020"
    "April 13th, 2020"     "April 13, 2020"
    "May 14th, 2020"       "May 14, 2020"
    "June 15th, 2020"      "June 15, 2020"
    "July 16th, 2020"      "July 16, 2020"
    "August 17th, 2020"    "August 17, 2020"
    "September 18th, 2020" "September 18, 2020"
    "October 19th, 2020"   "October 19, 2020"
    "November 20th, 2020"  "November 20, 2020"
    "December 21st, 2021"  "December 21, 2021"
    "November 22nd, 2021"  "November 22, 2021"
    "November 23rd, 2021"  "November 23, 2021"
    "November 24th, 2021"  "November 24, 2021"
    "November 25th, 2021"  "November 25, 2021"
    "November 26th, 2021"  "November 26, 2021"
    "November 27th, 2021"  "November 27, 2021"
    "November 28th, 2021"  "November 28, 2021"
    "November 29th, 2021"  "November 29, 2021"
    "November 30th, 2021"  "November 30, 2021"
    "November 31st, 2021"  "November 31, 2021")

  ;; does not match
  (are [x] (= x (patterns/replace-roam-date x))
    "February 1th, 2021"
    "February 2th, 2021"
    "February 3st, 2021"
    "February 3nd, 2021"
    "November 21rd, 2021"
    "November 21nd, 2021"
    "November 22rd, 2021"
    "November 22st, 2021"
    "February 11st, 2021"
    "February 12nd, 2021"
    "February 13rd, 2021"))


(deftest highlight
  (is (= ["foo " "bar" " baz"] (patterns/split-on "foo bar baz" "bar")))
  (is (= ["foo " "(bar)" " baz"] (patterns/split-on "foo (bar) baz" "(bar)"))))
