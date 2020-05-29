(ns athens.parser
  (:require
    [athens.parse-transform-helper :refer [combine-adjacent-strings]]   
    #?(:cljs [instaparse.core :as insta :refer-macros [defparser]]
       :clj [instaparse.core :as insta :refer [defparser]])))


(declare block-parser)


;; Instaparse docs: https://github.com/Engelberg/instaparse#readme

(defparser block-parser
  "(* This first rule is the top-level one. *)
   block = ( syntax-in-block / any-char )*
   (* `/` ordered alternation is used to, for example, try to interpret a string beginning with '[[' as a block-link before interpreting it as raw characters. *)
   
   <syntax-in-block> = (block-link | block-ref | hashtag | bold)
   
   block-link = <'[['> any-chars <']]'>
   
   block-ref = <'(('> any-chars <'))'>
   
   hashtag = <'#'> any-chars | <'#'> <'[['> any-chars <']]'>
   
   bold = <'**'> any-chars <'**'>
   
   (* It’s useful to extract this rule because its transform joins the individual characters everywhere it’s used. *)
   (* However, I think in many cases a more specific rule can be used. So we will migrate away from uses of this rule. *)
   any-chars = any-char+
   
   <any-char> = #'\\w|\\W'
   ")


(defn transform-to-ast
  "Transforms the Instaparse output tree to an abstract syntax tree for Athens markup."
  [tree]
  (insta/transform
    {:block      (fn [& raw-contents]
                    ;; use combine-adjacent-strings to collapse individual characters from any-char into one string
                   (into [:block] (combine-adjacent-strings raw-contents)))
     :any-chars  (fn [& chars]
                   (clojure.string/join chars))}
    tree))


(defn parse-to-ast
  "Converts a string of block syntax to an abstract syntax tree for Athens markup."
  [string]
  (transform-to-ast (block-parser string)))
