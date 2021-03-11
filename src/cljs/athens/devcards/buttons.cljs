(ns athens.devcards.buttons
  (:require
    ["@material-ui/icons/ChevronRight" :default ChevronRight]
    ["@material-ui/icons/Face" :default Face]
    [athens.views.buttons :refer [button]]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


(defcard-rg Default-button
  [:div (use-style {:display "grid" :grid-auto-flow "column" :justify-content "flex-start" :grid-gap "0.5rem"})
   [button "Button"]
   [button [:> Face]]
   [button [:<>
            [:> Face]
            [:span "Button"]]]
   [button [:<>
            [:span "Button"]
            [:> ChevronRight]]]
   [button {:disabled true} "Button"]
   [button {:disabled true} [:> Face]]
   [button {:disabled true} [:<>
                             [:> Face]
                             [:span "Button"]]]
   [button {:disabled true} [:<>
                             [:span "Button"]
                             [:> ChevronRight]]]])


(defcard-rg Primary-Button
  [:div (use-style {:display "grid" :grid-auto-flow "column" :justify-content "flex-start" :grid-gap "0.5rem"})
   [button {:primary true} "Button"]
   [button {:primary true} [:> Face]]
   [button {:primary true} [:<>
                            [:> Face]
                            [:span "Button"]]]
   [button {:primary true} [:<>
                            [:span "Button"]
                            [:> ChevronRight]]]
   [button {:primary true :disabled true} "Button"]
   [button {:primary true :disabled true} [:> Face]]
   [button {:primary true :disabled true} [:<>
                                           [:> Face]
                                           [:span "Button"]]]
   [button {:primary true :disabled true} [:<>
                                           [:span "Button"]
                                           [:> ChevronRight]]]])
