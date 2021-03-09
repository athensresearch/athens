(ns athens.devcards.icons
  (:require
    ["@material-ui/icons/Directions" :as Directions]
    ["@material-ui/icons/DirectionsOutlined" :as DirectionsOutlined]
    ["@material-ui/icons/DirectionsRounded" :as DirectionsRounded]
    ["@material-ui/icons/DirectionsSharp" :as DirectionsSharp]
    ["@material-ui/icons/DirectionsTwoTone" :as DirectionsTwoTone]
    ["@material-ui/icons/Face" :as Face]
    ["@material-ui/icons/Search" :as Search]
    ["@material-ui/icons/Settings" :as Settings]
    [athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [reagent.core :as r]))


(defcard-rg Standard-Icons
  [:div
   [:> Face]
   [:> Settings]
   [:> Search]])


(defcard-rg Icon-Types
  "Use the different built-in icon types by appending one of `Outlined`, `Rounded`, `TwoTone`, or `Sharp` to the icon name.
  List of icons: [https://material-ui.com/components/material-icons/](https://material-ui.com/components/material-icons/)"
  [:div
   [:> Directions]
   [:> DirectionsOutlined]
   [:> DirectionsRounded]
   [:> DirectionsTwoTone]
   [:> DirectionsSharp]])


(defcard-rg Styling-icons
  "To use icons in lazy seqs (like `for` loops or `map`), or to apply other properties like styles, use `r/adapt-react-class`. See [https://github.com/reagent-project/reagent/issues/369](https://github.com/reagent-project/reagent/issues/369)."
  [:div
   [(r/adapt-react-class Face) {:style {:color (color :link-color)}}]
   [(r/adapt-react-class Face) {:style {:color (color :highlight-color)}}]
   [(r/adapt-react-class Face) {:style {:color (color :warning-color)}}]
   [(r/adapt-react-class Face) {:style {:color (color :confirmation-color)}}]
   [(r/adapt-react-class Face) {:style {:color (color :body-text-color)}}]
   [(r/adapt-react-class Face) {:style {:opacity (:opacity-lower OPACITIES)}}]
   [(r/adapt-react-class Face) {:style {:opacity (:opacity-low OPACITIES)}}]
   [(r/adapt-react-class Face) {:style {:opacity (:opacity-med OPACITIES)}}]
   [(r/adapt-react-class Face) {:style {:opacity (:opacity-high OPACITIES)}}]
   [(r/adapt-react-class Face) {:style {:color (color :header-text-color)}}]])
