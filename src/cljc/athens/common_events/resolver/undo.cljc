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
  (let [{:block/keys [uid]} args
        {:block/keys [order _refs]
         :db/keys    [id]}  (common-db/get-block evt-db [:block/uid uid])
        parent-uid          (->> id (common-db/get-parent evt-db) :block/uid)
        position            (common-db/compat-position evt-db {:block/uid parent-uid
                                                               :relation  order})
        repr                [(common-db/get-internal-representation evt-db [:block/uid uid])]
        repr-ops            (bfs/internal-representation->atomic-ops evt-db repr position)
        save-ops            (->> _refs
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

;; only need neighbors from the thing that got removed
;; if >= 3 sidebar-elements, get a subvec of 3
;; if == 2 sidebar-elements, get a subvec of 2
;; if == 1 sidebar-elements, don't need subvec
;; if == 0 sidebar-elements, don't need subvec

(defn get-sidebar-neighbors
  [db title]
  (let [sidebar-elem   (common-db/get-sidebar-elements db)
        sidebar-titles (mapv :node/title sidebar-elem)
        idx            (.indexOf sidebar-titles title)
        neighbors      (case (count sidebar-titles)
                         1 []
                         2 sidebar-titles
                         3 (subvec sidebar-titles (dec idx) (+ 2 idx)))
        _ (prn neighbors)]
    neighbors))

(subvec [0 1] 0 3)
(get [0 1] 3)

(defn get-relative-position
  "if 1 element, no move.
  if 2 elements, move relative to other
  if 3 elements, your choice of before or after"
  [title neighbors]
  (let [cnt (count neighbors)
        idx (.indexOf neighbors title)]
    ;;(prn cnt idx neighbors)
    (case cnt
      1 nil
      2 (if (zero? idx)
          {:page/title (get neighbors (inc idx))
           :relation   :before}
          {:page/title (get neighbors (dec idx))
           :relation   :after})
      3 {:page/title (get neighbors (inc idx))
         :relation   :before})))

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
  [_db evt-db {:op/keys [args]}]
  (let [{prev-source-title :page/title} args
        ;; if using :before, find the element that was right :before prev-source-title
        make-op   (atomic-graph-ops/make-shortcut-new-op prev-source-title)
        neighbors (get-sidebar-neighbors evt-db prev-source-title)
        position  (get-relative-position prev-source-title neighbors)
        move-op   (atomic-graph-ops/make-shortcut-move-op prev-source-title position)]
    ;; if the last index was removed: new
    ;; if any other index was removed: new + move
    (cond-> [make-op]
      position (conj move-op))))



(defmethod resolve-atomic-op-to-undo-ops :shortcut/move
  [db evt-db {:op/keys [args]}]
  (let [{prev-source-title :page/title position :shortcut/position} args
        {_prev-target-title :page/title prev-relation :relation} position
        prev-source-order (common-db/find-order-from-title evt-db prev-source-title)
        new-target-title  (common-db/find-title-from-order db prev-source-order)
        flip-relation     (if (= prev-relation :before)
                            :after
                            :before)
        move-op           (atomic-graph-ops/make-shortcut-move-op prev-source-title {:page/title new-target-title
                                                                                     :relation   flip-relation})]
    (cond-> []
            new-target-title (conj move-op))))

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

