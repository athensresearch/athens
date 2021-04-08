(ns athens.parser.impl
  "3 pass parser implementation.

  1st pass: block structure
  2nd pass: inline structure
  3rd pass: raw urls"
  (:require
    [clojure.string :as string]
    [clojure.walk :as walk]
    #?(:cljs [instaparse.core :as insta :refer-macros [defparser]]
       :clj  [instaparse.core :as insta :refer [defparser]])))


(defparser block-parser
  "
block = (thematic-break /
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
  "(* inline spans parser, processes `:paragraph-text` from phase 1 *)

(* root of parse tree *)
inline = recur

(* `recur` so we can recursively parse inline formatting w/o bringing `:inline`. *)
<recur> = (backslash-escapes /
           text-run /
           code-span /
           strong-emphasis /
           emphasis /
           highlight /
           strikethrough /
           link /
           image /
           autolink /
           block-ref /
           page-link /
           hashtag /
           latex /
           special-char /
           newline)*

<backslash-escapes> = #'\\\\\\p{Punct}'

code-span = <backtick> #'(?s).*(?=`)' <backtick>

(* all inline-spans have `x` character (or pair) that is a boundary for this span *)
(* opening `x` has: *)
(* - `(?<!\\w)`: it can't be preceded by a word character *)
(* - `(?!\\s)`: it can't be followed by a white space *)
(* closing `x` has: *)
(* - `(?<!\\s)`: it can't be preceded by a white space *)
(* - `(?!\\w)`: it can't be followed by a word character *)
   
strong-emphasis = (<#'(?<!\\w)\\*\\*(?!\\s)'>
                   recur
                   <#'(?<!\\s)\\*\\*(?!\\w)'>)
                | (<#'(?<!\\w)__(?!\\s)'>
                   recur
                   <#'(?<!\\s)__(?!\\w)'>)

emphasis = (<#'(?<!\\w)\\*(?!\\s)'>
            recur
            <#'(?<!\\s)\\*(?!\\w)'>)
         | (<#'(?<!\\w)_(?!\\s)'>
            recur
            <#'(?<!\\s)_(?!\\w)'>)

highlight = <#'(?<!\\w)\\^\\^(?!\\s)'>
            recur
            <#'(?<!\\s)\\^\\^(?!\\w)'>

strikethrough = <#'(?<!\\w)~~(?!\\s)'>
                recur
                <#'(?<!\\s)~~(?!\\w)'>

link = md-link
image = <'!'> md-link

<md-link> = <#'(?<!\\w)\\[(?!\\s)'>
            link-text
            <#'(?<!\\s)\\]\\((?!\\s)'>
            link-target
            (<' '> link-title)?
            <#'(?<!\\s)\\)(?!\\w)'>

link-text = #'[^\\]]+'
link-target = #'[^\\s\\)]+'
link-title = <'\"'> #'[^\"]+' <'\"'>
           | <'\\''> #'[^\\']+' <'\\''>
           | <'('> #'[^\\)]+' <')'>

autolink = <#'(?<!\\w)<(?!\\s)'>
           #'[^>\\s]+'
           <#'(?<!\\s)>(?!\\w)'>

block-ref = <#'(?<!\\w)\\(\\((?!\\s)'>
            #'.+(?=\\)\\))'
            <#'(?<!\\s)\\)\\)(?!\\w)'>

page-link = <#'(?<!\\w)\\[\\[(?!\\s)'>
            #'[^\\]\\n]+'
            <#'(?<!\\s)\\]\\](?!\\w)'>

hashtag = <#'(?<!\\w)\\#\\[\\[(?!\\s)'>
          #'[^\\]\\n]+'
          <#'(?<!\\s)\\]\\](?!\\w)'>

latex = <#'(?<!\\w)\\$\\$(?!\\s)'>
        #'(?s).+(?=\\$\\$)'
        <#'(?<!\\s)\\$\\$(?!\\w)'>

(* characters with meaning (special chars) *)
(* every delimiter used as inline span boundary has to be added below *)

(* anything but special chars *)
text-run = #'[^\\*_`^~\\[!<\\(\\#\\$\\r\\n]*'

(* any special char *)
<special-char> = #'[\\*_`^~\\[!<\\(\\#\\$]'

<backtick> = #'(?<!`)`(?!`)'

newline = #'\\n'
")


(defn- transform-heading
  [atx p-text]
  [:heading {:n (count atx)}
   [:paragraph-text (string/trim p-text)]])


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
                        (map string/triml)
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


(defn- walker-hlb-candidate
  [candidate?]
  (fn [x]
    (if (and (vector? x)
             (= 2 (count x)))
      (let [[t s] x]
        (cond
          (and (= :text-run t) (string/ends-with? s "  "))
          (do
            (reset! candidate? true)
            x)

          (and (= :newline t) @candidate?)
          (do
            (reset! candidate? false)
            [:hard-line-break])

          (and (= :newline t) (not @candidate?))
          (do
            (reset! candidate? true)
            x)

          :else
          (do
            (reset! candidate? false)
            x)))
      x)))


(defn- inline-transform
  [& contents]
  (let [hlb-candidate? (atom false)]
    (apply conj []
           (map #(walk/postwalk (walker-hlb-candidate hlb-candidate?)
                                %)
                contents))))


(defn- link-transform
  [& link-parts]
  (let [{:keys [link-text link-target link-title]} (into {} link-parts)]
    [:link (cond-> {:text   link-text
                    :target link-target}
             link-title (assoc :title link-title))]))


(defn- image-transform
  [& link-parts]
  (let [{:keys [link-text link-target link-title]} (into {} link-parts)]
    [:image (cond-> {:alt link-text
                     :src link-target}
              link-title (assoc :title link-title))]))


(defn- autolink-transform
  [url]
  [:link {:text   url
          :target (if (string/includes? url "@")
                    (str "mailto:" url)
                    url)}])


(def stage-2-internal-transformations
  {:inline   inline-transform
   :link     link-transform
   :image    image-transform
   :autolink autolink-transform})


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
