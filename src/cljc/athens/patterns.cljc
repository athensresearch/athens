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


(defn update-links-in-block
  [s old-title new-title]
  (clojure.string/replace s
                          (linked old-title)
                          (str "$1$3$4" new-title "$2$5")))

