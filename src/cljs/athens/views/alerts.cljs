(ns athens.views.alerts
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES ZINDICES]]
    [athens.views.buttons :refer [button]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


;;; Styles

(def container-style
  {})


;;; Components


(defn alert
  [message]
  [:div (use-style {:background-color (color :confirmation-color :opacity-low)})
   [(r/adapt-react-class mui-icons/Check) {:style {:color (color :confirmation-color)}}]
   [:span message]])
   ;;[:span "Undo"]
   ;;[:hr]
   ;;[:span "Dismiss"]])

