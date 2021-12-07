(ns athens.parser
  (:require
    [athens.parser.impl      :as impl]
    [athens.parser.structure :as structure]))


(defn parse-to-ast
  "Converts a string of block syntax to an abstract syntax tree for Athens Flavoured Markdown."
  [string]
  (impl/staged-parser->ast string))


(defn structure-parse-to-ast
  "Converts a string to structure elements in it, AST of course."
  [string]
  (structure/structure-parser->ast string))
