(ns athens.devcards.right-sidebar
  (:require
    ["/components/Button/Button" :refer [Button]]
    [athens.views.right-sidebar :as right-sidebar]
    [devcards.core :refer [defcard-rg]]
    [re-frame.core :refer [dispatch]]))


(defcard-rg Toggle
  [:> Button {:isPrimary true :on-click-fn #(dispatch [:right-sidebar/toggle])} "Toggle"])


(defcard-rg Right-Sidebar
  [:div {:style {:display "flex" :height "60vh" :justify-content "flex-end"}}
   [right-sidebar/right-sidebar]]
  {:padding false})
