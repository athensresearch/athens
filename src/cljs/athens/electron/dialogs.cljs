(ns athens.electron.dialogs
  (:require
    [athens.athens-datoms :as athens-datoms]
    [athens.db :as db]
    [athens.electron.utils :as utils]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [re-frame.core :as rf]))


(def electron (js/require "electron"))
(def remote (.. electron -remote))
(def fs (js/require "fs"))
(def dialog (.. remote -dialog))
(def path (js/require "path"))


(rf/reg-event-fx
  :fs/open-dialog
  (fn [{:keys [db]} {:keys [base-dir]}]
    (js/alert (str (if base-dir
                     (str "No DB found at " base-dir ".")
                     "No DB found.")
                   "\nPlease open or create a new db."))
    {:dispatch-n [[:modal/toggle]]}))


(defn graph-already-exists-alert [{:keys [base-dir name]}]
  (js/alert (str "Directory " base-dir " already contains the " name " graph, sorry.")))


(def open-dir-opts
  (clj->js {:defaultPath (utils/default-dbs-dir)
            :properties  ["openDirectory"]}))


(defn move-dialog!
  "If new-dir/athens already exists, no-op and alert user.
  Else copy db to new db location. When there is an images folder, copy /images folder and all images.
    file:// image urls in block/string don't get updated, so if original images are deleted, links will be broken."
  []
  (let [res     (.showOpenDialogSync dialog open-dir-opts)
        new-dir (first res)]
    (when new-dir
      (let [{name            :name
             curr-images-dir :images-dir
             curr-db-path    :db-path
             curr-base-dir   :base-dir
             :as             curr-db} @(rf/subscribe [:db-picker/selected-db])
            ;; Merge the new local db info into the current db to preserve any other information there.
            {new-base-dir   :base-dir
             new-images-dir :images-dir
             new-db-path    :db-path
             :as            new-db}   (merge curr-db (utils/local-db (.resolve path new-dir name)))]
        (if (utils/local-db-dir-exists? new-db)
          (graph-already-exists-alert new-db)
          (do (.mkdirSync fs new-base-dir)
              (.copyFileSync fs curr-db-path new-db-path)
              (when (.existsSync fs curr-images-dir)
                (.mkdirSync fs new-images-dir)
                (let [imgs (->> (.readdirSync fs curr-images-dir)
                                array-seq
                                (map (fn [x]
                                       [(.join path curr-images-dir x)
                                        (.join path new-images-dir x)])))]
                  (doseq [[curr new] imgs]
                    (.copyFileSync fs curr new))))
              (.rmSync fs curr-base-dir #js {:recursive true :force true})
              (rf/dispatch [:db-picker/remove-db curr-db])
              (rf/dispatch [:db-picker/add-and-select-db new-db])))))))


(defn open-dialog!
  "Allow user to open db elsewhere from filesystem."
  []
  (let [res       (.showOpenDialogSync dialog open-dir-opts)
        open-file (first res)]
    (when (and open-file (.existsSync fs open-file))
      (rf/dispatch [:db-picker/add-and-select-db (utils/local-db (.dirname path open-file))]))))


(defn create-dialog!
  "Create a new database."
  [db-name]
  (let [res         (.showOpenDialogSync dialog open-dir-opts)
        db-location (first res)]
    (when (and db-location (not-empty db-name))
      (let [base-dir (.resolve path db-location db-name)
            local-db (utils/local-db base-dir)]
        (if (utils/local-db-dir-exists? local-db)
          (graph-already-exists-alert local-db)
          (rf/dispatch [:fs/create-and-watch local-db]))))))

(defn delete-dialog!
  "Delete an existing database and select the first db of the remaining ones."
  [{:keys [name base-dir] :as db}]
  (when (.confirm js/window (str "Do you really want to delete " name "?"))
    (.rmSync fs base-dir #js {:recursive true :force true})
    (rf/dispatch [:db-picker/remove-db db])
    (rf/dispatch [:db-picker/select-most-recent-db])))
