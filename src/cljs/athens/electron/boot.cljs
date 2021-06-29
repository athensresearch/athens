(ns athens.electron.boot
  (:require [athens.db :as db]
            [re-frame.core :as rf]
            [datascript.core :as d]
            [datascript.transit :as dt]
            [athens.patterns :as patterns]
            [athens.electron.utils :as utils]))


(def fs (js/require "fs"))


#_(rf/reg-event-fx
    :local-storage/get-db-filepath
    [(rf/inject-cofx :local-storage "db/filepath")]
    (fn [{:keys [local-storage]}]
      (let [default-db-path (.resolve path utils/documents-athens-dir utils/DB-INDEX)]
        (cond
          ;; No filepath in local storage, but an existing db suggests a dev chromium is running with a different local storage
          ;; Short-circuit the first load and just use the existing DB
          (and (nil? local-storage) (.existsSync fs default-db-path)) {:dispatch [:db/update-filepath default-db-path]}
          :else {:dispatch [:db/update-filepath local-storage]}))))


(rf/reg-event-fx
  :boot/desktop
  [#_(rf/inject-cofx :local-storage/get :athens/state)
   (rf/inject-cofx :local-storage/get "db/filepath")]
  (fn [cofx _]
    (let [{db-filepath :local-storage} cofx
          default-db-path (utils/default-db-dir-path)
          db-exists-on-fs? (.existsSync fs default-db-path)

          db-filepath (cond

                        ;; No filepath found in local storage, but an existing db at default db location suggests a dev chromium is running with a different local storage. Use default filepath
                        (and (nil? db-filepath)
                             db-exists-on-fs?)
                        default-db-path

                        ;; No filepath found in local storage and nothing found at default db location. Create new db at default location, reset-conn, and add to db-list
                        (nil? db-filepath) [:fs/create-new-db db-filepath]

                        ;; Filepath found in local storage and on filesystem. Watch filepath, load db, and add to local-storage list.
                        ;; Actually this is redundant. We don't need db/filepath if we have db-picker/all-dbs
                        (and db-filepath
                             db-exists-on-fs?)
                        [[:fs/watch db-filepath]
                         #_[:reset-conn db-filepath]]

                        :else [:fs/open-dialog])]

      ;; Filepath found in local storage but no db is found. Open open-dialog.




      (prn "0" db-filepath)
      (cond
        (nil? db-filepath)
        db-filepath)



      {:db         db/rfdb
       :async-flow {:first-dispatch [:local-storage/get-db-filepath]
                    :rules          [#_{:when        :seen?
                                        :events      :db/update-filepath
                                        :dispatch-fn (fn [[_ filepath]]
                                                       (cond
                                                         ;; No database path found in localStorage. Creating new one
                                                         (nil? filepath) (rf/dispatch [:fs/create-new-db])
                                                         ;; Database found in local storage and filesystem:
                                                         (.existsSync fs filepath) (let [read-db (.readFileSync fs filepath)
                                                                                         db (dt/read-transit-str read-db)]
                                                                                     (rf/dispatch [:fs/watch filepath])
                                                                                     (rf/dispatch [:reset-conn db])
                                                                                     (rf/dispatch [:local-storage/create-db-picker-list]))
                                                         :else (rf/dispatch [:fs/open-dialog])))}

                                     ;; if first time, go to Daily Pages and open left-sidebar
                                     {:when       :seen?
                                      :events     :fs/create-new-db
                                      :dispatch-n [[:navigate :home]
                                                   [:left-sidebar/toggle]]}

                                     ;; if nth time, remember dark/light theme and last page
                                     {:when       :seen?
                                      :events     :reset-conn
                                      :dispatch-n [[:local-storage/set-theme]
                                                   #_[:local-storage/navigate]]}

                                     ;; whether first or nth time, update athens pages
                                     #_{:when       :seen-any-of?
                                        :events     [:fs/create-new-db :reset-conn]
                                        :dispatch-n [[:db/retract-athens-pages]
                                                     [:db/transact-athens-pages]]}

                                     ;; bind windows toolbar electron buttons
                                     #_{:when     :seen-any-of?
                                        :events   [:fs/create-new-db :reset-conn]
                                        :dispatch [:bind-win-listeners]}


                                     #_{:when        :seen-any-of?
                                        :events      [:fs/create-new-db :reset-conn]
                                        ;; if schema is nil, update to 1 and reparse all block/string's for links
                                        :dispatch-fn (fn [_]
                                                       (let [schemas (d/q '[:find ?e ?v
                                                                            :where [?e :schema/version ?v]]
                                                                          @db/dsdb)
                                                             schema-cnt (count schemas)]
                                                         (cond
                                                           (= 0 schema-cnt) (let [linked-ref-pattern (patterns/linked ".*")
                                                                                  blocks-with-plain-links (d/q '[:find ?u ?s
                                                                                                                 :keys block/uid block/string
                                                                                                                 :in $ ?pattern
                                                                                                                 :where
                                                                                                                 [?e :block/uid ?u]
                                                                                                                 [?e :block/string ?s]
                                                                                                                 [(re-find ?pattern ?s)]]
                                                                                                               @db/dsdb
                                                                                                               linked-ref-pattern)
                                                                                  blocks-orig (map (fn [{:block/keys [uid string]}]
                                                                                                     {:db/id [:block/uid uid] :block/string string})
                                                                                                   blocks-with-plain-links)
                                                                                  blocks-temp (map (fn [{:block/keys [uid]}]
                                                                                                     {:db/id [:block/uid uid] :block/string ""})
                                                                                                   blocks-with-plain-links)]
                                                                              ;; give all blocks empty string - clears refs
                                                                              ;; give all blocks their original string - adds refs (for the period of time where block/refs were not added to db
                                                                              ;; update schema version, so this doesn't need to happen again
                                                                              (rf/dispatch [:transact blocks-temp])
                                                                              (rf/dispatch [:transact blocks-orig])
                                                                              (rf/dispatch [:transact [[:db/add -1 :schema/version 1]]]))
                                                           (= 1 schema-cnt) (let [schema-version (-> schemas first second)]
                                                                              (case schema-version
                                                                                1 (prn (str "Schema version " schema-version))
                                                                                (js/alert (js/Error (str "No matching case clause for schema version: " schema-version)))))
                                                           (< 1 schema-cnt)
                                                           (js/alert (js/Error (str "Multiple schema versions: " schemas))))

                                                         (rf/dispatch [:loading/unset])))
                                        :halt?       true}]}})))



