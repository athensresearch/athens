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

