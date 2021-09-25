(ns athens.views.alerts
  (:require
    ["/components/Button/Button" :refer [Button]]
    ["@material-ui/icons/Announcement" :default Announcement]
    ["@material-ui/icons/Check" :default Check]
    ["@material-ui/icons/Close" :default Close]
    [athens.style :refer [color]]
    ;; [garden.selectors :as selectors]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;; Styles

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


;; Components
