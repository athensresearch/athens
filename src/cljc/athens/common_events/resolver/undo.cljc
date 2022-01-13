(ns athens.common-events.resolver.undo
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common.logging                :as log]))


(defn undo?
  [event]
  (-> event :event/op :op/trigger :op/undo))


;; Impl according to https://github.com/athensresearch/athens/blob/main/doc/adr/0021-undo-redo.md#approach
(defmulti resolve-atomic-op-to-undo-ops
  #(:op/type %3))


(defmethod resolve-atomic-op-to-undo-ops :block/save
  [db evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]}    args
        {:block/keys [string]} (common-db/get-block evt-db [:block/uid uid])]
    [(graph-ops/build-block-save-op db uid string)]))


(defmethod resolve-atomic-op-to-undo-ops :composite/consequence
  [db evt-db {:op/keys [consequences] :as op}]
  (assoc op :op/consequences (map (partial resolve-atomic-op-to-undo-ops db evt-db)
                                  consequences)))


;; TODO: should there be a distinction between undo and redo?
(defn build-undo-event
  [db evt-db {:event/keys [id type op] :as event}]
  (log/debug "build-undo-event" event)
  (if-not (contains? #{:op/atomic} type)
    (throw (ex-info "Cannot undo non-atomic event" event))
    (->> op
         (resolve-atomic-op-to-undo-ops db evt-db)
         (composite/make-consequence-op {:op/undo id})
         common-events/build-atomic-event)))

