(ns athens.devcards.athena
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


(defn athena-prompt
  []
  [:button.primary (with-styles {:padding 0})
   [:div (with-styles {:display "inline-block" :padding "6px 0 6px 8px"})
    "🔍"]
   [:div (with-styles {:display "inline-block" :font-weight "normal" :padding "6px 16px" :color "#322F38"})
    "Find or Create a Page"]])


(defcard-rg Athena-Prompt
  [athena-prompt])