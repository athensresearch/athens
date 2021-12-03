(ns athens.parser.structure-test
  (:require
    [athens.parser.structure :as sut]
    [clojure.test :as t :refer [deftest is testing]]))


(defmacro parses-to
  [parser & tests]
  `(t/are [in# out#] (= out# (do
                               (println in#)
                               (time (~parser in#))))
     ~@tests))


(t/deftest page-links-test
  (t/testing "page links (Athens extension)"
    (parses-to sut/structure-parser->ast
               "[[Page Title]]"
               [:paragraph
                [:page-link {:from "[[Page Title]]"}
                 "Page Title"]]

               "In a middle [[Page Title]] of text"
               [:paragraph
                [:text-run "In a middle "]
                [:page-link {:from "[[Page Title]]"}
                 "Page Title"]
                [:text-run " of text"]]

               ;; But not when surrounded by word
               "abc[[def]]ghi"
               [:paragraph
                [:text-run "abc"]
                [:page-link {:from "[[def]]"} "def"]
                [:text-run "ghi"]]

               ;; also can't span newline
               "abc [[def\nghil]] jkl"
               [:paragraph
                [:text-run "abc [[def"]
                [:newline "\n"]
                [:text-run "ghil]] jkl"]]

               ;; apparently nesting page links is a thing
               "[[nesting [[nested]]]]"
               [:paragraph
                [:page-link {:from "[[nesting [[nested]]]]"}
                 "nesting "
                 [:page-link {:from "[[nested]]"}
                  "nested"]]]

               ;; Multiple page links in one blok
               "[[one]] and [[two]]"
               [:paragraph
                [:page-link {:from "[[one]]"}
                 "one"]
                [:text-run " and "]
                [:page-link {:from "[[two]]"}
                 "two"]])))
