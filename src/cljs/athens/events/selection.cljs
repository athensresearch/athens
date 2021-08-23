(ns athens.events.selection
  (:require [athens.self-hosted.client            :as client]
            [athens.db                            :as db]
            [athens.common-events                 :as common-events]
            [athens.common-events.resolver        :as resolver]
            [re-frame.core :as rf]))


(rf/reg-event-db
 ::set-items
 (fn [rf-db [_ uids]]
   (assoc-in rf-db [:selection :items] (into [] uids))))


(rf/reg-event-db
 ::add-item
 (fn [rf-db [_ uid position]]
   (let [selected-items (get-in rf-db [:selection :items])
         selected-count  (count selected-items)]
     (assoc-in rf-db [:selection :items]
               (cond
                 (= :last position)
                 (into selected-items [uid])

                 (= :first position)
                 (into [uid] selected-items)

                 (int? position)
                 (if (<= 0 position selected-count)
                   (let [new-selected-items (into [] (concat
                                                      (subvec selected-items 0 position)
                                                      [uid]
                                                      (subvec selected-items position selected-count)))]
                     (js/console.debug ::add-item "new-selected-items:" (pr-str new-selected-items))
                     new-selected-items)
                   (let [message (str "Invalid insert position:" (pr-str position)
                                      ". Tried to add uid:" (pr-str uid)
                                      ", to selected-items:" (pr-str selected-items))]
                     ;; Error, invalid insert position
                     (js/console.error message)
                     selected-items)))))))


(rf/reg-event-fx
 ::delete
 (fn [{rf-db :db} _]
   (let [local?         (not (client/open?))
         selected-uids  (get-in rf-db [:selection :items])
         sanitized-uids (map (comp first db/uid-and-embed-id) selected-uids)]
     (js/console.debug ::delete "args" selected-uids)
     (js/console.log ::delete "local?" local?)
     (if local?
       (let [selected-delete-event (common-events/build-selected-delete-event -1
                                                                              sanitized-uids)
             tx                    (resolver/resolve-event-to-tx @db/dsdb selected-delete-event)]
         (js/console.debug ::delete "tx" tx)
         {:fx [[:dispatch-n [[:transact    tx]
                             [:editing/uid nil]]]]
          :db (assoc-in rf-db [:selection :items] [])})
       {:fx [[:dispatch [:remote/selected-delete selected-uids]]]}))))


(rf/reg-event-db
 ::clear
 (fn [{rf-db :db} _]
   (assoc-in rf-db [:selection :items] [])))
