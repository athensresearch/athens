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
  "Use the different built-in icon types by appending one of `Outlined`, `Rounded`, `TwoTone`, or `Sharp` to the icon name.
  List of icons: [https://material-ui.com/components/material-icons/](https://material-ui.com/components/material-icons/)"
  [:div
   [:> mui-icons/Directions]
   [:> mui-icons/DirectionsOutlined]
   [:> mui-icons/DirectionsRounded]
   [:> mui-icons/DirectionsTwoTone]
   [:> mui-icons/DirectionsSharp]])


(defcard-rg Styling-icons
  "To use icons in lazy seqs (like `for` loops or `map`), or to apply other properties like styles, use `r/adapt-react-class`. See [https://github.com/reagent-project/reagent/issues/369](https://github.com/reagent-project/reagent/issues/369)."
  [:div
   [(r/adapt-react-class mui-icons/Face) {:style {:color (color :link-color)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:color (color :highlight-color)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:color (color :warning-color)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:color (color :confirmation-color)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:color (color :body-text-color)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:opacity (:opacity-lower OPACITIES)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:opacity (:opacity-low OPACITIES)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:opacity (:opacity-med OPACITIES)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:opacity (:opacity-high OPACITIES)}}]
   [(r/adapt-react-class mui-icons/Face) {:style {:color (color :header-text-color)}}]])
