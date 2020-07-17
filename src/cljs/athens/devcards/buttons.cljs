(ns athens.devcards.buttons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.views.buttons :refer [button button-primary]]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


(defcard-rg Default-Button
  [:div (use-style {:display "grid" :grid-auto-flow "column" :justify-content "flex-start" :grid-gap "0.5rem"})
   [button {:label "Button"}]
   [button {:label [:> mui-icons/Face]}]
   [button {:label [:<>
                    [:> mui-icons/Face]
                    [:span "Button"]]}]
   [button {:label [:<>
                    [:span "Button"]
                    [:> mui-icons/ChevronRight]]}]
   [button {:disabled true :label "Button"}]
   [button {:disabled true :label [:> mui-icons/Face]}]
   [button {:disabled true :label [:<>
                                   [:> mui-icons/Face]
                                   [:span "Button"]]}]
   [button {:disabled true :label [:<>
                                   [:span "Button"]
                                   [:> mui-icons/ChevronRight]]}]])


(defcard-rg Primary-Button
  [:div (use-style {:display "grid" :grid-auto-flow "column" :justify-content "flex-start" :grid-gap "0.5rem"})
   [button-primary {:label "Button"}]
   [button-primary {:label [:> mui-icons/Face]}]
   [button-primary {:label [:<>
                            [:> mui-icons/Face]
                            [:span "Button"]]}]
   [button-primary {:label [:<>
                            [:span "Button"]
                            [:> mui-icons/ChevronRight]]}]
   [:hr]
   [button-primary {:disabled true :label "Button"}]
   [button-primary {:disabled true :label [:> mui-icons/Face]}]
   [button-primary {:disabled true :label [:<>
                                           [:> mui-icons/Face]
                                           [:span "Button"]]}]
   [button-primary {:disabled true :label [:<>
                                           [:span "Button"]
                                           [:> mui-icons/ChevronRight]]}]])
