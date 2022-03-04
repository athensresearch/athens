(ns athens.common.sentry
  "Macros for Sentry monitoring"
  (:require
    #?(:cljs [athens.utils.sentry :as sentry])))


(defmacro wrap-span
  [span-name body]
  `(let [tx-running?#  (athens.utils.sentry/tx-running?)
         sentry-tx#    (if tx-running?#
                         (athens.utils.sentry/transaction-get-current)
                         (athens.utils.sentry/transaction-start (str ~span-name "-wrap-span-auto-tx")))
         active-span#  (athens.utils.sentry/span-active)
         current-span# (when sentry-tx#
                         (athens.utils.sentry/span-start (or active-span#
                                                             sentry-tx#)
                                                         (str ~span-name "-wrap-span")
                                                         false))
         result#       ~body]
     (when current-span#
       (athens.utils.sentry/span-finish current-span# false)
       (when-not tx-running?#
         (athens.utils.sentry/transaction-finish sentry-tx#)))
     result#))

