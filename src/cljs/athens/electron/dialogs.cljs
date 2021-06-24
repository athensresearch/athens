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


(defn move-dialog!
  "If new-dir/athens already exists, no-op and alert user.
  Else copy db to new db location. When there is an images folder, copy /images folder and all images.
    file:// image urls in block/string don't get updated, so if original images are deleted, links will be broken."
  []
  (let [res     (.showOpenDialogSync dialog (clj->js {:properties ["openDirectory"]}))
        new-dir (first res)]
    (when new-dir
      (let [curr-db-filepath @(rf/subscribe [:db/filepath])
            base-dir         (.dirname path curr-db-filepath)
            base-dir-name    (.basename path base-dir)
            curr-dir-images  (.resolve path base-dir utils/IMAGES-DIR-NAME)
            new-dir          (.resolve path new-dir base-dir-name)
            new-dir-images   (.resolve path new-dir utils/IMAGES-DIR-NAME)
            new-db-filepath  (.resolve path new-dir utils/DB-INDEX)]
        (if (.existsSync fs new-dir)
          (js/alert (str "Directory " new-dir " already exists, sorry."))
          (do (.mkdirSync fs new-dir)
              (.copyFileSync fs curr-db-filepath new-db-filepath)
              (rf/dispatch [:db-picker/move-db curr-db-filepath new-db-filepath])
              (rf/dispatch [:db/update-filepath new-db-filepath])
              (when (.existsSync fs curr-dir-images)
                (.mkdirSync fs new-dir-images)
                (let [imgs (->> (.readdirSync fs curr-dir-images)
                                array-seq
                                (map (fn [x]
                                       [(.join path curr-dir-images x)
                                        (.join path new-dir-images x)])))]
                  (doseq [[curr new] imgs]
                    (.copyFileSync fs curr new))))))))))


(defn open-dialog!
  "Allow user to open db elsewhere from filesystem."
  []
  (let [res       (.showOpenDialogSync dialog (clj->js {:properties ["openFile"]
                                                        :filters    [{:name "Transit" :extensions ["transit"]}]}))
        open-file (first res)]
    (when (and open-file (.existsSync fs open-file))
      (let [read-db (.readFileSync fs open-file)
            db      (dt/read-transit-str read-db)]
        (rf/dispatch-sync [:init-rfdb])
        (rf/dispatch [:local-storage/create-db-picker-list])
        (rf/dispatch [:fs/watch open-file])
        (rf/dispatch [:reset-conn db])
        (rf/dispatch [:db/update-filepath open-file])
        (rf/dispatch [:db-picker/add-new-db open-file])
        (rf/dispatch [:loading/unset])))))


;; mkdir db-location/name/
;; mkdir db-location/name/images
;; write db-location/name/index.transit
(defn create-dialog!
  "Create a new database."
  [db-name]
  (let [res         (.showOpenDialogSync dialog (clj->js {:properties ["openDirectory"]}))
        db-location (first res)]
    (when (and db-location (not-empty db-name))
      (let [db          (d/empty-db db/schema)
            dir         (.resolve path db-location db-name)
            dir-images  (.resolve path dir utils/IMAGES-DIR-NAME)
            db-filepath (.resolve path dir utils/DB-INDEX)]
        (if (.existsSync fs dir)
          (js/alert (str "Directory " dir " already exists, sorry."))
          (do
            (rf/dispatch-sync [:init-rfdb])
            (rf/dispatch [:local-storage/create-db-picker-list])
            (.mkdirSync fs dir)
            (.mkdirSync fs dir-images)
            (.writeFileSync fs db-filepath (dt/write-transit-str db))
            (rf/dispatch [:fs/watch db-filepath])
            (rf/dispatch [:db/update-filepath db-filepath])
            (rf/dispatch [:db-picker/add-new-db db-filepath])
            (rf/dispatch [:reset-conn db])
            (rf/dispatch [:transact athens-datoms/datoms])
            (rf/dispatch [:loading/unset])))))))
