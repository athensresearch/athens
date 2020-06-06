(ns athens.devcards.icons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [athens.style :refer [style-guide-css]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [reagent.core :as r]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Import-Styles
  [style-guide-css])


(defcard-rg Icon
  [:icon (r/as-element mui-icons/Face)])
  