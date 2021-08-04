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
  (fn [{:keys [db]} {:keys [db-path]}]
    (js/alert (str (if db-path
                     (str "No DB found at " db-path ".")
                     "No DB found.")
                   "\nPlease open or create a new db."))
    {:dispatch-n [[:modal/toggle]]}))


(defn graph-already-exists-alert [{:keys [base-dir name]}]
  (js/alert (str "Directory " base-dir " already contains the " name " graph, sorry.")))


(defn move-dialog!
  "If new-dir/athens already exists, no-op and alert user.
  Else copy db to new db location. When there is an images folder, copy /images folder and all images.
    file:// image urls in block/string don't get updated, so if original images are deleted, links will be broken."
  []
  (let [res     (.showOpenDialogSync dialog (clj->js {:properties ["openDirectory"]}))
        new-dir (first res)]
    (when new-dir
      (let [{name            :name
             curr-images-dir :images-dir
             curr-db-path    :db-path
             curr-base-dir   :base-dir
             :as             curr-db}    @(rf/subscribe [:db-picker/selected-db])
            new-db-path                  (.resolve path new-dir name utils/DB-INDEX)
            ;; Merge the new local db info into the current db to preserve any other information there.
            new-db                       (merge curr-db (utils/local-db new-db-path))
            {new-base-dir   :base-dir
             new-images-dir :images-dir} new-db]
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
  (let [res       (.showOpenDialogSync dialog (clj->js {:properties ["openFile"]
                                                        :filters    [{:name "Transit" :extensions ["transit"]}]}))
        open-file (first res)]
    (when (and open-file (.existsSync fs open-file))
      (rf/dispatch [:db-picker/add-and-select-db (utils/local-db open-file)]))))


(defn create-dialog!
  "Create a new database."
  [db-name]
  (let [res         (.showOpenDialogSync dialog (clj->js {:defaultPath (utils/default-dbs-dir)
                                                          :properties  ["openDirectory"]}))
        db-location (first res)]
    (when (and db-location (not-empty db-name))
      (let [db-path  (.resolve path db-location db-name utils/DB-INDEX)
            local-db (utils/local-db db-path)]
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
