(ns athens.common-events.resolver.undo
  (:require
    [athens.common-db               :as common-db]
    [athens.common-events.graph.ops :as graph-ops]))


(defmulti resolve-atomic-op-to-undo-op
  #(:op/type %3))


(defmethod resolve-atomic-op-to-undo-op :block/save
  [db op-db {:op/keys [args]}]
  (let [{:block/keys [uid]}    args
        {:block/keys [string]} (common-db/get-block op-db [:block/uid uid])]
    (graph-ops/build-block-save-op db uid string)))
