(ns athens.utils.sentry
  "Sentry integration utilities."
  (:require
    ["@sentry/react" :as Sentry]
    ["@sentry/react"]))


(defn transaction-start
  "Starts new Sentry Transaction"
  [tx-name]
  (.startTransaction Sentry (clj->js {:name tx-name})))


(defn transaction-get-current
  "Tries to find existing Sentry Transaction"
  []
  (let [hub         (.getCurrentHub Sentry)
        scope       (.getScope hub)
        transaction (.getTransaction scope)]
    transaction))


(defn transaction-finish
  "Finishes provided transaction"
  [transaction]
  (.finish transaction))


(defn span-start
  "Starts a *span* named `op-name` within given `transaction`,
  with optional `op-description`."
  ([transaction op-name]
   (.startChild transaction (clj->js {:op op-name})))
  ([transaction op-name op-description]
   (.startChild transaction (clj->js {:op          op-name
                                      :description op-description}))))


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
  [span]
  (.finish span))

