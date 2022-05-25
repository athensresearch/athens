(ns athens.patterns
  (:require
    [clojure.string :as string]))


(defn unlinked
  "Exclude #title or [[title]].
   JavaScript negative lookarounds https://javascript.info/regexp-lookahead-lookbehind
   Lookarounds don't consume characters https://stackoverflow.com/questions/27179991/regex-matching-multiple-negative-lookahead "
  [string]
  (re-pattern (str "(?i)(?<!#)(?<!\\[\\[)" string "(?!\\]\\])")))


;; Matches a date with an ordinal number (roam format), considering the correct ordinal
;; suffix based on the ending number of the date
;; Regular expression, with test cases can be found here https://regex101.com/r/vOzOl9/1
;; Any update to this should be done after testing it using the previous regex101 link
(def roam-date #"((?<=\s1\d)th|(?<=(\s|[023456789])\d)((?<=1)st|(?<=2)nd|(?<=3)rd|(?<=[4567890])th)),(?=\s\d{4})")


(defn date
  [str]
  (re-find #"(?=\d{2}-\d{2}-\d{4}).*" str))


(defn date-block-string
  [str]
  (re-find #"\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\s\d{1,2}(?:st|nd|rd|th),\s\d{4}\b" str))


(defn replace-roam-date
  [string]
  (string/replace string roam-date ","))


;; https://stackoverflow.com/a/11672480
(def regex-esc-char-map
  (let [esc-chars "()*&^%$#![]"]
    (zipmap esc-chars
            (map #(str "\\" %) esc-chars))))


;; TODO: consider https://clojuredocs.org/clojure.string/re-quote-replacement if this causes problems.
(defn escape-str
  "Take a string and escape all regex special characters in it"
  [str]
  (string/escape str regex-esc-char-map))


(defn re-case-insensitive
  "More options here https://clojuredocs.org/clojure.core/re-pattern"
  [query]
  (re-pattern (str "(?i)" (escape-str query))))


(defn highlight
  [query]
  (re-pattern (str "(?i)" "((?<=" (escape-str query) ")|(?=" (escape-str query) "))")))
