(ns athens.devcards.icons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [reagent.core :as r]))


(defcard-rg Standard-Icons
  [:div
   [:> mui-icons/Face]
   [:> mui-icons/Settings]
   [:> mui-icons/Search]])


(defcard-rg Icon-Types
  "Use the different built-in icon types by appending one of `Outlined`, `Rounded`, `TwoTone`, or `Sharp` to the icon name."
  [:div
   [:> mui-icons/Directions]
   [:> mui-icons/DirectionsOutlined]
   [:> mui-icons/DirectionsRounded]
   [:> mui-icons/DirectionsTwoTone]
   [:> mui-icons/DirectionsSharp]])


(defcard-rg Styling-icons
  "Color, opacity, and other properties can be applied to icons by placing them in an element with those styles applied."
  [:div
   [:span {:style {:color (color :link-color)}} (r/create-element mui-icons/Face)]
   [:span {:style {:color (color :highlight-color)}} (r/create-element mui-icons/Face)]
   [:span {:style {:color (color :warning-color)}} (r/create-element mui-icons/Face)]
   [:span {:style {:color (color :confirmation-color)}} (r/create-element mui-icons/Face)]
   [:span {:style {:color (color :body-text-color)}} (r/create-element mui-icons/Face)]
   [:span {:style {:opacity (:opacity-10 OPACITIES)}} (r/create-element mui-icons/Face)]
   [:span {:style {:opacity (:opacity-25 OPACITIES)}} (r/create-element mui-icons/Face)]
   [:span {:style {:opacity (:opacity-50 OPACITIES)}} (r/create-element mui-icons/Face)]
   [:span {:style {:opacity (:opacity-75 OPACITIES)}} (r/create-element mui-icons/Face)]
   [:span {:style {:color (color :body-text-color)}} (r/create-element mui-icons/Face)]])
