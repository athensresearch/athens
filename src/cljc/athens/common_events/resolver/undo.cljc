(ns athens.common-events.resolver.undo
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
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
    [(graph-ops/build-block-save-op db uid string)]))


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
  [db evt-db {:op/keys [consequences] :as _op}]
  (let [undo-composite     (mapcat (partial resolve-atomic-op-to-undo-ops db evt-db)
                                   consequences)
        removed-block-uids (->> undo-composite
                                (filter #(= :block/remove (:op/type %)))
                                (map #(get-in % [:op/args :block/uid]))
                                (into #{}))
        undo-consequences  (->> undo-composite
                                (remove #(and (not= :block/remove (:op/type %))
                                              (removed-block-uids (get-in % [:op/args :block/uid]))))
                                (into []))]
    undo-consequences))


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

