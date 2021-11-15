(ns athens.patterns-test
  (:require
    [athens.patterns :as patterns]
    [clojure.test :refer [deftest is]]))


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
         (patterns/update-links-in-block text "AnotherPage" "AwesomePage"))))


;; The results of these tests may surprise you.
;; We use .* to detect if a link exists. Can't actually find capture any arbitrary link, because regex is greedy.
;; Instead, use the Instaparse parser to actually capture the [[inner content]] of strings.
(deftest wildcard-tests
  (is (= nil
         (re-find (patterns/linked ".*") "no link")))

  (is (= "[[a link]]"
         (first (re-find (patterns/linked ".*") "[[a link]]"))))

  (is (= "[[link 1]] [[link 2]]"
         (first (re-find (patterns/linked ".*") "[[link 1]] [[link 2]]"))))

  (is (= "#[[link 1]] #hashtag"
         (first (re-find (patterns/linked ".*") "#[[link 1]] #hashtag")))))

(deftest roam-to-athens-date
  (let [roam-dates ["February 1st, 2021"
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
                    "November 31st, 2021"]
        expected-athens-dates ["February 01, 2021"
                               "February 02, 2021"
                               "February 03, 2021"
                               "February 04, 2021"
                               "February 05, 2021"
                               "February 06, 2021"
                               "February 07, 2021"
                               "February 08, 2021"
                               "February 09, 2021"
                               "November 10, 2020"
                               "November 11, 2020"
                               "November 12, 2020"
                               "November 13, 2020"
                               "November 14, 2020"
                               "November 15, 2020"
                               "November 16, 2020"
                               "November 17, 2020"
                               "November 18, 2020"
                               "November 19, 2020"
                               "November 20, 2020"
                               "November 21, 2021"
                               "November 22, 2021"
                               "November 23, 2021"
                               "November 24, 2021"
                               "November 25, 2021"
                               "November 26, 2021"
                               "November 27, 2021"
                               "November 28, 2021"
                               "November 29, 2021"
                               "November 30, 2021"
                               "November 31, 2021"]]
    (is (= (map patterns/replace-roam-date roam-dates) expected-athens-dates))))
