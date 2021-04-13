(ns athens.views.alerts
  (:require
    ["@material-ui/icons/Announcement" :default Announcement]
    ["@material-ui/icons/Check" :default Check]
    ["@material-ui/icons/Close" :default Close]
    [athens.style :refer [color]]
    [athens.views.buttons :refer [button]]
    [cljsjs.react]
    [cljsjs.react.dom]
    ;;[garden.selectors :as selectors]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles

(def alert-container-style
  {:background-color (color :highlight-color :opacity-low)
   :display          "flex"
   :align-items      "center"
   :justify-content  "center"
   :max-width        "500px"
   :min-width        "300px"
   :padding          "10px 5px"
   :color            (color :body-text-color)
   :border-radius "5px"})


;;; Components


(defn alert-component
  "A pop-up, only used for merging pages right now. Can abstract to generic alerts and messages as needed."
  [message confirm-fn close-fn]
  [:div (use-style alert-container-style)
   [button {:style {:color (color :highlight-color)}}
    [(r/adapt-react-class Announcement)]]
   [:span message]
   [button {:on-click confirm-fn :style {:color (color :header-text-color)}}
    [(r/adapt-react-class Check)]]
   [button {:on-click close-fn :style {:color (color :header-text-color)}}
    [(r/adapt-react-class Close)]]])
