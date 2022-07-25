(ns athens.views.notifications.popover
  (:require
    ["@chakra-ui/react" :refer [Box IconButton Spinner Flex Text Tooltip Heading VStack ButtonGroup PopoverBody PopoverTrigger ButtonGroup Popover PopoverContent PopoverCloseButton PopoverHeader Portal Button]]
    ["/components/Icons/Icons" :refer [CheckmarkIcon BellFillIcon]]
    ["/components/inbox/Inbox" :refer [InboxItemsList]]
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.views.notifications.core :refer [get-inbox-uid-for-user]]
    [re-frame.core :as rf]
    [athens.views.notifications.actions :as actions]
    [athens.router :as router]))



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
      (= type "athens/notification/type/comment")  "Comments"
      (= type "athens/notification/type/mention")  "Mentions")))


(defn get-notification-state-for-popover
  [prop]
  (let [state (:block/string (get prop "athens/notification/state"))]
    {:isArchived  (if (= state "read hidden")
                    true
                    false)
     :isRead      (if (or (= state "read hidden")
                          (= state "read unhidden"))
                    true
                    false)}))


(defn outliner->inbox-notifs
  [db notification]
  (let [notif-props           (:block/properties notification)
        notif-type            (get-notification-type-for-popover notif-props)
        notif-state           (get-notification-state-for-popover notif-props)
        trigger-parent-uid    (-> (:block/string (get notif-props "athens/notification/trigger/parent"))
                                  (common-db/strip-markup "((" "))"))
        trigger-parent-string (:block/string (common-db/get-block db [:block/uid trigger-parent-uid]))
        username              (:block/string (get notif-props "athens/notification/trigger/author"))
        trigger-uid           (-> (:block/string (get notif-props "athens/notification/trigger"))
                                  (common-db/strip-markup "((" "))"))
        body                  (:block/string (common-db/get-block db [:block/uid trigger-uid]))]
    {"id"         (:block/uid notification)
     "type"       notif-type
     "isArchived" (:isArchived notif-state)
     "isRead"     (:isRead     notif-state)
     "body"       body
     "object"     {"name"       trigger-parent-string
                   "parentUid" trigger-parent-uid}
     "subject"    {"username" username}}))


(defn get-inbox-items-for-popover
  [db userpage]
  (let [inbox-uid                 (get-inbox-uid-for-user db userpage)
        inbox-notifications       (:block/children (common-db/get-block-document db [:block/uid inbox-uid]))
        notifications-for-popover (into [] (map
                                            #(outliner->inbox-notifs db %)
                                            inbox-notifications))]
    (cljs.pprint/pprint notifications-for-popover)
    notifications-for-popover))


(defn notifications-popover
  []
  (let [username (rf/subscribe [:username])
        show-notifications-popover? (rf/subscribe [:notification/show-popover?])]
    (fn []
      [:<>
       [:> Popover {:closeOnBlur false
                    :isOpen @show-notifications-popover?}
        [:> PopoverTrigger
         [:> IconButton {"aria-label" "Notifications"
                         :onClick #(rf/dispatch [:notification/toggle-popover])}
          [:> BellFillIcon]]]
        [:> PopoverContent {:maxWidth  "max-content"
                            :maxHeight "calc(100vh - 4rem)"}
         [:> PopoverCloseButton {:onClick #(rf/dispatch [:notification/toggle-popover])}]
         [:> PopoverHeader "Notifications"]
         [:> Flex {:p             0
                   :as            PopoverBody
                   :flexDirection "column"
                   :overflow      "hidden"}
          [:> InboxItemsList
           {:onOpenItem        #(router/navigate-uid %)
            :onMarkAsRead      #(rf/dispatch (actions/update-state-prop % "read"))
            :onMarkAsUnread    #(rf/dispatch (actions/update-state-prop % "unread"))
            ;; TODO for later
            ;; :onArchive         #(rf/dispatch (actions/update-state-prop % "read hidden"))
            ;; :onUnarchive       #(js/console.log "tried to unarchive" %)
            :notificationsList (get-inbox-items-for-popover @db/dsdb (str "@Sid"))}]]]]])))
