(ns athens.views.pages.quick-capture
  (:require
    [athens.electron.db-menu.core      :refer [db-menu]]
    ["@chakra-ui/react"                :refer [Button]]
    ["/components/Quick/QuickCapture"  :refer [QuickCapture]]
    ["react"                           :as react]
    [reagent.core                      :as r]))


(defn quick-capture
  []
  (let [[notes setNotes] (react/useState [])
        [lastSyncTime, setLastSyncTime] (react/useState nil)
        mock-sync (fn [notes]
                    (prn notes)
                    (setNotes [])
                    (setLastSyncTime (js/Date.now)))
        mock-add-item (fn [string] (setNotes (conj notes {:isSaved false :string string :timestamp (js/Date.now)})))
        mock-get-new-event-id (fn [] "random id")]
    [:<> [:> QuickCapture {:dbMenu (r/as-element [db-menu])
                           :notes notes
                           :lastSyncTime lastSyncTime
                           :onAddItem mock-add-item
                           :newEventId mock-get-new-event-id}]
     [:> Button {:position "fixed"
                 :variant "text"
                 :size "xs"
                 :left "50%"
                 :top 5
                 :transform "translateX(-50%)"
                 :onClick mock-sync} "mock update"]]))

;; state
;; unsaved changes