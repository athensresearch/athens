(ns athens.devcards.right-sidebar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.devcards.buttons :refer [button-primary]]
    [athens.devcards.node-page :refer [node-page-component]]
    [athens.style :refer [color #_OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [posh.reagent :refer [q]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style #_use-sub-style]]))


;;; Styles


(def right-sidebar-style
  {:width "600px"
   :height "100%"
   :border "1px solid lightgray"
   :background-color (color :panel-color :opacity-low)
   :display "flex"
   :justify-content "space-between"
   :padding "30px"})


(def content-style
  {:display "flex"
   :flex-direction "column"
   :overflow-y "auto"})


(def toggle-page-style
  {:margin-top "30px"
   :margin-right "10px"
   :cursor "pointer"})

;;; Components

(def uids
  ["OaSVyM_nr"
   "p1Xv2crs3"])


(defn right-sidebar-el
  [state]
  (let [uids (->> @(q '[:find ?uid ?title
                        :in $ [?uid ...]
                        :where
                        [?e :block/uid ?uid]
                        [?e :node/title ?title]]
                      db/dsdb uids)
                  vec
                  (reduce-kv
                    (fn [m _ [uid title]]
                      (assoc m uid {:open  false
                                    :title title}))
                    {}))
        s (r/atom {:uids uids})]
    (when (:open state)
      [:div (use-style right-sidebar-style)
       [:div (use-style content-style)
        (doall
          (for [[uid {:keys [open title]}] (:uids @s)]
            ^{:key uid}
            [:div {:style {:display "flex"}}
             [:span (use-style toggle-page-style
                      {:on-click (fn [_]
                                   (swap! s update-in [:uids uid :open] not))})
              [:> mui-icons/KeyboardArrowDown]]
             (if open
               [node-page-component [:block/uid uid]]
               [:div [:h1 title]])]))]
       [:div {:on-click (fn [_] (dispatch [:toggle-right-sidebar]))}
        [:> mui-icons/Close]]])))


(defn right-sidebar-component
  []
  (let [state @(subscribe [:right-sidebar])]
    ;;(prn state)
    [right-sidebar-el state]))


;;; Devcards


;;(defcard-rg Init
;;  [button-primary {:label "Toggle" :on-click-fn #(dispatch [:open-in-rightbar "data"])}])



(defcard-rg Toggle
  [button-primary {:label "Toggle" :on-click-fn #(dispatch [:toggle-right-sidebar])}])


(defcard-rg Right-Sidebar
  [:div {:style {:display "flex" :height "60vh" :justify-content "flex-end"}}
   [right-sidebar-component]]
  {:padding false})
