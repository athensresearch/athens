(ns athens.views.comments.core
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.composite :as composite]
    [athens.views.notifications.core  :refer [get-subscriber-data new-notification]]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common.utils :as common.utils]
    [athens.db :as db]
    [re-frame.core :as rf]))


(defn enabled?
  []
  (:comments @(rf/subscribe [:feature-flags])))


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
  (fn [{:keys [db]} [_]]
    {:db (assoc db :comment/show-comment-textarea nil)}))


(rf/reg-sub
  :comment/show-inline-comments?
  (fn [db [_]]
    (= true (:comment/show-inline-comments db))))


(rf/reg-event-fx
  :comment/toggle-inline-comments
  (fn [{:keys [db]} [_]]
    (let [current-state (:comment/show-inline-comments db)]
      (println "toggle inline comments" current-state)
      {:db (assoc db :comment/show-inline-comments (not current-state))})))


(defn thread-child->comment
  [comment-block]
  (let [comment-uid (:block/uid comment-block)]
    {:block/uid comment-uid
     :string (:block/string comment-block)
     :author (-> comment-block :block/create :event/auth :presence/id)
     :time   (-> comment-block :block/create :event/time :time/ts)}))


(defn get-comments-in-thread
  [db thread-uid]
  (->> (common-db/get-block-document db [:block/uid thread-uid])
       :block/children
       (map thread-child->comment)))


(defn get-comment-thread-uid
  [_db parent-block-uid]
  (-> (common-db/get-block-property-document @db/dsdb [:block/uid parent-block-uid])
      ;; TODO Multiple threads
      ;; I think for multiple we would have a top level property for all threads
      ;; Individual threads are child of the property
      (get ":comment/threads")
      :block/uid))


(defn new-comment
  [db thread-uid comment-string]
  (let [comment-uid (common.utils/gen-block-uid)]
   {:comment-uid comment-uid
    :comment-op  (->> (bfs/internal-representation->atomic-ops db
                                                               [#:block{:uid    comment-uid
                                                                        :string comment-string
                                                                        :properties
                                                                        {":entity/type" #:block{:string "athens/comment"
                                                                                                :uid    (common.utils/gen-block-uid)}}}]
                                                               {:block/uid thread-uid
                                                                :relation  :last})
                      (composite/make-consequence-op {:op/type :new-comment}))}))


(defn new-thread
  [db thread-uid thread-name block-uid author]
  (let [members-prop-uid  (common.utils/gen-block-uid)
        subs-prop-uid     (common.utils/gen-block-uid)
        new-thread-op     (->> (bfs/internal-representation->atomic-ops db
                                                                        [#:block{:uid    thread-uid
                                                                                 :string thread-name
                                                                                 :properties
                                                                                 {":entity/type"                       #:block{:string "athens/comment-thread"
                                                                                                                               :uid    (common.utils/gen-block-uid)}
                                                                                  "athens/comment-thread/members"      #:block{:string   "athens/comment-thread/members"
                                                                                                                               :uid      members-prop-uid
                                                                                                                               :children [#:block{:string (str "[[@" author "]]")
                                                                                                                                                  :uid    (common.utils/gen-block-uid)}]}
                                                                                  "athens/comment-thread/subscribers"  #:block{:string   "athens/comment-thread/subscribers"
                                                                                                                               :uid      subs-prop-uid
                                                                                                                               :children [#:block{:string (str "[[@" author "]]")
                                                                                                                                                  :uid    (common.utils/gen-block-uid)}]}}}]
                                                                        {:block/uid block-uid
                                                                         :relation  :last})
                               (composite/make-consequence-op {:op/type :new-thread}))]
    {:members-prop-uid     members-prop-uid
     :subscribers-prop-uid subs-prop-uid
     :new-thread-op        new-thread-op}))


(defn get-thread-property
  [db thread-uid property-name]
  (get (common-db/get-block-property-document db [:block/uid thread-uid]) property-name))


