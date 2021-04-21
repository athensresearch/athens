(ns athens.devcards.parser
  (:require
    #_[athens.parse-renderer :refer [parse-and-render]]
    #_[athens.parser :refer [parse-to-ast combine-adjacent-strings]]
    [athens.views.core :refer [block-el]]
    #_[cljs.test :refer [is testing are async]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [#_defcard defcard-rg #_deftest]]))


;; not transacting for some reason
;;(transact! db/dsdb [[:db/add 5001 :block/uid "asd123" :block/string "block ref"]])


(def strings
  ["This is a plain block"
   "This is a [[page link]]"
   "This is a [[nested [[page link]]]]"
   "This is a #hashtag"
   "This is a #[[long hashtag]]"
   "This is a block ref: ((lxMRAb5Y5))"                                     ;; TODO
   "This is a **very** important block"
   "This is an [external link](https://github.com/athensresearch/athens/)"
   "This is an image: ![alt](https://raw.githubusercontent.com/athensresearch/athens/master/doc/athens-puk-patrick-unsplash.jpg)"
   "This is a piece of `preformatted code` or ```monospace text```"])


(defcard-rg Parse
  [:<>
   (for [s strings]
     ^{:key s} [block-el {:block/string s}])])
