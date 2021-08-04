(ns athens.electron.boot
  (:require [athens.db :as db]
            [re-frame.core :as rf]
            [datascript.core :as d]
            [datascript.transit :as dt]
            [athens.patterns :as patterns]
            [athens.electron.utils :as utils]))


(def fs (js/require "fs"))


(defn- init-app-db
  [app-db persist]
  (merge app-db {:athens/persist persist}))


(rf/reg-event-fx
  :boot/desktop
  [(rf/inject-cofx :local-storage :athens/persist)]
  (fn [{:keys [local-storage] :as _cofx} _]
    (let [init-app-db         (init-app-db db/rfdb local-storage)
          all-dbs             (get-in init-app-db [:athens/persist :db-picker/all-dbs])
          selected-db         (get-in init-app-db [:athens/persist :db-picker/selected-db])
          default-db          (utils/local-db (utils/default-db-path))
          selected-db-exists? (utils/local-db-exists? selected-db)
          default-db-exists?  (utils/local-db-exists? default-db)
          first-event         (cond
                                ;; No selected db but there are dbs listed.
                                ;; Load the first one.
                                (and (not selected-db-exists?)
                                     (seq all-dbs))
                                [:fs/read-and-watch (-> all-dbs first second)]

                               ;; Selected db not found in local storage, but default db found.
                               ;; Add default db and load it.
                                (and (not selected-db-exists?)
                                     default-db-exists?)
                                [:fs/add-read-and-watch default-db]

                                ;; Selected db not found in local storage, no default db found.
                                ;; Create new db and load it.
                                (and (not selected-db-exists?)
                                     (not default-db-exists?))
                                [:fs/create-and-watch default-db]

                                ;; Selected found in local storage and on filesystem.
                                ;; Load it.
                                selected-db-exists?
                                [:fs/read-and-watch selected-db]

                                ;; Selected db found in local storage but not on filesystem, or no matching condition.
                                ;; Open open-dialog.
                                :else [:fs/open-dialog selected-db])]


      ;; output => [:reset-conn] OR [:fs/create-and-watch] OR :local-storage/create-db-picker-list

      {:db         init-app-db
       :async-flow {:first-dispatch first-event
                    :rules          [;; if first time, go to Daily Pages and open left-sidebar
                                     {:when       :seen?
                                      :events     :fs/create-and-watch
                                      :dispatch-n [[:navigate :home]
                                                   [:left-sidebar/toggle]]}

                                     ;; if nth time, remember dark/light theme and last page
                                     {:when       :seen?
                                      :events     :reset-conn
                                      :dispatch-n [[:local-storage/set-theme]
                                                   #_[:local-storage/navigate]]}

                                     ;; whether first or nth time, update athens pages
                                     #_{:when       :seen-any-of?
                                        :events     [:fs/create-and-watch :reset-conn]
                                        :dispatch-n [[:db/retract-athens-pages]
                                                     [:db/transact-athens-pages]]}

                                     ;; bind windows toolbar electron buttons
                                     #_{:when     :seen-any-of?
                                        :events   [:fs/create-and-watch :reset-conn]
                                        :dispatch [:bind-win-listeners]}


                                     {:when        :seen-any-of?
                                      :events      [:fs/create-and-watch :reset-conn]
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
                                      :halt? true}]}})))


