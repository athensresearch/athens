(ns athens.events.selection
  (:require
    [athens.common-events          :as common-events]
    [athens.db                     :as db]
    [re-frame.core                 :as rf]))


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
  (fn [{:keys [db]} _]
    (let [selected-uids  (get-in db [:selection :items])
          sanitized-uids (map (comp first db/uid-and-embed-id) selected-uids)]
      (js/console.debug ::delete "args" selected-uids)
      (let [event (common-events/build-selected-delete-event (:remote/last-seen-tx db)
                                                             sanitized-uids)]
        {:fx [[:dispatch-n [[:transact-and-forward event]
                            [:editing/uid          nil]]]]
         :db (assoc-in db [:selection :items] [])}))))


(rf/reg-event-db
  ::clear
  (fn [rf-db _]
    (assoc-in rf-db [:selection :items] [])))
