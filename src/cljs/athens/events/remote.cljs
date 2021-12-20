(ns athens.events.remote
  "`re-frame` events related to `:remote/*`."
  (:require
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common-events.schema    :as schema]
    [athens.common.logging          :as log]
    [athens.common.utils            :as utils]
    [athens.db                      :as db]
    [clojure.pprint                 :as pp]
    [datascript.core                :as d]
    [event-sync.core                :as event-sync]
    [malli.core                     :as m]
    [malli.error                    :as me]
    [re-frame.core                  :as rf]))


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
  [tx-data]
  (->> tx-data
       (map undo-datom)
       reverse
       vec))


(rf/reg-event-db
  :remote/rollback-dsdb
  (fn [db _]
    (let [in-memory-events       (-> db :remote/event-sync :stages :memory)
          saved-tx-data          (-> db :remote/tx-data)
          count-in-memory-events (count in-memory-events)
          count-saved-tx-data    (count saved-tx-data)]
      (when (not= count-in-memory-events count-saved-tx-data)
        (log/warn ":remote/rollback-dsdb in-memory-events count does not match saved-tx-data count")
        (log/warn ":remote/rollback-dsdb in-memory-events ids" (keys in-memory-events))
        (log/warn ":remote/rollback-dsdb saved-tx-data ids" (keys saved-tx-data)))

      (if (= count-in-memory-events 0)
        (log/debug ":remote/rollback-dsdb rollback-txs skipped, nothing to rollback")
        (do
          (log/debug ":remote/rollback-dsdb rollback rollback" count-in-memory-events "events")
          (doseq [[id event] (reverse in-memory-events)]
            (let [tx (saved-tx-data id)
                  rollback-tx (undo-tx-data tx)]
              (log/debug ":remote/rollback-dsdb rollback event id" (pr-str id))
              (log/debug ":remote/rollback-dsdb rollback event:")
              (log/debug (with-out-str (pp/pprint event)))
              (log/debug ":remote/rollback-dsdb rollback original tx:")
              (log/debug (with-out-str (pp/pprint tx)))
              (log/debug ":remote/rollback-dsdb rollback rollback tx:")
              (log/debug (with-out-str (pp/pprint rollback-tx)))
              (d/transact! db/dsdb tx)))))
      (assoc db :remote/tx-data {}))))


(rf/reg-event-db
  :remote/start-event-sync
  (fn [db _]
    (assoc db
           :remote/event-sync (event-sync/create-state :athens [:memory :server])
           :remote/tx-data {})))


(rf/reg-event-db
  :remote/stop-event-sync
  (fn [db _]
    (dissoc db :remote/event-sync :remote/tx-data)))


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
                         (update :remote/event-sync #(event-sync/remove % :memory id event))
                         (update :remote/tx-data dissoc id))
          memory-log (event-sync/stage-log (:remote/event-sync db') :memory)]
      (log/info ":remote/reject-forwarded-event memory-log count" (count memory-log))
      {:db db'
       ;; Rollback and reapply all events in the memory stage.
       ;; If there's no events to reapply, just mark as synced.
       :fx [[:dispatch-n (into [[:remote/rollback-dsdb]]
                               (if (empty? memory-log)
                                 [[:db/sync]]
                                 (map (fn [e] [:resolve-transact-forward (second e) true])
                                      (-> db' :remote/event-sync :stages :memory))))]]})))


(rf/reg-event-fx
  :remote/apply-forwarded-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (log/debug ":remote/apply-forwarded-event event:" (pr-str event))
    (let [db'          (-> db
                           (update :remote/event-sync #(event-sync/add % :server id event))
                           (update :remote/tx-data dissoc (utils/uuid->string id)))
          new-event?   (new-event? (-> db' :remote/event-sync :last-op))
          memory-log   (event-sync/stage-log (:remote/event-sync db') :memory)
          page-removes (graph-ops/contains-op? (:event/op event) :page/remove)]
      (log/debug ":remote/apply-forwarded-event new event?:" (pr-str new-event?))
      (log/debug ":remote/apply-forwarded-event memory-log count" (count memory-log))
      {:db db'
       :fx [[:dispatch-n (cond-> []
                           ;; Mark as synced if there's no events left in memory.
                           (empty? memory-log) (into [[:db/sync]])
                           ;; when it was remove op
                           page-removes        (into [[:page/removed (-> page-removes
                                                                         first
                                                                         :op/args
                                                                         :page/title)]])
                           ;; If there's a new event, apply it over the last dsdb snapshot from
                           ;; the server, then use that as the new snapshot, then reapply
                           ;; all events in the memory stage.
                           new-event?          (into [[:remote/rollback-dsdb]
                                                      [:resolve-transact-forward event true true]])
                           new-event?          (into (map (fn [e] [:resolve-transact-forward (second e) true])
                                                          (-> db' :remote/event-sync :stages :memory)))
                           ;; Remove the server event after everything is done.
                           true                (into [[:remote/clear-server-event event]]))]]})))


(rf/reg-event-fx
  :remote/forward-event
  (fn [{db :db} [_ {:event/keys [id] :as event}]]
    (log/debug ":remote/forward-event event:" (pr-str event))
    {:db (update db :remote/event-sync #(event-sync/add % :memory id event))
     :fx [[:dispatch-n [[:remote/send-event! event]
                        [:db/not-synced]]]]}))
