(ns athens.patterns
  (:require
    [clojure.string :as string]))


(defn unlinked
  "Exclude #title or [[title]].
   JavaScript negative lookarounds https://javascript.info/regexp-lookahead-lookbehind
   Lookarounds don't consume characters https://stackoverflow.com/questions/27179991/regex-matching-multiple-negative-lookahead "
  [string]
  (re-pattern (str "(?i)(?<!#)(?<!\\[\\[)" string "(?!\\]\\])")))


(defn date
  [str]
  (re-find #"(?=\d{2}-\d{2}-\d{4}).*" str))


(defn date-block-string
  [str]
  (re-find #"\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\s\d{1,2}(?:st|nd|rd|th),\s\d{4}\b" str))


(def ordinal->number
  {"1st"  "1"
   "2nd"  "2"
   "3rd"  "3"
   "4th"  "4"
   "5th"  "5"
   "6th"  "6"
   "7th"  "7"
   "8th"  "8"
   "9th"  "9"
   "10th" "10"
   "11th" "11"
   "12th" "12"
   "13th" "13"
   "14th" "14"
   "15th" "15"
   "16th" "16"
   "17th" "17"
   "18th" "18"
   "19th" "19"
   "20th" "20"
   "21st" "21"
   "22nd" "22"
   "23rd" "23"
   "24th" "24"
   "25th" "25"
   "26th" "26"
   "27th" "27"
   "28th" "28"
   "29th" "29"
   "30th" "30"
   "31st" "31"})


(defn replace-roam-date
  [string]
  (string/replace string #"\d?\d(?:st|nd|rd|th)" #(or (ordinal->number %) %)))


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
