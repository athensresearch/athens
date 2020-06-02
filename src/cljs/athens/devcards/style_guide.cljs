(ns athens.devcards.style-guide
  (:require
    [athens.db]
    [athens.lib.dom.attributes :refer [with-styles]]
    [athens.style :refer [+flex +flex-center +flex-space-between +flex-space-around +flex-column +flex-wrap
                          +text-shadow +box-shadow
                          +link-bg
                          style-guide-css COLORS OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard defcard-rg]]
    [garden.core :refer [css]]))


(def log js/console.log)


(def +circle
  (with-styles {:width 80
                :height 80
                :border-radius 40}))


(defcard-rg Import-Styles
  "CSS is imported here"
  [style-guide-css])


(defcard-rg Colors
  "`+box-shadow` and `+text-shadow` used on the circles and text, respectively."
  [:div (with-styles +flex-space-around +flex-wrap
          {:background-color "#E5E5E5" :padding 20 :border-radius 5})
   (for [c (keys COLORS)]
     ^{:key c}
     [:div (with-styles +flex-center +flex-column {:width 150})
      [:div (with-styles +box-shadow +circle {:background-color (c COLORS)})]
      [:span c]
      [:span (with-styles +text-shadow {:color (c COLORS)}) (c COLORS)]])]
  {}
  {})


(defcard-rg Opacities
  [:div +flex-space-between
   (for [o OPACITIES]
     ^{:key o}
     [:div (with-styles +flex-center +flex-column)
      [:div (with-styles +circle +link-bg {:opacity o})]
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
     [:div +flex-space-between
      [:span t]
      [t (with-styles {:font-family (second fonts)}) "Welcome to Athens"]])])


(defcard-rg Serif-Types
  [:div
   (for [t types]
     ^{:key t}
     [:div +flex-space-between
      [:span t]
      [t (with-styles {:font-family (first fonts)}) "Welcome to Athens"]])])


(defcard-rg Monospace-Types
  [:div
   (for [t types]
     ^{:key t}
     [:div +flex-space-between
      [:span t]
      [t (with-styles {:font-family (last fonts)}) "Welcome to Athens"]])])


(defcard-rg Material-UI-Icons
  "Not sure how to import Material UI Icons
  resources
  - https://shadow-cljs.github.io/docs/UsersGuide.html#cljsjs
  - https://github.com/cljsjs/packages/tree/master/material-ui-icons
  ")
