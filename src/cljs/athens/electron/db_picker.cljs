(ns athens.electron.db-picker
  (:require
    [athens.electron.fs]
    [athens.electron.utils :as utils]
    [athens.electron.window]
    [re-frame.core :as rf]))


(defn all-dbs
  [db]
  (-> db :athens/persist :db-picker/all-dbs))


(defn selected-db
  [db]
  (when-let [selected-db-id (-> db :athens/persist :db-picker/selected-db-id)]
    (get-in db [:athens/persist :db-picker/all-dbs selected-db-id])))


(defn select-db
  [db id]
  (assoc-in db [:athens/persist :db-picker/selected-db-id] id))


(defn remote-db?
  [rfdb]
  (-> rfdb
      selected-db
      utils/remote-db?))


(rf/reg-sub
  :db-picker/all-dbs
  (fn [db _]
    (all-dbs db)))


(rf/reg-sub
  :db-picker/selected-db
  (fn [db _]
    (selected-db db)))


(rf/reg-sub
  :db-picker/remote-db?
  (fn [db _]
    (remote-db? db)))


;; Add a db to the db picker list and select it as the current db.
;; Adding a db with the same id will overwrite the previous one.
(rf/reg-event-fx
  :db-picker/add-and-select-db
  (fn [{:keys [db]} [_ {:keys [id] :as added-db}]]
    {:db       (assoc-in db [:athens/persist :db-picker/all-dbs id] added-db)
     :dispatch [:db-picker/select-db added-db]}))


;; Select a db from the all db list and reboot the app into it.
;; If the db is no longer in the db picker, alert the user to add it again,
;; If the selected db is deleted from disk then show an alert describing the
;; situation and remove this db from db list.
;; Unless ignore-sync-check? is true, prevent selecting another db when sync
;; is happening and instead shows an alert.
(rf/reg-event-fx
  :db-picker/select-db
  (fn [{:keys [db]} [_ {:keys [id] :as target-db} ignore-sync-check?]]
    (let [synced?          (or ignore-sync-check? (:db/synced db))
          curr-selected-db (selected-db db)
          db-exists?       (utils/db-exists? target-db)]
      (cond
        (not synced?)
        {:dispatch [:alert/js "Database is saving your changes, if you switch now your changes will not be saved."]}

        db-exists?
        {:db         (select-db db id)
         :dispatch-n [(when (utils/remote-db? curr-selected-db)
                        [:remote/disconnect!])
                      [:boot]]}

        :else
        {:dispatch-n [[:alert/js "This database does not exist anymore, removing it from list."]
                      [:db-picker/remove-db target-db]]}))))


(rf/reg-event-fx
  :db-picker/select-most-recent-db
  (fn [{:keys [db]} [_]]
    ;; TODO: this is just getting the first one, not the most recent
    (let [most-recent-db (second (first (get-in db [:athens/persist :db-picker/all-dbs])))]
      {:dispatch (if most-recent-db
                   [:db-picker/select-db most-recent-db true]
                   [:fs/open-dialog])})))


(rf/reg-event-fx
  :db-picker/select-default-db
  (fn [_ [_]]
    {:dispatch [:db-picker/add-and-select-db (utils/get-default-db)]}))


;; Delete a db from the db-picker.
(rf/reg-event-fx
  :db-picker/remove-db
  (fn [{:keys [db]} [_ {:keys [id]}]]
    (let [new-db (update-in db [:athens/persist :db-picker/all-dbs] dissoc id)]
      {:db         new-db
       ;; reboot, and run default bd logic, when removing the db leaves db picker empty
       :dispatch-n [(when (empty? (all-dbs new-db)) [:boot])]})))


;; Select no db, leave it to the boot sequence to decide what to do.
(rf/reg-event-fx
  :db-picker/remove-selection
  (fn [{:keys [db]} [_]]
    {:db       (update-in db [:athens/persist] dissoc :db-picker/selected-db-id)
     :dispatch [:boot]}))
