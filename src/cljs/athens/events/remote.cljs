(ns athens.events.remote
  "`re-frame` events related to `:remote/*`."
  (:require
   ["/web3.storage" :as web3.storage]
   [clojure.edn :as edn]
    [athens.common-db                     :as common-db]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common-events.schema          :as schema]
    [athens.common.logging                :as log]
    [athens.db                            :as db]
    [datascript.core                      :as d]
    [event-sync.core                      :as event-sync]
    [malli.core                           :as m]
    [malli.error                          :as me]
    [re-frame.core                        :as rf]))


;; Connection Management

(rf/reg-event-fx
  :remote/connect!
  (fn [_ [_ remote-db]]
    (log/info ":remote/connect!" (pr-str remote-db))
    {:remote/client-connect! remote-db
     :fx                     [[:dispatch [:loading/set]]
                              [:dispatch [:conn-status :connecting]]]}))


(rf/reg-event-fx
  :remote/connected
  (fn [_ _]
    (log/info ":remote/connected")
    {:fx [[:dispatch-n [[:loading/unset]
                        [:conn-status :connected]
                        [:db/sync]]]]}))


(rf/reg-event-fx
  :remote/connection-failed
  (fn [_ _]
    (log/warn ":remote/connection-failed")
    {:fx [[:dispatch-n [[:loading/unset]
                        [:conn-status :disconnected]
                        [:db/sync]]]]}))


(rf/reg-event-fx
  :remote/disconnect!
  (fn [_ _]
    {:remote/client-disconnect!   nil
     :remote/clear-dsdb-snapshot! nil
     :dispatch                    [:remote/stop-event-sync]}))


(rf/reg-event-db
  :remote/updated-last-seen-tx
  (fn [db _]
    ;; TODO(RTC): clean this up, we don't need last seen TX no mo
    (log/debug ":remote/updated-last-seen-tx")
    db))


(rf/reg-event-fx
  :remote/last-seen-tx!
  (fn [{db :db} [_ new-tx-id]]
    (log/debug "last-seen-tx!" new-tx-id)
    {:db (assoc db :remote/last-seen-tx new-tx-id)
     :fx [[:dispatch [:remote/updated-last-seen-tx]]]}))


;; Send it

(rf/reg-event-fx
  :remote/send-event!
  (fn [_ [_ event]]
    (if (schema/valid-event? event)
      ;; valid event, send item
      {:remote/send-event-fx! event}
      (let [explanation (-> schema/event
                            (m/explain event)
                            (me/humanize))]
        ;; TODO display alert?
        (log/warn "Not sending invalid event. Error:" (pr-str explanation)
                  "\nInvalid event was:" (pr-str event))))))


;; Remote graph related events


(rf/reg-fx
  :remote/clear-dsdb-snapshot!
  (fn []
    (reset! db/dsdb-snapshot nil)))


(rf/reg-event-fx
  :remote/snapshot-dsdb
  (fn [_ _]
    {:remote/snapshot-dsdb! nil}))


(rf/reg-fx
  :remote/snapshot-dsdb!
  (fn []
    (log/debug ":remote/snapshot-dsdb! event at time" (:max-tx @db/dsdb))
    (reset! db/dsdb-snapshot @db/dsdb)))


(rf/reg-event-fx
  :remote/rollback-dsdb
  (fn [_ _]
    (log/debug ":remote/rollback-dsdb event to time" (:max-tx @db/dsdb-snapshot))
    {:reset-conn! @db/dsdb-snapshot}))


(def prefix "athens-web3-poc-3-")
(def first-listen (atom true))

(defn listen []
  (web3.storage/listen prefix (fn [events]
                                (doseq [event events]
                                  (println "--web3 apply" event)
                                  (rf/dispatch [:remote/apply-forwarded-event (edn/read-string event)]))
                                (when @first-listen
                                  (println "--web3 first listen done")
                                  (rf/dispatch [:remote/connected])
                                  (reset! first-listen false)))))

(def last-sent-id (atom nil))

(defn send [id event]
  (when (not= id @last-sent-id)
    ;; send oldest event
    (web3.storage/put (str prefix id) (pr-str event))
    (reset! last-sent-id id)))


(rf/reg-event-db
  :remote/start-event-sync
  (fn [db _]

    ;; start listening
    (listen)

    (assoc db :event-sync (event-sync/create-state :athens [:memory :server]))))


(rf/reg-event-db
  :remote/stop-event-sync
  (fn [db _]
    (dissoc db :event-sync)))


