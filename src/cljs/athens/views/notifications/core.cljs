(ns athens.views.notifications.core
  (:require
    [athens.common-db :as common-db]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common.utils :as common.utils]
    [re-frame.core :as rf]))


(defn enabled?
  []
  (:notifications @(rf/subscribe [:feature-flags])))


(defn create-notif-message
  [{:keys [notification-type notification-trigger-uid notification-trigger-parent notification-trigger-author notification-for-user] :as _opts}]
  (cond
    (= notification-type  "athens/notification/type/comment")
    (str "**" notification-trigger-author "** " "commented on: " "**((" notification-trigger-parent "))**" "\n"
         "***((" notification-trigger-uid "))***")

    (= notification-type  "athens/notification/type/mention")
    (str "**" notification-trigger-author "** " "mentioned you: " "**((" notification-trigger-uid "))**")

    (= notification-type "athens/notification/type/task/assigned/to")
    (str "**" notification-trigger-author "** " "assigned you task: " "***((" notification-trigger-uid "))***")

    (= notification-type "athens/notification/type/task/assigned/by")
    (str "You assigned a new task: " "***((" notification-trigger-uid "))*** to " notification-for-user)))


(defn new-notification
  [{:keys [db inbox-block-uid notification-position notification-for-user notification-type notification-trigger-uid notification-trigger-parent notification-trigger-author] :as opts}]
  ;; notification-from-block can be used to show context for the notification
  ;; notification-type: "*-notification" for e.g "task-notification", "athens/notification/type/comment"
  (->> (bfs/internal-representation->atomic-ops
         db
         [#:block{:uid        (common.utils/gen-block-uid)
                  :string     (create-notif-message opts); Should the string be message or the breadcrumb or something else?
                  :open?      false
                  :properties {":entity/type"
                               #:block{:string "[[athens/notification]]"
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/type"
                               #:block{:string (str "[[" notification-type "]]")
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/trigger"
                               #:block{:string (str "((" notification-trigger-uid "))")
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/for-user"
                               #:block{:string notification-for-user
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/trigger/author"
                               #:block{:string (str "[[@" notification-trigger-author "]]")
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/is-archived"
                               #:block{:string  "false"
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/is-read"
                               #:block{:string  "false"
                                       :uid    (common.utils/gen-block-uid)}
                               "athens/notification/trigger/parent"
                               #:block{:string  (str "((" notification-trigger-parent "))")
                                       :uid    (common.utils/gen-block-uid)}}}]
         {:block/uid inbox-block-uid
          :relation  notification-position})
       (composite/make-consequence-op {:op/type :new-notification})))


(defn get-inbox-uid-for-user
  [db at-username]
  (let [page-uid       (common-db/get-page-uid db at-username)
        inbox-document (common-db/get-block-property-document db [:block/uid page-uid])
        inbox-uid      (:block/uid (get inbox-document "athens/inbox"))]
    inbox-uid))


(defn create-comments-inbox
  [db at-username]
  (let [inbox-uid    (common.utils/gen-block-uid)]
    [[(->> (bfs/internal-representation->atomic-ops
             db
             [#:block{:uid        inbox-uid
                      :string     ""
                      :properties {":entity/type"
                                   #:block{:string "[[athens/inbox]]"
                                           :uid    (common.utils/gen-block-uid)}}}]
             {:relation {:page/title "athens/inbox"}
              :page/title at-username})
           (composite/make-consequence-op {:op/type :new-inbox}))]
     inbox-uid]))


(defn create-userpage
  [db at-username]
  (let [new-page-op        [(graph-ops/build-page-new-op db at-username)]
        [comments-inbox-op
         inbox-uid]        (create-comments-inbox db at-username)
        userpage-inbox-op  (concat new-page-op
                                   comments-inbox-op)]
    [userpage-inbox-op inbox-uid]))


(defn get-userpage-data
  ;; Returns a list of all the subscriber maps
  ;; {:inbox-uid "some-uid"
  ;;  :name      "subscriber-name"}
  ;; Someone can subscribe to a thread without having the userpage, inbox or both.
  ;; So in that case we need to create these depending on the situation.
  [db userpage]
  (let [at-username          (common-db/strip-markup userpage "[[" "]]")
        user-exists?         (common-db/get-page-uid db at-username)
        [new-userpage-op
         comments-inbox-uid] (when (not user-exists?)
                               (create-userpage db at-username))
        ;; inbox-uid being nil means user page exists but inbox does not
        inbox-uid            (or (get-inbox-uid-for-user db at-username)
                                 comments-inbox-uid)
        [new-inbox-op
         inbox-uid]          (if (not inbox-uid)
                               (create-comments-inbox db at-username)
                               [[] inbox-uid])
        userpage-inbox-op    (concat []
                                     new-userpage-op
                                     new-inbox-op)]
    {:inbox-uid         inbox-uid
     :userpage          userpage
     :userpage-inbox-op userpage-inbox-op}))
