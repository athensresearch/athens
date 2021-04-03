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
   heading = #'[#]+' <space> paragraph-text
   indented-code-block = (<'    '> code-text)+
   fenced-code-block = <'```'> #'(?s).+(?=(```|\\n))'+ <'```'>
   block-quote = (<#' {0,3}' #'> ?'> #'.+' <newline>?) block-quote-lazy-cont* <blankline>?
   block-quote-lazy-cont = (<#' {0,3}' #'>? ?'> #'.+' <newline>?)

   paragraph-text = (<#' {0,3}'> #'.+' <newline>?)+ <blankline>?
   code-text = #'.+' <newline>?
   space = ' '
   blankline = #'\\n\\n'
   newline = #'\\n'")


(defn- transform-heading
  [atx p-text]
  [:heading {:n (count atx)}
   p-text])


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


(defn- transform-block-quote
  [& strings]
  [:block-quote
   [:paragraph-text (->> strings
                         (map string/trim)
                         (string/join "\n"))]])


(defn- transform-block-quote-lazy-cont
  [& strings]
  (->> strings
       (map string/trim)
       (string/join "\n")))


(defn block-parser->ast
  "Parse `in` string with `block-parser`."
  [in]
  (->> in
       block-parser
       (insta/transform {:heading             transform-heading
                         :indented-code-block transform-indented-code-block
                         :fenced-code-block   transform-fenced-code-block
                         :paragraph-text      transform-paragraph-text
                         :block-quote         transform-block-quote
                         :block-quote-lazy-cont transform-block-quote-lazy-cont})))
