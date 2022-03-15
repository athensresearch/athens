(ns athens.views.comments.core
  (:require [athens.views.comments.inline :as inline]
            [athens.views.comments.right-side :as right-side]
            [athens.common-events.graph.atomic :as atomic-graph-ops]
            [re-frame.core :as rf]
            [athens.common-events :as common-events]
            [athens.common.utils :as common.utils]))

;; user presses "Comment" from context-menu
;; place to write comment appears
;; user writes their comment
;; user presses enter or "Comment"
;; comment appears in read-state


;; COMMENT STATES
;; write a comment
;; indicator that comments exist (but hidden)
;; list of comments and a place to write a comment

#_{:comments [{:uid "" ;; source
               :comments []}]}

(def comments-ratom (reagent.core/atom []))


(rf/reg-fx
  :add-comment
  (fn [x]
    (prn "fx" x)))

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

(rf/reg-event-fx
  :comment/write-comment
  (fn [_ [_ uid comment-string author]]
    (let [new-comment {:block/uid (common.utils/gen-block-uid)
                       :block/type :comment
                       :string      comment-string
                       :author      author
                       :time        "12:09 pm"}
          event           (common-events/build-atomic-event
                            (atomic-graph-ops/make-comment-add-op uid new-comment))]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(rf/reg-sub
  :comment/show-inline-comments?
  (fn [db [_]]
    (= true (:comment/show-inline-comments db))))

(rf/reg-sub
  :comment/show-right-side-comments?
  (fn [db [_]]
    (= true (:comment/show-right-side-comments db))))

(rf/reg-event-fx
  :comment/toggle-inline-comments
  (fn [{:keys [db]} [_]]
    (let [current-state (:comment/show-inline-comments db)]
      {:db (assoc db :comment/show-inline-comments (not current-state))})))

(rf/reg-event-fx
  :comment/toggle-right-side-comments
  (fn [{:keys [db]} [_]]
    (let [current-state (:comment/show-right-side-comments db)]
      {:db (assoc db :comment/show-right-side-comments (not current-state))})))


(def mock-data
  [{:string "[[Brandon Toner]] Agree with the jumpiness"}
   {:string "[[Matt Vogel]] Also experiencing this. Someone closed the parent of the block I was on (I was not zoomed in) and I got kicked out of the block"}])


(def mock-data-with-author-and-time
  [{:string "Agree with the jumpiness"
    :author "Brandon Toner"
    :time "12:30pm"}
   {:string "Also experiencing this. Someone closed the parent of the block I was on (I was not zoomed in) and I got kicked out of the block"
    :author "Matt Vogel"
    :time "12:35pm"}])


