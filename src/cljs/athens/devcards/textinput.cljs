(ns athens.devcards.textinput
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [athens.style :refer [color OPACITIES DEPTH-SHADOWS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def textinput-style
  {:min-height "32px"
   :color (color :body-text-color)
   :caret-color (color :link-color)
   :border-radius "4px"
   :background (color :panel-color)
   :padding "2px 8px"
   :flex-basis "100%"
   :border "1px solid #C9C7C7" ;; TODO: this should be defined by a system color
   :transition-property "box-shadow, border, background"
   :transition-duration "0.1s"
   :transition-timing-property "ease"
   ::stylefy/manual [[:placeholder {:opacity (:opacity-med OPACITIES)}]
                     [:&:hover {
                                :box-shadow (:4 DEPTH-SHADOWS)}]
                     [:&:focus :&:focus:hover {:outline "none"
                                :border "1px solid"
                                :box-shadow (:8 DEPTH-SHADOWS)}]]})

(def input-wrap
  {:position "relative"
   ::stylefy/manual [[:input {:padding-left "28px"}]]})

(def input-icon
  {:position "absolute"
   :top "50%"
   :display "flex"
   :pointer-events "none"
   :transform "translateY(-50%)"
   :left "6px"
   :color (color :body-text-color)
   :opacity (:opacity-med OPACITIES)
   ::stylefy/manual [[:svg {:font-size "20px"}]]})


;;; Components

(defn textinput
  [{:keys [type defaultValue placeholder style icon]}]
  (if icon
    [:div (use-style input-wrap)
     [:input (use-style (merge textinput-style style) {:type type :defaultValue defaultValue :placeholder placeholder})]
     [:span (use-style input-icon) icon]]
    [:input (use-style (merge textinput-style style) {:type type :defaultValue defaultValue :placeholder placeholder})]))



;;; Devcards


(defcard-rg Input
  [textinput {:placeholder "pink"}])

(defcard-rg Input-with-icon
  [textinput {:placeholder "pink" :icon [:> mui-icons/Face]}])

