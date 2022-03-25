(ns athens.views.dropdown
  (:require
    [athens.db]
    [athens.style :refer [color DEPTH-SHADOWS ZINDICES]]
    [garden.selectors :as selectors]
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


(def menu-style
  {:display "grid"
   :grid-gap "0.125rem"
   :min-width "9em"
   :align-items "stretch"
   :grid-auto-flow "row"
   :overflow "auto"
   ::stylefy/manual [[(selectors/& (selectors/not (selectors/first-child))) {:margin-block-start "0.25rem"}]
                     [(selectors/& (selectors/not (selectors/last-child))) {:margin-block-end "0.25rem"}]
                     [:button {:min-height "1.5rem"}]]})


#_(def menu-heading-style
    {:min-height "2rem"
     :text-align "center"
     :padding "0.375rem 0.5rem"
     :display "flex"
     :align-content "flex-end"
     :justify-content "center"
     :align-items "center"
     :font-size "12px"
     :max-width "100%"
     :overflow "hidden"
     :text-overflow "ellipsis"})

