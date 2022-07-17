(ns athens.views.comments.core
  (:require [athens.db :as db]
            [re-frame.core :as rf]
            [athens.common-db :as common-db]
            [athens.common.utils :as common.utils]
            [athens.common-events.graph.composite :as composite]
            [athens.views.notifications.core  :refer [get-subscriber-data new-notification]]
            [athens.common-events.graph.ops :as graph-ops]
            [athens.common-events.bfs :as bfs]
            [athens.common-events :as common-events]))


(rf/reg-sub
  :comment/show-comment-textarea?
  (fn [db [_ uid]]
    (= uid (:comment/show-comment-textarea db))))

(rf/reg-event-fx
  :comment/show-comment-textarea
  (fn [{:keys [db]} [_ uid]]
    {:db (assoc db :comment/show-comment-textarea uid)}))

(rf/reg-event-fx
  :comment/hide-comment-textarea
  (fn [{:keys [db]} [_ uid]]
    {:db (assoc db :comment/show-comment-textarea nil)}))

(rf/reg-sub
  :comment/show-inline-comments?
  (fn [db [_]]
    (println "sub toggle inline comments"(= true (:comment/show-inline-comments db)))
    (= true (:comment/show-inline-comments db))))

(rf/reg-event-fx
  :comment/toggle-inline-comments
  (fn [{:keys [db]} [_]]
    (let [current-state (:comment/show-inline-comments db)]
      (println "toggle inline comments" current-state)
      {:db (assoc db :comment/show-inline-comments (not current-state))})))


(defn thread-child->comment
  [comment-block]
  (let [comment-uid (:block/uid comment-block)
        properties  (common-db/get-block-property-document @db/dsdb [:block/uid comment-uid])]
    {:block/uid comment-uid
     :string (:block/string comment-block)
     :author (:block/string (get properties ":comment/author"))
     :time   (:block/string (get properties ":comment/time"))}))


(defn get-comments-in-thread
  [db thread-uid]
  (->> (common-db/get-block-document db [:block/uid thread-uid])
       :block/children
       (map thread-child->comment)))


(defn get-comment-thread-uid
  [db parent-block-uid]
  (-> (common-db/get-block-property-document @db/dsdb [:block/uid parent-block-uid])
      ;; TODO Multiple threads
      ;; I think for multiple we would have a top level property for all threads
      ;; Individual threads are child of the property
      (get ":comment/threads")
      :block/uid))


(defn new-comment
  [db thread-uid comment-string author time]
  (->> (bfs/internal-representation->atomic-ops db
                                                [#:block{:uid    (common.utils/gen-block-uid)
                                                         :string comment-string
                                                         :properties
                                                         {":block/type"     #:block{:string "comment"
                                                                                    :uid    (common.utils/gen-block-uid)}
                                                          ":comment/author" #:block{:string author
                                                                                    :uid    (common.utils/gen-block-uid)}
                                                          ":comment/time"   #:block{:string time
                                                                                    :uid    (common.utils/gen-block-uid)}}}]
                                                {:block/uid thread-uid
                                                 :relation  :last})
       (composite/make-consequence-op {:op/type :new-comment})))

(defn new-thread
  [db thread-uid thread-name block-uid author]
  (->> (bfs/internal-representation->atomic-ops db
                                                [#:block{:uid    thread-uid
                                                         :string thread-name
                                                         :properties
                                                         {":block/type"          #:block{:string "comment/thread"
                                                                                         :uid    (common.utils/gen-block-uid)}
                                                          ":thread/members"      #:block{:string   "comment/thread"
                                                                                         :uid      (common.utils/gen-block-uid)
                                                                                         :children [#:block{:string author
                                                                                                            :uid    (common.utils/gen-block-uid)}]}
                                                          ":thread/subscribers"  #:block{:string   "comment/thread"
                                                                                         :uid      (common.utils/gen-block-uid)
                                                                                         :children [#:block{:string author
                                                                                                            :uid    (common.utils/gen-block-uid)}]}}}]
                                                {:block/uid block-uid
                                                 :relation  :last})
       (composite/make-consequence-op {:op/type :new-thread})))


(defn get-thread-property
  [db thread-uid property-name]
  (get (common-db/get-block-property-document db [:block/uid thread-uid]) property-name))


(defn user-in-thread-as?
  [db member-or-subscriber thread-uid username]
  (filter
    #(= (str "@" username) (:block/string %))
    (:block/children (get-thread-property db thread-uid member-or-subscriber))))



