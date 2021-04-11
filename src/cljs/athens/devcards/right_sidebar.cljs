(ns athens.devcards.ref-sidebar
  (:require
    [athens.views.buttons :refer [button]]
    [athens.views.ref-sidebar :refer [ref-sidebar-component]]
    [devcards.core :refer [defcard-rg]]
    [re-frame.core :refer [dispatch]]))


(defcard-rg Toggle
  [button {:primary true :on-click-fn #(dispatch [:ref-sidebar/toggle])} "Toggle"])


(defcard-rg ref-sidebar
  [:div {:style {:display "flex" :height "60vh" :justify-content "flex-end"}}
   [ref-sidebar-component]]
  {:padding false})
