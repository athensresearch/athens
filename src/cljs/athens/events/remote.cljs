(ns athens.events.remote
  "`re-frame` events related to `:remote/*`."
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.resolver        :as resolver]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common-events.schema          :as schema]
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
    (js/console.log ":remote/connect!" (pr-str remote-db))
    {:remote/client-connect! remote-db
     :fx                     [[:dispatch [:loading/set]]
                              [:dispatch [:conn-status :connecting]]]}))


(rf/reg-event-fx
  :remote/connected
  (fn [_ _]
    (js/console.log ":remote/connected")
    {:fx [[:dispatch-n [[:loading/unset]
                        [:conn-status :connected]
                        [:db/sync]]]]}))


(rf/reg-event-fx
  :remote/connection-failed
  (fn [_ _]
    (js/console.log ":remote/connection-failed")
    {:fx [[:dispatch-n [[:loading/unset]
                        [:conn-status :disconnected]
                        [:db/sync]
                        ;; THIS was not recommendation from Stu
                        [:modal/toggle]]]]}))


(rf/reg-event-fx
  :remote/disconnect!
  (fn [_ _]
    {:remote/client-disconnect!   nil
     :remote/clear-dsdb-snapshop! nil
     :dispatch                    [:remote/stop-event-sync]}))


(rf/reg-event-db
  :remote/updated-last-seen-tx
  (fn [db _]
    (js/console.debug ":remote/updated-last-seen-tx")
    db))


(rf/reg-event-fx
  :remote/last-seen-tx!
  (fn [{db :db} [_ new-tx-id]]
    (js/console.debug "last-seen-tx!" new-tx-id)
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
        (js/console.warn "Not sending invalid event. Error:" (pr-str explanation))
        (js/console.warn "Invalid event was:" (pr-str event))))))


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
    (js/console.debug ":remote/snapshot-dsdb! event at time" (:max_tx @db/dsdb))
    (reset! db/dsdb-snapshot @db/dsdb)))


(rf/reg-event-fx
  :remote/rollback-dsdb
  (fn [_ _]
    (js/console.debug ":remote/rollback-dsdb event from time" (:max_tx @db/dsdb-snapshot))
    {:reset-conn! @db/dsdb-snapshot}))


(rf/reg-event-db
  :remote/start-event-sync
  (fn [db _]
    (assoc db :event-sync (event-sync/create-state :athens [:memory :server]))))


(rf/reg-event-db
  :remote/stop-event-sync
  (fn [db _]
    (dissoc db :event-sync)))


(rf/reg-event-fx
  :remote/clear-server-event
  (fn [{db :db} [_ event]]
    {:db (update db :event-sync (partial event-sync/remove :server (:event/id event) event))}))


(defn- resolve-op
  ([event]
   (resolve-op @db/dsdb event))
  ([db {:event/keys [type op] :as event}]
   (if (contains? #{:op/atomic} type)
     (atomic-resolver/resolve-atomic-op-to-tx db op)
     (resolver/resolve-event-to-tx db event))))


(defn- changed-order?
  [[type _ _ _ noop?]]
  (and (= type :add) (not noop?)))


(rf/reg-event-fx
  :remote/snapshot-transact
  (fn [_ [_ tx-data]]
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
  :remote/apply-forwarded-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (js/console.debug ":remote/apply-forwarded-event event:" (pr-str event))
    (let [db'            (update db :event-sync (partial event-sync/add :server id event))
          changed-order? (changed-order? (-> db' :event-sync :last-op))
          memory-log     (event-sync/stage-log (:event-sync db) :memory)
          txs            (resolve-op @db/dsdb-snapshot event)]
      (js/console.debug ":remote/apply-forwarded-event event changed order?:" changed-order?)
      (js/console.debug ":remote/apply-forwarded-event resolved txs:" (pr-str txs))
      {:db db'
       :fx [[:dispatch-n (cond-> []
                           ;; Mark as synced if there's no events left in memory.
                           (empty? memory-log)  (into [[:db/sync]])
                           ;; If order does not change, just update the snapshot with tx.
                           (not changed-order?) (into [[:remote/snapshot-transact txs]])
                           ;; If order changes, apply the tx over the last dsdb snapshot from,
                           ;; the server, then use that as the new snapshot, then reapply
                           ;; all events in the memory stage.
                           changed-order?       (into [[:remote/rollback-dsdb]
                                                       [:transact txs]
                                                       [:remote/snapshot-dsdb]])
                           changed-order?       (into (map (fn [e] [:transact (-> e second resolve-op)])
                                                           (-> db' :event-sync :stages :memory)))
                           ;; Remove the server event after everything is done.
                           true                  (into [[:remote/clear-server-event event]]))]]})))


(rf/reg-event-fx
  :remote/forward-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (js/console.debug ":remote/forward-event event:" (pr-str event))
    {:db (update db :event-sync (partial event-sync/add :memory id event))
     :fx [[:dispatch-n [[:remote/send-event! event]
                        [:db/not-synced]]]]}))
