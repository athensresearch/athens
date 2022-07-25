(ns athens.views.notifications.core
  (:require [athens.common-events.bfs :as bfs]
            [athens.common.utils :as common.utils]
            [athens.common-events.graph.composite :as composite]
            [athens.common-db :as common-db]
            [athens.common-events.graph.ops :as graph-ops]
            [re-frame.core :as rf]))

(defn create-notif-message
  [{:keys [db inbox-block-uid notification-position notification-for-user notification-type message notification-state notification-trigger-uid notification-trigger-parent notification-trigger-author] :as opts}]
  (cond
    (= notification-type  "athens/notification/type/comment")
    (str "**" notification-trigger-author "** " "commented on: " "**((" notification-trigger-parent "))**" "\n"
         "***((" notification-trigger-uid "))***")

    (= notification-type  "athens/notification/type/mention")
    (str "**" notification-trigger-author "** " "mentioned you: " "**((" notification-trigger-uid"))**")))


(defn new-notification
  [{:keys [db inbox-block-uid notification-position notification-for-user notification-type message notification-state notification-trigger-uid notification-trigger-parent notification-trigger-author] :as opts}]
  ;; notification-from-block can be used to show context for the notification
  ;; notification-type: "*-notification" for e.g "task-notification", "athens/notification/type/comment"
  (->> (bfs/internal-representation->atomic-ops
         db
         [#:block{:uid        (common.utils/gen-block-uid)
                  :string     (create-notif-message opts);; Should the string be message or the breadcrumb or something else?
                  :open?      false
                  :properties {":entity/type"
                               #:block{:string "notification"
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/type"
                               #:block{:string notification-type
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/trigger"
                               #:block{:string (str "((" notification-trigger-uid "))")
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/for-user"
                               #:block{:string notification-for-user
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/trigger/author"
                               #:block{:string notification-trigger-author
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/state"
                               #:block{:string notification-state
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/trigger/parent"
                               #:block{:string  (str "((" notification-trigger-parent "))")
                                       :uid    (common.utils/gen-block-uid)}}}]
         {:block/uid inbox-block-uid
          :relation  notification-position})
       (composite/make-consequence-op {:op/type :new-notification})))


(defn update-notification-state
  [notification-block-uid to-state]
  (rf/dispatch [:properties/update-in [:block/uid notification-block-uid] ["athens/notification/state"]
                       (fn [db prop-uid]
                         [(graph-ops/build-block-save-op db prop-uid to-state)])]))




(defn get-inbox-uid-for-user
  [db username]
  (let [page-uid       (common-db/get-page-uid db username)
        page-children  (:block/children (common-db/get-page-document db [:block/uid page-uid]))
        inbox-uid      (:block/uid (first (filter
                                            #(when (= "Comments inbox" (:block/string %))
                                               %)
                                            page-children)))]
    inbox-uid))


(defn create-comments-inbox
  [db userpage]
  (let [inbox-uid    (common.utils/gen-block-uid)]
    [[(->> (bfs/internal-representation->atomic-ops
             db
             [#:block{:uid        inbox-uid
                      :string     "Comments inbox"
                      :properties {":entity/type"
                                   #:block{:string "athens/inbox/type/comments"
                                           :uid    (common.utils/gen-block-uid)}}}]
             {:relation :first
              :page/title userpage})
           (composite/make-consequence-op {:op/type :new-inboxx}))]
     inbox-uid]))


(defn create-userpage
  [db userpage-name]
  (let [new-page-op        [(graph-ops/build-page-new-op db userpage-name)]
        [comments-inbox-op
         inbox-uid]        (create-comments-inbox db userpage-name)
        userpage-inbox-op  (concat new-page-op
                                   comments-inbox-op)]
    [userpage-inbox-op inbox-uid]))






(defn get-subscriber-data
  ;; Returns a list of all the subscriber maps
  ;; {:inbox-uid "some-uid"
  ;;  :name      "subscriber-name"}
  ;; Someone can subscribe to a thread without having the userpage, inbox or both.
  ;; So in that case we need to create these depending on the situation.
  [db userpage]
  (let [user-exists?         (common-db/get-page-uid db userpage)
        [new-userpage-op
         comments-inbox-uid] (when (not user-exists?)
                               (create-userpage db userpage))
        ;; inbox-uid being nil means user page exists but inbox does not
        inbox-uid            (or (get-inbox-uid-for-user db userpage)
                                 comments-inbox-uid)
        [new-inbox-op
         inbox-uid]          (if (not inbox-uid)
                               (create-comments-inbox db userpage)
                               [[] inbox-uid])
        userpage-inbox-op    (concat []
                                     new-userpage-op
                                     new-inbox-op)]
    {:inbox-uid         inbox-uid
     :userpage          userpage
     :userpage-inbox-op userpage-inbox-op}))


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
        username              (:block/string (get notif-props "athens/notification/trigger/author"))]
    {"id"         (:block/uid notification)
     "type"       notif-type
     "isArchived" (:isArchived notif-state)
     "isRead"     (:isRead     notif-state)
     "object"     {"name" trigger-parent-string}
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