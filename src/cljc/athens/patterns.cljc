(ns athens.patterns)

; match [[title]] or #title or #[[title]]
; provides groups useful for replacing
; e.g.: $1$3$4new-string$2$5
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


;; Positive Lookbehind: between 1 and 2 digits
;; One of an oridinal suffix, e.g. -st, -nd, -rd, -th, see https://en.wikipedia.org/wiki/Ordinal_indicator
;; Comma
;; Positive Lookahead: whitespace and 4 digits
(def roam-date #"(?<=\d{1,2})(st|nd|rd|th),(?=\s\d{4})")
