(ns athens.utils.sentry
  "Sentry integration utilities."
  (:require
    [athens.common.logging :as log]
    ["@sentry/react" :as Sentry]
    ["@sentry/react"]))


(def tx-active (atom nil))


(defn transaction-start
  "Starts new Sentry Transaction"
  [tx-name]
  (log/debug "Sentry: Starting TX:" tx-name)
  (let [tx (.startTransaction Sentry (clj->js {:name tx-name}))]
    (reset! tx-active {:name tx-name
                       :tx   tx})
    tx))


(defn transaction-get-current
  "Tries to find existing Sentry Transaction"
  []
  (let [{:keys [name tx]} @tx-active]
    (log/debug "Sentry: Current TX:" (when tx
                                       name))
    tx))


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
  with optional `op-description`."
  ([transaction op-name]
   (when transaction
     (let [span (.startChild transaction (clj->js {:op op-name}))]
       (swap! span-stack conj span)
       span)))
  ([transaction op-name op-description]
   (when transaction
     (let [span (.startChild transaction (clj->js {:op          op-name
                                                   :description op-description}))]
       (swap! span-stack conj span)
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
   (span-finish (span-active)))
  ([span]
   (let [active-span (span-active)]
     (when (= active-span span)
       (swap! span-stack pop))
     (.finish span))))

