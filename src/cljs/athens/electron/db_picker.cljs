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
    (:db-picker/all-dbs db)))


(defn get-db-name
  "From a dbpath find out db name.
   e.g : /home/sid597/Desktop/athens db/test db/index.transit --> test db"
  [dbpath]
  (.basename path (.dirname path dbpath)))


(defn check-duplicate-db
  "Check if a db is already in the list.
   Implementation thoughts
    - Should this be done whenever a new db is added or should we maintain a
      set universally and check that?
    - No performance issues if this is done on every new db addition because
      not many dbs would be made frequently to cause performance issue. What
      about merging 2 large dbs? Not there yet, but in that case also calculating
      the set of all paths is O(n).
    - Also re-frame is in-memory so saving data which can be easily calculated is
      not a good idea I think"
  [db-list check-path]
  (seq (filter #(= check-path (:path %)) db-list)))


(rf/reg-event-fx
  :db-picker/add-new-db
  (fn [{:keys [db]} [_ dbpath]]
    "Add a new to db picker list.
     Can be invoked when a db is added, opened, moved, selected or removed.
     Update local storage value with the new db list"
    (let [current-db-list (:db-picker/all-dbs db)
          duplicate?       (check-duplicate-db
                             current-db-list
                             dbpath)]
      (if duplicate?
        {:fx [[:dispatch [:alert/js (str "Database already in the list")]]]}
        (let [dbname  (get-db-name dbpath)
              newdb   {:path dbpath
                       :name dbname}
              all-dbs (conj current-db-list newdb)]
          {:db       (assoc db :db-picker/all-dbs all-dbs)
           :fx [[:dispatch [:local-storage/set-db-picker-list]]]})))))


(rf/reg-event-fx
  :local-storage/set-db-picker-list
  (fn [{:keys [db]} _]
    "Save current db-picker list to local storage. Why using `pr-str`? Checkout
     this link for discussion on how to serialize data to store"
    ;; https://stackoverflow.com/questions/67821181/how-to-set-and-get-a-vector-in-local-storage
    (let [current-db-list (:db-picker/all-dbs db)]
      {:local-storage/set! ["db-picker/all-dbs" (pr-str current-db-list)]})))


(rf/reg-event-fx
  :local-storage/create-db-picker-list
  (fn [{:keys [db]} _]
    "Check if local storage contains db-picker list.
     If not it means this is the first time opening Athens or local storage was
     cleared, if this is the case we update db-picker list with the current running db."
    (let [val                 (cljs.reader/read-string
                                (js/localStorage.getItem "db-picker/all-dbs"))
          current-db-filepath (:db/filepath db)]
      (if (nil? val)
        {:fx [[:dispatch [:db-picker/add-new-db current-db-filepath]]]}
        {:db (assoc db :db-picker/all-dbs val)}))))


(rf/reg-event-fx
  :db-picker/remove-db-from-list
  (fn [{:keys [db]} [_ db-path]]
    "Remove the selected db from db-list. Update local storage value with the new db list"
    (let [current-db-list (:db-picker/all-dbs db)
          new-db-list     (into [] (filter
                                     (fn [db-list-item] (not= db-path (:path db-list-item)))
                                     current-db-list))]
      {:db       (assoc db :db-picker/all-dbs new-db-list)
       :fx [[:dispatch [:local-storage/set-db-picker-list]]]})))


(rf/reg-event-fx
  :db-picker/move-db
  (fn [_ [_ previous-path new-path]]
    "Move db from current location."
    {:dispatch-n [[:db-picker/remove-db-from-list previous-path]
                  [:db-picker/add-new-db new-path]]}))


(rf/reg-event-fx
  :db-picker/select-new-db
  (fn [{:keys [db]} [_ db-path synced?]]
    "Select a new db from db list.
    If the selected db is deleted from disk then show an alert describing the
    situation and remove this db from db list. Prevent selecting a db when sync
    is happening, instead show an alert describing the situation."
    (let [file-exists? (and db-path (.existsSync fs db-path))]
      (cond
        (and file-exists? synced?)        {:dispatch-n [[:db/update-filepath db-path]
                                                        [:boot/desktop]]}
        (and file-exists? (not synced?))  {:fx   [[:dispatch [:alert/js "Database is saving your changes, if you switch now your changes will not be saved"]]]}
        :else                             {:dispatch-n [[:alert/js "This database does not exist, removing it from list"]
                                                        [:db-picker/remove-db-from-list db-path]]}))))


(rf/reg-event-fx
  :db-picker/delete-db
  (fn [{:keys [db]} [_ db-filepath]]
    "Delete selected db.
    Delete the selected db and set the first db in the updated db list as active.
    Another approach could be to keep state of last visited db before the current
    one, and when the current db is deleted switch to previous one. This approach
    is comparatively more complex but would improve UX, we are leaving this one
    out in current scope.

    `select-new-db` event call has 2nd argument (synced) as true because one
    is deleting a db so to them it does not matter if the db is synced or not."

    ;; TODO implement delete db from filesystem not implemented
    ;; so that we can test without accidently deleting real db
    (let [new-list         (:db-picker/all-dbs db)
          next-db-filepath (:path (nth new-list 0))]
      {:fx   [[:dispatch [:db-picker/select-new-db next-db-filepath true]]
              [:local-storage/set! ["db-picker/all-dbs" new-list]]]})))