(defn add-new-member-or-subscriber-to-thread
  [db thread-uid property-name username]
  (let [thread-prop-uid   (:block/uid (get-thread-property db thread-uid property-name))]
    (bfs/internal-representation->atomic-ops db
                                             [#:block{:uid    (common.utils/gen-block-uid)
                                                      :string (str "@" username)}]
                                             {:block/uid thread-prop-uid
                                              :relation  :last})))



(defn unsubscribe-from-thread
  [db thread-uid username]
  (-> (:block/children (get-thread-property db thread-uid ":thread/subscribers"))
      (filter
        #((= username (:block/string %))))
      (:block/uid)
      [:block/uid]
      [:db/retractEntity]))


(defn add-user-as-member-or-subscriber?
  [db thread-uid username]
  (let [user-member?       (user-in-thread-as? db ":thread/members" thread-uid username)
        user-subscriber?   (user-in-thread-as? db ":thread/subscribers" thread-uid username)]
    (cond-> []
            (empty? user-member?)     (concat (add-new-member-or-subscriber-to-thread db thread-uid ":thread/members" username))
            (empty? user-subscriber?) (concat (add-new-member-or-subscriber-to-thread db thread-uid ":thread/subscribers" username)))))

;; Notifications

(defn get-subscribers-for-notifying
  [db thread-uid author]
  (->> (:block/children (get-thread-property db thread-uid ":thread/subscribers"))
       (filter #(not= (:block/string %) (str "@" author)))
       (map #(:block/string %))))

(defn create-notification-op-for-users
  ;; Find all the subscribed members to the thread
  ;; Find the uid of the inbox for these notifications for all the subscribers
  ;; Create a notification for all the subscribers, apart from the subscriber who wrote the comment.
  [db parent-block-uid users author notification-message]
  (let [subscriber-data (map
                          #(get-subscriber-data db %)
                          users)
        notifications    (into [] (map
                                    #(let [{:keys [inbox-uid username userpage-inbox-op]}  %
                                           author                                          (str "@" author)]
                                       (when (not= username author)
                                         (composite/make-consequence-op {:op/type :userpage-notification-op}
                                                                        (concat userpage-inbox-op
                                                                                [(new-notification db
                                                                                                   inbox-uid
                                                                                                   :first
                                                                                                   "comment-notification"
                                                                                                   notification-message
                                                                                                   "unread"
                                                                                                   parent-block-uid)]))))

                                    subscriber-data))
        ops              notifications]
    ops))


(defn create-notification-op-for-comment
  ;; Find all the subscribed members to the thread
  ;; Find the uid of the inbox for these notifications for all the subscribers
  ;; Create a notification for all the subscribers, apart from the subscriber who wrote the comment.
  [db parent-block-uid thread-uid author notification-message]
  (let [subscribers (get-subscribers-for-notifying db thread-uid author)]
    (when subscribers
      (create-notification-op-for-users db parent-block-uid subscribers author notification-message))))


(def athens-users
  ["@Stuart" "@Alex" "@Jeff" "@Filipe" "@Sid"])

(defn get-all-mentions
  [block-string]
  (filter
    #(when (clojure.string/includes? block-string %)
       %)
    athens-users))

(defn create-mention-notifications
  [db block-uid mentioned-users author block-string]
  (let [notification-message  (str  "A new mention bitch: " block-string)
        notification-ops      (create-notification-op-for-users db block-uid mentioned-users author notification-message)]
    notification-ops))


(rf/reg-event-fx
  :comment/write-comment
  (fn [{db :db} [_ uid comment-string author]]
    (let [thread-exists?            (get-comment-thread-uid @db/dsdb uid)
          thread-uid                (or thread-exists?
                                       (common.utils/gen-block-uid))
          active-comment-ops        (concat (if thread-exists?
                                              []
                                              [(new-thread @db/dsdb thread-uid "" uid author)
                                               (graph-ops/build-block-move-op @db/dsdb thread-uid {:block/uid uid
                                                                                                   :relation  {:page/title ":comment/threads"}})])
                                            [(new-comment @db/dsdb  thread-uid comment-string author "12:09 pm")])
          add-as-mem-or-subs        (when thread-exists?
                                      (add-user-as-member-or-subscriber? @db/dsdb thread-uid author))
          notification-message      (str  "New notification bitch" comment-string)
          notification-op           (create-notification-op-for-comment @db/dsdb uid thread-uid author notification-message)
          comment-notif-op          (composite/make-consequence-op {:op/type :comment-notif-op}
                                                                   (concat add-as-mem-or-subs notification-op active-comment-ops))
          event                     (common-events/build-atomic-event comment-notif-op)]
     {:fx [[:dispatch [:resolve-transact-forward event]]]})))