(ns athens.utils.sentry
  "Sentry integration utilities."
  (:require
    ["@sentry/react" :as Sentry]))


(defn start-transaction
  "Starts new Sentry Transaction"
  [tx-name]
  (.startTransaction Sentry (clj->js {:name tx-name})))


(defn get-current-transaction
  "Tries to find existing Sentry Transaction"
  []
  (let [hub         (.getCurrentHub Sentry)
        scope       (.getScope hub)
        transaction (.getTransaction scope)]
    transaction))


(defn finish-transaction
  "Finishes provided transaction"
  [transaction]
  (.finish transaction))

;; TODO add span support
