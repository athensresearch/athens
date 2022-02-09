(ns athens.utils.sentry
  "Sentry integration utilities."
  (:require
    ["@sentry/react" :as Sentry]
    ["@sentry/react"]
    [athens.common.logging :as log]))


(def tx-active (atom nil))


(defn transaction-start
  "Starts new Sentry Transaction"
  [tx-name]
  (log/debug "Sentry: Starting TX:" tx-name)
  (let [tx (.startTransaction Sentry (clj->js {:name tx-name}))]
    (reset! tx-active {:name tx-name
                       :tx   tx})
    tx))


(defn tx-running?
  "Checks if there is TX running"
  []
  (not (nil? @tx-active)))


(defn transaction-get-current
  "Tries to find existing Sentry Transaction"
  []
  (let [{:keys [name tx]} @tx-active]
    (log/debug "Sentry: Current TX:"
               (when tx
                 (aget tx "name"))
               (when tx
                 name))
    (if tx
      tx
      (try
        (throw (js/Error. "FU from transaction-get-current"))
        (catch js/Error ex
          (js/console.warn ex))))))


(defn transaction-get-current-name
  "Tries to find existing Sentry Transaction name"
  []
  (let [{:keys [name tx]} @tx-active]
    (log/debug "Sentry: Current TX Name:"
               (when tx
                 (aget tx "name"))
               (when tx
                 name))
    name))


(defn transaction-finish
  "Finishes provided transaction"
  ([]
   (when-let [{:keys [name tx]} @tx-active]
     (log/debug "Sentry: Finishing TX:" name)
     (.finish tx)
     (reset! tx-active nil)))
  ([tx]
   (when tx
     (log/debug "Sentry: Finishing TX, don't know the name though, maybe it is:" (:name @tx-active))
     (.finish tx)
     (reset! tx-active nil))))


(def span-stack (atom []))


(defn span-active
  "Provides active Sentry Span if any exists."
  []
  (peek @span-stack))


(defn span-start
  "Starts a *span* named `op-name` within given `transaction`,
  with optional `op-description`.

  Arguments:
  * `op-name`: operation name
  * `transaction`: Sentry transaction like object
  * `stack?`: (optional - defaults to true) if newly created span should be put on stack"
  ([op-name]
   (span-start (transaction-get-current) op-name))
  ([transaction op-name]
   (span-start transaction op-name true))
  ([transaction op-name stack?]
   (when transaction
     (let [span (.startChild transaction (clj->js {:op op-name}))]
       (log/debug "Sentry: Started Span:" op-name stack?)
       (when stack?
         (swap! span-stack conj span))
       span))))


(def span-status-mapping
  {:ok                  "ok"
   :deadline-exceeded   "deadline_exceeded"
   :unauthenticated     "unauthenticated"
   :permission-denied   "permission_denied"
   :not-found           "not_found"
   :resource-exhausted  "resource_exhausted"
   :invalid-argument    "invalid_argument"
   :unimplemented       "unimplemented"
   :unavailable         "unavailable"
   :internal-error      "internal_error"
   :unknown-error       "unknown_error"
   :cancelled           "cancelled"
   :already-exists      "already_exists"
   :failed-precondition "failed_precondition"
   :aborted             "aborted"
   :out-of-range        "out_of_range"
   :data-loss           "data_loss"})


(defn span-set-status
  "Sets `status` on given `span`.
  Allowed statuses are keys in `span-status-mapping`, defaults to `:unknown-error`."
  [span status]
  (.setStatus span {:op (span-status-mapping status "unknown_error")}))


(defn span-finish
  "Finish provided `span`."
  ([]
   (if-let [active (span-active)]
     (span-finish active)
     (try
       (throw (js/Error. "FU, can't finish, there is nothing to finish"))
       (catch js/Error ex
        (js/console.warn ex "Oh well, you've asked me to close span, but there is no span, go figure.")))))
  ([span]
   (span-finish span true))
  ([span stack?]
   {:pre [(not (nil? span))]}
   (log/debug "Sentry: Finished Span" (aget span "op") stack?)
   (let [active-span (span-active)]
     (when (and stack?
                active-span
                (= active-span span))
       (swap! span-stack pop))
     (.finish span))))
