(ns athens.devcards.buttons
  (:require
    ["/components/Button/Button" :refer [Button]]
    ["@material-ui/icons/ChevronRight" :default ChevronRight]
    ["@material-ui/icons/Face" :default Face]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


(defcard-rg Default-button
  [:div (use-style {:display "grid" :grid-auto-flow "column" :justify-content "flex-start" :grid-gap "0.5rem"})
   [:> Button "Button"]
   [:> Button [:> Face]]
   [:> Button [:<>
               [:> Face]
               [:span "Button"]]]
   [:> Button [:<>
               [:span "Button"]
               [:> ChevronRight]]]
   [:> Button {:disabled true} "Button"]
   [:> Button {:disabled true} [:> Face]]
   [:> Button {:disabled true} [:<>
                                [:> Face]
                                [:span "Button"]]]
   [:> Button {:disabled true} [:<>
                                [:span "Button"]
                                [:> ChevronRight]]]])


(defcard-rg Primary-Button
  [:div (use-style {:display "grid" :grid-auto-flow "column" :justify-content "flex-start" :grid-gap "0.5rem"})
   [:> Button {:is-primary true} "Button"]
   [:> Button {:is-primary true} [:> Face]]
   [:> Button {:is-primary true} [:<>
                                  [:> Face]
                                  [:span "Button"]]]
   [:> Button {:is-primary true} [:<>
                                  [:span "Button"]
                                  [:> ChevronRight]]]
   [:> Button {:is-primary true :disabled true} "Button"]
   [:> Button {:is-primary true :disabled true} [:> Face]]
   [:> Button {:is-primary true :disabled true} [:<>
                                                 [:> Face]
                                                 [:span "Button"]]]
   [:> Button {:is-primary true :disabled true} [:<>
                                                 [:span "Button"]
                                                 [:> ChevronRight]]]])
