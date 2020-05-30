(ns athens.devcards.style-guide
  (:require
   [athens.style :as s]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros [defcard defcard-rg]]
   [garden.core :refer [css]]
   [garden.stylesheet :refer [at-import]]))

(def log js/console.log)

(def +flex-center
  (s/with-style {:display "flex" :flex-direction "column" :justify-content "center" :align-items "center"}))

(def +flex-space-between
  (s/with-style {:display "flex" :align-items "center" :justify-content "space-between"}))

;; Colors

(def +colors-container
  (comp +flex-space-between
        (s/with-style {:background "#E5E5E5"
                       :padding 20})))

(def +circle (s/with-style {:width 80
                            :height 80
                            :border-radius 40}))

(def colors
  [:blue :orange :red :green
   :dark-gray :warm-gray :ivory :white])

;; TODO: refactor
(defcard-rg Colors
  [:div (+colors-container)
   (for [c colors]
     [:div (+flex-center)
      [:div {:style {:background-color (c s/COLORS)
                     :width            80
                     :height           80
                     :border-radius    40}}]
      [:span c]
      [:span (c s/COLORS)]])]
  {}
  {:padding false})

;; Types

;; TODO: refactor to athens.style
(defn main-css
  []
  [:style (css
            [:* {:font-family "IBM Plex Sans"}]
            [:h1 {:font-size "50px"
                  :font-weight 600
                  :line-height "65px"}]
            [:h2 {:font-size "38px"
                  :font-weight 500
                  :line-height "49px"}]
            [:h3 {:font-size "28px"
                  :font-weight 500
                  :line-height "36px"}]
            [:h4 {:font-size "21px"
                  :line-height "27px"}]
            [:h5 {:font-size "12px"
                  :font-weight 500
                  :line-height "16px"
                  :text-transform "uppercase"}])])

(def types [:h1 :h2 :h3 :h4 :h5])

(defcard-rg Serif-Types
  [:div
   [main-css]
   (for [t types]
     ^{:key t}
     [:div (+flex-space-between)
      [:span t]
      [t "Welcome to Athens"]])])

(defcard Font "Not sure how to import fonts.

  resources
  - https://fonts.googleapis.com/css2?family=IBM+Plex+Sans
  - https://fonts.google.com/specimen/IBM+Plex+Sans
  - https://cljdoc.org/d/garden/garden/1.3.10/api/garden.stylesheet
  - https://gist.github.com/paulkoegel/1c17be411c26d959fc6d75776d86e4f8")


(defcard-rg Material-UI-Icons
  "Not sure how to import Material UI icons.
  - Author of shadow-cljs has https://github.com/cljsjs/packages/tree/master/material-ui-icons
  - but this library was deprecated on npm https://www.npmjs.com/package/material-ui-icons

  resources
  - https://shadow-cljs.github.io/docs/UsersGuide.html#cljsjs
  - https://shadow-cljs.github.io/docs/UsersGuide.html#infer-externs
  - https://github.com/cljsjs/packages/tree/master/material-ui-icons
  - https://material-ui.com/components/icons/#svg-material-icons
  - https://www.npmjs.com/package/@material-ui/icons")