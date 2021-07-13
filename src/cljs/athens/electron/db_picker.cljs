(ns athens.electron.db-picker
  (:require
    [athens.electron.boot]
    [athens.electron.fs]
    [athens.electron.window]
    [cljs.reader :refer [read-string]]
    [re-frame.core :as rf]))

(def fs (js/require "fs"))
(def path (js/require "path"))

(rf/reg-sub
  :db-picker/all-dbs
  (fn [db _]
    (-> db :athens/persist :db-picker/all-dbs)))


(defn get-db-name
  "From a dbpath find out db name.
   e.g : /home/sid597/Desktop/athens db/test db/index.transit --> test db"
  [dbpath]
  (.basename path (.dirname path dbpath)))


(rf/reg-event-fx
  :db-picker/add-new-db
  (fn [{:keys [db]} [_ dbpath]]
    "Add a db picker list. If already exists, just re-open."
    (let [dbname     (get-db-name dbpath)
          newdb      {:path dbpath :name dbname}
          new-app-db (-> db
                         (assoc-in [:athens/persist :db-picker/all-dbs dbpath] newdb)
                         (assoc-in [:athens/persist :db/filepath] dbpath))]
      {:db      new-app-db
       :persist new-app-db})))


(rf/reg-event-fx
  :db-picker/select-new-db
  (fn [{:keys [db]} [_ db-path synced?]]
    "Select a new db from db list.
    If the selected db is deleted from disk then show an alert describing the
    situation and remove this db from db list. Prevent selecting a db when sync
    is happening, instead show an alert describing the situation."
    (let [file-exists? (and db-path (.existsSync fs db-path))]
      (cond
        (and file-exists? synced?)
        {:dispatch-n [[:db-picker/add-new-db db-path]
                      [:boot/desktop]]}

        (and file-exists? (not synced?))
        {:fx   [[:dispatch [:alert/js "Database is saving your changes, if you switch now your changes will not be saved"]]]}

        :else
        {:dispatch-n [[:alert/js "This database does not exist, removing it from list"]
                      [:db-picker/delete-db db-path]]}))))


(rf/reg-event-fx
  :db-picker/move-db
  (fn [_ [_ previous-path new-path]]
    "Move db from current location."
    {:dispatch-n [[:db-picker/remove-db-from-list previous-path]
                  [:db-picker/add-new-db new-path]]}))


(rf/reg-event-fx
  :db-picker/delete-db
  (fn [{:keys [db]} [_ db-filepath]]
    "Delete selected db and randomly open another db.
    Going forward, we should look at most recently opened db before.

    `select-new-db` event call has 2nd argument (synced) as true because one
    is deleting a db so to them it does not matter if the db is synced or not."
    ;; TODO implement delete db from filesystem not implemented
    ;; so that we can test without accidently deleting real db
    (let [new-app-db       (update-in db [:athens/persist :db-picker/all-dbs] dissoc db-filepath)
          next-db-filepath (-> (get-in new-app-db [:athens/persist :db-picker/all-dbs])
                               ffirst)]
      {:db      new-app-db
       :persist new-app-db
       :fx      [[:dispatch [:db-picker/select-new-db next-db-filepath true]]]})))


