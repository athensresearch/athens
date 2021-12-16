;; # Structure parsing shenanigans

;; ## Require parser
;; `(:require [athens.parser.structure :as structure])`
^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns parser-notebook
  (:require
    [athens.parser.structure :as structure]
    [nextjournal.clerk :as clerk]))


;; ## Page links

;; This a basic page link.
^{:nextjournal.clerk/visibility #{:hide}}
(clerk/code
  (structure/structure-parser->ast "[[page link]]"))


;; Page links can also nest
(clerk/code
  (structure/structure-parser->ast "[[[[notebook]] parser]]"))


;; ## Hashtags

;; They are special kind of page link, and we have 2 types of hashtags

;; ### Naked hashtags
;; That is just `#` before word
(clerk/code
  (structure/structure-parser->ast "#abc"))


;; No spaces in between needed
(clerk/code
  (structure/structure-parser->ast "#abc#123"))


;; ### Braced hashtags
(clerk/code
  (structure/structure-parser->ast "#[[abc]]"))


;; We can also nest, oh my
(clerk/code
  (structure/structure-parser->ast "#[[[[notebook]] parser]]"))


;; All sorts of wired nesting should be fine
(clerk/code
  (structure/structure-parser->ast "#[[#[[notebook]] #parser]]"))


;; ## Block refs
(clerk/code
  (structure/structure-parser->ast "((abc))"))