(rf/reg-event-fx
  :remote/clear-server-event
  (fn [{db :db} [_ event]]
    {:db (update db :event-sync #(event-sync/remove % :server (:event/id event) event))}))


(defn- changed-order?
  [[type _ _ _ noop?]]
  ;; TODO: if we support rejections via event removal, this also needs to check
  ;; if the :remove changed order, while still ignoring removal from the tail.
  (and (= type :add) (not noop?)))


(rf/reg-event-fx
  :remote/snapshot-transact
  (fn [_ [_ tx-data]]
    (log/debug ":remote/snapshot-transact update to time" (inc (:max-tx @db/dsdb-snapshot)))
    {:remote/snapshot-transact! tx-data}))


(rf/reg-fx
  :remote/snapshot-transact!
  (fn [tx-data]
    (swap! db/dsdb-snapshot
           (fn [db]
             (d/db-with db (->> tx-data
                                (common-db/linkmaker db)
                                (common-db/linkmaker db)))))))


(rf/reg-event-fx
  :remote/reject-forwarded-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (log/debug ":remote/reject-forwarded-event event:" (pr-str event))
    (let [db'            (update db :event-sync #(event-sync/remove % :memory id event))
          memory-log     (event-sync/stage-log (:event-sync db') :memory)]
      {:db db'
       ;; Rollback and reapply all events in the memory stage.
       ;; If there's no events to reapply, just mark as synced.
       :fx [[:dispatch-n (into [[:remote/rollback-dsdb]] (if (empty? memory-log)
                                                           [[:db/sync]]
                                                           (map (fn [e] [:resolve-transact (second e)])
                                                                (-> db' :event-sync :stages :memory))))]]})))

(rf/reg-event-fx
  :remote/apply-forwarded-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (log/debug ":remote/apply-forwarded-event event:" (pr-str event))
    (let [db'            (update db :event-sync #(event-sync/add % :server id event))
          changed-order? (changed-order? (-> db' :event-sync :last-op)) ;; something wrong here on live, breaks web3 on startup
          memory-log     (event-sync/stage-log (:event-sync db') :memory) ;; just adding doesn't need rollback, right?
          _ (println "before resolve")
          ;;txs            (atomic-resolver/resolve-to-tx @db/dsdb-snapshot event)
          ]
      (log/debug ":remote/apply-forwarded-event event changed order?:" changed-order?)
      ;;(log/debug ":remote/apply-forwarded-event resolved txs:" (pr-str txs))

      (when-let [[id event] (last memory-log)]
        (println "--web3 events in memory-log" (count memory-log))
        ;; got an event but still have more left, send oldest event
        (send id event))

      {:db db'
       :fx [[:dispatch-n (cond-> []
                           ;; Mark as synced if there's no events left in memory.
                           (empty? memory-log)  (into [[:db/sync]])
                           ;; If order does not change, just update the snapshot with tx.
                           ;; (not changed-order?) (into [[:remote/snapshot-transact txs]])
                           ;; If order changes, apply the tx over the last dsdb snapshot from,
                           ;; the server, then use that as the new snapshot, then reapply
                           ;; all events in the memory stage.
                           changed-order?       (into [#_[:remote/rollback-dsdb]
                                                       [:resolve-transact event] ;; bug in live code!, needs to resolve against rollback
                                                       #_[:remote/snapshot-dsdb]]) ;; rollback-resolve-transact-snap
                           changed-order?       (into (map (fn [e] [:resolve-transact (second e)])
                                                           (-> db' :event-sync :stages :memory)))
                           ;; Remove the server event after everything is done.
                           true                 (into [[:remote/clear-server-event event]]))]]})))


(rf/reg-event-fx
  :remote/forward-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (log/debug ":remote/forward-event event:" (pr-str event))

    (when (empty? (event-sync/stage-log (:event-sync db) :memory))
      ;; don't have any in flight, send the new event
      (send id event))

    {:db (update db :event-sync #(event-sync/add % :memory id event))
     :fx [[:dispatch-n [#_[:remote/send-event! event]
                        [:db/not-synced]]]]}))


#_(.. (web3.storage/list "")
      (then println))
#_(web3.storage/put "{:hello 3}")
#_(.. (web3.storage/get "bafybeiakzwlsfqudrf4vjtrpyntpjihzi6cuwnrwclcgas5tikrvb7vbx4")
      (then println))

#_(web3.storage/listen "" (fn [events]
                          (println (js->clj events))))
