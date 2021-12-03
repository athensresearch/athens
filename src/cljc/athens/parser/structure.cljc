(ns athens.parser.structure
  "Graph Structure Parser.

  Discover:
  * `[[...]]` page links
  * `#...` naked hashtags
  * `#[[...]] braced hashtags
  * `((...)) block refs
  * `{{type: ((...))}}` typed block refs
  * `{{type: ((...)), atr1: val1}}` typed block refs with optional attributes"
  (:require
    [athens.common.logging    :as log]
    [clojure.string :as string]
    #?(:cljs [instaparse.core :as insta :refer-macros [defparser]]
       :clj [instaparse.core  :as insta :refer [defparser]])))


(defparser structure-parser
  "text-or = ( page-link /
               braced-hashtag /
               naked-hashtag /
               block-ref /
               text-run )*
   (* below we need to list all significant groups in lookbehind + $ *)
   text-run = #'.+?(?=(\\[\\[|\\]\\]|#|\\(\\(|\\)\\)|$))\\n?'
   page-link = < double-square-open >
               ( text-till-double-square-close /
                 page-link /
                 braced-hashtag /
                 naked-hashtag )+
               < double-square-close >
   naked-hashtag = < hash > #'\\w+'
   braced-hashtag = < hash double-square-open >
                    ( text-till-double-square-close /
                      page-link /
                      braced-hashtag /
                      naked-hashtag)+
                    < double-square-close >
   block-ref = < double-paren-open >
               text-till-double-paren-close
               < double-paren-close >
   < text-till-double-square-close > = #'[^\\n$]*?(?=(\\]\\]|\\[\\[|#))'
   < text-till-double-paren-close > = #'[^\\s]*?(?=(\\)\\)))'
   hash = '#'
   double-square-open = '[['
   double-square-close = ']]'
   double-paren-open = '(('
   double-paren-close = '))'")


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


(defn page-link-transform
  [& contents]
  (apply conj [:page-link {:from (str "[[" (apply string-representation contents) "]]")}]
         contents))


(defn naked-hashtag-transform
  [& contents]
  (apply conj [:naked-hashtag {:from (str "#" (apply string-representation contents))}]
         contents))


(defn braced-hashtag-transform
  [& contents]
  (apply conj [:braced-hashtag {:from (str "#[[" (apply string-representation contents) "]]")}]
         contents))


(def transformations
  {:text-or        text-or-transform
   :page-link      page-link-transform
   :naked-hashtag  naked-hashtag-transform
   :braced-hashtag braced-hashtag-transform})


(defn structure-parser->ast
  [in]
  (let [parse-result (insta/parse structure-parser in)]
    (if-not (insta/failure? parse-result)
      (insta/transform transformations parse-result)
      (insta/get-failure parse-result))))

