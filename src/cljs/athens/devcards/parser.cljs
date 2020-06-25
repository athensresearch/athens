(ns athens.devcards.parser
  (:require
    [athens.devcards.blocks :refer [block-el]]
    #_[athens.parse-renderer :refer [parse-and-render]]
    #_[athens.parser :refer [parse-to-ast combine-adjacent-strings]]
    #_[cljs.test :refer [is testing are async]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [#_defcard defcard-rg #_deftest]]))


(def strings
  ["This is a plain block"
   "This is a [[page link]]"
   "This is a [[nested [[page link]]]]"
   "This is a #hashtag"
   "This is a #[[long hashtag]]"
   "This is a block ref: ((lxMRAb5Y5))"
   "This is a **very** important block"
   "This is an [external link](https://github.com/athensresearch/athens/)"
   "This is an image: ![alt](https://raw.githubusercontent.com/athensresearch/athens/master/doc/athens-puk-patrick-unsplash.jpg)"])


(defcard-rg Parse
  [:<>
   (map-indexed
     (fn [i x]
       ^{:key x} [block-el {:block/string x :block/uid i}])
     strings)])
