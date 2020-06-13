(ns athens.devcards.style-guide
  (:require
    [athens.db]
    [athens.style :refer [base-styles COLORS OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


(defcard-rg Import-Styles
  "CSS is imported here"
  [base-styles])


(def color-group-style
  {:display "grid"
   :padding "1rem"
   :grid-template-columns "repeat( auto-fit, minmax(9rem, 1fr))"
   :grid-gap "3rem 1rem"
   :text-align "center"
   :align-items "center"})


(def color-item-style
  {:display "grid"
   :grid-gap "0.25rem"
   ::stylefy/manual [[:div {:border-radius "1000px"
                            :background (:link-color COLORS)
                            :height "4rem"
                            :margin "auto"
                            :width "4rem"}]]})


(def text-item-style
  {:display "flex"
   :justify-content "space-between"})


(defcard-rg Colors
  [:div (use-style (merge color-group-style {:background "#e5e5e5"}))
   (for [c (keys COLORS)]
     ^{:key c}
     [:div (use-style color-item-style)
      [:div {:style {:background (c COLORS) :box-shadow "0 0 0 1px rgba(0,0,0,0.15)"}}]
      [:span c]
      [:span {:style {:color (c COLORS)}} (c COLORS)]])]
  {}
  {:padding false})


(defcard-rg Opacities
  [:div (use-style color-group-style)
   (for [o OPACITIES]
     ^{:key o}
     [:div (use-style color-item-style)
      [:div {:style {:opacity o}}]
      [:span o]])])


(def types [:h1 :h2 :h3 :h4 :h5 :span :span.block-ref])


(def fonts
  [["IBM Plex Serif" "serif"]
   ["IBM Plex Sans" "sans-serif"]
   ["IBM Plex Mono" "monospace"]])


(defcard-rg Sans-Types
  [:div
   (for [t types]
     ^{:key t}
     [:div (use-style text-item-style)
      [:span t]
      [t {:style {:font-family (second fonts)}} "Welcome to Athens"]])])


(defcard-rg Serif-Types
  [:div
   (for [t types]
     ^{:key t}
     [:div (use-style text-item-style)
      [:span t]
      [t {:style {:font-family (first fonts)}} "Welcome to Athens"]])])


(defcard-rg Monospace-Types
  [:div
   (for [t types]
     ^{:key t}
     [:div (use-style text-item-style)
      [:span t]
      [t {:style {:font-family (last fonts)}} "Welcome to Athens"]])])


(defcard-rg Material-UI-Icons
  [:a {:href "/cards.html#!/athens.devcards.icons"} "Icons DevCard"])
