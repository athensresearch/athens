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
   block = (pre-formatted / url-raw / syntax-in-block / reserved-chars / non-reserved-chars) *

   (* The following regular expression expresses this: (any character except '`') <- This repeated as many times as possible *)
   <pre-formatted> = ( inline-pre-formatted / block-pre-formatted )
   any-non-pre-formatted-chars = #'(?s)[^`{3}]+' (* #'(?s).*(?=`{3})' *)
   block-pre-formatted = <'```'> any-non-pre-formatted-chars <'```'>

   <backtick-not-preceded-by-backtick> = #'(?<!`)`'
   <backtick-not-followed-by-backtick> = #'`(?!`)'
   <any-non-inline-pre-formatted-chars> = #'[^`]+(?=`)'
   inline-pre-formatted = <backtick-not-preceded-by-backtick> any-non-inline-pre-formatted-chars <backtick-not-followed-by-backtick>

 (* Because code blocks are pre-formatted, we process them before these applied syntaxes. *)
   <syntax-in-block> = (component | page-link | block-ref | hashtag | url-image | url-link | bold | latex)
   
   <syntax-in-component> = (page-link / block-ref)
   <any-non-component-reserved-chars> = #'[^\\{\\}]*'
   component = <'{{'> any-non-component-reserved-chars <'}}'>
   
   (* The following regular expression expresses this: (any character except '[' or ']') <- This repeated as many times as possible *)
   <any-non-page-link-chars> = #'[^\\[\\]]*'
   <page-link-content> = (any-non-page-link-chars / page-link)*
   page-link = <'[['> page-link-content <']]'>
   
   (* A block reference could only be letters, numbers, and lower and regular dash. *)
   block-ref = <'(('> #'[a-zA-Z0-9_\\-]+' <'))'>
   
   hashtag = hashtag-bare | hashtag-delimited
   <hashtag-bare> = <'#'> #'[^\\ \\+\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\?\\\"\\;\\:\\]\\[]+'  (* Unicode: L = letters, M = combining marks, N = numbers *)
   <hashtag-delimited> = <'#'> <'[['> page-link-content <']]'>

   url-raw = #'(?i)\\b(?:(?:https?|ftp)://)(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?\\b'
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
   
   (* LaTeX *)
   <not-dollars> = #'.*?(?=\\$\\$)'
   latex = <'$$'> not-dollars <'$$'>

   (* -- It’s useful to extract this rule because its transform joins the individual characters everywhere it’s used. *)
   (* -- However, I think in many cases a more specific rule can be used. So we will migrate away from uses of this rule. *)
   
   (* Here are a list of 'stop characters' we implemented, to get the LL(1) performance. *)
   (* The current reserved characters are:  ->  ( [ * < ` {  # ! $ <- *)
   (* Note that since our grammar is a left-recursive one, we only use the opening chars in the pair. *)
   (* IMPORTANT: if you are adding new reserved characters to the list, remember to change them all in the following regex & update the list above! *)
   (* Regex could be a thinker at times, but you can use this tool https://regex101.com/ for a visual debugging experience. *)
   <non-reserved-char> =  #'[^\\(\\[\\*\\<\\`\\{\\#\\!\\$]'
   reserved-chars =       #'[\\(\\[\\*\\<\\`\\{\\#\\!\\$]*'
   <non-reserved-chars> = #'[^\\(\\[\\*\\<\\`\\{\\#\\!\\$]*'
   any-char = #'\\w|\\W'
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
     :url-raw                (fn [url]
                               [:url-link {:url url} url])
     :url-link-text-contents (fn [& raw-contents]
                               (combine-adjacent-strings raw-contents))
     :url-link-url-parts     (fn [& chars]
                               (string/join chars))
     :any-char (fn [char]
                 char)
     :any-chars              (fn [& chars]
                               (string/join chars))
     :reserved-chars          (fn [& char]
                                (string/join char))
     :non-reserved-chars      (fn [& chars]
                                (string/join chars))
     :any-non-pre-formatted-chars (fn [& chars]
                                    (string/join chars))
     :block-pre-formatted    (fn [code]
                               (let [split (string/split-lines code)]
                                 (cond
                                   (and (< 1 (count split))
                                        (not (string/blank? (first split))))
                                   [:block-pre-formatted
                                    (string/join "\n" (rest split))
                                    (first split)]

                                   (and (< 1 (count split))
                                        (string/blank? (first split)))
                                   [:block-pre-formatted (string/join "\n" (rest split))]

                                   :else [:block-pre-formatted (first split)])))
     :component              (fn [raw-content-string]
                               (into [:component raw-content-string] (rest (block-parser raw-content-string))))}
    tree))


(defn parse-to-ast
  "Converts a string of block syntax to an abstract syntax tree for Athens markup."
  [string]
  (transform-to-ast (block-parser string #_#_:trace true)))


(defparser block-parser-new
  "block = (heading |
            unordered-list |
            ordered-list |
            pre-code |
            inline-code |
            anchor |
            image |
            paragraph
           )*
   heading = #'[#]+' <space> #'[a-zA-Z0-9 ]+' <blankline>?
   <paragraph> = (inline-code /
                anchor /
                strong /
                emphasis /
                page-link /
                block-ref /
                hashtag /
                paragraph-text
               )+ <#'\n\n'?>
   <paragraph-text> = #'[^`#*\\[\\]\n{2}]+'
   strong = <'**'> strong-text <'**'> 
   <strong-text> = #'[^\\*\\*]+'
   emphasis =  <'*'> emphasis-text <'*'>
   <emphasis-text> = #'[^\\*]+'
   page-link = <'[['> page-link-text <']]'>
   <page-link-text> = ( #'[^\\[\\]{2}]+' | page-link )+
   block-ref = <'(('> block-ref-text <'))'>
   <block-ref-text> = #'[a-zA-Z0-9_\\-]+'
   hashtag = hashtag-bare | hashtag-delimited
   <hashtag-bare> = <'#'> #'[^\\ \\+\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\?\\\"\\;\\:\\]\\[]+'
   <hashtag-delimited> = <'#[['> page-link-text <']]'>
   unordered-list = unordered-item+ <blankline>
   unordered-item = <'- '> #'[a-zA-Z ]+' <newline>?
   ordered-list = ordered-item+ <blankline>
   ordered-item = <ol-item-token> #'[a-zA-Z0-9 ]+' <newline>?
   ol-item-token = #'[0-9]+\\. '
   inline-code = <'`'> #'[^`]+' <'`'>
   pre-code = <'```'> lang? <newline?>
              codetext
              <newline? '```'> <blankline?>
   lang = <' '?> #'[a-zA-Z]+'
   codetext = #'.+?(?=\\n?```)'
   anchor = auto-anchor | braced-anchor
   <auto-anchor> = <'<'> url <'>'>
   <braced-anchor> = <'['> text <']'> <'('> url <')'>
   <text> = #'[^\\]]+'
   <url> = #'[^>)]+'
   image = <'!'>
           <'['> alt <']'>
           <'('> path title? <')'>
   <alt> = #'[^\\]]+'
   <path> = #'[^) ]+'
   <title> = <spaces> #'[^)]+'
   spaces = space+
   space = ' '
   blankline = #'\n\n'
   newline = #'\r?\n'
   ")


(defn extract-paragraphs [coll]
  (let [result (mapcat #(if (= :paragraph (first %))
                          (rest %)
                          %)
                       coll)]
    (println :extract-paragraph (pr-str {:in coll :out result}))
    result))


(defn transform-block [& args]
  (->> args
       combine-adjacent-strings
       #_extract-paragraphs
       (into [:block])))


(defn transform-paragraph [& args]
  ;; NOTE muted paragraph
  (println :transform-paragraph (pr-str args))
  (into [:paragraph] (combine-adjacent-strings args)))


(defn transform-strong [str]
  [:bold str])


(defn transform-inline-code [str]
  [:inline-pre-formatted str])


(defn transform-pre-code
  ([codetext]
   [:block-pre-formatted codetext])
  ([lang codetext]
   [:block-pre-formatted codetext lang]))


(defn transform-lang [lang]
  lang)


(defn transform-codetext [str]
  str)


(defn transform-to-ast-new
  "Builds AST from `block-parser-new` parse tree."
  [tree]
  (insta/transform {:block       transform-block
                    :paragraph   transform-paragraph
                    :strong      transform-strong
                    :inline-code transform-inline-code
                    :pre-code    transform-pre-code
                    :lang        transform-lang
                    :codetext    transform-codetext}
                   tree))


(defn parse-to-ast-new
  "Converts a string of block syntax to an abstract syntax tree for Athens markup."
  [string]
  (transform-to-ast-new (let [result (insta/parse block-parser-new
                                                  string
                                                  ;; :trace true
                                                  )]
                          (if (insta/failure? result)
                            ^{:parse-error (insta/get-failure result)}
                            [:block string]

                            result))))
