(ns athens.electron.boot
  (:require
    [athens.common.sentry      :refer-macros [wrap-span-no-new-tx]]
    [athens.db                 :as db]
    [athens.electron.db-picker :as db-picker]
    [athens.electron.utils     :as utils]
    [athens.router             :as router]
    [athens.utils.sentry       :as sentry]
    [re-frame.core             :as rf]))


(rf/reg-event-fx
  :boot
  [(rf/inject-cofx :local-storage :athens/persist)]
  (fn [{:keys [local-storage]} [_ first-boot?]]
    (let [boot-tx             (sentry/transaction-start "boot-sequence")
          init-app-db         (wrap-span-no-new-tx "db/init-app-db"
                                                   (db/init-app-db local-storage))
          graph-param         (router/consume-graph-param)
          init-app-db         (if graph-param
                                (db-picker/select-db init-app-db graph-param)
                                init-app-db)
          all-dbs             (db-picker/all-dbs init-app-db)
          selected-db         (db-picker/selected-db init-app-db)
          default-db          (utils/get-default-db)
          selected-db-exists? (utils/db-exists? selected-db)
          default-db-exists?  (utils/db-exists? default-db)
          first-event         (cond
                                ;; DB is in-memory, just create a new one.
                                (utils/in-memory-db? selected-db)
                                [:create-in-memory-conn]

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
       :async-flow {:id             :boot-async-flow
                    :db-path        [:async-flow :boot/desktop]
                    :first-dispatch first-event
                    :rules          [;; if first time, go to Daily Pages and open left-sidebar
                                     {:when       :seen?
                                      :events     :fs/create-and-watch
                                      :dispatch-n [[:left-sidebar/toggle]]}

                                     ;; if nth time, remember dark/light theme
                                     {:when       :seen?
                                      :events     :stage/success-db-load
                                      :dispatch-n [[:fs/update-write-db]
                                                   [:db/sync]
                                                   ;; [:restore-navigation]  ; This functionality is there but unreliable we can use it once we make it reliable
                                                   [:reset-undo-redo]
                                                   ;; Only init the router after the db
                                                   ;; is loaded, otherwise we can't check
                                                   ;; if titles/uids in the URL exist.
                                                   [:init-routes!]
                                                   (when-not first-boot?
                                                     ;; Go to home on graph change, but not
                                                     ;; on the first boot.
                                                     ;; We might have a permalink to follow
                                                     ;; on first boot.
                                                     [:navigate :home])
                                                   [:posthog/set-super-properties]
                                                   [:loading/unset]]}
                                     {:when     :seen-all-of?
                                      :events   [[:fs/update-write-db]
                                                 [:db/sync]
                                                 [:reset-undo-redo]
                                                 [:posthog/set-super-properties]
                                                 [:loading/unset]]
                                      :dispatch [:sentry/end-tx boot-tx]
                                      :halt?    true}

                                     ;; halt when started connecting to remote
                                     {:when       :seen?
                                      :events     [:stage/fail-db-load]
                                      :dispatch-n [[:posthog/set-super-properties]
                                                   [:loading/unset]
                                                   [:sentry/end-tx boot-tx]]
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


