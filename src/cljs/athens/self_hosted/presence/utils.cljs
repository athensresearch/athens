(ns athens.self-hosted.presence.utils)

;; colors do not persist across sessions
;; colors are not shared between users

(def PALETTE
  ["#DDA74C"
   "#C45042"
   "#611A58"
   "#21A469"
   "#009FB8"
   "#0062BE"])


(def NAMES
  ["Zeus"
   "Poseidon"
   "Hera"
   "Demeter"
   "Athena"
   "Apollo"])
;;"Artemis"
;;"Ares"
;;"Aphrodite"
;;"Hephaestus"
;;"Hermes"
;;"Hestia"
;;"Dionysus"
;;"Hades"])


(def BLOCK-UIDS
  ["" ;; on page, not block
   "6b8c28b09" ;; poseidon
   "ed9f20b26" ;; way down
   "8b66a56f3" ;; different page
   "4135c0ecb" ;; different page on a block
   ""])


(comment
  (def MEMBERS
    (mapv
      (fn [username color uid]
        {:username username :color color :block/uid uid})
      NAMES PALETTE BLOCK-UIDS))

  ;; Possible default values to put in app-db
  {:users {"Zeus"     {:username "Zeus", :color "#DDA74C", :block/uid "6aecd4172"},
           "Poseidon" {:username "Poseidon", :color "#C45042", :block/uid "87ba25e9d"},
           "Hera"     {:username "Hera", :color "#611A58", :block/uid "7c2b4b308"},
           "Demeter"  {:username "Demeter", :color "#21A469", :block/uid "e0d06f525"},
           "Athena"   {:username "Athena", :color "#009FB8", :block/uid "4135c0ecb"},
           "Apollo"   {:username "Apollo", :color "#0062BE", :block/uid "f24df1ea6"}}})