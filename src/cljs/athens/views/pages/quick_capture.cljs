(ns athens.views.pages.quick-capture
  (:require
    [athens.common-events.graph.ops    :as graph-ops]
    [athens.common.utils               :as utils]
    [athens.dates                      :as dates]
    [athens.electron.db-menu.core      :refer [db-menu]]
    [clojure.data                      :as data]
    ["@chakra-ui/react"                :refer [Button]]
    ["/components/Quick/QuickCapture"  :refer [QuickCapture]]
    ["react"                           :as react]
    [reagent.core                      :as r]
    [re-frame.core                     :as rf]))


(defn quick-capture
  []
  (let [[notes setNotes] (react/useState [])
        [lastSyncTime, setLastSyncTime] (react/useState nil)
        memory-log @(rf/subscribe [:remote/event-sync-memory-log])
        unsynced-uids (->> memory-log
                           (map #(-> % second :event/op :op/consequences first :op/args :block/uid))
                           (filter some?)
                           set)
        unsynced-uids-notes (->> notes
                                 (filter #(-> % :isSaved false?))
                                 (map :uid)
                                 set)
        mock-sync (fn [notes]
                    (prn notes)
                    (setNotes [])
                    (setLastSyncTime (js/Date.now)))
        mock-add-item (fn [string]
                        ;; Send via reframe.
                        (rf/dispatch [:properties/update-in [:node/title (:title (dates/get-day))]
                                      ["last-qc-message"]
                                      ;; TODO: need to support first/last, and deep path positions
                                      #_["Quick Capture" current-username :last]
                                      (fn [db uid]
                                        (let [new-note (merge {:string string :timestamp (js/Date.now) :uid uid}
                                                              (when-not memory-log {:isSaved true}))]
                                          ;; Save on local comp state.
                                          (setNotes (conj notes new-note))
                                          ;; Save on graph.
                                          [(graph-ops/build-block-save-op db uid string)]))]))]

    (when memory-log
      (let [[a b] (data/diff unsynced-uids unsynced-uids-notes)]
        (when (or a b)
          (println notes a b)
          (setNotes (map (fn [{:keys [uid] :as x}]
                           (cond
                             (and a (a uid)) (assoc x :isSaved false)
                             (and b (b uid)) (assoc x :isSaved true)
                             :else x))
                         notes)))))

    [:<> [:> QuickCapture {:dbMenu (r/as-element [db-menu])
                           :notes notes
                           :lastSyncTime lastSyncTime
                           :onAddItem mock-add-item
                           :newEventId utils/gen-event-id}]
     [:> Button {:position "fixed"
                 :variant "text"
                 :size "xs"
                 :left "50%"
                 :top 5
                 :transform "translateX(-50%)"
                 :onClick mock-sync} "mock update"]]))

;; state
;; unsaved changes