(ns athens.parser
  (:require
    [clojure.string :as string]
    #?(:cljs [instaparse.core :as insta :refer-macros [defparser]]
       :clj  [instaparse.core :as insta :refer [defparser]])))



(declare block-parser)


;; Instaparse docs: https://github.com/Engelberg/instaparse#readme

(defparser block-parser
  "(* This first rule is the top-level one. *)
   block = ( syntax-in-block / any-char )*
   (* `/` ordered alternation is used to, for example, try to interpret a string beginning with '[[' as a page-link before interpreting it as raw characters. *)
   
   <syntax-in-block> = (page-link | block-ref | hashtag | url-image | url-link | bold)
   
   page-link = <'[['> any-chars <']]'>
   
   block-ref = <'(('> #'[a-zA-Z0-9_\\-]+' <'))'>
   
   hashtag = hashtag-bare | hashtag-delimited
   <hashtag-bare> = <'#'> #'[\\p{L}\\p{M}\\p{N}_]+'  (* Unicode: L = letters, M = combining marks, N = numbers *)
   <hashtag-delimited> = <'#'> <'[['> #'[^\\]]+' <']]'>

   url-image = <'!'> url-link-text url-link-url
   
   url-link = url-link-text url-link-url
   <url-link-text> = <'['> url-link-text-contents <']'>
   url-link-text-contents = ( (bold | backslash-escaped-right-bracket) / any-char )*
   <backslash-escaped-right-bracket> = <'\\\\'> ']'
   <url-link-url> = <'('> url-link-url-parts <')'>
   url-link-url-parts = url-link-url-part+
   <url-link-url-part> = (backslash-escaped-paren | '(' url-link-url-part* ')') / any-char
   <backslash-escaped-paren> = <'\\\\'> ('(' | ')')
   
   bold = <'**'> any-chars <'**'>
   
   (* It’s useful to extract this rule because its transform joins the individual characters everywhere it’s used. *)
   (* However, I think in many cases a more specific rule can be used. So we will migrate away from uses of this rule. *)
   any-chars = any-char+
   
   <any-char> = #'\\w|\\W'
   ")


(defn combine-adjacent-strings
  "In a sequence of strings mixed with other values, returns the same sequence with adjacent strings concatenated.
   (If the sequence contains only strings, use clojure.string/join instead.)"
  [coll]
  (reduce
    (fn [elements-so-far elmt]
      (if (and (string? elmt) (string? (peek elements-so-far)))
        (let [previous-elements (pop elements-so-far)
              combined-last-string (str (peek elements-so-far) elmt)]
          (conj previous-elements combined-last-string))
        (conj elements-so-far elmt)))
    []
    coll))


(defn transform-to-ast
  "Transforms the Instaparse output tree to an abstract syntax tree for Athens markup."
  [tree]
  (insta/transform
    {:block                  (fn [& raw-contents]
                                ;; use combine-adjacent-strings to collapse individual characters from any-char into one string
                               (into [:block] (combine-adjacent-strings raw-contents)))
     :url-image              (fn [[text-contents] url]
                               (into [:url-image {:url url :alt text-contents}]))
     :url-link               (fn [text-contents url]
                               (into [:url-link {:url url}] text-contents))
     :url-link-text-contents (fn [& raw-contents]
                               (combine-adjacent-strings raw-contents))
     :url-link-url-parts     (fn [& chars]
                               (string/join chars))
     :any-chars              (fn [& chars]
                               (string/join chars))}
    tree))


(defn parse-to-ast
  "Converts a string of block syntax to an abstract syntax tree for Athens markup."
  [string]
  (transform-to-ast (block-parser string)))
