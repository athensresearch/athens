(ns athens.parser.impl-test
  (:require
    [athens.parser.impl :as sut]
    [clojure.test :as t :refer [deftest is are testing]]
    [instaparse.core :as insta]))


(defmacro parses-to
  [parser & tests]
  `(t/are [in# out#] (= out# (~parser in#))
     ~@tests))


(t/deftest block-structure

  (t/testing "that headings are parsed"

    (parses-to sut/block-parser->ast

               "# Heading"
               [:block [:heading {:n 1}
                        [:paragraph-text "Heading"]]]

               "# Heading\n\n"
               [:block [:heading {:n 1}
                        [:paragraph-text "Heading"]]]

               "### Heading\n"
               [:block [:heading {:n 3}
                        [:paragraph-text "Heading"]]]))

  (t/testing "that thematic-breaks are parsed"

    (parses-to sut/block-parser->ast

               "***"
               [:block [:thematic-break "***"]]

               "---"
               [:block [:thematic-break "---"]]

               "___"
               [:block [:thematic-break "___"]]))

  (t/testing "that indented-code-blocks are parsed"

    (parses-to sut/block-parser->ast

               "    some code"
               [:block [:indented-code-block
                        [:code-text "some code"]]]

               "    multiline\n    code"
               [:block [:indented-code-block
                        [:code-text "multiline\ncode"]]]

               "    multiline\n    code\n      with indentation"
               [:block [:indented-code-block
                        [:code-text "multiline\ncode\n  with indentation"]]]))

  (t/testing "that fenced-code-blocks are parsed"

    (parses-to sut/block-parser->ast

               "```\nsome code```"
               [:block [:fenced-code-block {:lang ""}
                        [:code-text "some code"]]]

               "```javascript\nvar a = 1;\n```"
               [:block [:fenced-code-block {:lang "javascript"}
                        [:code-text "var a = 1;"]]]

               "```javascript\nvar a = \"with` ticks`\";\nand multiline```"
               [:block [:fenced-code-block {:lang "javascript"}
                        [:code-text "var a = \"with` ticks`\";\nand multiline"]]]))

  (t/testing "that paragraphs are parsed"

    (parses-to sut/block-parser->ast

               "aaa"
               [:block [:paragraph-text "aaa"]]

               "aaa\n\nbbb"
               [:block
                [:paragraph-text "aaa"]
                [:paragraph-text "bbb"]]

               "aaa\nbbb\n\nccc\nddd"
               [:block
                [:paragraph-text "aaa\nbbb"]
                [:paragraph-text "ccc\nddd"]]

               "aaa\n\n\nbbb"
               [:block
                [:paragraph-text "aaa"]
                [:paragraph-text "bbb"]]

               "  aaa\n bbb" ;; leading spaces are skipped
               [:block [:paragraph-text "aaa\nbbb"]]

               "aaa\n    bbb\n        ccc"
               [:block [:paragraph-text "aaa\nbbb\nccc"]]

               "   aaa\nbbb" ;; 3 spaces max
               [:block [:paragraph-text "aaa\nbbb"]]

               "    aaa\nbbb" ;; or code block is triggered
               [:block
                [:indented-code-block [:code-text "aaa"]]
                [:paragraph-text "bbb"]]

               "aaa    \nbbb    " ;; final spaces are stripped
               [:block [:paragraph-text "aaa\nbbb"]])))
