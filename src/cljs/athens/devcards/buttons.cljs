(ns athens.devcards.buttons
  (:require
    [athens.db]
    [athens.style :refer [style-guide-css]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Import-Styles
  [style-guide-css])


(defcard-rg Button
  [:button "Press Me"])


(defcard-rg Disabled-Button
  [:button {:disabled true} "Disabled"])


(defcard-rg Primary-Button
  [:button.primary "Press Me"])
