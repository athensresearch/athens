(ns athens.views.notifications.actions
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.graph.composite :as composite-ops]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.db :as db]
    [re-frame.core :as rf]))


(defn is-block-inbox?
  [properties]
  (= "[[athens/inbox]]"
     (:block/string (get properties ":entity/type"))))


(defn is-block-notification?
  [properties]
  (= "[[athens/notification]]"
     (:block/string (get properties ":entity/type"))))


(defn unread-notification?
  [properties]
  (= "false"
     (:block/string (get properties "athens/notification/is-read"))))


#_(defn read-notification?
    [properties]
    (= "true"
       (:block/string (get properties "athens/notification/is-read"))))


(defn archived-notification?
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
                          (is-block-notification? block-properties) [:properties/update-in [:block/uid uid] [block-state-prop]
                                                                     (fn [db prop-uid]
                                                                       [(graph-ops/build-block-save-op db prop-uid new-state)])])]
    updated-prop))


(defn multi-uids-prop-update
  [uids key new-val]
  (let [ops                  (into [] (map
                                        #(let [[prop-uid] (graph-ops/build-property-path @db/dsdb  % [key])
                                               save-op    (graph-ops/build-block-save-op @db/dsdb prop-uid new-val)]
                                           save-op)
                                        uids))
        composite-op         (composite-ops/make-consequence-op {:op/type :show-hidden-notifications}
                                                                ops)
        event                (common-events/build-atomic-event composite-op)]
    (rf/dispatch [:resolve-transact-forward event])))


(defn unarchive-all-notifications
  [inbox-uid]
  (let [all-notifications (->> (common-db/get-block-document @db/dsdb [:block/uid inbox-uid])
                               :block/children
                               (mapv :block/uid))]
    (multi-uids-prop-update all-notifications "athens/notification/is-archived" "false")))


(defn archive-all-notifications
  [inbox-uid]
  (let [all-notifications (->> (common-db/get-block-document @db/dsdb [:block/uid inbox-uid])
                               :block/children
                               (mapv :block/uid))]
    (multi-uids-prop-update all-notifications "athens/notification/is-archived" "true")))


