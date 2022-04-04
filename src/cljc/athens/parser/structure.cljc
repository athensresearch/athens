(ns athens.parser.structure
  "Graph Structure Parser.

  Discovers following structure references:
  * [x] `[[...]]` page links
  * [x] `#...` naked hashtags
  * [x] `#[[...]] braced hashtags
  * [x] `((...)) block refs
  * [x] Ignore structure when in code blocks
  * [x] `{{type: ((...))}}` typed block refs
  * [ ] `{{type: ((...)), atr1: val1}}` typed block refs with optional attributes"
  (:require
    [clojure.string            :as string]
    #?(:cljs [instaparse.core :as insta :refer-macros [defparser]]
       :clj [instaparse.core  :as insta :refer [defparser]])))


(defparser structure-parser
  "text-or = ( code-block /
               code-span /
               page-link /
               braced-hashtag /
               naked-hashtag /
               block-ref /
               typed-block-ref /
               text-run )*
   (* below we need to list all significant groups in lookbehind + $ *)
   text-run = #'.+?(?=(\\[\\[|\\]\\]|#|\\(\\(|\\)\\)|$|\\`))\\n?'
   code-span = < '`' > text-or < '`' >
   code-block = < '```' >
                ( text-or | '\\n' )+
                < '```' >
   page-link = < double-square-open >
               ( text-till-double-square-close /
                 page-link /
                 braced-hashtag /
                 naked-hashtag )+
               < double-square-close >
   naked-hashtag = < hash > #'[^\\ \\+\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\?\\\"\\;\\:\\]\\[]+'
   braced-hashtag = < hash double-square-open >
                    ( text-till-double-square-close /
                      page-link /
                      braced-hashtag /
                      naked-hashtag)+
                    < double-square-close >
   block-ref = < double-paren-open >
               text-till-double-paren-close
               < double-paren-close >
   < text-till-double-square-close > = #'[^\\n$\\[\\]\\#]+?(?=(\\]\\]|\\[\\[|#))'
   < text-till-double-paren-close > = #'[^\\s]+?(?=(\\)\\)))'
   typed-block-ref = < double-curly-open >
                     ref-type < #':\\s*' > block-ref
                     < double-curly-close >
   ref-type = #'[^:]+'
   hash = '#'
   double-square-open = '[['
   double-square-close = ']]'
   double-paren-open = '(('
   double-paren-close = '))'
   double-curly-open = '{{'
   double-curly-close = '}}'")


(defn- string-representation
  [& contents]
  (->> contents
       (map (fn [content]
              (if (string? content)
                content
                (-> content
                    second
                    :from))))
       string/join))


(defn text-or-transform
  [& contents]
  (apply conj [:paragraph] contents))


(defn code-span-transform
  [& _contents]
  ;; simply ignore code contents for structure
  [:code-span])


(defn code-block-transform
  [& _contents]
  ;; simply ignore code contents for structure
  [:code-block])


(defn page-link-transform
  [& contents]
  (let [string-repr (apply string-representation contents)]
    (apply conj [:page-link {:from   (str "[[" string-repr "]]")
                             :string string-repr}]
           contents)))


(defn naked-hashtag-transform
  [& contents]
  (let [string-repr (apply string-representation contents)]
    (apply conj [:hashtag {:from   (str "#" string-repr)
                           :string string-repr}]
           contents)))


(defn braced-hashtag-transform
  [& contents]
  (let [string-repr (apply string-representation contents)]
    (apply conj [:hashtag {:from   (str "#[[" string-repr "]]")
                           :string string-repr}]
           contents)))


(defn block-ref-transform
  [block-ref-str]
  [:block-ref {:from   (str "((" block-ref-str "))")
               :string block-ref-str}
   block-ref-str])


(defn typed-block-ref-transform
  [ref-type-el block-ref-el]
  (let [ref-type       (second ref-type-el)
        block-ref-from (-> block-ref-el second :from)
        string-repr    (str ref-type ": " block-ref-from)]
    [:typed-block-ref {:from   (str "{{" string-repr "}}")
                       :string string-repr}
     ref-type-el
     block-ref-el]))


(def transformations
  {:text-or         text-or-transform
   :code-span       code-span-transform
   :code-block      code-block-transform
   :page-link       page-link-transform
   :naked-hashtag   naked-hashtag-transform
   :braced-hashtag  braced-hashtag-transform
   :block-ref       block-ref-transform
   :typed-block-ref typed-block-ref-transform})


(defn structure-parser->ast
  [in]
  (let [parse-result (insta/parse structure-parser in :total true)]
    (insta/transform transformations parse-result)))

