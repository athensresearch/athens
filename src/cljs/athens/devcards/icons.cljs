(ns athens.devcards.icons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]])
    [reagent.core :as r])


(defcard-rg Standard-Icons
  [:div
   [:span (r/create-element mui-icons/Face)]
   [:span (r/create-element mui-icons/Settings)]
   [:span (r/create-element mui-icons/Search)]])


(defcard-rg Icon-Styles
  "Use different icon styles by appending one of Outlined, Rounded, TwoTone, or Sharp."
  [:div
   [:span (r/create-element mui-icons/Directions)]
   [:span (r/create-element mui-icons/DirectionsOutlined)]
   [:span (r/create-element mui-icons/DirectionsRounded)]
   [:span (r/create-element mui-icons/DirectionsTwoTone)]
   [:span (r/create-element mui-icons/DirectionsSharp)]])
