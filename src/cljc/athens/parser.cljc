(ns athens.parser
  (:require
    [clojure.string :as string]
    #?(:cljs [instaparse.core :as insta :refer-macros [defparser]]
       :clj  [instaparse.core :as insta :refer [defparser]])))


(declare block-parser)


;; Instaparse docs: https://github.com/Engelberg/instaparse#readme
;; Main parser documentation: `doc/parser.md` in this repository

(defparser block-parser
  "(* Welcome to the Athens Block Parser! *)
   (* We're currently building a more robust + performant one, so if you have any idea *)
   (* regarding how to implement it better, feel free to open an issue and lend us a hand! :) *)
   (* Currently, this is implemented similar to a LL(1) parser, which should keep its performance levels at O(n). *)
   
   (* This first rule is the top-level one. *)
   (* `/` ordered alternation is used to, for example, try to interpret a string beginning with '[[' as a page-link before interpreting it as raw characters. *)
   block = (non-reserved-chars / pre-formatted / syntax-in-block / reserved-char) *
   
   (* The following regular expression expresses this: (any character except '`') <- This repeated as many times as possible *)
   <any-non-pre-formatted-chars> = #'[^\\`]*'
   pre-formatted = block-pre-formatted | inline-pre-formatted
   <block-pre-formatted> = <'```'> any-non-pre-formatted-chars <'```'>
   <inline-pre-formatted> = <'`'> any-non-pre-formatted-chars <'`'>
   
   (* Because code blocks are pre-formatted, we process them before these applied syntaxes. *)
   <syntax-in-block> = (component | page-link | block-ref | hashtag | url-image | url-link | bold)
   
   <syntax-in-component> = (page-link | block-ref)
   <any-non-component-reserved-chars> = #'[^\\{\\}]*'
   component = <'{{'> any-non-component-reserved-chars <'}}'>
   
   (* The following regular expression expresses this: (any character except '[' or ']') <- This repeated as many times as possible *)
   <any-non-page-link-chars> = #'[^\\[\\]]*'
   <page-link-content> = (any-non-page-link-chars | page-link)*
   page-link = <'[['> page-link-content <']]'>
   
   (* A block reference could only be letters, numbers, and lower and regular dash. *)
   block-ref = <'(('> #'[a-zA-Z0-9_\\-]+' <'))'>
   
   hashtag = hashtag-bare | hashtag-delimited
   <hashtag-bare> = <'#'> #'[^\\ \\+\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\?\\\"\\;\\:\\]\\[]+'  (* Unicode: L = letters, M = combining marks, N = numbers *)
   <hashtag-delimited> = <'#'> <'[['> page-link-content <']]'>

   url-image = <'!'> url-link-text url-link-url
   
   url-link = url-link-text url-link-url
   <url-link-text> = <'['> url-link-text-contents <']'>
   url-link-text-contents = ( (bold | backslash-escaped-right-bracket) / any-char )*
   <backslash-escaped-right-bracket> = <'\\\\'> ']'
   <url-link-url> = <'('> url-link-url-parts <')'>
   url-link-url-parts = url-link-url-part+
   <url-link-url-part> = (backslash-escaped-paren | '(' url-link-url-part* ')') / any-char
   <backslash-escaped-paren> = <'\\\\'> ('(' | ')')
   
   (* The following regular expression expresses this: (any character except '*') <- This repeated as many times as possible *)
   <non-bold-chars> = #'[^\\*]*'
   bold = <'**'> non-bold-chars <'**'>
   
   (* -- It’s useful to extract this rule because its transform joins the individual characters everywhere it’s used. *)
   (* -- However, I think in many cases a more specific rule can be used. So we will migrate away from uses of this rule. *)
   
   (* Here are a list of 'stop characters' we implemented, to get the LL(1) performance. *)
   (* The current reserved characters are:  ->  ( [ * < ` {  # ! <- *)
   (* Note that since our grammar is a left-recursive one, we only use the opening chars in the pair. *)
   (* IMPORTANT: if you are adding new reserved characters to the list, remember to change them all in the following regex & update the list above! *)
   (* Regex could be a thinker at times, but you can use this tool https://regex101.com/ for a visual debugging experience. *)
   <non-reserved-char> =  #'[^\\(\\[\\*\\<\\`\\{\\#\\!]'
   <reserved-char> =       #'[\\(\\[\\*\\<\\`\\{\\#\\!]'
   <non-reserved-chars> = #'[^\\(\\[\\*\\<\\`\\{\\#\\!]*'
   <any-char> = #'\\w|\\W'
   <any-chars> = #'[\\w|\\W]+'
   
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
                               (string/join chars))
     :component              (fn [raw-content-string]
                               (into [:component raw-content-string] (rest (block-parser raw-content-string))))}
    tree))


(defn parse-to-ast
  "Converts a string of block syntax to an abstract syntax tree for Athens markup."
  [string]
  (transform-to-ast (block-parser string)))
