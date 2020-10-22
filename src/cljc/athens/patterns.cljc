(ns athens.patterns)

; match [[title]] or #title or #[[title]]
; provides groups useful for replacing
; e.g.: $1$3$4new-string$2$5
(defn linked
  [string]
  (re-pattern (str "(\\[{2})" string "(\\]{2})"
                   "|" "(#)" string
                   "|" "(#\\[{2})" string "(\\]{2})")))

; also excludes [title] :(
(defn unlinked
  [string]
  (re-pattern (str "(?i)[^\\[|#]" string)))
