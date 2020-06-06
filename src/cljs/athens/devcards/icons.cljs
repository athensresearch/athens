(ns athens.devcards.icons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [athens.lib.dom.attributes :refer [with-styles]]
    [athens.style :refer [COLORS OPACITIES]]
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
   [:span (with-styles {:color (:link-color COLORS)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:color (:highlight-color COLORS)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:color (:warning-color COLORS)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:color (:confirmation-color COLORS)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:color (:body-text-color COLORS)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:opacity (nth OPACITIES 0)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:opacity (nth OPACITIES 1)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:opacity (nth OPACITIES 2)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:opacity (nth OPACITIES 3)}) (r/create-element mui-icons/Face)]
   [:span (with-styles {:color (:body-text-color COLORS)}) (r/create-element mui-icons/Face)]])
