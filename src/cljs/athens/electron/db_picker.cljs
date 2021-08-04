(ns athens.electron.db-picker
  (:require
    [athens.electron.boot]
    [athens.electron.fs]
    [athens.electron.window]
    [athens.electron.utils :as utils]
    [cljs.reader :refer [read-string]]
    [re-frame.core :as rf]))

(def fs (js/require "fs"))
(def path (js/require "path"))


(rf/reg-sub
  :db-picker/all-dbs
  (fn [db _]
    (-> db :athens/persist :db-picker/all-dbs)))


(rf/reg-sub
 :db-picker/selected-db
 (fn [db _]
   (-> db :athens/persist :db-picker/selected-db)))


(rf/reg-event-fx
  :db-picker/add-and-select-db
  (fn [{:keys [db]} [_ {:keys [db-path] :as added-db}]]
    "Add a db to the db picker list and select it as the current db.
     Adding a db with the same db-path will show an alert."
    (if (get-in db [:athens/persist :db-picker/all-dbs db-path])
      {:dispatch [:alert/js (str "This Database is already listed as " (:name added-db) ".")]}
      (let [new-app-db (assoc-in db [:athens/persist :db-picker/all-dbs db-path] added-db)]
        {:db       new-app-db
         :persist  new-app-db
         :dispatch [:db-picker/select-db added-db]}))))


(rf/reg-event-fx
  :db-picker/select-db
  (fn [{:keys [db]} [_ {:keys [db-path] :as selected-db} ignore-sync-check?]]
    "Select a db from the all db list and reboot the app into it.
    If the db is no longer in the db picker, alert the user to add it again,
    If the selected db is deleted from disk then show an alert describing the
    situation and remove this db from db list.
    Unless ignore-sync-check? is true, prevent selecting another db when sync
    is happening and instead shows an alert."
    (let [synced?       (or ignore-sync-check? (:db/synced db))
          db-in-picker? (get-in db [:athens/persist :db-picker/all-dbs db-path])
          db-exists?    (and db-path (utils/local-db-exists? selected-db))]
      (cond
        (not db-in-picker?)
        {:dispatch [:alert/js "Database is no longer listed, please add it again."]}

        (and db-exists? synced?)
        (let [new-app-db (assoc-in db [:athens/persist :db-picker/selected-db] selected-db)]
          {:db       new-app-db
           :persist  new-app-db
           :dispatch [:boot/desktop]})

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
                  [:db-picker/select-db most-recent-db]
                  [:fs/open-dialog])})))

(rf/reg-event-fx
  :db-picker/remove-db
  (fn [{:keys [db]} [_ {:keys [db-path]}]]
    "Delete a db from the db-picker."
    (let [new-app-db (update-in db [:athens/persist :db-picker/all-dbs] dissoc db-path)]
      {:db      new-app-db
       :persist new-app-db})))


