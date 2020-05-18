(ns athens.patterns-test
  (:require
    [athens.patterns :as patterns]
    [clojure.string :as str]
    [clojure.test :refer [deftest is]]))


(defn update-links-in-block
  [s old-title new-title]
  (str/replace s
               (patterns/linked old-title)
               (str "$1$3$4" new-title "$2$5")))


(def text
  "A block with multipe link to [[AnotherPage]] in different forms #[[AnotherPage]] #AnotherPage.")


(def new-text
  "A block with multipe link to [[AwesomePage]] in different forms #[[AwesomePage]] #AwesomePage.")


(deftest linked-patterns-tests
  (is (= "[[Page Title]]"
         (first (re-find (patterns/linked "Page Title")
                         "Some text with a [[Page Title]]"))))
  (is (= "#PageTitle"
         (first (re-find (patterns/linked "PageTitle")
                         "Some text with a #PageTitle"))))
  (is (= "#[[Page Title]]"
         (first (re-find (patterns/linked "Page Title")
                         "Some text with a #[[Page Title]]"))))
  (is (= new-text
         (update-links-in-block text "AnotherPage" "AwesomePage"))))
