(ns athens.devcards.buttons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.views.buttons :refer [button]]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


(defcard-rg Default-button
  [:div (use-style {:display "grid" :grid-auto-flow "column" :justify-content "flex-start" :grid-gap "0.5rem"})
   [button "Button"]
   [button [:> mui-icons/Face]]
   [button [:<>
            [:> mui-icons/Face]
            [:span "Button"]]]
   [button [:<>
            [:span "Button"]
            [:> mui-icons/ChevronRight]]]
   [button {:disabled true} "Button"]
   [button {:disabled true} [:> mui-icons/Face]]
   [button {:disabled true} [:<>
                             [:> mui-icons/Face]
                             [:span "Button"]]]
   [button {:disabled true} [:<>
                             [:span "Button"]
                             [:> mui-icons/ChevronRight]]]])


(defcard-rg Primary-Button
  [:div (use-style {:display "grid" :grid-auto-flow "column" :justify-content "flex-start" :grid-gap "0.5rem"})
   [button {:primary true} "Button"]
   [button {:primary true} [:> mui-icons/Face]]
   [button {:primary true} [:<>
                            [:> mui-icons/Face]
                            [:span "Button"]]]
   [button {:primary true} [:<>
                            [:span "Button"]
                            [:> mui-icons/ChevronRight]]]
   [button {:primary true :disabled true} "Button"]
   [button {:primary true :disabled true} [:> mui-icons/Face]]
   [button {:primary true :disabled true} [:<>
                                           [:> mui-icons/Face]
                                           [:span "Button"]]]
   [button {:primary true :disabled true} [:<>
                                           [:span "Button"]
                                           [:> mui-icons/ChevronRight]]]])
