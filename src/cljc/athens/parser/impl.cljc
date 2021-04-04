(ns athens.parser.impl
  "3 pass parser implementation.

  1st pass: block structure
  2nd pass: inline structure
  3rd pass: raw urls"
  (:require
    [clojure.string :as string]
    #?(:cljs [instaparse.core :as insta :refer-macros [defparser]]
       :clj  [instaparse.core :as insta :refer [defparser]])))


(defparser block-parser
  "block = (thematic-break /
            heading /
            indented-code-block /
            fenced-code-block /
            block-quote /
            paragraph-text)*
   thematic-break = #'[*_-]{3}'
   heading = #'[#]+' <space> #'.+' <newline>*
   indented-code-block = (<'    '> code-text)+
   fenced-code-block = <'```'> #'(?s).+(?=(```|\\n))'+ <'```'>
   block-quote = (<#' {0,3}' #'> ?'> #'.*' <newline>?)+ <blankline>?

   paragraph-text = (<#' {0,3}'> #'.+' <newline>?)+ <blankline>?
   code-text = #'.+' <newline>?
   space = ' '
   blankline = #'\\n\\n'
   newline = #'\\n'")


(defparser inline-parser
  "inline = (backslash-escapes /
             code-span /
             strong-emphasis /
             emphasis /
             text-run)*

   backslash-escapes = #'\\\\\\p{Punct}'

   code-span = <backtick> #'(?s).*(?=`)' <backtick>

   strong-emphasis = (<'**'> #'.*(?=\\*\\*)' <'**'>) | (<'__'> #'.*(?=__)' <'__'>)
   emphasis = (<'*'> #'.*(?=\\*)' <'*'>) | (<'_'> #'.*(?=_)' <'_'>)

   text-run = #'[^\\*_`]*' (* anything but special chars *)

   backtick = #'(?<!`)`(?!`)'")


(defn- transform-heading
  [atx p-text]
  [:heading {:n (count atx)}
   [:paragraph-text p-text]])


(defn- transform-indented-code-block
  [& code-texts]
  [:indented-code-block
   [:code-text (->> code-texts
                    (map second)
                    (string/join "\n"))]])


(defn- transform-fenced-code-block
  [code-text]
  (let [lang (-> code-text
                 (string/split #"\n")
                 first)
        text (string/join
               "\n"
               (-> code-text
                   (string/split #"\n")
                   rest))]
    (if (string/blank? text)
      [:fenced-code-block {:lang ""} lang]
      [:fenced-code-block {:lang lang}
       [:code-text text]])))


(defn- transform-paragraph-text
  [& strings]
  [:paragraph-text (->> strings
                        (map string/trim)
                        (string/join "\n"))])


(declare block-parser->ast)


(defn- transform-block-quote
  [& strings]
  (into [:block-quote]
        (rest (block-parser->ast (string/join "\n" strings)))))


(def stage-1-transformations
  {:heading             transform-heading
   :indented-code-block transform-indented-code-block
   :fenced-code-block   transform-fenced-code-block
   :paragraph-text      transform-paragraph-text
   :block-quote         transform-block-quote})


(defn block-parser->ast
  "Stage 1. Parse `in` string with `block-parser`."
  [in]
  (->> in
       (insta/parse block-parser)
       (insta/transform stage-1-transformations)))


(declare inline-parser->ast)


(defn- transform-inline-formatting
  "Recursively descend parsing inline blocks"
  [container-type text]
  (println container-type (pr-str text))
  (apply conj
         [container-type]
         (inline-parser->ast text)))


(def stage-2-internal-transformations
  {:strong-emphasis #(transform-inline-formatting :strong-emphasis %)
   :emphasis        #(transform-inline-formatting :emphasis %)
   :inline          (fn [& contents]
                      ;; hide `[:inline ]`, leaving only contents
                      (apply conj [] contents))})


(defn inline-parser->ast
  [in]
  (let [parse-result (insta/parse inline-parser in)]
    (if-not (insta/failure? parse-result)
      (insta/transform stage-2-internal-transformations parse-result)
      [^{:parse-error (insta/get-failure parse-result)}
       [:text-run in]])))


(def stage-2-transformations
  {:paragraph-text inline-parser->ast})


(defn staged-parser->ast
  [in]
  (->> in
       (insta/parse block-parser)
       (insta/transform stage-1-transformations)
       (insta/transform stage-2-transformations)))
