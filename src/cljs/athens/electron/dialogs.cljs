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
  (fn [{:keys [db]} _]
    (js/alert (str "No DB found at " (:db/filepath db) "."
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
      (let [curr-db-path                  @(rf/subscribe [:db/filepath])
            {name            :name
             curr-images-dir :images-dir} (utils/local-graph curr-db-path)
            new-db-path                   (.resolve path new-dir name utils/DB-INDEX)
            {new-base-dir   :base-dir
             new-images-dir :images-dir
             :as            new-graph}    (utils/local-graph new-db-path)]
        (if (utils/local-graph-dir-exists? new-graph)
          (graph-already-exists-alert new-graph)
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
              (rf/dispatch [:db-picker/move-db curr-db-path new-db-path])
              (rf/dispatch [:db/update-filepath new-db-path])))))))


(defn open-dialog!
  "Allow user to open db elsewhere from filesystem."
  []
  (let [res       (.showOpenDialogSync dialog (clj->js {:properties ["openFile"]
                                                        :filters    [{:name "Transit" :extensions ["transit"]}]}))
        open-file (first res)]
    (when (and open-file (.existsSync fs open-file))
      (rf/dispatch-sync [:db-picker/select-new-db open-file @(rf/subscribe [:db/synced])]))))


(defn create-dialog!
  "Create a new database."
  [db-name]
  (let [res         (.showOpenDialogSync dialog (clj->js {:properties ["openDirectory"]}))
        db-location (first res)]
    (when (and db-location (not-empty db-name))
      (let [db-path     (.resolve path db-location name utils/DB-INDEX)
            local-graph (utils/local-graph db-path)]
        (if (utils/local-graph-dir-exists? local-graph)
          (graph-already-exists-alert local-graph)
          (do
            (rf/dispatch-sync [:init-rfdb])
            (rf/dispatch [:local-storage/create-db-picker-list])
            (rf/dispatch [:fs/create-and-watch local-graph])
            (rf/dispatch [:loading/unset])))))))
