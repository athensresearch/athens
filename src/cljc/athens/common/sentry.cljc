(ns athens.common.sentry
  "Macros for Sentry monitoring"
  (:require
    [athens.macros :as macros]
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


(defmacro wrap-span-no-new-tx
  [span-name body]
  `(let [tx-running?#  (athens.utils.sentry/tx-running?)
         sentry-tx#    (if tx-running?#
                         (athens.utils.sentry/transaction-get-current))
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


(defn span-start
  [_span-name]
  #?(:clj  nil
     :cljs (let [tx-running?  (sentry/tx-running?)
                 sentry-tx    (when tx-running?
                                (sentry/transaction-get-current))
                 active-span  (sentry/span-active)
                 current-span (when sentry-tx
                                (sentry/span-start (or active-span
                                                       sentry-tx)
                                                   (str _span-name "-defntrace-span")
                                                   false))]
             [tx-running? sentry-tx current-span])))


(defn span-finish
  [[_tx-running? _sentry-tx _current-span]]
  #?(:clj  nil
     :cljs (when _current-span
             (sentry/span-finish _current-span false)
             (when-not _tx-running?
               (sentry/transaction-finish _sentry-tx)))))


(defn wrap-span-xform
  [span-name body]
  `((let [span-ret# (span-start ~span-name)
          result#   (do ~@body)]
      (span-finish span-ret#)
      result#)))


(defmacro defntrace
  "Define a function that is traced by Sentry using a span-start and span-finish as a prepost block.
  Accepts the same arguments as defn. You can optionally include a string as the first argument to
  use that as the span name, otherwise the function name is used.

  Given the following function:

    (defn foo [x]
      (bar) (baz))

  Using defntraced instead of defn will output the following function definition:

    (defn foo []
      (let [span-ret (span-start ~span-name)
          result     (do (bar) (baz))]
      (span-finish span-ret)
      result))
  "
  [& args]
  (let [first-arg     (first args)
        provided-name (when (string? first-arg) first-arg)
        args'         (if provided-name (rest args) args)
        xform         (fn [conf name]
                        (->> (partial wrap-span-xform (str name))
                             (partial macros/update-body-body)
                             (macros/update-bodies conf)))]
    (cons `defn (macros/defn-args-xform xform args'))))
