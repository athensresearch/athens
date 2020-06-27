(ns athens.devcards.right-sidebar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.devcards.athena :refer [athena-prompt-el]]
    [athens.devcards.buttons :refer [button button-primary]]
    [athens.router :refer [navigate navigate-uid]]
    [athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [posh.reagent :refer [q transact! pull-many]]
    [re-frame.core :as re-frame :refer [dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


;;; Styles


(def right-sidebar-style
  {:width "600px"
   :height "100%"
   :display "flex"
   :flex-direction "column"
   :border "1px solid lightgray"})


;;; Components

(def ids
  [[:block/uid "OaSVyM_nr"]
   [:block/uid "p1Xv2crs3"]])

(defn right-sidebar
  []
  (let [nodes (->> @(pull-many db/dsdb db/node-pull-pattern ids))]
    (fn []
      [:div (use-style right-sidebar-style)
       (for [{:block/keys [uid] :node/keys [title]} nodes]
         ^{:key uid}
         [:div
          [:h1 title]])])))




;;; Devcards


(defcard-rg Right-Sidebar
  [:div {:style {:display "flex" :height "60vh" :justify-content "flex-end"}}
   [right-sidebar]]
  {:padding false})
