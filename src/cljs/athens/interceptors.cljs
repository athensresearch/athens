(ns athens.interceptors
  (:require
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


(defn sentry-span
  "Wraps Event Handler into Sentry Span for measurement."
  [span-name]
  (rf/->interceptor
    :id     :sentry-span
    :before (fn [context]
              (let [tx-running?   (sentry/tx-running?)
                    auto-tx       (if tx-running?
                                    (sentry/transaction-get-current)
                                    (sentry/transaction-start (str span-name "-auto-tx")))
                    existing-span (sentry/span-active)
                    sentry-span   (sentry/span-start (or existing-span
                                                         auto-tx)
                                                     span-name)]
                (cond-> (assoc context :sentry-span sentry-span)
                  (not tx-running?) (assoc :sentry-auto-tx auto-tx))))
    :after  (fn [context]
              (let [sentry-span (:sentry-span context)
                    auto-tx     (:sentry-auto-tx context)]
                (when sentry-span
                  (sentry/span-finish sentry-span))
                (when auto-tx
                  (sentry/transaction-finish auto-tx))
                (cond-> (dissoc context :sentry-span)
                  auto-tx (dissoc :sentry-auto-tx))))))


(defn sentry-span-no-new-tx
  "Wraps Event Handler into Sentry Span for measurement when there is no running tx."
  [span-name]
  (rf/->interceptor
    :id     :sentry-span
    :before (fn [context]
              (let [tx-running?   (sentry/tx-running?)
                    auto-tx       (when tx-running?
                                    (sentry/transaction-get-current))
                    existing-span (sentry/span-active)
                    sentry-span   (sentry/span-start (or existing-span
                                                         auto-tx)
                                                     span-name)]
                (cond-> (assoc context :sentry-span sentry-span)
                  (not tx-running?) (assoc :sentry-auto-tx auto-tx))))
    :after  (fn [context]
              (let [sentry-span (:sentry-span context)
                    auto-tx     (:sentry-auto-tx context)]
                (when sentry-span
                  (sentry/span-finish sentry-span))
                (when auto-tx
                  (sentry/transaction-finish auto-tx))
                (cond-> (dissoc context :sentry-span)
                  auto-tx (dissoc :sentry-auto-tx))))))


(rf/reg-global-interceptor persist-db)
