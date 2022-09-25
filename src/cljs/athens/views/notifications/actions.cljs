(ns athens.views.notifications.actions
  (:require
    [athens.common-db :as common-db]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.db :as db]))


(defn is-block-notification?
  [properties]
  (= "[[athens/notification]]"
     (:block/string (get properties ":entity/type"))))


#_(defn unread-notification?
  [properties]
  (= "false"
     (:block/string (get properties "athens/notification/is-read"))))


#_(defn read-notification?
    [properties]
    (= "true"
       (:block/string (get properties "athens/notification/is-read"))))


#_(defn archived-notification?
  [properties]
  (= "true"
     (:block/string (get properties "athens/notification/is-archived"))))


;; Mark as
;; uid of the notification
;; new state of the uid

;; What to do for the case of marking multiple selected uids as something?

(defn update-state-prop
  [uid prop-key new-state]
  (let [block-properties (common-db/get-block-property-document @db/dsdb [:block/uid uid])
        ;; Find the prop that needs to be updated due to this action
        block-state-prop (cond
                           ;; for a block
                           ;; for a page
                           ;; for a channel
                           ;; for a thread
                           ;; for an inbox(assuming inbox is a list of notification blocks)
                           (is-block-notification? block-properties) prop-key)
        updated-prop    (cond
                          ;; Maybe for a block we need to update the prop of the children also
                          (is-block-notification? block-properties) [:graph/update-in [:block/uid uid] [block-state-prop]
                                                                     (fn [db prop-uid]
                                                                       [(graph-ops/build-block-save-op db prop-uid new-state)])])]
    updated-prop))
