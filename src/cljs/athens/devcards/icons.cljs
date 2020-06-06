(ns athens.devcards.icons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [cljsjs.react]
    [cljsjs.react.dom]
    [reagent.core :as r]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Standard-Icons
  [:div
   [:icon (r/create-element mui-icons/Face)]
   [:icon (r/create-element mui-icons/Settings)]
   [:icon (r/create-element mui-icons/Search)]])


(defcard-rg Icon-Styles
  "Use different icon styles by appending one of Outlined, Rounded, TwoTone, or Sharp."
  [:div
   [:icon (r/create-element mui-icons/Directions)]
   [:icon (r/create-element mui-icons/DirectionsOutlined)]
   [:icon (r/create-element mui-icons/DirectionsRounded)]
   [:icon (r/create-element mui-icons/DirectionsTwoTone)]
   [:icon (r/create-element mui-icons/DirectionsSharp)]])
