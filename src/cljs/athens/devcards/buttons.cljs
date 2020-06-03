(ns athens.devcards.buttons
  (:require
    [athens.db]
    [athens.lib.dom.attributes :refer [with-styles]]
    [athens.style :refer [+flex-center +flex-space-between +flex-space-around +flex-column +flex-wrap
                          +text-shadow +box-shadow
                          +link-bg
                          style-guide-css COLORS OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.core :refer [css]]))



(defcard-rg Import-Styles
  [style-guide-css])




;;(defcard-rg Button-CSS
;;  [:style (css
;;            )])


(defcard-rg Button
  [:button "Press Me"])

(defcard-rg Disabled-Button
  [:button {:disabled true} "Disabled"])

(defcard-rg Primary-Button
  [:button.primary "Press Me"])
