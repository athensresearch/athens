(ns athens.interceptors
  (:require
    [athens.common.logging :as log]
    [athens.util           :as util]
    [athens.utils.sentry   :as sentry]
    [re-frame.core         :as rf]))


(def persist-db
  "Saves the :athens/persist key in db to persistent storage.
  This special key is used for two main reasons:
  - performance, by using identical instead of map comparison
  - clarity, to make it obvious on access that it will be persisted"
  (rf/->interceptor
    :id    :persist
    :after (fn [{:keys [coeffects effects] :as context}]
             (let [k      :athens/persist
                   before (-> coeffects :db k)
                   after  (-> effects :db k)]
               (when (and after (not (identical? before after)))
                 (util/local-storage-set! k after)))
             context)))


(defn sentry-tx
  "Wraps Event Handler with Sentry Transaction for measurement."
  ([tx-name] (sentry-tx tx-name false))
  ([tx-name auto-close?]
   (rf/->interceptor
     :id     :sentry-tx
     :before (fn [context]
               (let [sentry-tx (sentry/transaction-start tx-name)]
                 (assoc context :sentry-tx sentry-tx)))
     :after  (fn [context]
               (if auto-close?
                 (let [sentry-tx (:sentry-tx context)]
                   (sentry/transaction-finish sentry-tx)
                   (dissoc context :sentry-tx))
                 context)))))


(defn sentry-span
  "Wraps Event Handler into Sentry Span for measurement."
  [span-name]
  (rf/->interceptor
    :id     :sentry-span
    :before (fn [context]
              (let [sentry-tx     (sentry/transaction-get-current)
                    auto-tx       (when-not sentry-tx
                                    (sentry/transaction-start (str span-name "-auto-tx")))
                    existing-span (sentry/span-active)
                    sentry-span   (sentry/span-start (or existing-span
                                                         sentry-tx
                                                         auto-tx)
                                                     span-name)]
                (when-not sentry-tx
                  (log/warn "Auto generated local Sentry TX for span:" (pr-str span-name)))
                (cond-> context
                  true    (assoc :sentry-span sentry-span)
                  auto-tx (assoc :sentry-auto-tx auto-tx))))
    :after  (fn [context]
              (let [sentry-span (:sentry-span context)
                    auto-tx     (:sentry-auto-tx context)]
                (when sentry-span
                  (sentry/span-finish sentry-span))
                (when auto-tx
                  (sentry/transaction-finish auto-tx))
                (dissoc context :sentry-span)))))


(rf/reg-global-interceptor persist-db)
