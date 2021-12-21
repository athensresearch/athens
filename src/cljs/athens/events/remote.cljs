(ns athens.events.remote
  "`re-frame` events related to `:remote/*`."
  (:require
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common-events.schema          :as schema]
    [athens.common.logging                :as log]
    [athens.common.utils                  :as utils]
    [athens.db                            :as db]
    [clojure.pprint                       :as pp]
    [datascript.core                      :as d]
    [event-sync.core                      :as event-sync]
    [malli.core                           :as m]
    [malli.error                          :as me]
    [re-frame.core                        :as rf]))


;; Connection Management

(rf/reg-event-fx
  :remote/connect!
  (fn [_ [_ remote-db]]
    (log/info ":remote/connect!" (pr-str (:url remote-db)))
    {:remote/client-connect! remote-db
     :fx                     [[:dispatch [:conn-status :connecting]]]}))


(rf/reg-event-fx
  :remote/connected
  (fn [_ _]
    (log/info ":remote/connected")
    {:fx [[:dispatch [:conn-status :connected]]]}))


(rf/reg-event-fx
  :remote/connection-failed
  (fn [_ _]
    (log/warn ":remote/connection-failed")
    {:fx [[:dispatch-n [[:alert/js "Was not able to connect to the remote database."]
                        [:conn-status :disconnected]
                        [:db-picker/select-default-db]]]]}))


(rf/reg-event-fx
  :remote/disconnect!
  (fn [_ _]
    {:remote/client-disconnect! nil
     :dispatch-n                [[:remote/stop-event-sync]
                                 [:presence/clear]]}))


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

(defn- undo-datom
  [[e a v _t added?]]
  [(if added? :db/retract :db/add) e a v])


(defn- undo-tx-data
  "Returns tx-data with all added datoms removed, and all removed datoms added.
  Datoms are returned in reversed order, so the first datom in the undo tx reverses
  the last datom in the original.
  Only works over plain datoms, such as the ones returned from a transaction in :tx-data."
  [tx-data]
  (->> tx-data
       (map undo-datom)
       reverse
       vec))


(rf/reg-event-db
  :remote/update-optimistic-state
  (fn [db [_ new-server-event]]
    ;; This event updates the dsdb optimistic state to reflect the events in the in-memory
    ;; stage of event-sync, plus an optional new server event.
    ;; We do this in a single event so that no other reframe events can happen in the middle.
    ;; Also see :resolve-transact-forward for how new optimistic events are added.

    ;; We apply the new server event (if any) by rolling back all the optimistic events, applying
    ;; the new one, and then reapplying all the optimistic ones on top.
    ;; So for instance, given the sequence of events below
    ;;   [s1 s2 s3 o1 o2 o3]
    ;; where s* events are server events, and o* events are optimistic events, if we
    ;; receive s4 we want to
    ;; - rollback o1 o2 o3
    ;; - apply s4 on top of s1 s2 s3
    ;; - apply o1 o2 o3 on top
    ;; This way we end with
    ;;   [s1 s2 s3 s4 o1 o2 o3]
    ;; Rollback here is performed by undoing the previous transactions.
    ;; This ensures rollback time is proportional to the size of optimistic txs.
    ;; After each rollback, we have to save the new tx-data for the next undo.
    ;; Another approach is to rollback by keeping the last known db state with all the
    ;; server events, but then we'd have to reset the db state and that would cause
    ;; all listeners to process a whole new db state, instead of an incremental one.

    (let [in-memory-events       (-> db :remote/event-sync :stages :memory)
          rollback-tx-data       (-> db :remote/rollback-tx-data)
          count-in-memory-events (count in-memory-events)
          reapplied-tx-data      (atom {})]

      ;; Rollback
      (if (= count-in-memory-events 0)
        (log/debug ":remote/update-optimistic-state rollback skipped, nothing to rollback")
        (do
          (log/debug ":remote/update-optimistic-state rollback" count-in-memory-events "events")
          ;; Undo events in the reverse order (e.g. first undo reverse last event).
          (doseq [[id event] (reverse in-memory-events)]
            (let [id          (utils/uuid->string id)
                  tx          (rollback-tx-data id)
                  rollback-tx (undo-tx-data tx)]
              (if (nil? tx)
                (do
                  ;; It's very bad if this happens. It means we're not rolling back one of the events.
                  ;; It shouldn't ever happen, but we should keep an eye out for it in the beta console logs.
                  (log/warn ":remote/update-optimistic-state no tx-data found for" id)
                  (log/warn ":remote/update-optimistic-state in-memory-events ids" (keys in-memory-events))
                  (log/warn ":remote/update-optimistic-state rollback-tx-data ids" (keys rollback-tx-data)))
                (do
                  (log/debug ":remote/update-optimistic-state rollback event id" (pr-str id))
                  (log/debug ":remote/update-optimistic-state rollback event:")
                  (log/debug (with-out-str (pp/pprint event)))
                  (log/debug ":remote/update-optimistic-state rollback original tx:")
                  (log/debug (with-out-str (pp/pprint tx)))
                  (log/debug ":remote/update-optimistic-state rollback rollback tx:")
                  (log/debug (with-out-str (pp/pprint rollback-tx)))
                  (d/transact! db/dsdb rollback-tx)))))))

      ;; Apply the new event
      (log/debug ":remote/update-optimistic-state apply new event id" (pr-str (:event/id new-server-event)))
      (atomic-resolver/resolve-transact! db/dsdb new-server-event)

      ;; Reapply the optimistic events.
      (doseq [[id event] in-memory-events]
        (log/debug ":remote/update-optimistic-state reapply optimistic event id" (pr-str id))
        (swap! reapplied-tx-data assoc
               (utils/uuid->string id)
               (atomic-resolver/resolve-transact! db/dsdb event)))

      ;; Update rollback-tx-data with the new tx-data.
      (update db :remote/rollback-tx-data merge @reapplied-tx-data))))


