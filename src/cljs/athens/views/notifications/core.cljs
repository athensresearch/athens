(ns athens.views.notifications.core
  (:require [athens.common-events.bfs :as bfs]
            [athens.common.utils :as common.utils]
            [athens.common-events.graph.composite :as composite]
            [athens.common-db :as common-db]
            [athens.common-events.graph.ops :as graph-ops]
            [re-frame.core :as rf]))


(defn new-notification
  [db inbox-block-uid notification-position notification-type message state notification-from-block]
  ;; notification-from-block can be used to show context for the notification
  ;; notification-type: "*-notification" for e.g "task-notification", "comment-notification"
  (->> (bfs/internal-representation->atomic-ops
         db
         [#:block{:uid        (common.utils/gen-block-uid)
                  :string     message;; Should the string be message or the breadcrumb or something else?
                  :open?      false
                  :properties {":block/type"
                               #:block{:string "notification"
                                       :uid    (common.utils/gen-block-uid)}
                               ":notification/type"
                               #:block{:string notification-type
                                       :uid    (common.utils/gen-block-uid)}
                               ":notification/message"
                               #:block{:string message
                                       :uid    (common.utils/gen-block-uid)}
                               ":notification/state"
                               #:block{:string state
                                       :uid    (common.utils/gen-block-uid)}
                               ":notification/from-block"
                               #:block{:string (str "((" notification-from-block "))")
                                       :uid    (common.utils/gen-block-uid)}}}]
         {:block/uid inbox-block-uid
          :relation  notification-position})
       (composite/make-consequence-op {:op/type :new-notification})))


(defn update-notification-state
  [notification-block-uid to-state]
  (rf/dispatch [:properties/update-in [:block/uid notification-block-uid] [":notification/state"]
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
  [db username]
  (let [inbox-uid    (common.utils/gen-block-uid)
        block-new-op (graph-ops/build-block-new-op db
                                                   inbox-uid
                                                   {:relation   :first
                                                    :page/title username})
        block-save-op (graph-ops/build-block-save-op db inbox-uid "Comments inbox")
        new-save-op   [block-new-op
                       block-save-op]]
    [new-save-op inbox-uid]))


(defn create-userpage
  [db username]
  (let [new-page-op        [(graph-ops/build-page-new-op db username)]
        [comments-inbox-op
         inbox-uid]        (create-comments-inbox db username)
        userpage-inbox-op  (concat new-page-op
                                   comments-inbox-op)]
    [userpage-inbox-op inbox-uid]))






(defn get-subscriber-data
  ;; Someone can subscribe to a thread without having the userpage, inbox or both.
  ;; So in that case we need to create these depending on the situation.
  ;; Returns a list of all the subscriber maps
  ;; {:inbox-uid "some-uid"
  ;;  :name      "subscriber-name"}
  [db username]
  (let [user-exists?         (common-db/get-page-uid db username)
        [new-userpage-op
         comments-inbox-uid] (when (not user-exists?)
                               (create-userpage db username))
        ;; inbox-uid being nil means user page exists but inbox does not
        inbox-uid            (or (get-inbox-uid-for-user db username)
                                 comments-inbox-uid)
        [new-inbox-op
         inbox-uid]          (if (not inbox-uid)
                               (create-comments-inbox db username)
                               [[] inbox-uid])
        userpage-inbox-op    (concat []
                                     new-userpage-op
                                     new-inbox-op)]
    {:inbox-uid         inbox-uid
     :username          username
     :userpage-inbox-op userpage-inbox-op}))




