(ns athens.views.notifications.popover
  (:require
    ["/components/Icons/Icons" :refer [BellFillIcon ArrowRightIcon]]
    ["/components/Notifications/NotificationItem" :refer [NotificationItem]]
    ["/timeAgo.js" :refer [timeAgo]]
    ["framer-motion" :refer [AnimatePresence motion]]
    ["@chakra-ui/react" :refer [Badge Box VStack Center Text IconButton Flex PopoverBody PopoverTrigger Popover PopoverContent PopoverCloseButton PopoverHeader Button]]
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.reactive :as reactive]
    [athens.router :as router]
    [athens.views.notifications.actions :as actions]
    [athens.views.notifications.core :as notifications :refer [get-inbox-uid-for-user]]
    [re-frame.core :as rf]
    [reagent.core :as r]))


(rf/reg-sub
  :notification/show-popover?
  (fn [db [_]]
    (= true (:notification/show-popover db))))


(rf/reg-event-fx
  :notification/toggle-popover
  (fn [{:keys [db]} [_]]
    (println "toggle notification popover")
    (let [current-state (:notification/show-popover db)]
      {:db (assoc db :notification/show-popover (not current-state))})))


(defn get-notification-type-for-popover
  [prop]
  (let [type (:block/string (get prop "athens/notification/type"))]
    (cond
      (= type "[[athens/notification/type/comment]]")  "Comments"
      (= type "[[athens/notification/type/mention]]")  "Mentions")))


(defn get-archive-state
  [prop]
  (let [state (:block/string (get prop "athens/notification/is-archived"))]
    (= state "true")))


(defn get-read-state
  [prop]
  (let [state (:block/string (get prop "athens/notification/is-read"))]
    (= state "true")))


(defn outliner->inbox-notifs
  [db notification]
  (let [{:block/keys [properties uid create]} notification
        notif-type            (get-notification-type-for-popover properties)
        ;; we could use presence auth instead of notification trigger
        _presence-auth         (-> create
                                   :event/auth
                                   :presence/id)
        create-time           (-> create
                                  :event/time
                                  :time/ts
                                  timeAgo)
        archive-state         (get-archive-state properties)
        read-state            (get-read-state properties)
        trigger-parent-uid    (-> (get properties "athens/notification/trigger/parent")
                                  :block/string
                                  (common-db/strip-markup "((" "))"))
        trigger-parent-string (-> (common-db/get-block db [:block/uid trigger-parent-uid])
                                  :block/string)
        username              (-> (get properties "athens/notification/trigger/author")
                                  :block/string
                                  (common-db/strip-markup "[[" "]]"))
        trigger-uid           (-> (get properties "athens/notification/trigger")
                                  :block/string
                                  (common-db/strip-markup "((" "))"))
        body                  (-> (common-db/get-block db [:block/uid trigger-uid])
                                  :block/string)]
    {"id"         uid
     "type"       notif-type
     "isArchived" archive-state
     "notificationTime" create-time
     "isRead"     read-state
     "body"       body
     "object"     {"name"      trigger-parent-string
                   "parentUid" trigger-parent-uid}
     "subject"    {"username" username}}))


(defn filter-hidden-notifs
  [inbox-notif]
  (not (get inbox-notif "isArchived")))


(defn get-inbox-items-for-popover
  [db at-username]
  (let [inbox-uid                 (get-inbox-uid-for-user db at-username)
        reactive-inbox            (reactive/get-reactive-block-document [:block/uid inbox-uid])
        reactive-inbox-items-ids  (->> reactive-inbox
                                       :block/children
                                       (map :db/id))
        reactive-inbox-items      (mapv #(reactive/get-reactive-block-document %) reactive-inbox-items-ids)
        notifications-for-popover (->> (mapv #(outliner->inbox-notifs db %) reactive-inbox-items)
                                       (filterv filter-hidden-notifs))]
    notifications-for-popover))


;; TODO: if already on user page, close
(defn on-click-notification-item
  [parent-uid notification-uid]
  (router/navigate-uid parent-uid)
  (rf/dispatch (actions/update-state-prop notification-uid "athens/notification/is-read" "true")))


(defn notifications-popover
  []
  (let [username (rf/subscribe [:username])]
    (fn []
      (when (notifications/enabled?)
        (let [user-page-title    (str "@" @username)
              notification-list  (get-inbox-items-for-popover @db/dsdb user-page-title)
              navigate-user-page #(router/navigate-page user-page-title)
              num-notifications  (count notification-list)]
          [:> Popover {:closeOnBlur true
                       :size "md"}
           [:> PopoverTrigger
            [:> Box {:position "relative"}
             [:> IconButton {"aria-label"   "Notifications"
                             :onDoubleClick navigate-user-page
                             :onClick       (fn [e]
                                              (when (.. e -shiftKey)
                                                (rf/dispatch [:right-sidebar/open-item [:node/title user-page-title]])))
                             :icon          (r/as-element [:> BellFillIcon])}]
             (when (> num-notifications 0)
               [:> Badge {:position "absolute"
                          :bg "gold"
                          :pointerEvents "none"
                          :color "goldContrast"
                          :right "-3px"
                          :bottom "-1px"
                          :zIndex 1} num-notifications])]]

           [:> PopoverContent {:maxHeight "calc(100vh - 4rem)"}
            [:> PopoverCloseButton]
            [:> PopoverHeader
             [:> Button {:onClick navigate-user-page :rightIcon (r/as-element [:> ArrowRightIcon])}
              "Notifications"]]
            [:> VStack {:as PopoverBody
                        :flexDirection "column"
                        :align "stretch"
                        :overflowY "auto"
                        :-webkit-overflow-scrolling "touch"
                        :overscrollBehavior "contain"
                        :p 2}
             [:> AnimatePresence {:initial false}
              (if (seq notification-list)
                (for [notification notification-list]
                  [:> NotificationItem
                   {:notification   notification
                    :onOpenItem     on-click-notification-item
                    :onMarkAsRead   #(rf/dispatch (actions/update-state-prop % "athens/notification/is-read" "true"))
                    :onMarkAsUnread #(rf/dispatch (actions/update-state-prop % "athens/notification/is-read" "false"))
                    :onArchive      (fn [e uid]
                                      (.. e stopPropagation)
                                      (rf/dispatch (actions/update-state-prop uid "athens/notification/is-archived" "true")))}])
                [:> Center
                 [:> Text "Notifications you receive will appear here."]])]]]])))))