(rf/reg-event-db
  :remote/start-event-sync
  (fn [db _]
    (assoc db
           :remote/event-sync (event-sync/create-state :athens [:memory :server])
           :remote/rollback-tx-data {})))


(rf/reg-event-db
  :remote/stop-event-sync
  (fn [db _]
    (dissoc db :remote/event-sync :remote/rollback-tx-data)))


(rf/reg-event-fx
  :remote/clear-server-event
  (fn [{db :db} [_ event]]
    {:db (update db :remote/event-sync #(event-sync/remove % :server (:event/id event) event))}))


(defn- new-event?
  [[type _ _ _ noop?]]
  (and (= type :add) (not noop?)))


(rf/reg-event-fx
  :remote/reject-forwarded-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (log/debug ":remote/reject-forwarded-event event:" (pr-str event))
    (let [db'        (-> db
                         (update :remote/event-sync #(event-sync/remove % :memory (utils/uuid->string id) event))
                         (update :remote/rollback-tx-data dissoc id))
          memory-log (event-sync/stage-log (:remote/event-sync db') :memory)]
      (log/info ":remote/reject-forwarded-event memory-log count" (count memory-log))
      {:db db'
       ;; If there's no events to reapply, just mark as synced, otherwise update the state to remove the
       ;; rejected event.
       :fx [[:dispatch-n (into [[:remote/update-optimistic-state]]
                               (if (empty? memory-log)
                                 [[:db/sync]]
                                 [[:remote/update-optimistic-state]]))]]})))


(rf/reg-event-fx
  :remote/apply-forwarded-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (log/debug ":remote/apply-forwarded-event event:" (pr-str event))
    (let [db'          (-> db
                           ;; Add this event to the server stage.
                           ;; If it was in the in-memory stage, it will be promoted.
                           (update :remote/event-sync #(event-sync/add % :server (utils/uuid->string id) event))
                           ;; Remove this event from the saved tx data for rollbacks.
                           (update :remote/rollback-tx-data dissoc (utils/uuid->string id)))
          new-event?   (new-event? (-> db' :remote/event-sync :last-op))
          memory-log   (event-sync/stage-log (:remote/event-sync db') :memory)
          page-removes (graph-ops/contains-op? (:event/op event) :page/remove)]
      (log/debug ":remote/apply-forwarded-event new event?:" (pr-str new-event?))
      {:db db'
       :fx [[:dispatch-n (cond-> []
                           ;; Mark as synced if there's no events left in memory.
                           (empty? memory-log) (into [[:db/sync]])
                           ;; when it was remove op
                           page-removes        (into [[:page/removed (-> page-removes
                                                                         first
                                                                         :op/args
                                                                         :page/title)]])
                           ;; If there's a new event, apply it.
                           new-event?          (into [[:remote/update-optimistic-state event]])
                           ;; Remove the server event after everything is done.
                           true                (into [[:remote/clear-server-event event]]))]]})))


(rf/reg-event-fx
  :remote/forward-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (log/debug ":remote/forward-event event:" (pr-str event))
    {:db (update db :remote/event-sync #(event-sync/add % :memory (utils/uuid->string id) event))
     :fx [[:dispatch-n [[:remote/send-event! event]
                        [:db/not-synced]]]]}))
