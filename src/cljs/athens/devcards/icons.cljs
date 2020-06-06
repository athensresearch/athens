(ns athens.devcards.icons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [reagent.core :as r]))


(defcard-rg Standard-Icons
  [:div
   [:> mui-icons/Face]
   [:> mui-icons/Settings]
   [:> mui-icons/Search]])


(defcard-rg Icon-Styles
  "Use different icon styles by appending one of Outlined, Rounded, TwoTone, or Sharp."
  [:div
   [:> mui-icons/Directions]
   [:> mui-icons/DirectionsOutlined]
   [:> mui-icons/DirectionsRounded]
   [:> mui-icons/DirectionsTwoTone]
   [:> mui-icons/DirectionsSharp]])
