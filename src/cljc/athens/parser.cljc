(ns athens.parser
  (:require
    [athens.parser.impl :as impl]))


(defn parse-to-ast
  "Converts a string of block syntax to an abstract syntax tree for Athens Flavoured Markdown."
  [string]
  (impl/staged-parser->ast string))
