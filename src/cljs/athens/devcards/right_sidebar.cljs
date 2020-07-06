(ns athens.devcards.right-sidebar
  (:require
    [athens.views.buttons :refer [button-primary]]
    [athens.views.right-sidebar :refer [right-sidebar-component]]
    [devcards.core :refer [defcard-rg]]
    [re-frame.core :refer [dispatch]]))


;;(defcard-rg Init
;;  [button-primary {:label "Toggle" :on-click-fn #(dispatch [:open-in-rightbar "data"])}])



(defcard-rg Toggle
  [button-primary {:label "Toggle" :on-click-fn #(dispatch [:right-sidebar/toggle])}])


(defcard-rg Right-Sidebar
  [:div {:style {:display "flex" :height "60vh" :justify-content "flex-end"}}
   [right-sidebar-component]]
  {:padding false})
