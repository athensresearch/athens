(ns athens.common.sentry
  "Macros for Sentry monitoring"
  #?(:cljs
     (:require [athens.utils.sentry :as sentry])))

(defn span*
  [span-name f]
  #?(:cljs
     (let [sentry-tx    (sentry/transaction-get-current)
           active-span  (sentry/span-active)
           current-span (when sentry-tx
                          (sentry/span-start (or active-span
                                                 sentry-tx)
                                             span-name))
           result       (f)]
       (when current-span
         (sentry/span-finish current-span))
       result))
  #?(:clj (f)))


(defmacro wrap-span
  [span-name body]
  `(let [sentry-tx#    (athens.utils.sentry/transaction-get-current)
         active-span#  (athens.utils.sentry/span-active)
         current-span# (when sentry-tx#
                         (athens.utils.sentry/span-start (or active-span#
                                                             sentry-tx#)
                                                         ~span-name))
         result#       ~body]
     (when current-span#
       (athens.utils.sentry/span-finish current-span#))
     result#))

