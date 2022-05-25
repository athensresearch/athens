(ns athens.patterns-test
  (:require
    [athens.patterns :as patterns]
    [clojure.string :as string]
    [clojure.test :refer [deftest is are]]))


(deftest unlinked
  ;; matches
  (are [x] (some? (re-find (patterns/unlinked "foo bar") x))
    "foo bar"
    "Foo Bar"
    "foo bar baz"
    "baz foo bar")

  ;; does not match
  (are [x] (nil? (re-find (patterns/unlinked "foo bar") x))
    "[[foo bar]]"
    "[[Foo Bar]]"
    "#[[foo bar]]"))


;; From https://regex101.com/r/vOzOl9/1, mentioned in patterns/roam-date
(deftest roam-date
  ;; matches
  (are [x] (some? (re-find patterns/roam-date x))
    "February 1st, 2021"
    "February 2nd, 2021"
    "February 3rd, 2021"
    "February 4th, 2021"
    "February 5th, 2021"
    "February 6th, 2021"
    "February 7th, 2021"
    "February 8th, 2021"
    "February 9th, 2021"
    "November 10th, 2020"
    "November 11th, 2020"
    "November 12th, 2020"
    "November 13th, 2020"
    "November 14th, 2020"
    "November 15th, 2020"
    "November 16th, 2020"
    "November 17th, 2020"
    "November 18th, 2020"
    "November 19th, 2020"
    "November 20th, 2020"
    "November 21st, 2021"
    "November 22nd, 2021"
    "November 23rd, 2021"
    "November 24th, 2021"
    "November 25th, 2021"
    "November 26th, 2021"
    "November 27th, 2021"
    "November 28th, 2021"
    "November 29th, 2021"
    "November 30th, 2021"
    "November 31st, 2021")

  ;; does not match
  (are [x] (nil? (re-find patterns/roam-date x))
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
  (is (= ["foo " "bar" " baz"] (string/split "foo bar baz" (patterns/highlight "bar"))))
  (is (= ["foo " "(bar)" " baz"] (string/split "foo (bar) baz" (patterns/highlight "(bar)")))))
