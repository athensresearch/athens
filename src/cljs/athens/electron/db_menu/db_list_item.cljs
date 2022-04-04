(ns athens.electron.db-menu.db-list-item
  (:require
    ["/components/Icons/Icons" :refer [XmarkIcon]]
    ["@chakra-ui/react" :refer [VStack Box Flex Text Button IconButton]]
    [athens.electron.db-menu.db-icon :refer [db-icon]]
    [athens.electron.dialogs :as dialogs]
    [re-frame.core :refer [dispatch]]))


(defn active-db
  [{:keys [db]}]
  [:> Flex {:gap 2
            :p 2
            :borderRadius "none"
            :whiteSpace "nowrap"
            :height "auto"
            :align "stretch"
            :background "transparent"
            :justifyContent "stretch"
            :textAlign "left"}
   [db-icon {:db db}]
   [:> VStack {:align "stretch"
               :flex "1 1 100%"
               :overflow "hidden"
               :spacing 0
               :textOverflow "ellipsis"}
    [:> Text {:textOverflow "ellipsis"
              :overflow "hidden"
              :fontWeight "bold"}
     (:name db)]
    [:> Text {:textOverflow "ellipsis"
              :fontSize "sm"
              :color "foreground.secondary"
              :overflow "hidden"
              :title (:id db)}
     (:id db)]]])


(defn db-item
  [{:keys [db on-click on-remove is-disabled]}]
  [:> Box {:display "grid"
           :_notFirst {:borderTopWidth "1px"
                       :borderTopStyle "solid"
                       :borderTopColor "separator.divider"}
           :gridTemplateAreas "'main'"}
   [:> Button {:onClick (when on-click on-click)
               :isDisabled (or is-disabled (not on-click))
               :gridArea "main"
               :whiteSpace "nowrap"
               :bg "transparent"
               :display "flex"
               :gap 2
               :py 2
               :pr 10
               :borderRadius "none"
               :height "auto"
               :align "stretch"
               :justifyContent "stretch"
               :_focusVisible {:boxShadow "focusInset"}
               :textAlign "left"}
    [db-icon {:db db}]
    [:> VStack {:align "stretch"
                :flex "1 1 100%"
                :spacing 1
                :overflow "hidden"
                :textOverflow "ellipsis"}
     [:> Text {:textOverflow "ellipsis"
               :fontWeight "bold"
               :overflow "hidden"} (:name db)]
     [:> Text {:textOverflow "ellipsis"
               :size "sm"
               :color "foreground.secondary"
               :overflow "hidden"
               :title (:id db)}
      (:id db)]]]
   (when on-remove
     [:> IconButton
      {:onClick on-remove
       :gridArea "main"
       :alignSelf "center"
       :justifySelf "flex-end"
       :size "sm"
       :mr 2
       :bg "transparent"}
      [:> XmarkIcon]])])


(defn db-list-item
  [{:keys [db is-current is-disabled]}]
  (let [remove-db-click-handler (fn [e]
                                  (dialogs/delete-dialog! db)
                                  (.. e stopPropagation))]
    (if is-current
      [active-db {:db db}]
      [db-item {:db db
                :is-disabled is-disabled
                :on-click #(dispatch [:db-picker/select-db db])
                :on-remove remove-db-click-handler}])))

