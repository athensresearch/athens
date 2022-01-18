(ns athens.common-events.resolver.undo
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common.logging                :as log]
    [clojure.pprint                       :as pp]))


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
    ;; if block wasn't present in `event-db`
    (if string
      [(graph-ops/build-block-save-op db uid string)]
      [])))


(defmethod resolve-atomic-op-to-undo-ops :block/remove
  [_db evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]}     args
        {backrefs :block/_refs} (common-db/get-block evt-db [:block/uid uid])
        position                (common-db/get-position evt-db uid)
        repr                    [(common-db/get-internal-representation evt-db [:block/uid uid])]
        repr-ops                (bfs/internal-representation->atomic-ops evt-db repr position)
        save-ops                (->> backrefs
                                     (map :db/id)
                                     (map (partial common-db/get-block evt-db))
                                     (map (fn [{:block/keys [uid string]}]
                                            (atomic-graph-ops/make-block-save-op uid string))))]
    (vec (concat repr-ops save-ops))))


(defmethod resolve-atomic-op-to-undo-ops :block/open
  [_db evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]}    args
        {:block/keys [open]} (common-db/get-block evt-db [:block/uid uid])]
    [(atomic-graph-ops/make-block-open-op uid open)]))


(defmethod resolve-atomic-op-to-undo-ops :block/new
  [_db _evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]} args]
    [(atomic-graph-ops/make-block-remove-op uid)]))


(defmethod resolve-atomic-op-to-undo-ops :composite/consequence
  [db evt-db {:op/keys [_consequences] :as op}]
  (let [atomic-ops (graph-ops/extract-atomics op)
        undo-ops   (->> atomic-ops
                        reverse
                        (mapcat (partial resolve-atomic-op-to-undo-ops db evt-db))
                        (into []))]
    undo-ops))


;; TODO: should there be a distinction between undo and redo?
(defn build-undo-event
  [db evt-db {:event/keys [id type op] :as event}]
  (log/debug "build-undo-event\n"
             (with-out-str
               (pp/pprint event)))
  (if-not (contains? #{:op/atomic} type)
    (throw (ex-info "Cannot undo non-atomic event" event))
    (let [undo-ops (->> op
                        (resolve-atomic-op-to-undo-ops db evt-db)
                        (composite/make-consequence-op {:op/undo id})
                        common-events/build-atomic-event)]
      (log/debug "undo-ops:\n"
                 (with-out-str
                   (pp/pprint undo-ops)))
      undo-ops)))

