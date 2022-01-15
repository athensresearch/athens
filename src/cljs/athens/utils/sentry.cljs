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


(def span-stack (atom []))


(defn span-active
  "Provides active Sentry Span if any exists."
  []
  (peek @span-stack))


(defn span-start
  "Starts a *span* named `op-name` within given `transaction`,
  with optional `op-description`."
  ([transaction op-name]
   (let [span (.startChild transaction (clj->js {:op op-name}))]
     (swap! span-stack conj span)
     span))
  ([transaction op-name op-description]
   (let [span (.startChild transaction (clj->js {:op          op-name
                                                 :description op-description}))]
     (swap! span-stack conj span)
     span)))


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
   (span-finish (peek @span-stack)))
  ([span]
   (let [active-span (peek @span-stack)]
     (when (= active-span span)
       (swap! active-span pop))
     (.finish span))))

