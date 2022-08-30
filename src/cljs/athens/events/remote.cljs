(ns athens.events.remote
  "`re-frame` events related to `:remote/*`."
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging                :as log]
    [athens.common.utils                  :as utils]
    [athens.db                            :as db]
    [athens.interceptors                  :as interceptors]
    [athens.undo                          :as undo]
    [clojure.pprint                       :as pp]
    [clojure.string                       :as string]
    [datascript.core                      :as d]
    [event-sync.core                      :as event-sync]
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
    {:fx [[:dispatch-n [[:alert/js "Couldn't connect to remote workspace."]
                        [:conn-status :disconnected]
                        [:db-picker/select-default-db]]]]}))


(rf/reg-event-fx
  :remote/disconnect!
  (fn [_ _]
    {:remote/client-disconnect! nil
     :dispatch-n                [[:remote/stop-event-sync]
                                 [:presence/clear]]}))


;; Optimistic state management

;; These fns update the dsdb optimistic state to reflect the events in event-sync.
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


(defn- promotion?
  [[type _ _ _ noop?]]
  (and (= type :promote) (not noop?)))


(defn- rollback!
  "Revert all events in the :memory stage of event-sync via undo.
  Should only be used followed by rollforward."
  [[db conn]]
  (let [in-memory-events       (-> db :remote/event-sync :stages :memory)
        rollback-tx-data       (-> db :remote/rollback-tx-data)
        count-in-memory-events (count in-memory-events)
        rolled-back-events     (atom #{})]

    (if (= count-in-memory-events 0)
      (log/debug "rollback skipped, nothing to rollback")
      (do
        (log/debug "rollback" count-in-memory-events "events")
        ;; Undo events in the reverse order (e.g. first undo reverse last event).
        (doseq [[id event]  (reverse in-memory-events)]
          (let [id          (utils/uuid->string id)
                tx          (rollback-tx-data id)
                rollback-tx (undo-tx-data tx)]
            (if (nil? tx)
              (do
                ;; It's very bad if this happens. It means we're not rolling back one of the events.
                ;; It shouldn't ever happen, but we should keep an eye out for it in the beta console logs.
                (log/warn "rollback no tx-data found for" id)
                (log/warn "rollback in-memory-events ids" (keys in-memory-events))
                (log/warn "rollback rollback-tx-data ids" (keys rollback-tx-data)))
              (do
                (log/debug "rollback event id" (pr-str id))
                (log/debug "rollback event:")
                (log/debug (with-out-str (pp/pprint event)))
                (log/debug "rollback original tx:")
                (log/debug (with-out-str (pp/pprint tx)))
                (log/debug "rollback rollback tx:")
                (log/debug (with-out-str (pp/pprint rollback-tx)))
                (d/transact! conn rollback-tx)
                (swap! rolled-back-events conj id)))))))

    ;; Remove rolled back txs from rollback-tx-data.
    (let [db' (update db :remote/rollback-tx-data #(apply dissoc % @rolled-back-events))]
      [db' conn])))


(defn- rollforward!
  "Apply all events in the :memory stage of event-sync via resolve.
  Should only be used preceded by rollback."
  [[db conn]]
  (let [in-memory-events       (-> db :remote/event-sync :stages :memory)
        count-in-memory-events (count in-memory-events)
        reapplied-tx-data      (atom {})]

    (if (= count-in-memory-events 0)
      (do
        (log/debug "rollforward skipped, nothing to rollforward")
        [db conn])
      (do
        (log/debug "rollforward" count-in-memory-events "events")

        ;; Reapply the optimistic events.
        (doseq [[id event] in-memory-events]
          (log/debug "rollforward apply event id" (pr-str id))
          (swap! reapplied-tx-data assoc
                 (utils/uuid->string id)
                 (atomic-resolver/resolve-transact! conn event)))

        ;; Add new txs to rollback-tx-data.
        (let [db' (update db :remote/rollback-tx-data merge @reapplied-tx-data)]
          [db' conn])))))


(defn add-memory-event!
  "Add an event to the memory stage."
  [[db conn] {:event/keys [id] :as event}]
  ;; Apply the new event, store it in the memory stage, and save the tx-data for rollback.
  (log/debug "add-memory-event apply event id" (pr-str id))
  (let [id'     (utils/uuid->string id)
        tx-data (atomic-resolver/resolve-transact! conn event)
        db'     (-> db
                    (update :remote/event-sync #(event-sync/add % :memory id' event))
                    (update :remote/rollback-tx-data assoc id' tx-data))]
    [db' conn]))


(defn- add-server-event!
  "Add an event to the server stage.
  Should only be used between rollback and rollforward."
  [[db conn] {:event/keys [id] :as event}]
  ;; Apply the new event and store it in the server stage.
  (log/debug "add-server-event apply event id" (pr-str (:event/id event)))
  (let [id' (utils/uuid->string id)
        _   (atomic-resolver/resolve-transact! conn event)
        db' (-> db
                (update :remote/event-sync #(event-sync/add % :server id' event))
                (update :remote/rollback-tx-data dissoc id'))]
    [db' conn]))


(defn- promote-event!
  "Promotes an event to the server stage.
  Should only be used without rollback and rollforward.
  Returns nil if event is not a promotion."
  [[db conn] {:event/keys [id] :as event}]
  ;; Apply the new event and store it in the server stage.
  (log/debug "promote-event apply event id" (pr-str (:event/id event)))
  (let [id' (utils/uuid->string id)
        db' (-> db
                (update :remote/event-sync #(event-sync/add % :server id' event))
                (update :remote/rollback-tx-data dissoc id'))]
    (if (promotion? (-> db' :remote/event-sync :last-op))
      [db' conn]
      nil)))


(defn- remove-memory-event!
  "Remove an event from the memory stage.
  Should only be used between rollback and rollforward."
  [[db conn] {:event/keys [id] :as event}]
  (log/debug "remove-memory-event apply event id" (pr-str id))
  (let [id' (utils/uuid->string id)
        _   (atomic-resolver/resolve-transact! conn event)
        db' (-> db
                (update :remote/event-sync #(event-sync/remove % :memory id' event))
                (update :remote/rollback-tx-data dissoc id'))]
    [db' conn]))


;; Remote graph related events

(rf/reg-event-db
  :remote/start-event-sync
  [(interceptors/sentry-span-no-new-tx ":remote/start-event-sync")]
  (fn [db _]
    (assoc db
           :remote/event-sync (event-sync/create-state :athens [:memory :server])
           :remote/rollback-tx-data {})))


(rf/reg-event-db
  :remote/stop-event-sync
  [(interceptors/sentry-span-no-new-tx ":remote/stop-event-sync")]
  (fn [db _]
    (dissoc db :remote/event-sync :remote/rollback-tx-data)))


(rf/reg-event-fx
  :remote/clear-server-event
  [(interceptors/sentry-span-no-new-tx ":remote/clear-server-event")]
  (fn [{db :db} [_ event]]
    {:db (update db :remote/event-sync #(event-sync/remove % :server (:event/id event) event))}))


(rf/reg-event-fx
  :remote/reject-forwarded-event
  [(interceptors/sentry-span ":remote/reject-forwarded-event")]
  (fn [{db :db} [_ event]]
    (log/debug ":remote/reject-forwarded-event event:" (pr-str event))
    (let [[db']      (-> [db db/dsdb]
                         rollback!
                         (remove-memory-event! event)
                         rollforward!)
          db''       (undo/remove db (:event/id event))
          memory-log (event-sync/stage-log (:remote/event-sync db') :memory)]
      {:db db''
       ;; If there's no events to reapply mark as synced.
       :fx [[:dispatch-n (when (empty? memory-log)
                           [[:db/sync]])]]})))


(defn current-page-removed?
  [db title]
  (let [route-template     (get-in db [:current-route :template])
        page-title?        (string/starts-with? route-template "/page-t/")
        current-page-title (if page-title?
                             (get-in db [:current-route :path-params :title])
                             (when-let [block-uid (get-in db [:current-route :path-params :id])]
                               (loop [block (common-db/get-block @db/dsdb [:block/uid block-uid])]
                                 (cond
                                   (nil? block)        nil
                                   (:node/title block) (:node/title block)
                                   :else               (recur (common-db/get-parent @db/dsdb [:block/uid (:block/uid block)]))))))]
    (= current-page-title title)))


(rf/reg-event-fx
  :remote/apply-forwarded-event
  [(interceptors/sentry-span ":remote/apply-forwarded-event")]
  (fn [{db :db} [_ event]]
    (log/debug ":remote/apply-forwarded-event event:" (pr-str event))
    (let [;; Check if the current page will be removed before processing the event.
          page-removed (graph-ops/contains-op? (:event/op event) :page/remove)
          page-title   (when page-removed
                         (-> page-removed first :op/args :page/title))
          removed?     (when page-title
                         (current-page-removed? db page-title))
          ;; Process events.
          [db']        (or (promote-event! [db db/dsdb] event)
                           (-> [db db/dsdb]
                               rollback!
                               (add-server-event! event)
                               rollforward!))
          memory-log   (event-sync/stage-log (:remote/event-sync db') :memory)]
      {:db db'
       :fx [[:dispatch-n (cond-> []
                           ;; Mark as synced if there's no events left in memory.
                           (empty? memory-log)
                           (into [[:db/sync]])

                           ;; When the current page was removed
                           removed?
                           (into [[:alert/js (str "This page \"" page-title "\" has being deleted by other player.")]
                                  [:navigate :home]])

                           ;; Remove the server event after everything is done.
                           true
                           (into [[:remote/clear-server-event event]]))]]})))
