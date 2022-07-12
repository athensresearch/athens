(ns athens.views.comments.core
  (:require [athens.db :as db]
            [re-frame.core :as rf]
            [athens.common-db :as common-db]
            [athens.common.utils :as common.utils]
            [athens.common-events.graph.composite :as composite]
            [athens.common-events.graph.atomic :as atomic-graph-ops]
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
  [db thread-uid thread-name block-uid]
  (->> (bfs/internal-representation->atomic-ops db
                                                [#:block{:uid    thread-uid
                                                         :string thread-name
                                                         :properties
                                                         {":block/type"          #:block{:string "comment/thread"
                                                                                         :uid    (common.utils/gen-block-uid)}
                                                          ":thread/members"      #:block{:string "comment/thread"
                                                                                         :uid    (common.utils/gen-block-uid)}
                                                          ":thread/subscribers"  #:block{:string "comment/thread"
                                                                                         :uid    (common.utils/gen-block-uid)}}}]
                                                {:block/uid block-uid
                                                 :relation  :last})
       (composite/make-consequence-op {:op/type :new-thread})))


(rf/reg-event-fx
  :comment/write-comment
  (fn [{db :db} [_ uid comment-string author]]
    (let [thread-exists?            (get-comment-thread-uid @db/dsdb uid)
          thread-uid                (or thread-exists?
                                       (common.utils/gen-block-uid))
          active-comment-ops        (composite/make-consequence-op {:op/type :active-comments-op}
                                                                  (concat (if thread-exists?
                                                                            []
                                                                            [(new-thread @db/dsdb thread-uid "" uid)
                                                                             (graph-ops/build-block-move-op @db/dsdb thread-uid {:block/uid uid
                                                                                                                                 :relation  {:page/title ":comment/threads"}})])
                                                                          [(new-comment @db/dsdb  thread-uid comment-string author "12:09 pm")]))
          event                     (common-events/build-atomic-event active-comment-ops)]
     {:fx [[:dispatch [:resolve-transact-forward event]]]})))