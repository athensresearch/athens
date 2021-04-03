(ns athens.parser.impl-test
  (:require
   [athens.parser.impl :as sut]
   [clojure.test :as t :refer [deftest is are testing]]
   [instaparse.core :as insta]))

(t/deftest block-structure

  (t/testing "that headings are parsed"

    (t/are [in out] (= out (sut/block-parser->ast in))

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

    (t/are [in out] (= out (sut/block-parser->ast in))
      "***"
      [:block [:thematic-break "***"]]

      "---"
      [:block [:thematic-break "---"]]

      "___"
      [:block [:thematic-break "___"]]))

  (t/testing "that indented-code-blocks are parsed"

    (t/are [in out] (= out (sut/block-parser->ast in))

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

    (t/are [in out] (= out (sut/block-parser->ast in))

      "```\nsome code```"
      [:block [:fenced-code-block {:lang ""}
               [:code-text "some code"]]]

      "```javascript\nvar a = 1;\n```"
      [:block [:fenced-code-block {:lang "javascript"}
               [:code-text "var a = 1;"]]]

      "```javascript\nvar a = \"with` ticks`\";\nand multiline```"
      [:block [:fenced-code-block {:lang "javascript"}
               [:code-text "var a = \"with` ticks`\";\nand multiline"]]])))
