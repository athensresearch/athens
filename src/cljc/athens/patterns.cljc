(ns athens.patterns)


;; match [[title]] or #title or #[[title]]
;; provides groups useful for replacing
;; e.g.: $1$3$4new-string$2$5
(defn linked
  [string]
  (re-pattern (str "(\\[{2})" string "(\\]{2})"
                   "|" "(#)" string
                   "|" "(#\\[{2})" string "(\\]{2})")))


(defn unlinked
  "Exclude #title or [[title]].
   JavaScript negative lookarounds https://javascript.info/regexp-lookahead-lookbehind
   Lookarounds don't consume characters https://stackoverflow.com/questions/27179991/regex-matching-multiple-negative-lookahead "
  [string]
  (re-pattern (str "(?i)(?<!#)(?<!\\[\\[)" string "(?!\\]\\])")))


(defn update-links-in-block
  [s old-title new-title]
  (clojure.string/replace s
                          (linked old-title)
                          (str "$1$3$4" new-title "$2$5")))


;; Matches a date with an ordinal number (roam format), considering the correct ordinal
;; suffix based on the ending number of the date
;; Regular expression, with test cases can be found here https://regex101.com/r/vOzOl9/1
;; Any update to this should be done after testing it using the previous regex101 link
(def roam-date #"((?<=\s1\d)th|(?<=(\s|[023456789])\d)((?<=1)st|(?<=2)nd|(?<=3)rd|(?<=[4567890])th)),(?=\s\d{4})")

(def pad-single-digit-date-pattern #"(?<=\s)\d(?=,)")

(defn date
  [str]
  (re-find #"(?=\d{2}-\d{2}-\d{4}).*" str))

(defn date-block-string
  [str]
  (re-find #"\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\s\d{1,2}(?:st|nd|rd|th),\s\d{4}\b" str))


(defn replace-roam-date
  [string]
  (-> string
    (clojure.string/replace roam-date ",")
    ;; Adds a 0 for single-digit roam dates.
    (clojure.string/replace pad-single-digit-date-pattern #(str "0" %))))

