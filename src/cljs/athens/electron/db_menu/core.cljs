(ns athens.electron.db-menu.core
  (:require
   ["@chakra-ui/react" :refer [Box IconButton VStack ButtonGroup PopoverTrigger ButtonGroup Popover PopoverContent Portal Button Divider]]
   ["react-focus-lock" :default FocusLock]
   [athens.electron.db-menu.db-icon :refer [db-icon]]
   [athens.electron.db-menu.db-list-item :refer [db-list-item]]
   [athens.electron.dialogs :as dialogs]
   [re-frame.core :refer [dispatch subscribe]]))

;; Components

(defn current-db-tools
  ([{:keys [db]} all-dbs]
   (if (:is-remote db)
     [:> ButtonGroup {:size "sm" :pl 10  :ml "auto" :width "100%"}
      [:> Button "Import"]
      [:> Button "Copy Link"]
      [:> Button "Remove"]]
     [:> ButtonGroup {:size "sm"  :pl 10 :ml "auto" :width "100%"}
      [:> Button {:onClick #(dialogs/move-dialog!)} "Move"]
      [:> Button {:onClick #(if (= 1 (count all-dbs))
                              (js/alert "Can't remove last db from the list")
                              (dialogs/delete-dialog! db))}
       "Remove"]])))


(defn db-menu
  []
  (let [all-dbs          @(subscribe [:db-picker/all-dbs])
        active-db        @(subscribe [:db-picker/selected-db])
        inactive-dbs     (dissoc all-dbs (:id active-db))
        sync-status      (if @(subscribe [:db/synced])
                           :running
                           :synchronising)]
    [:> Popover {:placement "bottom-start"}
     [:> PopoverTrigger
      [:> IconButton {:p 0
                      :bg "background.floor"
                      :_hover {:bg "background.upper"}
                      :_active {:bg "background.upper"}}
        ;; DB Icon + Dropdown toggle
       [db-icon {:db     active-db
                 :status sync-status}]]]
    ;; Dropdown menu
     [:> Portal
      [:> PopoverContent {}
       [:> FocusLock
        [:> VStack {:align "stretch"
                    :overflowY "auto"
                    :spacing 2}
        ;; Show active DB first
         [:> Box
          [db-list-item {:db active-db
                         :is-current true
                         :key (:id active-db)}]
          [current-db-tools {:db active-db} all-dbs]]
                    ;; Show all inactive DBs and a separator
         [:> VStack
          {:align "stretch" :spacing 0}
          (doall
           (for [[key db] inactive-dbs]
             [:<>
              [db-list-item {:db db
                             :is-current false
                             :key key}]
              [:> Divider]]))]
                    ;; Add DB control
         [:> Divider]
         [:> ButtonGroup {:p 2 :pt 0 :pl 10 :size "sm" :width "100%" :ml 10 :justifyContent "flex-start"}
          [:> Button {:onClick #(dispatch [:modal/toggle])}
           "Add Database"]]]]]]]))
