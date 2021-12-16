(ns athens.common-events.resolver)


;; TODO start using this resolution in handlers
(defmulti resolve-event-to-tx
  "Resolves `:datascript/*` event in context of existing DB into transactions."
  #(:event/type %2))


;; resetting ds-conn re-computes all subs and is the cause
;;    for huge delays when undo/redo is pressed
;; here is another alternative strategy for undo/redo
;; Core ideas are inspired from here https://tonsky.me/blog/datascript-internals/
;;    1. db-before + tx-data = db-after
;;    2. DataScript DB contains only currently relevant datoms.
;; 1 is the math that is happening here when you undo/redo but in a much more performant way
;; 2 asserts that even if you are reapplying same txn over and over only relevant ones will be present
;;    - and overall size of transit file does not increase
;; Note: only relevant txns(ones that user deliberately made, not undo/redo ones) go into history
;; Note: Once session is lost(App is closed) edit history is also lost
;; Also cmd + z -> cmd + z -> edit something --- future(cmd + shift + z) doesn't work
;;    - as there is no logical way to assert the future when past has changed hence history is reset
;;    - very similar to intelli-j edit model or any editor's undo/redo mechanism for that matter
(defmethod resolve-event-to-tx :datascript/undo-redo
  [db-history {:event/keys [args]}]
  (let [{:keys [redo?]}  args
        [tx-m-id datoms] (cond->> @db-history
                           redo? reverse
                           true (some (fn [[tx bool datoms]]
                                        (and ((if redo? not (complement not)) bool) [tx datoms]))))]

    (reset! db-history (->> @db-history
                            (map (fn [[tx-id bool datoms]]
                                   (if (= tx-id tx-m-id)
                                     [tx-id redo? datoms]
                                     [tx-id bool datoms])))
                            doall))

    (cond->> datoms
      (not redo?) reverse
      true (map (fn [datom]
                  (let [[id attr val _txn sig?] #?(:clj datom
                                                   :cljs (vec datom))]
                    [(cond
                       (and sig? (not redo?)) :db/retract
                       (and (not sig?) (not redo?)) :db/add
                       (and sig? redo?) :db/add
                       (and (not sig?) redo?) :db/retract)
                     id attr val])))
      ;; Caveat -- we need a way to signal history watcher if this txn is relevant
      ;;     - send a dummy datom, this will get added to user's data
      ;;     - we can easily filter it out while writing to fs but it will have a perf penalty
      ;;     - Unless we are exporting transit to a common format, this can stay(only one datom -- point 2 mentioned above)
      ;;           - although a filter while exporting is more strategic -- once in a while op, compared to fs write(very frequent)
      true (concat [[:db/add "new" :from-undo-redo true]]))))

