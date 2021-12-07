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
               "[[a\nb]]"
               [:paragraph
                [:instaparse/failure "[[a\nb]]"]]

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
                 "two"]]))

  (t/testing "nested page links"
    (parses-to sut/structure-parser->ast
               "[[[[topic]] subtopic]]"
               [:paragraph
                [:page-link {:from "[[[[topic]] subtopic]]"}
                 [:page-link {:from "[[topic]]"}
                  "topic"]
                 " subtopic"]]

               "[[abc #hasttag def]]"
               [:paragraph          
                [:page-link {:from "[[abc #hasttag def]]"}
                 "abc "
                 [:naked-hashtag {:from "#hasttag"}
                  "hasttag"]
                 " def"]]

               "[[#[[topic]] subtopic]]"
               [:paragraph          
                [:page-link {:from "[[#[[topic]] subtopic]]"}
                 [:braced-hashtag {:from "#[[topic]]"} "topic"]
                 " subtopic"]])))

(t/deftest hashtags-test

  (t/testing "naked hashtags"
    (parses-to sut/structure-parser->ast
               "#hashtag"
               [:paragraph
                [:naked-hashtag {:from "#hashtag"}
                 "hashtag"]]

               "#hasttag#without#spaces#between"
               [:paragraph          
                [:naked-hashtag {:from "#hasttag"} "hasttag"]
                [:naked-hashtag {:from "#without"} "without"]
                [:naked-hashtag {:from "#spaces"} "spaces"]
                [:naked-hashtag {:from "#between"} "between"]]

               "#hash #tags"
               [:paragraph          
                [:naked-hashtag {:from "#hash"} "hash"]
                [:text-run " "]
                [:naked-hashtag {:from "#tags"} "tags"]]))

  (t/testing "braced hashtags"
    (parses-to sut/structure-parser->ast
               "#[[hashtag]]"
               [:paragraph
                [:braced-hashtag {:from "#[[hashtag]]"}
                 "hashtag"]]

               "#[[hasttag]]#[[without]]#[[spaces]]#[[between]]"
               [:paragraph          
                [:braced-hashtag {:from "#[[hasttag]]"} "hasttag"]
                [:braced-hashtag {:from "#[[without]]"} "without"]
                [:braced-hashtag {:from "#[[spaces]]"} "spaces"]
                [:braced-hashtag {:from "#[[between]]"} "between"]]

               "#[[hash]] #[[tags]]"
               [:paragraph          
                [:braced-hashtag {:from "#[[hash]]"} "hash"]
                [:text-run " "]
                [:braced-hashtag {:from "#[[tags]]"} "tags"]]))

  (t/testing "mixed hashtags"
    (parses-to sut/structure-parser->ast
               "#[[hashtag #nested-bare]]"
               [:paragraph          
                [:braced-hashtag {:from "#[[hashtag #nested-bare]]"}
                 "hashtag "
                 [:naked-hashtag {:from "#nested-bare"}
                  "nested-bare"]]])))


(t/deftest block-ref-test
  (parses-to sut/structure-parser->ast
             "((abc))"
             [:paragraph
              [:block-ref {:from "((abc))"}
               "abc"]]

             "((a b c))" ;; no spaces in block refs
             [:paragraph
              [:text-run "((a b c"]
              [:text-run "))"]]))

(t/deftest typed-block-ref-test
  (parses-to sut/structure-parser->ast
             "{{embed: ((yeah))}}"
             [:paragraph          
              [:typed-block-ref
               {:from "{{embed: ((yeah))}}"}
               [:ref-type "embed"]
               [:block-ref {:from "((yeah))"} "yeah"]]]

             "{{but not this}}"
             [:paragraph [:text-run "{{but not this}}"]]

             "{{neither: this}}"
             [:paragraph [:text-run "{{neither: this}}"]]))
