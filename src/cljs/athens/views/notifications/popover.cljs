(ns athens.views.notifications.popover
  (:require
    ["@chakra-ui/react" :refer [Box IconButton Spinner Flex Text Tooltip Heading VStack ButtonGroup PopoverBody PopoverTrigger ButtonGroup Popover PopoverContent PopoverCloseButton PopoverHeader Portal Button]]
    ["/components/Icons/Icons" :refer [CheckmarkIcon BellIcon ArrowRightIcon]]
    ["/components/inbox/Inbox" :refer [InboxItemsList]]
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.views.notifications.core :refer [get-inbox-uid-for-user]]
    [re-frame.core :as rf]
    [athens.views.notifications.actions :as actions]
    [athens.router :as router]
    [athens.reactive :as reactive]
    [cljs.analyzer :as cljs]))




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
  (let [notif-props           (:block/properties notification)
        notif-type            (get-notification-type-for-popover notif-props)
        archive-state         (get-archive-state notif-props)
        read-state            (get-read-state notif-props)
        trigger-parent-uid    (-> (:block/string (get notif-props "athens/notification/trigger/parent"))
                                  (common-db/strip-markup "((" "))"))
        trigger-parent-string (:block/string (common-db/get-block db [:block/uid trigger-parent-uid]))
        username              (common-db/strip-markup (:block/string (get notif-props "athens/notification/trigger/author")) "[[" "]]")
        trigger-uid           (-> (:block/string (get notif-props "athens/notification/trigger"))
                                  (common-db/strip-markup "((" "))"))
        body                  (:block/string (common-db/get-block db [:block/uid trigger-uid]))]
    {"id"         (:block/uid notification)
     "type"       notif-type
     "isArchived" archive-state
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

(defn on-click-notification-item
  [parent-uid notification-uid]
  (do (router/navigate-uid parent-uid)
      (rf/dispatch (actions/update-state-prop notification-uid "athens/notification/is-read" "true"))))

(defn notifications-popover
  []
  (let [username (rf/subscribe [:username])]
    (fn []
      (let [notification-list (get-inbox-items-for-popover @db/dsdb (str "@" @username))]
        [:<>
         [:> Popover {:closeOnBlur true}

          [:> PopoverTrigger
           [:> IconButton {"aria-label" "Notifications"}
            [:> BellIcon]]]
          [:> PopoverContent {:maxWidth  "max-content"
                              :maxHeight "calc(100vh - 4rem)"}
           [:> PopoverCloseButton]
           [:> PopoverHeader  [:> Button {:onClick #(router/navigate-page (str "@" @username))} "Notifications" [:> ArrowRightIcon]]]
           [:> Flex {:p             0
                     :as            PopoverBody
                     :flexDirection "column"
                     :overflow      "hidden"}
            [:> InboxItemsList
             {:onOpenItem        on-click-notification-item
              :onMarkAsRead      #(rf/dispatch (actions/update-state-prop % "athens/notification/is-read" "true"))
              :onMarkAsUnread    #(rf/dispatch (actions/update-state-prop % "athens/notification/is-read" "false"))
              :onArchive         #(rf/dispatch (actions/update-state-prop % "athens/notification/is-archived" "true"))
              ;;:onUnarchive       #(rf/dispatch (actions/update-state-prop % "athens/notification/is-read" "false"))
              :notificationsList notification-list}]]]]]))))
