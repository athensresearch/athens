(ns athens.common-events.resolver.undo
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common.logging                :as log]
    [clojure.pprint                       :as pp]
    [datascript.core :as d]))


(defn undo?
  [event]
  (-> event :event/op :op/trigger :op/undo))


(defn- restore-shortcut
  [evt-db title]
  (let [new-op            (atomic-graph-ops/make-shortcut-new-op title)
        neighbors         (common-db/get-shortcut-neighbors evt-db title)
        neighbor-position (common-db/flip-neighbor-position neighbors)
        move-op           (cond neighbors
                                (atomic-graph-ops/make-shortcut-move-op title neighbor-position))]
    (cond-> [new-op]
      neighbor-position (conj move-op))))


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


(defmethod resolve-atomic-op-to-undo-ops :block/move
  [_ evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]}       args
        position                  (common-db/get-position evt-db uid)]
    [(atomic-graph-ops/make-block-move-op uid position)]))


(defmethod resolve-atomic-op-to-undo-ops :block/open
  [_db evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]}    args
        {:block/keys [open]} (common-db/get-block evt-db [:block/uid uid])]
    [(atomic-graph-ops/make-block-open-op uid open)]))


(defmethod resolve-atomic-op-to-undo-ops :block/new
  [_db _evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]} args]
    [(atomic-graph-ops/make-block-remove-op uid)]))


(defmethod resolve-atomic-op-to-undo-ops :page/remove
  [_db evt-db {:op/keys [args]}]
  (let [{:page/keys [title]} args
        {sidebar   :page/sidebar
         page-refs :block/_refs} (common-db/get-page-document evt-db [:node/title title])
        page-repr                 [(common-db/get-internal-representation evt-db (:db/id (d/entity evt-db [:node/title title])))]
        repr-ops                  (bfs/internal-representation->atomic-ops evt-db page-repr nil)
        save-ops                  (->> page-refs
                                       (map :db/id)
                                       (map (partial common-db/get-block evt-db))
                                       (map (fn [{:block/keys [uid string]}]
                                              (atomic-graph-ops/make-block-save-op uid string))))
        shortcut-ops            (when sidebar
                                  (restore-shortcut evt-db (:page/title args)))]
    (vec (concat repr-ops save-ops shortcut-ops))))


(defmethod resolve-atomic-op-to-undo-ops :page/rename
  [db _event-db {:op/keys [args]}]
  (let [from-title (:page/title args)
        to-title   (get-in args [:target :page/title])
        reverse-op (graph-ops/build-page-rename-op db to-title from-title)]
    [reverse-op]))


(defmethod resolve-atomic-op-to-undo-ops :page/merge
  [_db evt-db {:op/keys [args]}]
  (let [{from :page/title}      args
        {children :block/children
         sidebar  :page/sidebar
         backrefs :block/_refs} (common-db/get-page evt-db [:node/title from])
        page-new                (atomic-graph-ops/make-page-new-op from)
        save-ops                (->> backrefs
                                     (map :db/id)
                                     (map (partial common-db/get-block evt-db))
                                     (map (fn [{:block/keys [uid string]}]
                                            (atomic-graph-ops/make-block-save-op uid string))))
        move-ops                (->> children
                                     (sort-by :block/order)
                                     (map :block/uid)
                                     (map #(atomic-graph-ops/make-block-move-op % {:page/title from
                                                                                   :relation   :last})))
        shortcut-ops            (when sidebar
                                  (restore-shortcut evt-db (:page/title args)))]
    (vec (concat [page-new] move-ops save-ops shortcut-ops))))


(defmethod resolve-atomic-op-to-undo-ops :page/new
  [_db _evt-db {:op/keys [args]}]
  (let [{:page/keys [title]} args]
    [(atomic-graph-ops/make-page-remove-op title)]))


(defmethod resolve-atomic-op-to-undo-ops :shortcut/new
  [_db _evt-db {:op/keys [args]}]
  (let [{:page/keys [title]} args]
    [(atomic-graph-ops/make-shortcut-remove-op title)]))


(defmethod resolve-atomic-op-to-undo-ops :shortcut/remove
  [_db evt-db {:op/keys [args]}]
  (restore-shortcut evt-db (:page/title args)))


(defmethod resolve-atomic-op-to-undo-ops :shortcut/move
  [_db evt-db {:op/keys [args]}]
  (let [{moved-title :page/title} args
        neighbors                 (common-db/get-shortcut-neighbors evt-db moved-title)
        neighbor-position         (common-db/flip-neighbor-position neighbors)
        move-op                   (atomic-graph-ops/make-shortcut-move-op moved-title neighbor-position)]
    [move-op]))


(defn reorder-ops
  "Reverse the order of operations in coll.
  Then, for all contiguous operations involving positions, restore their original relative order.
    e.g.: a b m1 m2 m3 c -> c m1 m2 m3 b a
  Position operations keep their relative order to ensure that chains of relative moves still work.
  This is in part a quirk of the `forward bias` in our location resolution that favors :first
  and :after positions, and is not meant to be a universal solution.
  There are valid combination of relative moves that will still not be correctly undone."
  [coll]
  (let [position-op?       #(-> % :op/type #{:block/move :block/new
                                             ;; Neither :block/save or :block/remove use positions,
                                             ;; but :block/remove is undo to :block/new followed by
                                             ;; :block/save, and thus these two types end up being
                                             ;; part of sequential move operations.
                                             :block/save :block/remove})
        restore-move-order #(if (position-op? (first %))
                              (reverse %)
                              %)]
    (->> coll
         reverse
         (partition-by position-op?)
         (map restore-move-order)
         (apply concat)
         (into []))))


(defmethod resolve-atomic-op-to-undo-ops :composite/consequence
  [db evt-db {:op/keys [_consequences] :as op}]
  (let [atomic-ops (graph-ops/extract-atomics op)
        undo-ops   (->> atomic-ops
                        reorder-ops
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

