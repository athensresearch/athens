(ns athens.electron.boot
  (:require
    [athens.db :as db]
    [athens.electron.db-picker :as db-picker]
    [athens.electron.utils :as utils]
    [re-frame.core :as rf]))


(rf/reg-event-fx
  :boot/desktop
  [(rf/inject-cofx :local-storage :athens/persist)]
  (fn [{:keys [local-storage]} _]
    (let [init-app-db         (db/init-app-db local-storage)
          all-dbs             (db-picker/all-dbs init-app-db)
          selected-db         (db-picker/selected-db init-app-db)
          default-db          (utils/local-db (utils/default-base-dir))
          selected-db-exists? (utils/db-exists? selected-db)
          default-db-exists?  (utils/db-exists? default-db)
          first-event         (cond
                                ;; DB is remote, attempt to connect to it.
                                (utils/remote-db? selected-db)
                                [:remote/connect! selected-db]

                                ;; No selected db but there are dbs listed.
                                ;; Load the first one.
                                (and (not selected-db-exists?)
                                     (seq all-dbs))
                                [:fs/add-read-and-watch (-> all-dbs first second)]

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


      ;; output => [:reset-conn] OR [:fs/create-and-watch]

      {:db         init-app-db
       :dispatch-n [[:theme/set]
                    [:loading/set]]
       :async-flow {:first-dispatch first-event
                    :rules          [;; if first time, go to Daily Pages and open left-sidebar
                                     {:when       :seen?
                                      :events     :fs/create-and-watch
                                      :dispatch-n [[:navigate :home]
                                                   [:left-sidebar/toggle]]}

                                     ;; if nth time, remember dark/light theme
                                     {:when       :seen?
                                      :events     :reset-conn
                                      :dispatch-n [[:fs/update-write-db]
                                                   [:db/sync]
                                                   ;; [:restore-navigation]  ; This functionality is there but unreliable we can use it once we make it reliable
                                                   [:navigate :home]
                                                   [:reset-undo-redo]
                                                   [:posthog/set-super-properties]
                                                   [:loading/unset]]
                                      ;; This event ends the async flow successfully.
                                      :halt?      true}

                                     {:when       :seen?
                                      :events     :remote/connection-failed
                                      :dispatch   [:db-picker/remove-selection]
                                      ;; This event ends the async flow unsuccessfully
                                      ;; and tries to reboot on a different db.
                                      :halt?      true}

                                     ;; whether first or nth time, update athens pages
                                     #_{:when       :seen-any-of?
                                        :events     [:fs/create-and-watch :reset-conn]
                                        :dispatch-n [[:db/retract-athens-pages]
                                                     [:db/transact-athens-pages]]}

                                     ;; bind windows toolbar electron buttons
                                     #_{:when     :seen-any-of?
                                        :events   [:fs/create-and-watch :reset-conn]
                                        :dispatch [:bind-win-listeners]}]}})))


