(ns athens.utils.sentry
  "Sentry integration utilities."
  (:require
    ["@sentry/react" :as Sentry]
    [athens.common.logging :as log]))


(def tx-active (atom nil))


(defn transaction-start
  "Starts new Sentry Transaction"
  [tx-name]
  (let [tx (.startTransaction Sentry (clj->js {:name tx-name}))]
    (log/debug "Sentry: Starting TX:" tx-name)
    (reset! tx-active tx)
    tx))


(defn tx-running?
  "Checks if there is TX running"
  []
  (not (nil? @tx-active)))


(defn transaction-get-current
  "Tries to find existing Sentry Transaction"
  []
  (let [tx @tx-active]
    (if tx
      tx
      (try
        (throw (js/Error. "transaction-get-current called but no TX running"))
        (catch js/Error ex
          (log/warn ex))))))


(defn transaction-get-current-name
  "Tries to find existing Sentry Transaction name"
  []
  (aget @tx-active "name"))


(defn transaction-finish
  "Finishes provided transaction"
  ([]
   (when-let [tx @tx-active]
     (transaction-finish tx)))
  ([tx]
   (when tx
     (log/debug "Sentry: Finishing TX:" (aget tx "name"))
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
       (when stack?
         (swap! span-stack conj span))
       span))))


(defn span-finish
  "Finish provided `span`."
  ([]
   (if-let [active (span-active)]
     (span-finish active)
     (try
       (throw (js/Error. "Can't finish Sentry Span, there is no active span."))
       (catch js/Error ex
         (log/warn ex)))))
  ([span]
   (span-finish span true))
  ([span stack?]
   {:pre [(not (nil? span))]}
   (let [active-span (span-active)]
     (when (and stack?
                active-span
                (= active-span span))
       (swap! span-stack pop))
     (.finish span))))
