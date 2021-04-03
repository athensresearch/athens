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
    (println "fenced:" (pr-str code-text) :-> (pr-str lang) (pr-str text))
    [:fenced-code-block {:lang lang}
     [:code-text text]]))


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


(defn block-parser->ast
  "Parse `in` string with `block-parser`."
  [in]
  (->> in
       (insta/parse block-parser)
       (insta/transform {:heading             transform-heading
                         :indented-code-block transform-indented-code-block
                         :fenced-code-block   transform-fenced-code-block
                         :paragraph-text      transform-paragraph-text
                         :block-quote         transform-block-quote})))
