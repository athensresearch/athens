(ns athens.parser.structure-test
  (:require
    [athens.common.utils :as utils]
    [athens.parser.structure :as sut]
    [clojure.test :as t]))


(t/deftest page-links-test

  (t/testing "page links (Athens extension)"
    (utils/parses-to sut/structure-parser->ast
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

                     ;; Also when surrounded by word (NOTE different from md-parser)
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
    (utils/parses-to sut/structure-parser->ast
                     "[[[[topic]] subtopic]]"
                     [:paragraph
                      [:page-link {:from "[[[[topic]] subtopic]]"}
                       [:page-link {:from "[[topic]]"}
                        "topic"]
                       " subtopic"]]

                     "[[abc #hashtag def]]"
                     [:paragraph
                      [:page-link
                       {:from "[[abc #hashtag def]]"}
                       "abc "
                       [:hashtag {:from "#hashtag"} "hashtag"]
                       " def"]]

                     "[[#[[topic]] subtopic]]"
                     [:paragraph
                      [:page-link {:from "[[#[[topic]] subtopic]]"}
                       [:hashtag {:from "#[[topic]]"} "topic"]
                       " subtopic"]])))


(t/deftest hashtags-test

  (t/testing "naked hashtags"
    (utils/parses-to sut/structure-parser->ast
                     "#hashtag"
                     [:paragraph
                      [:hashtag {:from "#hashtag"}
                       "hashtag"]]

                     "#can#do#without#spaces#between"
                     [:paragraph
                      [:hashtag {:from "#can"} "can"]
                      [:hashtag {:from "#do"} "do"]
                      [:hashtag {:from "#without"} "without"]
                      [:hashtag {:from "#spaces"} "spaces"]
                      [:hashtag {:from "#between"} "between"]]

                     "#hash #tags"
                     [:paragraph
                      [:hashtag {:from "#hash"} "hash"]
                      [:text-run " "]
                      [:hashtag {:from "#tags"} "tags"]]))

  (t/testing "braced hashtags"
    (utils/parses-to sut/structure-parser->ast
                     "#[[hashtag]]"
                     [:paragraph
                      [:hashtag {:from "#[[hashtag]]"}
                       "hashtag"]]

                     "#[[hashtag]]#[[without]]#[[spaces]]#[[between]]"
                     [:paragraph
                      [:hashtag {:from "#[[hashtag]]"} "hashtag"]
                      [:hashtag {:from "#[[without]]"} "without"]
                      [:hashtag {:from "#[[spaces]]"} "spaces"]
                      [:hashtag {:from "#[[between]]"} "between"]]

                     "#[[hash]] #[[tags]]"
                     [:paragraph
                      [:hashtag {:from "#[[hash]]"} "hash"]
                      [:text-run " "]
                      [:hashtag {:from "#[[tags]]"} "tags"]]))

  (t/testing "mixed hashtags"
    (utils/parses-to sut/structure-parser->ast
                     "#[[hashtag #nested-bare]]"
                     [:paragraph
                      [:hashtag {:from "#[[hashtag #nested-bare]]"}
                       "hashtag "
                       [:hashtag {:from "#nested-bare"}
                        "nested-bare"]]]))

  (t/testing "unicode"
    (utils/parses-to sut/structure-parser->ast
                     "learn #官话?"
                     [:paragraph
                      [:text-run "learn "]
                      [:hashtag {:from "#官话"} "官话"]
                      [:text-run "?"]])))


(t/deftest block-ref-test
  (utils/parses-to sut/structure-parser->ast
                   "((abc))"
                   [:paragraph
                    [:block-ref {:from "((abc))"}
                     "abc"]]

                   "((a b c))" ; no spaces in block refs
                   [:paragraph
                    [:text-run "((a b c"]
                    [:text-run "))"]]))


(t/deftest typed-block-ref-test
  (utils/parses-to sut/structure-parser->ast
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
