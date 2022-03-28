(ns athens.views.dropdown
  (:require
    [athens.db]
    [athens.style :refer [color DEPTH-SHADOWS ZINDICES]]
    [stylefy.core :as stylefy]))


;; Styles


(stylefy/keyframes "dropdown-appear"
                   [:from {:opacity 0
                           :transform "translateY(-10%)"}]
                   [:to {:opacity 1
                         :transform "translateY(0)"}])


(def dropdown-style
  {:display "inline-flex"
   :color (color :body-text-color)
   :z-index (:zindex-dropdown ZINDICES)
   :padding "0.25rem"
   :border-radius "calc(0.25rem + 0.25rem)" ; Button corner radius + container padding makes "concentric" container radius
   :min-height "2em"
   :min-width "2em"
   :animation "dropdown-appear 0.125s"
   :animation-fill-mode "both"
   :background (color :background-plus-2)
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]
   :flex-direction "column"})
