(ns athens.devcards.daily-notes
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.util :refer [get-day]]
    [athens.db :as db]
    [athens.devcards.node-page :refer [node-page-component]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [posh.reagent :refer [pull pull-many q]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles

;;; Components

(defn daily-notes-component
  []
  (let [note-refs (subscribe [:daily-notes])]
    (if (empty? @note-refs)
      (dispatch [:next-daily-note (get-day)]))
    (fn []

      (let [notes (pull-many db/dsdb
                    '[*]
                    ;;'[:db/id :block/uid :block/string :block/open :block/order {:block/children ...}]
                    (map (fn [x] [:block/uid x]) @note-refs))]
        [:div (use-style {:display        "flex"
                          :flex           "0 0 auto"
                          :flex-direction "column"})

         (for [{:keys [block/uid node/title]} @notes]
           [:<>
            [:div (use-style {:min-height "550px"} {:key uid})
             [:h1 title]]
            ;;[node-page-component [:block/uid uid]]
            [:hr (use-style {:border "1px solid black"
                             :width  "100%"}
                   {:key title})]])

         [:div
          [:h1 (use-style {:color "gray"}) "Preview"]]]))))



;;; Devcards

(defcard-rg Daily-Notes
  [daily-notes-component])
