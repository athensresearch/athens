(ns athens.electron.db-picker
  (:require
    [athens.electron.boot]
    [athens.electron.fs]
    [athens.electron.utils :as utils]
    [athens.electron.window]
    [re-frame.core :as rf]))


(rf/reg-sub
  :db-picker/all-dbs
  (fn [db _]
    (-> db :athens/persist :db-picker/all-dbs)))


(rf/reg-sub
  :db-picker/selected-db
  (fn [db _]
    (-> db :athens/persist :db-picker/selected-db)))


;; Add a db to the db picker list and select it as the current db.
;; Adding a db with the same base-dir will show an alert.
(rf/reg-event-fx
  :db-picker/add-and-select-db
  (fn [{:keys [db]} [_ {:keys [location] :as added-db}]]
    {:db       (assoc-in db [:athens/persist :db-picker/all-dbs location] added-db)
     :dispatch [:db-picker/select-db added-db]}))


;; Select a db from the all db list and reboot the app into it.
;; If the db is no longer in the db picker, alert the user to add it again,
;; If the selected db is deleted from disk then show an alert describing the
;; situation and remove this db from db list.
;; Unless ignore-sync-check? is true, prevent selecting another db when sync
;; is happening and instead shows an alert.
(rf/reg-event-fx
  :db-picker/select-db
  (fn [{:keys [db]} [_ {:keys [location] :as selected-db} ignore-sync-check?]]
    (let [synced?          (or ignore-sync-check? (:db/synced db))
          curr-selected-db (get-in db [:athens/persist :db-picker/selected-db])
          db-in-picker?    (get-in db [:athens/persist :db-picker/all-dbs location])
          db-exists?       (utils/db-exists? selected-db)]
      (cond
        (not db-in-picker?)
        {:dispatch [:alert/js "Database is no longer listed, please add it again."]}

        (and db-exists? synced?)
        {:db         (-> db
                         (assoc-in [:athens/persist :db-picker/selected-db] selected-db)
                         (assoc-in [:athens/persist :current-route/uid] nil))
         :dispatch-n [(when (utils/remote-db? curr-selected-db)
                        [:remote/disconnect!])
                      [:boot/desktop]]}

        (and db-exists? (not synced?))
        {:dispatch [:alert/js "Database is saving your changes, if you switch now your changes will not be saved."]}

        :else
        {:dispatch-n [[:alert/js "This database does not exist anymore, removing it from list."]
                      [:db-picker/remove-db selected-db]]}))))


(rf/reg-event-fx
  :db-picker/select-most-recent-db
  (fn [{:keys [db]} [_]]
    ;; TODO: this is just getting the first one, not the most recent
    (let [most-recent-db (second (first (get-in db [:athens/persist :db-picker/all-dbs])))]
      {:dispatch (if most-recent-db
                   [:db-picker/select-db most-recent-db true]
                   [:fs/open-dialog])})))


;; Delete a db from the db-picker.
(rf/reg-event-fx
  :db-picker/remove-db
  (fn [{:keys [db]} [_ {:keys [location]}]]
    {:db      (update-in db [:athens/persist :db-picker/all-dbs] dissoc location)}))

