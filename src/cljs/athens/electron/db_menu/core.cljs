(ns athens.electron.db-menu.core
  (:require
    ["@chakra-ui/react" :refer [Box IconButton Spinner Text Tooltip Heading VStack ButtonGroup PopoverTrigger ButtonGroup Popover PopoverContent Portal Button]]
    ["react-focus-lock" :default FocusLock]
    [athens.electron.db-menu.db-icon :refer [db-icon]]
    [athens.electron.db-menu.db-list-item :refer [db-list-item]]
    [athens.electron.db-modal :as db-modal]
    [athens.electron.dialogs :as dialogs]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]))


;; Components

(defn current-db-tools
  ([{:keys [db]} all-dbs merge-open?]
   (when-not (:is-remote db)
     [:> ButtonGroup {:size "xs" :pr 4 :pl 10 :ml "auto" :width "100%"}
      [:> Button {:onClick #(dialogs/move-dialog!)} "Move"]
      [:> Button {:mr "auto" :onClick #(reset! merge-open? true)} "Merge from Roam"]
      [:> Tooltip {:label "Can't remove last database" :placement "right" :isDisabled (< 1 (count all-dbs))}
       [:> Button {:isDisabled (= 1 (count all-dbs))
                   :onClick #(dialogs/delete-dialog! db)}
        "Remove"]]])))


(defn db-menu
  []
  (let [all-dbs          @(subscribe [:db-picker/all-dbs])
        merge-open?      (r/atom false)
        active-db        @(subscribe [:db-picker/selected-db])
        inactive-dbs     (dissoc all-dbs (:id active-db))
        sync-status      (if @(subscribe [:db/synced])
                           :running
                           :synchronising)]
    [:<>
     [db-modal/merge-modal merge-open?]
     [:> Popover {:placement "bottom-start" :isLazy true}
      [:> PopoverTrigger
       [:> IconButton {:p 0
                       "aria-label" "Database menu"
                       :bg "background.floor"}
        ;; DB Icon + Dropdown toggle
        [db-icon {:db     active-db
                  :status sync-status}]]]
      ;; Dropdown menu
      [:> Portal
       [:> PopoverContent {:overflow-y "auto"}
        [:> FocusLock
         [:> VStack {:align "stretch"
                     :overflow "hidden"
                     :spacing 0}
          ;; Show active DB first
          [:> Box {:bg "background.floor"
                   :pb 4}
           [db-list-item {:db active-db
                          :is-current true
                          :key (:id active-db)}]
           [current-db-tools {:db active-db} all-dbs merge-open?]]
          ;; Show all inactive DBs and a separator
          [:> Heading {:fontSize "xs"
                       :py 4
                       :pb 3
                       :borderTop "1px solid"
                       :borderTopColor "separator.divider"
                       ;;  :bg "background.floor"
                       :px 10
                       :letterSpacing "wide"
                       :textTransform "uppercase"
                       :fontWeight "bold"
                       :color "foreground.secondary"}
           "Other databases"]
          [:> VStack {:align "stretch"
                      :position "relative"
                      :spacing 0
                      :overflow-y "auto"}
           (doall
             (for [[key db] inactive-dbs]
               [db-list-item {:db db
                              :is-disabled (= sync-status :synchronising)
                              :is-current false
                              :key key}]))
           (when (= :synchronising sync-status)
             [:> VStack {:align "center"
                         :background "background.vibrancy"
                         :backdropFilter "blur(0.25ch)"
                         :justify "center" :position "absolute" :inset 0}
              [:> Spinner]
              [:> Text "Syncing..."]])]
          ;; Add DB control
          [:> ButtonGroup {:borderTop "1px solid" :borderTopColor "separator.divider" :p 2 :pt 0 :pl 10 :size "sm" :width "100%" :ml 10 :justifyContent "flex-start"}
           [:> Button {:onClick #(dispatch [:modal/toggle])}
            "Add Database"]]]]]]]]))
