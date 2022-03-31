(ns athens.views.comments.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [athens.views.comments.inline :as inline]
            [athens.views.comments.right-side :as right-side]
            [athens.common-events.graph.atomic :as atomic-graph-ops]
            [athens.common-events.graph.ops :as graph-ops]
            [athens.common-events.graph.composite :as composite-ops]
            [re-frame.core :as rf]
            [athens.bot]
            [athens.common-events :as common-events]
            [athens.common.utils :as common.utils]
            [athens.common-db :as common-db]
            [athens.db :as db]
            [athens.common-events.graph.composite :as composite]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]))

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

(defn show-comments-actively-block
  [db page-title last-block]
  (if (not= "Actively show comments on this page. NOTE: Please do not create a sibling block after this block."
            (:block/string last-block))
    (let [new-block-uid      (common.utils/gen-block-uid)
          new-block-op       (atomic-graph-ops/make-block-new-op new-block-uid {:page/title page-title
                                                                                :relation   :last})
          new-block-save-op  (graph-ops/build-block-save-op @db/dsdb new-block-uid "Actively show comments on this page. NOTE: Please do not create a sibling block after this block.")]
      [new-block-uid [new-block-op new-block-save-op]])
    nil))





(rf/reg-event-fx
  :comment/write-comment
  (fn [{db :db} [_ uid comment-string author]]
    (let [new-comment               {:block/uid (common.utils/gen-block-uid)
                                     :block/type :comment
                                     :string      comment-string
                                     :author      author
                                     :time        "12:09 pm"}
          parent-page-title         (:node/title (db/get-root-parent-page uid))
          last-block-uid            (last
                                      (common-db/get-children-uids @db/dsdb
                                                                   [:node/title parent-page-title]))
          last-block                (common-db/get-block @db/dsdb [:block/uid last-block-uid])
          [show-comments-uid
           show-comments-op]        (show-comments-actively-block db parent-page-title last-block)
          new-active-block-rel     (if show-comments-uid
                                     show-comments-uid
                                     last-block-uid)
          new-active-block-uid      (common.utils/gen-block-uid)
          new-active-block-op       (atomic-graph-ops/make-block-new-op new-active-block-uid {:block/uid new-active-block-rel
                                                                                              :relation  :last})
          new-active-block-save-op  (graph-ops/build-block-save-op @db/dsdb
                                                                   new-active-block-uid
                                                                   (str  "**((" uid "))**" "\n"
                                                                        "*[[@" author "]]: " comment-string "*"))
          comment-add-op            (atomic-graph-ops/make-comment-add-op uid new-comment)
          active-comment-ops        (composite/make-consequence-op {:op/type :active-comments-op}
                                                                   (concat (if show-comments-uid
                                                                             show-comments-op
                                                                             [])
                                                                           [new-active-block-op
                                                                            new-active-block-save-op
                                                                            comment-add-op]))

          event                     (common-events/build-atomic-event active-comment-ops)]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:dispatch [:posthog/report-feature "comments"]]
            [:dispatch [:prepare-message uid author :comment {:string comment-string}]]]})))


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


