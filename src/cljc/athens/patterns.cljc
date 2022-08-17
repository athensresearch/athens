(ns athens.patterns
  (:require
    [clojure.string :as string]))


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


(defn contains-unlinked?
  "Returns true if string contains title unlinked (e.g. not as #title or [[title]])."
  [title string]
  ;; This would be easier with a lookbehind: (re-pattern (str "(?i)(?!#)(?!\\[\\[)" string "(?!\\]\\])"))
  ;; But Safari doesn't support lookbehinds, so we're using a more complex trick
  ;; https://www.rexegg.com/regex-best-trick.html#pseudoregex.
  ;; The regex to find unlinked foo bar would be #foo bar|\[\[foo bar\]\]|(foo bar)
  ;; the general formula is NotThis|NotThat|GoAway|(WeWantThis)
  ;; The way it works is that the bad cases fall outside the capture group, so the capture
  ;; group will only contain the right thing.
  ;; We need to look inside the capture groups with this method though.
  (let [t (escape-str title)]
    (-> (re-pattern (str "(?i)" "#" t "|\\[\\[" t "\\]\\]|(" t ")"))
        (re-find string)
        second
        boolean)))


(defn re-case-insensitive
  "More options here https://clojuredocs.org/clojure.core/re-pattern"
  [query]
  (re-pattern (str "(?i)" (escape-str query))))


(defn split-on
  "Splits string whenever value is encountered. Returns all substrings including value."
  [s value]
  (loop [last-idx       0
         word-start-idx (string/index-of s value)
         ret            []]
    (if word-start-idx
      (let [word-end-idx' (+ word-start-idx (count value))]
        (recur word-end-idx'
               (string/index-of s value word-end-idx')
               (-> ret
                   (conj (subs s last-idx word-start-idx))
                   (conj (subs s word-start-idx word-end-idx')))))
      (conj ret (subs s last-idx)))))