;; if localStorage is empty, assume first open
;; create a Documents/athens directory and Documents/athens/db.transit file
;; store path in localStorage and re-frame
;; if localStorage has filepath, and there is a file
;; Open and set db
;; else - localStorage has filepath, but no file at filepath
;; open or create a new starter db

;; Watch filesystem, e.g. in case db is updated via Dropbox sync

#_(rf/reg-event-fx
    :boot/desktop
    (fn [_ _]
      {:db         db/rfdb
       :async-flow {:first-dispatch [:local-storage/get-db-filepath]
                    :rules          [{:when        :seen?
                                      :events      :db/update-filepath
                                      :dispatch-fn (fn [[_ filepath]]
                                                     (cond
                                                       ;; No database path found in localStorage. Creating new one
                                                       (nil? filepath) (rf/dispatch [:fs/create-new-db])
                                                       ;; Database found in local storage and filesystem:
                                                       (.existsSync fs filepath) (let [read-db (.readFileSync fs filepath)
                                                                                       db      (dt/read-transit-str read-db)]
                                                                                   (rf/dispatch [:fs/watch filepath])
                                                                                   (rf/dispatch [:reset-conn db])
                                                                                   (rf/dispatch [:local-storage/create-db-picker-list]))
                                                       :else (rf/dispatch [:fs/open-dialog])))}

                                     ;; remote graph
                                     {:when        :seen?
                                      :events      :start-socket}

                                     ;; if first time, go to Daily Pages and open left-sidebar
                                     {:when       :seen?
                                      :events     :fs/create-new-db
                                      :dispatch-n [[:navigate :home]
                                                   [:left-sidebar/toggle]]}

                                     ;; if nth time, remember dark/light theme and last page
                                     {:when       :seen?
                                      :events     :reset-conn
                                      :dispatch-n [[:local-storage/set-theme]
                                                   #_[:local-storage/navigate]]}

                                     ;; whether first or nth time, update athens pages
                                     #_{:when       :seen-any-of?
                                        :events     [:fs/create-new-db :reset-conn]
                                        :dispatch-n [[:db/retract-athens-pages]
                                                     [:db/transact-athens-pages]]}

                                     ;; bind windows toolbar electron buttons
                                     {:when       :seen-any-of?
                                      :events     [:fs/create-new-db :reset-conn]
                                      :dispatch   [:bind-win-listeners]}


                                     {:when        :seen-any-of?
                                      :events      [:fs/create-new-db :reset-conn]
                                      ;; if schema is nil, update to 1 and reparse all block/string's for links
                                      :dispatch-fn (fn [_]
                                                     (let [schemas    (d/q '[:find ?e ?v
                                                                             :where [?e :schema/version ?v]]
                                                                           @db/dsdb)
                                                           schema-cnt (count schemas)]
                                                       (cond
                                                         (= 0 schema-cnt) (let [linked-ref-pattern      (patterns/linked ".*")
                                                                                blocks-with-plain-links (d/q '[:find ?u ?s
                                                                                                               :keys block/uid block/string
                                                                                                               :in $ ?pattern
                                                                                                               :where
                                                                                                               [?e :block/uid ?u]
                                                                                                               [?e :block/string ?s]
                                                                                                               [(re-find ?pattern ?s)]]
                                                                                                             @db/dsdb
                                                                                                             linked-ref-pattern)
                                                                                blocks-orig             (map (fn [{:block/keys [uid string]}]
                                                                                                               {:db/id [:block/uid uid] :block/string string})
                                                                                                             blocks-with-plain-links)
                                                                                blocks-temp             (map (fn [{:block/keys [uid]}]
                                                                                                               {:db/id [:block/uid uid] :block/string ""})
                                                                                                             blocks-with-plain-links)]
                                                                            ;; give all blocks empty string - clears refs
                                                                            ;; give all blocks their original string - adds refs (for the period of time where block/refs were not added to db
                                                                            ;; update schema version, so this doesn't need to happen again
                                                                            (rf/dispatch [:transact blocks-temp])
                                                                            (rf/dispatch [:transact blocks-orig])
                                                                            (rf/dispatch [:transact [[:db/add -1 :schema/version 1]]]))
                                                         (= 1 schema-cnt) (let [schema-version (-> schemas first second)]
                                                                            (case schema-version
                                                                              1 (prn (str "Schema version " schema-version))
                                                                              (js/alert (js/Error (str "No matching case clause for schema version: " schema-version)))))
                                                         (< 1 schema-cnt)
                                                         (js/alert (js/Error (str "Multiple schema versions: " schemas))))

                                                       (rf/dispatch [:loading/unset])))
                                      :halt?       true}]}}))