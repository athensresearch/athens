(ns athens.views.comments.core
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common.utils :as common.utils]
    [athens.db :as db]
    [athens.views.notifications.core  :refer [get-userpage-data new-notification]]
    [re-frame.core :as rf]))


(defn enabled?
  []
  (:comments @(rf/subscribe [:feature-flags])))


(rf/reg-sub
  :comment/show-editor?
  (fn [db [_ uid]]
    (= uid (:comment/show-editor db))))


(rf/reg-event-fx
  :comment/show-editor
  (fn [{:keys [db]} [_ uid]]
    {:db (assoc db :comment/show-editor uid)}))


(rf/reg-event-fx
  :comment/hide-editor
  (fn [{:keys [db]} [_]]
    {:db (assoc db :comment/show-editor nil)}))


(rf/reg-sub
  :comment/show-comments?
  (fn [db [_]]
    (= true (:comment/show-comments? db))))


(rf/reg-event-db
  :comment/toggle-comments
  (fn [db [_]]
    (update db :comment/show-comments? not)))


(defn thread-child->comment
  [comment-block]
  (let [{:block/keys [uid string create properties]} comment-block]
    {:block/uid uid
     :string    string
     :author    (-> create :event/auth :presence/id)
     :time      (-> create :event/time :time/ts)
     :edited?   (boolean (get properties "athens/comment/edited"))}))


(defn add-is-follow-up?
  [comments-data]
  (let [is-followups (for [i (range (count comments-data))]
                       (let [prev-item (nth comments-data (dec i) nil)
                             curr-item (nth comments-data i)
                             {prev-author :author prev-time :time} prev-item
                             {curr-author :author curr-time :time} curr-item
                             time-delta (- curr-time prev-time)
                             ;; hard-code to 30 minutes for now (* 1000 60 30)
                             greater-than-time-delta? (> time-delta 1800000)]
                         {:is-followup? (cond
                                          (zero? i) false
                                          greater-than-time-delta? false
                                          (and (= prev-author curr-author)
                                               (seq prev-author)
                                               (seq curr-author)) true
                                          :else false)}))]
    (mapv merge comments-data is-followups)))


(defn get-comments-in-thread
  [db thread-uid]
  (->> (common-db/get-block-document db [:block/uid thread-uid])
       :block/children
       (map thread-child->comment)
       add-is-follow-up?))


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
                                                                         {":entity/type" #:block{:string "[[athens/comment]]"
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
                                                                                 {":entity/type"                       #:block{:string "[[athens/comment-thread]]"
                                                                                                                               :uid    (common.utils/gen-block-uid)}
                                                                                  "athens/comment-thread/members"      #:block{:string   ""
                                                                                                                               :uid      members-prop-uid
                                                                                                                               :children [#:block{:string (str "[[@" author "]]")
                                                                                                                                                  :uid    (common.utils/gen-block-uid)}]}
                                                                                  "athens/comment-thread/subscribers"  #:block{:string   ""
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


#_(defn unsubscribe-from-thread
    [db thread-uid userpage]
    (->> (:block/children (get-thread-property db thread-uid "athens/comment-thread/subscribers"))
         (filter #(= userpage (:block/string %)))
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
  [{:keys [db parent-block-uid notification-for-users author trigger-block-uid notification-type]}]
  (let [subscriber-data (map
                          #(get-userpage-data db %)
                          notification-for-users)
        notifications   (mapv
                          #(let [{:keys [inbox-uid userpage userpage-inbox-op]}  %]
                             (composite/make-consequence-op {:op/type :userpage-notification-op}
                                                            (concat userpage-inbox-op
                                                                    [(new-notification {:db                          db
                                                                                        :inbox-block-uid             inbox-uid
                                                                                        :notification-position       :first
                                                                                        :notification-type           notification-type
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
  [db parent-block-uid thread-uid author comment-string comment-block-uid]
  (let [subscribers (if (empty? (get-all-mentions comment-string author))
                      (get-subscribers-for-notifying db thread-uid author)
                      (set (concat (get-subscribers-for-notifying db thread-uid author)
                                   (get-all-mentions comment-string author))))]
    (when subscribers
      (create-notification-op-for-users {:db                     @db/dsdb
                                         :parent-block-uid       parent-block-uid
                                         :notification-for-users subscribers
                                         :author                 author
                                         :trigger-block-uid      comment-block-uid
                                         :notification-type      "athens/notification/type/comment"}))))


(rf/reg-event-fx
  :comment/write-comment
  ;; There is a sequence for how the operations are to be executed because for some ops, information
  ;; related to prior op is needed. The sequence is:
  ;; - Create a thread if it does not exist with the block and the comment author as member and subscriber to the thread.
  ;; - Add comment to the thread.
  ;; - If the comment contains mentions to users not subscribed to the thread, then add them as subscribers and members.
  ;; - If this is not the first comment on the thread then add the author of comment as subscriber and member to the thread.
  ;; - Create notifications for the subscribed members.

  (fn [{_db :db} [_ uid comment-string author]]
    (let [thread-exists?                             (get-comment-thread-uid @db/dsdb uid)
          thread-uid                                 (or thread-exists?
                                                         (common.utils/gen-block-uid))
          block-author                               (-> (common-db/get-block-document @db/dsdb [:block/uid uid])
                                                         :block/create
                                                         :event/auth
                                                         :presence/id)
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
          [add-block-author-as-sub-and-mem
           block-author-notification-op]             (when (and
                                                             (not= author block-author)
                                                             (not thread-exists?))
                                                       [(concat []
                                                                (add-new-member-or-subscriber-to-prop-uid @db/dsdb thread-members-uid (str "[[@" block-author "]]"))
                                                                (add-new-member-or-subscriber-to-prop-uid @db/dsdb thread-subs-uid (str "[[@" block-author "]]")))
                                                        (create-notification-op-for-users {:db                     @db/dsdb
                                                                                           :parent-block-uid       uid
                                                                                           :notification-for-users [(str "[[@" block-author "]]")]
                                                                                           :author                 author
                                                                                           :trigger-block-uid      comment-uid
                                                                                           :notification-type      "athens/notification/type/comment"})])
          notification-op                            (create-notification-op-for-comment @db/dsdb uid thread-uid author comment-string comment-uid)
          ops                                        (concat add-author-as-mem-or-subs
                                                             new-thread-op
                                                             [comment-op]
                                                             add-mentions-in-str-as-mem-subs-op
                                                             add-block-author-as-sub-and-mem
                                                             block-author-notification-op
                                                             notification-op)

          comment-notif-op                           (composite/make-consequence-op {:op/type :comment-notif-op}
                                                                                    ops)
          event                                     (common-events/build-atomic-event comment-notif-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))
