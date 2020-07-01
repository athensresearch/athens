(ns athens.devcards.dropdown
  (:require
   ["@material-ui/icons" :as mui-icons]
   [athens.db]
   [athens.devcards.buttons :refer [button]]
   [athens.style :refer [color DEPTH-SHADOWS]]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros [defcard-rg]]
   [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def dropdown-style
  {:display "inline-flex"
   :padding "4px"
   :border-radius "6px"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]
   :flex-direction "column"})


(def menu-style
  {:display "flex"
   :min-width "8em"
   :align-items "stretch"
   :flex-direction "column"})


(def menu-item-style
  {:min-height "32px"
   :margin-inline-start "2px"})


(def input-style {:background (color :panel-color :opacity-low)
                  :min-height "24px"
                  :border-radius "4px"
                  :padding "4px 8px"
                  :margin-bottom "4px"
                  :border "none"})


(def kbd-style {:margin-left "auto"
                :opacity "0.5"
                :display "inline-flex"
                :place-content "center"
                :padding "0 8px"
                :font-family "inherit"
                :font-size "0.6em"})


;;; Components


(defn dropdown
  [children]
  [:div (use-style dropdown-style)
   children])


(defn kbd
  [kbd]
  [:kbd (use-style kbd-style) kbd])


(defn menu-item
  [{:keys [disabled label]}]
  [button {:label label :style menu-item-style}]
)


;;; Devcards

(defn menu []
  [:div (use-style menu-style)
   [:input (use-style input-style {:placeholder "Type to search"})]
   [menu-item {:label "label-only"}]
   [menu-item {:label [:<> [:> mui-icons/Face] [:span "label and icon"]]}]
   [menu-item {:label [:<> [:> mui-icons/Face] [:span "label and icon"] [kbd "shift-click"]]}]]
  )


(defcard-rg Default
  [dropdown
   [menu]])

