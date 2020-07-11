(ns athens.views.textinput
  (:require
    [athens.db]
    [athens.style :refer [cssv OPACITIES DEPTH-SHADOWS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def textinput-style
  {:min-height "32px"
   :color (cssv "body-text-color")
   :caret-color (cssv "link-color")
   :border-radius "4px"
   :background (cssv "background-minus-1")
   :padding "2px 8px"
   :flex-basis "100%"
   :border [["1px solid " (cssv "border-color")]]
   :transition-property "box-shadow, border, background"
   :transition-duration "0.1s"
   :transition-timing-function "ease"
   ::stylefy/manual [[:placeholder {:opacity (:opacity-med OPACITIES)}]
                     [:&:hover {:box-shadow (:4 DEPTH-SHADOWS)}]
                     [:&:focus :&:focus:hover {:outline "none"
                                               :border "1px solid"
                                               :box-shadow (:8 DEPTH-SHADOWS)}]]})


(def input-wrap
  {:position "relative"
   :display "inline-flex"
   :align-items "stretch"
   :justify-content "stretch"
   ::stylefy/manual [[:input {:padding-left "28px"}]]})


(def input-icon
  {:position "absolute"
   :top "50%"
   :display "flex"
   :pointer-events "none"
   :transform "translateY(-50%)"
   :left "6px"
   :color (cssv "body-text-color")
   :opacity (:opacity-med OPACITIES)
   ::stylefy/manual [[:svg {:font-size "20px"}]]})


;;; Components

(defn textinput
  [{:keys [type
           autoFocus
           defaultValue
           placeholder
           on-change
           value
           style
           icon]}]
  (if icon
    [:div (use-style input-wrap)
     [:input (use-style (merge textinput-style style) {:type type
                                                       :autoFocus autoFocus
                                                       :defaultValue defaultValue
                                                       :value value
                                                       :on-change on-change
                                                       :placeholder placeholder})]
     [:span (use-style input-icon) icon]]
    [:input (use-style (merge textinput-style style) {:type type
                                                      :autoFocus autoFocus
                                                      :defaultValue defaultValue
                                                      :value value
                                                      :on-change on-change
                                                      :placeholder placeholder})]))