(defn user-in-thread-as?
  [db member-or-subscriber thread-uid userpage]
  (filter
    #(= userpage (:block/string %))
    (:block/children (get-thread-property db thread-uid member-or-subscriber))))



(defn add-new-member-or-subscriber-to-thread
  [db thread-uid property-name userpage]
  (let [thread-prop-uid   (:block/uid (get-thread-property db thread-uid property-name))]
    (bfs/internal-representation->atomic-ops db
                                             [#:block{:uid    (common.utils/gen-block-uid)
                                                      :string  userpage}]
                                             {:block/uid thread-prop-uid
                                              :relation  :last})))

(defn add-new-member-or-subscriber-to-prop-uid
  [db thread-prop-uid userpage]
  (bfs/internal-representation->atomic-ops db
                                           [#:block{:uid    (common.utils/gen-block-uid)
                                                    :string  userpage}]
                                           {:block/uid thread-prop-uid
                                            :relation  :last}))


(defn unsubscribe-from-thread
  [db thread-uid userpage]
  (-> (:block/children (get-thread-property db thread-uid "athens/comment-thread/subscribers"))
      (filter
        #((= userpage (:block/string %))))
      (:block/uid)
      [:block/uid]
      [:db/retractEntity]))


(defn add-user-as-member-or-subscriber?
  [db thread-uid userpage]
  (let [user-member?       (user-in-thread-as? db "athens/comment-thread/members" thread-uid userpage)
        user-subscriber?   (user-in-thread-as? db "athens/comment-thread/subscribers" thread-uid userpage)]
    (cond-> []
            (empty? user-member?)     (concat (add-new-member-or-subscriber-to-thread db thread-uid "athens/comment-thread/members" userpage))
            (empty? user-subscriber?) (concat (add-new-member-or-subscriber-to-thread db thread-uid "athens/comment-thread/subscribers" userpage)))))

;; Notifications

(defn get-subscribers-for-notifying
  [db thread-uid author]
  (->> (:block/children (get-thread-property db thread-uid "athens/comment-thread/subscribers"))
       (filter #(not= (:block/string %) (str "[[@" author "]]")))
       (map #(:block/string %))))

(defn create-notification-op-for-users
  ;; Find all the subscribed members to the thread
  ;; If the user does not have a userpage or inbox we create it
  ;; Find the uid of the inbox for these notifications for all the subscribers
  ;; Create a notification for all the subscribers, apart from the subscriber who wrote the comment.
  [db parent-block-uid users author notification-message trigger-block-uid notification-type]
  (let [subscriber-data (map
                          #(get-subscriber-data db %)
                          users)
        notifications   (mapv
                            #(let [{:keys [inbox-uid userpage userpage-inbox-op]}  %]
                               (composite/make-consequence-op {:op/type :userpage-notification-op}
                                                              (concat userpage-inbox-op
                                                                      [(new-notification {:db                          db
                                                                                          :inbox-block-uid             inbox-uid
                                                                                          :notification-position       :first
                                                                                          :notification-type           notification-type
                                                                                          :notification-message        notification-message
                                                                                          :notification-state          "unread"
                                                                                          :notification-trigger-uid    trigger-block-uid
                                                                                          :notification-trigger-parent parent-block-uid
                                                                                          :notification-trigger-author author
                                                                                          :notification-for-user       userpage})])))
                            subscriber-data)
        ops              notifications]
    ops))



(def athens-users
  ["[[@Stuart]]" "[[@Alex]]" "[[@Jeff]]" "[[@Filipe]]" "[[@Sid]]"])

(defn get-all-mentions
  [block-string exclude]
  (filter
    #(when (and (clojure.string/includes? block-string %)
                (not (= exclude (common-db/strip-markup % "[[@" "]]"))))
       %)
    athens-users))

(defn create-mention-notifications
  [db block-uid mentioned-users author block-string]
  (let [notification-message  (str "**[[@" author "]] mentioned you: **" "*"  block-string "*")
        notification-ops      (create-notification-op-for-users db block-uid mentioned-users author notification-message block-uid "athens/notification/type/mention")]
    notification-ops))

(defn add-mentioned-users-as-member-and-subscriber
  [db thread-members-uid thread-subs-uid comment-string thread-uid thread-exists? author]
  (if thread-exists?
    (mapcat
        #(add-user-as-member-or-subscriber? @db/dsdb thread-uid %)
        (get-all-mentions comment-string author))
    (mapcat
      #(concat []
               (add-new-member-or-subscriber-to-prop-uid db thread-members-uid %)
               (add-new-member-or-subscriber-to-prop-uid db thread-subs-uid %))
      (get-all-mentions comment-string author))))


(defn create-notification-op-for-comment
  ;; Find all the subscribed members to the thread
  ;; Find the uid of the inbox for these notifications for all the subscribers
  ;; Create a notification for all the subscribers, apart from the subscriber who wrote the comment.
  [db parent-block-uid thread-uid author comment-string notification-message comment-block-uid]
  (let [subscribers (if (empty? (get-all-mentions comment-string author))
                      (get-subscribers-for-notifying db thread-uid author)
                      (concat (get-subscribers-for-notifying db thread-uid author)
                              (get-all-mentions comment-string author)))]
    (when subscribers
      (create-notification-op-for-users db parent-block-uid subscribers author notification-message comment-block-uid "athens/notification/type/comment"))))

(rf/reg-event-fx
  :comment/write-comment
  ;; There is a sequence for how the operations are to be executed because for some ops, information
  ;; related to prior op is needed. The sequence is:
  ;; - Create a thread if it does not exist with the author as member and subscriber to the thread.
  ;; - Add comment to the thread.
  ;; - If the comment contains mentions to users not subscribed to the thread, then add them as subscribers and members.
  ;; - If this is not the first comment on the thread then add the author of comment as subscriber and member to the thread.
  ;; - Create notifications for the subscribed members.

  (fn [{db :db} [_ uid comment-string author]]
    (let [thread-exists?                             (get-comment-thread-uid @db/dsdb uid)
          thread-uid                                 (or thread-exists?
                                                        (common.utils/gen-block-uid))

          {thread-members-uid :members-prop-uid
           thread-subs-uid    :subscribers-prop-uid
           new-thread-op      :new-thread-op}        (when (not thread-exists?)
                                                           (new-thread @db/dsdb thread-uid "" uid author))
          new-thread-op                              (if thread-exists?
                                                       []
                                                       [new-thread-op
                                                        (graph-ops/build-block-move-op @db/dsdb thread-uid {:block/uid uid
                                                                                                            :relation  {:page/title ":comment/threads"}})])
          {comment-uid :comment-uid
           comment-op  :comment-op}                  (new-comment @db/dsdb thread-uid comment-string)
          add-mentions-in-str-as-mem-subs-op         (add-mentioned-users-as-member-and-subscriber @db/dsdb thread-members-uid thread-subs-uid comment-string thread-uid thread-exists? author)
          add-author-as-mem-or-subs                  (when thread-exists?
                                                       (add-user-as-member-or-subscriber? @db/dsdb thread-uid (str "[[@" author "]]")))

          notification-message                       (str "**((" uid "))**" "\n"
                                                          "*[[@" author "]] commented: "  comment-string "*")
          notification-op                            (create-notification-op-for-comment @db/dsdb uid thread-uid author comment-string notification-message comment-uid)
          ops                                        (concat add-author-as-mem-or-subs
                                                             new-thread-op
                                                             [comment-op]
                                                             add-mentions-in-str-as-mem-subs-op
                                                             notification-op)

          comment-notif-op                           (composite/make-consequence-op {:op/type :comment-notif-op}
                                                                                    ops)
          event                                     (common-events/build-atomic-event comment-notif-op)]
     {:fx [[:dispatch [:resolve-transact-forward event]]]})))