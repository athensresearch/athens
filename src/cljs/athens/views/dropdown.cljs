(ns athens.views.dropdown
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [athens.style :refer [color DEPTH-SHADOWS]]
    [athens.views.buttons :refer [button]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(stylefy/keyframes "dropdown-appear"
                   [:from {:opacity 0}]
                   [:to {:opacity 1}])


(def dropdown-style
  {:display "inline-flex"
   :padding "4px"
   :border-radius "6px"
   :min-height "2em"
   :min-width "2em"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]
   :flex-direction "column"})


(def menu-style
  {:display "grid"
   :grid-gap "2px"
   :min-width "9em"
   :align-items "stretch"
   :grid-auto-flow "row"
   :overflow "auto"
   ::stylefy/manual [[(selectors/& (selectors/not (selectors/first-child))) {:margin-block-start "4px"}]
                     [(selectors/& (selectors/not (selectors/last-child))) {:margin-block-end "4px"}]]})


(def menu-item-style
  {:min-height "32px"
   ::stylefy/manual [[:svg:first-child {:font-size "16px" :margin-right "6px" :margin-left "-2px"}]]})


(def menu-heading-style
  {:min-height "32px"
   :text-align "center"
   :padding "6px 8px"
   :display "flex"
   :align-content "flex-end"
   :justify-content "center"
   :align-items "center"
   :font-size "12px"
   :max-width "100%"
   :overflow "hidden"
   :text-overflow "ellipsis"})


(def menu-separator-style
  {:border "0"
   :background (color :panel-color)
   :align-self "stretch"
   :justify-self "stretch"
   :height "1px"
   :margin "4px 0"})


(def kbd-style
  {:margin-left "auto"
   :opacity "0.5"
   :display "inline-flex"
   :place-content "center"
   :padding "0 16px"
   :font-family "inherit"
   :font-size "0.6em"
   ::stylefy/manual [[:&:last-child {:padding-inline-end "0"}]]})


(def submenu-indicator-style
  {:margin-left "auto"
   :opacity "0.5"
   :display "flex"
   :order 10
   :align-self "flex-end"
   :font-family "inherit"
   ::stylefy/manual [[:&:last-child {:padding-inline-end "0"}]]})


;;; Components


(defn dropdown
  [{:keys [style content]}]
  [:div (use-style (merge dropdown-style style))
   content])


(defn menu
  [{:keys [style content]}]
  [:div (use-style (merge menu-style style))
   content])


(defn menu-separator
  []
  [:hr (use-style menu-separator-style)])


(defn menu-item
  [{:keys [disabled label style]}]
  [button {:label label :disabled disabled :style (merge menu-item-style style)}])


(defn kbd
  [text]
  [:kbd (use-style kbd-style) text])


(defn submenu-indicator
  []
  [:> mui-icons/ChevronRight (use-style submenu-indicator-style)])


(defn menu-heading
  [heading]
  [:header (use-style menu-heading-style) [:span heading]])
