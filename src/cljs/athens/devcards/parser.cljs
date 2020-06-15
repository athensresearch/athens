(ns athens.devcards.parser
  (:require
    [athens.db :as db]
    [athens.devcards.blocks :refer [block-el]]
    #_[athens.parse-renderer :refer [parse-and-render]]
    #_[athens.parser :refer [parse-to-ast combine-adjacent-strings]]
    [athens.style :refer [base-styles]]
    #_[cljs.test :refer [is testing are async]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [datascript.core :as d]
    [devcards.core :refer [#_defcard defcard-rg #_deftest]]
    [posh.reagent :refer [posh! transact!]]))


(defonce conn (d/create-conn db/schema))
(posh! conn)
;; TODO: parser transformation requires database to transclude block refs. Currently uses re-frame, not sure how we want to do it with posh.
(transact! conn [[:db/add 1 :block/uid "uid1" :block/string "block ref"]])


(def strings
  ["This is a plain block"
   "This is a [[page link]]"
   "This is a ((uid1))"                                     ;; TODO
   "This is a **very** important block"
   "This is an [external link](https://github.com/athensresearch/athens/)"
   "This is an image: ![alt](https://github.com/athensresearch/athens/blob/master/doc/athens-puk-patrick-unsplash.jpg"])


(defcard-rg Import-Styles
  [base-styles])


(defcard-rg Parse
  [:div
   (for [s strings]
     ^{:key s} [block-el {:block/string s}])])

