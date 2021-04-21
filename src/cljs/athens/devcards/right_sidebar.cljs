(ns athens.devcards.right-sidebar
  (:require
    [athens.views.buttons :refer [button]]
    [athens.views.right-sidebar :as right-sidebar]
    [devcards.core :refer [defcard-rg]]
    [re-frame.core :refer [dispatch]]))


(defcard-rg Toggle
  [button {:primary true :on-click-fn #(dispatch [:right-sidebar/toggle])} "Toggle"])


(defcard-rg Right-Sidebar
  [:div {:style {:display "flex" :height "60vh" :justify-content "flex-end"}}
   [right-sidebar/right-sidebar]]
  {:padding false})
