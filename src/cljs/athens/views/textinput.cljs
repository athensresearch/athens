(ns athens.views.textinput
  (:require
    [athens.db]
    [athens.style :refer [color OPACITIES DEPTH-SHADOWS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def textinput-style
  {:min-height "2rem"
   :color (color :body-text-color)
   :caret-color (color :link-color)
   :border-radius "0.25rem"
   :background (color :background-minus-1)
   :padding "0.125rem 0.5rem"
   :border [["1px solid " (color :border-color)]]
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
   ::stylefy/manual [[:input {:padding-left "1.75rem"}]]})


(def input-icon
  {:position "absolute"
   :top "50%"
   :display "flex"
   :pointer-events "none"
   :transform "translateY(-50%)"
   :left "0.375rem"
   :color (color :body-text-color)
   :opacity (:opacity-med OPACITIES)
   ::stylefy/manual [[:svg {:font-size "20px"}]]})


;;; Components


(defn textinput
  [{:keys [style icon class] :as props}]
  (let [props- (dissoc props :style :icon :class)]
    (if icon
      [:div (use-style input-wrap)
       [:input (use-style (merge textinput-style style)
                          (merge props- {:class (vec (flatten class))}))]
       [:span (use-style input-icon) icon]]
      [:input (use-style (merge textinput-style style)
                         (merge props- {:class (vec (flatten class))}))])))
