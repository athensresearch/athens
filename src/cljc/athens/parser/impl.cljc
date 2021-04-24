(ns athens.parser.impl
  "3 pass parser implementation.

  1st pass: block structure
  2nd pass: inline structure
  3rd pass: raw urls"
  (:require
    #?(:cljs [athens.config :as config])
    [clojure.string :as string]
    [clojure.walk :as walk]
    #?(:cljs [instaparse.core :as insta :refer-macros [defparser]]
       :clj [instaparse.core :as insta :refer [defparser]]))
  #?(:clj
     (:import
       (java.time
         LocalDateTime))))


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
fenced-code-block = <'```'> #'(?s)(.+(?=(```|\\n))|\\n)+' <'```'>
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
           component /
           latex /
           special-char /
           newline)*

<backslash-escapes> = #'\\\\\\p{Punct}'

(* all inline-spans have `x` character (or pair) that is a boundary for this span *)
(* opening `x` has: *)
(* - `(?<!\\w)`: it can't be preceded by a word character, when it can don't include it *)
(* - `(?!\\s)`: it can't be followed by a white space *)
(* closing `x` has: *)
(* - `(?<!\\s)`: it can't be preceded by a white space *)
(* - `(?!\\w)`: it can't be followed by a word character, when it can don't include it *)
   
code-span = <#'(?<!\\w)`'>
            #'(?s)([^`]|(?<=\\s)`(?=\\s))+'
            <#'`(?!\\w)'>

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

link-text = #'([^\\]]|\\\\\\])*?(?=\\]\\()'
link-target = ( #'[^\\s\\(\\)]+' | '(' #'[^\\s\\)]*' ')' | '\\\\' ( '(' | ')' ) | #'\\s(?![\"\\'\\(])' )+
link-title = <'\"'> #'[^\"]+' <'\"'>
           | <'\\''> #'[^\\']+' <'\\''>
           | <'('> #'[^\\)]+' <')'>

autolink = <#'(?<!\\w)<(?!\\s)'>
           #'[^>\\s]+'
           <#'(?<!\\s)>(?!\\w)'>

block-ref = <#'\\(\\((?!\\s)'>
            #'.+?(?=\\)\\))'
            <#'(?<!\\s)\\)\\)'>

page-link = <#'(?<!\\w)\\[\\[(?!\\s)'>
            (#'[^\\[\\]\\n]+' | page-link)+
            <#'(?<!\\s)\\]\\](?!\\w)'>

hashtag = <#'(?<!\\w)\\#\\[\\[(?!\\s)'>
          (#'[^\\[\\]\\n]+' | page-link)+
          <#'(?<!\\s)\\]\\](?!\\w)'>
        | <#'(?<!\\w)\\#(?!\\s)'>
          #'[^\\ \\+\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\?\\\"\\;\\:\\]\\[]+(?!\\w)'

component = <#'(?<!\\w)\\{\\{(?!\\s)'>
            (page-link / block-ref / #'.+(?=\\}\\})')
            <#'(?<!\\s)\\}\\}(?!\\w)'>

latex = <#'(?<!\\w)\\$\\$(?!\\s)'>
        #'(?s).+?(?=\\$\\$)'
        <#'(?<!\\s)\\$\\$(?!\\w)'>

(* characters with meaning (special chars) *)
(* every delimiter used as inline span boundary has to be added below *)

(* anything but special chars *)
text-run = #'(?:[^\\*_`\\^~\\[!<\\(\\#\\$\\{\\r\\n]|(?<=\\S)[`!\\#\\$\\{])+'

(* any special char *)
<special-char> = #'(?<!\\w)[\\*_`^~\\[!<\\(\\#\\$\\{]'

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
  (let [hlb-candidate? (atom false)
        result (apply conj [:paragraph]
                      (->> contents
                           (map #(walk/postwalk (walker-hlb-candidate hlb-candidate?) %))
                           (reduce (fn [acc el]
                                     (let [last-el (last acc)
                                           new-val (cond
                                                     (string? el)
                                                     el

                                                     (and (vector? el)
                                                          (< 1 (count el))
                                                          (= :text-run (first el)))
                                                     (string/join (rest el))

                                                     :else
                                                     el)]
                                       (if (and (string? new-val)
                                                (or (nil? last-el)
                                                    (= :text-run (first last-el))))
                                         (conj (if (nil? last-el)
                                                 acc
                                                 (pop acc))
                                               [:text-run (string/join [(or (second last-el) "") new-val])])
                                         (conj acc el))))
                                   [])))]
    result))


(defn- link-parts->map
  [link-parts]
  (let [safe-parts (->> link-parts
                        (remove #(= :link-target (first %)))
                        (into {}))
        link-target-rest (->> link-parts
                              (filter #(= :link-target (first %)))
                              first
                              rest)
        link-target (if (= 1 (count link-target-rest))
                      (first link-target-rest)
                      (string/join link-target-rest))]
    (assoc safe-parts :link-target link-target)))


(defn- link-transform
  [& link-parts]
  (let [{:keys [link-text link-target link-title]} (link-parts->map link-parts)]
    [:link (cond-> {:text   link-text
                    :target link-target}
             link-title (assoc :title link-title))]))


(defn- image-transform
  [& link-parts]
  (let [{:keys [link-text link-target link-title]} (link-parts->map link-parts)]
    [:url-image (cond-> {:alt link-text
                         :src link-target}
                  link-title (assoc :title link-title))]))


(defn- autolink-transform
  [url]
  [:autolink {:text   url
              :target (if (string/includes? url "@")
                        (str "mailto:" url)
                        url)}])


(defn- component-transform
  [contents]
  [:component (if (vector? contents)
                (let [[tag text] contents]
                  (cond
                    (= :page-link tag)
                    (str "[[" text "]]")

                    (= :block-ref tag)
                    (str "((" text "))")

                    :else
                    text))
                contents)
   contents])


(def stage-2-internal-transformations
  {:inline    inline-transform
   :link      link-transform
   :image     image-transform
   :autolink  autolink-transform
   :component component-transform})


(defn inline-parser->ast
  [in]
  (let [parse-result (insta/parse inline-parser in)]
    (if-not (insta/failure? parse-result)
      (insta/transform stage-2-internal-transformations parse-result)
      ^{:parse-error (insta/get-failure parse-result)}
      [:paragraph
       [:text-run in]])))


(def stage-2-transformations
  {:paragraph-text inline-parser->ast})


(def uri-pattern
  #"(?i)(https?|ftp)://[^\s/\$\.\?\#].[^\s]*")


(defn- append-link
  ([acc before uri] (append-link acc before uri nil))
  ([acc before uri after]
   (cond-> acc
     (and (seq before)
          (pos? (count before)))
     (conj before)

     :true
     (conj [:link {:text   uri
                   :target uri}])

     (and (seq after)
          (pos? (count after)))
     (conj after))))


(defn- text-run-transform
  [text-run]
  (let [matches (re-seq uri-pattern text-run)]
    (if (seq matches)
      (into [:span]
            (loop [t   text-run
                   m   matches
                   acc []]
              (let [uri       (ffirst m)
                    uri-index (string/index-of t uri)
                    before    (subs t 0 uri-index)
                    after     (subs t
                                    (+ uri-index (count uri))
                                    (count t))]
                (if (seq (rest m))
                  (recur after
                         (rest m)
                         (append-link acc before uri))
                  (append-link acc before uri after)))))
      text-run)))


(def stage-3-transformations
  {:text-run        text-run-transform
   ;; TODO move below transformations to rendering when we're sure to use this parser
   :strong-emphasis (fn [& contents]
                      (apply conj [:bold] contents))
   :emphasis        (fn [& contents]
                      (apply conj [:italic] contents))
   :hard-line-break (fn []
                      [:br])
   :block-quote     (fn [& contents]
                      (apply conj [:blockquote] contents))
   :code-span       (fn [text]
                      [:inline-pre-formatted text])})


(defn- timed
  [name fn-to-time]
  (fn [arg]
    #?(:cljs
       (let [t-0 (js/performance.now)
             result (fn-to-time arg)
             t-1 (js/performance.now)]
         (when config/measure-parser?
           (js/console.log name ", time:" (- t-1 t-0)))
         result)
       :clj
       (let [t-0 (.getNano (LocalDateTime/now))
             result (fn-to-time arg)
             t-1 (.getNano (LocalDateTime/now))]
         (println name ", time:" (/ (- t-1 t-0)
                                    1000000) "milliseconds")
         result))))


(defn staged-parser->ast
  [in]
  (->> in
       ((timed :block #(insta/parse block-parser %)))
       ((timed :stage-1 #(insta/transform stage-1-transformations %)))
       ((timed :stage-2 #(insta/transform stage-2-transformations %)))
       ((timed :stage-3 #(insta/transform stage-3-transformations %)))))
