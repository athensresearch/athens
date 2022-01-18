(ns athens.common-events.resolver.undo
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.composite :as composite]
    [athens.common.logging                :as log]))


(defn undo?
  [event]
  (-> event :event/op :op/trigger :op/undo))


;; Impl according to https://github.com/athensresearch/athens/blob/main/doc/adr/0021-undo-redo.md#approach
(defmulti resolve-atomic-op-to-undo-ops
  #(:op/type %3))


(defmethod resolve-atomic-op-to-undo-ops :block/save
  [_db evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]}    args
        {:block/keys [string]} (common-db/get-block evt-db [:block/uid uid])]
    [(atomic-graph-ops/make-block-save-op uid string)]))


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


(defmethod resolve-atomic-op-to-undo-ops :composite/consequence
  [db evt-db {:op/keys [consequences] :as op}]
  [(assoc op :op/consequences (mapcat (partial resolve-atomic-op-to-undo-ops db evt-db)
                                      consequences))])





(defn flip-neighbor-position
  "Flips neighbor position to undo a remove.

  --Setup--
  Page 1 <- remove shortcut
  Page 2 <- :after

  --After Remove--
  Page 2 <-

  --Undo--
  Page 1 <- restore shortcut (new)
  Page 2 <- :before"
  [{:keys [before after] :as _neighbors}]
  (cond
    before {:relation :after
            :page/title before}
    after {:relation :before
           :page/title after}))

(defmethod resolve-atomic-op-to-undo-ops :shortcut/new
  [_db _evt-db {:op/keys [args]}]
  (let [{:page/keys [title]} args]
    [(atomic-graph-ops/make-shortcut-remove-op title)]))


;; 1 <- no move

;; 1 <- :before 2
;; 2

;; 2
;; 3 <- :after 3

;; 1
;; 2 <- :before 3 or :after 1
;; 3

(defmethod resolve-atomic-op-to-undo-ops :shortcut/remove
  [_db evt-db {:op/keys [args] :as op}]
  (let [{removed-title :page/title} args
        new-op            (atomic-graph-ops/make-shortcut-new-op removed-title)
        neighbors         (common-db/get-shortcut-neighbors evt-db removed-title)
        neighbor-position (flip-neighbor-position neighbors)
        move-op           (cond neighbors
                                (atomic-graph-ops/make-shortcut-move-op removed-title neighbor-position))]
    ;; if the last index was removed: new
    ;; if any other index was removed: new + move
    (prn "OG OP")
    (cljs.pprint/pprint op)
    (prn "REMOVE" neighbor-position)
    (cljs.pprint/pprint (cond-> [new-op]
                                neighbor-position (conj move-op)))
    (cond-> [new-op]
            neighbor-position (conj move-op))))


(defmethod resolve-atomic-op-to-undo-ops :shortcut/move
  [_db evt-db {:op/keys [args] :as op}]
  (let [{moved-title :page/title position :shortcut/position} args
        {_prev-target-title :page/title prev-relation :relation} position
        neighbors         (common-db/get-shortcut-neighbors evt-db moved-title)
        neighbor-position (flip-neighbor-position neighbors)
        move-op           (atomic-graph-ops/make-shortcut-move-op moved-title neighbor-position)]
    [move-op]))


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

