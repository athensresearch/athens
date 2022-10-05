(ns athens.views.notifications.popover
  (:require
    ["/components/Empty/Empty" :refer [Empty EmptyTitle EmptyIcon EmptyMessage]]
    ["/components/Icons/Icons" :refer [BellIcon ArrowRightIcon]]
    ["/components/Notifications/NotificationItem" :refer [NotificationItem]]
    ["/timeAgo.js" :refer [timeAgo]]
    ["@chakra-ui/react" :refer [Badge Text Box Heading VStack IconButton PopoverBody PopoverTrigger Popover PopoverContent PopoverCloseButton PopoverHeader Button]]
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.parse-renderer :as parse-renderer]
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
      (= type "[[athens/notification/type/comment]]")           "Comments"
      (= type "[[athens/notification/type/mention]]")           "Mentions"
      (= type "[[athens/notification/type/task/assigned/to]]")  "Assignments"
      (= type "[[athens/notification/type/task/assigned/by]]")  "Created")))


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


(def event-verb
  {"Comments" "commented on"
   "Mentions" "mentioned you in"
   "Assignments" "assigned you to"
   "Created" "created"})


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
      (let [user-page-title    (str "@" @username)
            notification-list  (get-inbox-items-for-popover @db/dsdb user-page-title)
            navigate-user-page #(router/navigate-page user-page-title)
            notifications-grouped-by-object (group-by #(get % "object") notification-list)
            num-notifications  (count notification-list)]

        [:> Popover {:closeOnBlur false
                     :isLazy true
                     :size "lg"}
         [:> PopoverTrigger
          [:> Box {:position "relative"}
           [:> IconButton {"aria-label"   "Notifications"
                           :onDoubleClick navigate-user-page
                           :onClick       (fn [e]
                                            (when (.. e -shiftKey)
                                              (rf/dispatch [:right-sidebar/open-item [:node/title user-page-title]])))
                           :icon          (r/as-element [:> BellIcon])}]
           (when (> num-notifications 0)
             [:> Badge {:position "absolute"
                        :bg "gold"
                        :pointerEvents "none"
                        :color "goldContrast"
                        :right "-3px"
                        :bottom "-1px"
                        :zIndex 1} num-notifications])]]

         [:> PopoverContent {:maxHeight "calc(var(--app-height) - 4rem)"}
          [:> PopoverCloseButton]
          [:> PopoverHeader
           [:> Button {:onClick navigate-user-page :rightIcon (r/as-element [:> ArrowRightIcon])}
            "Notifications"]]
          [:> VStack {:as PopoverBody
                      :flexDirection "column"
                      :align "stretch"
                      :overflowY "auto"
                      :overscrollBehavior "contain"
                      :spacing 6
                      :p 2}

           (doall
             (if (seq notifications-grouped-by-object)
               (for [[object notifs] notifications-grouped-by-object]

                 ^{:key (get object "parentUid")}
                 [:> VStack {:align "stretch"
                             :key (str (:parentUid object))}
                  [:> Heading {:size "xs"
                               :fontWeight "normal"
                               :noOfLines 1
                               :color "foreground.secondary"
                               :lineHeight "base"
                               :px 2
                               :pt 2}
                   [parse-renderer/parse-and-render (or (get object "name") (get object "string")) (get object "parentUid")]]

                  (for [notification notifs]

                    ^{:key (get notification "id")}
                    [:> NotificationItem
                     {:notification   notification
                      :onOpenItem     on-click-notification-item
                      :onMarkAsRead   #(rf/dispatch (actions/update-state-prop % "athens/notification/is-read" "true"))
                      :onMarkAsUnread #(rf/dispatch (actions/update-state-prop % "athens/notification/is-read" "false"))
                      :onArchive      #(rf/dispatch (actions/update-state-prop % "athens/notification/is-archived" "true"))}
                     [:> Text {:fontWeight "bold" :noOfLines 2 :fontSize "sm"}
                      (str (get-in notification ["subject" "username"]) " "
                           (get event-verb (get notification "type")) " ")
                      [parse-renderer/parse-and-render (or
                                                         (get object "name")
                                                         (get object "string"))
                       (:id notification)]]
                     [:> Text [parse-renderer/parse-and-render (get notification "body")]]])])

               [:> Empty {:size "sm" :py 8}
                [:> EmptyIcon]
                [:> EmptyTitle "All clear"]
                [:> EmptyMessage "Unread notifications will appear here."]]))]]]))))
