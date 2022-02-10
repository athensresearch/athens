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


;; We redefine span-start/finish here to add reader conditionals to them.
;; We can't add reader conditionals in macros, they just get interpreted before the macro quotes.
#_{:clj-kondo/ignore #?(:clj [:unused-binding] :cljs [])}


(defn span-start
  [name]
  #?(:cljs (sentry/span-start name)))


(defn span-finish
  []
  #?(:cljs (sentry/span-finish)))


(defmacro deftrace-old
  "Define a function that is traced by Sentry using a span-start and span-finish as a prepost block.
  Accepts the same arguments as defn. You can optionally include a string as the first argument to
  use that as the span name, otherwise the function name is used."
  [& args]
  (let [first-arg     (first args)
        provided-name (when (string? first-arg) first-arg)
        args'         (if provided-name (rest args) args)
        xform (fn [conf name]
                (let [name-str       (str (or provided-name name) "-deftrace")
                      prepost-form   `{:pre [(or (span-start ~name-str) true)]
                                       :post [(or (span-finish) true)]}
                      body-update-fn (partial macros/add-prepost prepost-form)]
                  (macros/update-bodies conf body-update-fn)))]
    (cons `defn (macros/defn-args-xform xform args'))))


(defn wrap-span-xform
  [span-name body]
  `((let [tx-running?#  (athens.utils.sentry/tx-running?)
          sentry-tx#    (if tx-running?#
                          (athens.utils.sentry/transaction-get-current)
                          (athens.utils.sentry/transaction-start (str ~span-name "-wrap-span-auto-tx")))
          active-span#  (athens.utils.sentry/span-active)
          current-span# (when sentry-tx#
                          (athens.utils.sentry/span-start (or active-span#
                                                              sentry-tx#)
                                                          (str ~span-name "-wrap-span")
                                                          false))
          result#       (do ~@body)]
      (when current-span#
        (athens.utils.sentry/span-finish current-span# false)
        (when-not tx-running?#
          (athens.utils.sentry/transaction-finish sentry-tx#)))
      result#)))


(defmacro deftrace
  "Define a function that is traced by Sentry using a span-start and span-finish as a prepost block.
  Accepts the same arguments as defn. You can optionally include a string as the first argument to
  use that as the span name, otherwise the function name is used."
  [& args]
  (let [first-arg     (first args)
        provided-name (when (string? first-arg) first-arg)
        args'         (if provided-name (rest args) args)
        xform         (fn [conf name]
                        (->> (partial wrap-span-xform name)
                             (partial macros/update-body-body)
                             (macros/update-bodies conf)))]
    (cons `defn (macros/defn-args-xform xform args'))))
