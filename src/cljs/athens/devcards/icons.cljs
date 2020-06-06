(ns athens.devcards.icons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [athens.style :refer [style-guide-css]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [reagent.core :as r]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Face
  [:div
   [:icon (r/create-element mui-icons/Face)]])


(defcard-rg Settings
  [:div
   [:icon (r/create-element mui-icons/Settings)]])