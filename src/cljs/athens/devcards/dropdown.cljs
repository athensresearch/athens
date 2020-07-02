(ns athens.devcards.dropdown
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [athens.devcards.buttons :refer [button]]
    [athens.devcards.textinput :refer [textinput]]
    [athens.style :refer [color DEPTH-SHADOWS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [reagent.core :as r]
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
   :animation "dropdown-appear 1s ease"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]
   :flex-direction "column"})


(def menu-style
  {:display "grid"
   :grid-gap "2px"
   :min-width "9em"
   :align-items "stretch"
   :grid-auto-flow "row"
   ::stylefy/manual [[(selectors/& (selectors/not (selectors/first-child))) {:margin-block-start "4px"}]
                     [(selectors/& (selectors/not (selectors/last-child))) {:margin-block-end "4px"}]]})


(def menu-item-style
  {:min-height "32px"})


(def menu-heading-style
  {:min-height "32px"
   :text-align "center"
   :padding "4px 8px"
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
  []
  (let [this (r/current-component)]
    (into [:div (use-style dropdown-style) (r/props this)]
          (r/children this))))


(defn menu
  []
  (let [this (r/current-component)]
    (into [:div (use-style menu-style) (r/props this)]
          (r/children this))))


(defn menu-separator
  []
  [:hr (use-style menu-separator-style)])


(defn menu-item
  [{:keys [disabled label]}]
  [button {:label label :disabled disabled :style menu-item-style}])


(defn kbd
  [text]
  [:kbd (use-style kbd-style) text])


(defn submenu-indicator
  []
  [:> mui-icons/ChevronRight (use-style submenu-indicator-style)])


(defn menu-heading
  [heading]
  [:header (use-style menu-heading-style) [:span heading]])


;;; Devcards


(defn menu-1
  []
  [:div (use-style menu-style)
   [menu-item {:label "label-only"}]
   [menu-item {:label [:<> [:> mui-icons/Face] [:span "label and icon"]]}]
   [menu-item {:label [:<> [:> mui-icons/Face] [:span "label and icon"] [kbd "shift-click"]]}]])


(defn menu-with-input
  []
  [:div (use-style menu-style)
   [textinput {:placeholder "Type to search"}]
   [menu-item {:label "Label"}]
   [menu-item {:label [:<> [:> mui-icons/Face] [:span "Label and Icon"]]}]
   [menu-item {:label [:<> [:> mui-icons/Face] [:span "With Keyboard Hint"] [kbd "shift-click"]]}]])


(defcard-rg Default
  [dropdown
   [:p "Default dropdown with a paragraph element inside."]])


(defcard-rg With-Menu
  [dropdown
   [menu
    [menu-item {:label "Label"}]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "Label and Icon"]]}]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "With Keyboard Hint"] [kbd "shift-click"]]}]]])


(defcard-rg With-Input
  [dropdown
   [textinput {:icon [:> mui-icons/Face] :placeholder "Type to filter"}]])


(defcard-rg With-Input-And-Menu
  [dropdown
   [textinput {:icon [:> mui-icons/Face] :placeholder "Type to filter"}]
   [menu
    [menu-item {:label "Label"}]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "Label and Icon"]]}]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "With Keyboard Hint"] [kbd "shift-click"]]}]]])


(defcard-rg With-Heading-Input-And-Menu
  [dropdown
   [menu
    [menu-heading "Modify Block 'Day of Datomic On-Prem 2016'"]
    [textinput {:icon [:> mui-icons/Face] :placeholder "Type to filter"}]
    [menu-item {:label "Label"}]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "Label and Icon"]]}]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "With Keyboard Hint"] [kbd "shift-click"]]}]]])


(defcard-rg With-Heading-Input-And-Menu-and-Separator
  [dropdown
   [menu
    [menu-heading "Modify Block 'Day of Datomic On-Prem 2016'"]
    [textinput {:icon [:> mui-icons/Face] :placeholder "Type to filter"}]
    [menu-item {:label "Label"}]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "Label and Icon"]]}]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "Label and Icon"] [submenu-indicator]]}]
    [menu-separator]
    [menu-item {:label [:<> [:> mui-icons/Face] [:span "With Keyboard Hint"] [kbd "shift-click"]]}]]])

