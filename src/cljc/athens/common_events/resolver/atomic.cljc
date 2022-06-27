(ns athens.common-events.resolver.atomic
  (:require
    [athens.common-db                       :as common-db]
    [athens.common-events.graph.ops         :as graph-ops]
    [athens.common-events.resolver.order    :as order]
    [athens.common-events.resolver.position :as position]
    [athens.common.logging                  :as log]
    [athens.common.utils                    :as utils]
    [athens.dates                           :as dates]
    [clojure.pprint                         :as pp]
    [datascript.core                        :as d]))


(defmulti resolve-atomic-op-to-tx
  "Resolves ⚛️ Atomic Graph Ops to TXs."
  #(:op/type %2))


(defmethod resolve-atomic-op-to-tx :block/new
  [db {:op/keys [args]}]
  (let [{:block/keys [uid position]} args
        now-ref                      {:time/ts (utils/now-ts)}
        new-block                    {:block/uid    uid
                                      :block/string ""
                                      :block/open   true
                                      :time/create  now-ref
                                      :time/edit    now-ref}
        position-tx                  (condp = (position/position-type position)
                                       :child    (position/add-child db uid position now-ref)
                                       :property (position/add-property db uid position now-ref))
        tx-data                      (into [new-block] position-tx)]
    tx-data))


;; This is Atomic Graph Op, there is also composite version of it
(defmethod resolve-atomic-op-to-tx :block/save
  [_db {:op/keys [args]}]
  (let [{:block/keys [uid string]} args
        now-ref {:time/ts (utils/now-ts)}]
    [{:block/uid    uid
      :block/string string
      :time/edit    now-ref}]))


(defmethod resolve-atomic-op-to-tx :block/open
  [db {:op/keys [args]}]
  (log/debug "atomic-resolver :block/open args:" (pr-str args))
  (let [{:block/keys [uid open?]} args
        block-eid                 (common-db/e-by-av db :block/uid uid)
        current-open?             (when (int? block-eid)
                                    (common-db/v-by-ea db block-eid :block/open))]
    (if (= current-open? open?)
      (do
        (log/info ":block/open already at desired state, :block/open" open?)
        [])
      (let [now-ref       {:time/ts (utils/now-ts)}
            updated-block {:block/uid  uid
                           :block/open open?
                           :time/edit  now-ref}]
        [updated-block]))))


(defmethod resolve-atomic-op-to-tx :block/move
  [db {:op/keys [args]}]
  (log/debug "atomic-resolver :block/move args:" (pr-str args))
  (let [{:block/keys [uid position]} args
        _valid-block-uid             (when (common-db/get-page-title db uid)
                                       (throw (ex-info "Block to be moved is a page, cannot move pages." args)))
        [_ new-parent-uid]           (common-db/position->uid+parent db position)
        {old-parent-uid :block/uid}  (common-db/get-parent db [:block/uid uid])
        same-parent?                 (= new-parent-uid old-parent-uid)
        old-position-type            (-> (common-db/get-position db uid)
                                         position/position-type)
        new-position-type            (position/position-type position)
        now-ref                      {:time/ts (utils/now-ts)}
        updated-block'               {:block/uid uid
                                      :time/edit now-ref}
        position-tx                  (condp = [old-position-type new-position-type]
                                       [:child :child]
                                       (if same-parent?
                                         (position/move-child-within
                                           db old-parent-uid uid position now-ref)
                                         (position/move-child-between
                                           db old-parent-uid new-parent-uid uid position now-ref))

                                       [:child :property]
                                       (concat
                                         (position/remove-child db uid old-parent-uid now-ref)
                                         (position/add-property db uid position now-ref))

                                       [:property :child]
                                       (concat
                                         (position/remove-property db uid old-parent-uid now-ref)
                                         (position/add-child db uid position now-ref))

                                       [:property :property]
                                       ;; No need to remove previous name, schema ensures
                                       ;; a block has a single name.
                                       (position/add-property db uid position now-ref))]
    (into [updated-block'] position-tx)))


(defmethod resolve-atomic-op-to-tx :block/remove
  [db {:op/keys [args]}]
  (let [{:block/keys [uid]}   args
        now-ref               {:time/ts (utils/now-ts)}
        block-exists?         (common-db/e-by-av db :block/uid uid)
        block                 (when block-exists?
                                (common-db/get-block db [:block/uid uid]))
        parent-eid            (when block-exists?
                                (common-db/get-parent-eid db [:block/uid uid]))
        parent-uid            (when parent-eid
                                (common-db/v-by-ea db parent-eid :block/uid))
        ;; Reorder parent children if needed.
        children-tx           (position/remove-child db uid parent-uid now-ref)
        retract-parents-child (when parent-uid
                                [:db/retract [:block/uid parent-uid] :block/children [:block/uid uid]])
        retract-uid           (when block-exists?
                                (common-db/retract-uid-recursively-tx db uid now-ref))
        txs                   (when block-exists?
                                (cond-> []
                                  parent-uid  (conj retract-parents-child)
                                  children-tx (into children-tx)
                                  true        (into retract-uid)))]
    (log/debug ":block/remove block-uid:" (pr-str uid)
               "\nblock:" (with-out-str
                            (pp/pprint block))
               "\nparent-eid:" (pr-str parent-eid)
               "\nparent-uid:" (pr-str parent-uid)
               "\nretract-uid:" (pr-str retract-uid)
               "\nresolved to txs:" (with-out-str
                                      (pp/pprint txs)))
    txs))


(defmethod resolve-atomic-op-to-tx :page/new
  [db {:op/keys [args]}]
  (let [{:page/keys [title]} args
        page-exists?         (common-db/e-by-av db :node/title title)
        page-uid             (or (-> title dates/title-to-date dates/date-to-day :uid)
                                 (utils/gen-block-uid))
        now-ref              {:time/ts (utils/now-ts)}
        page                 {:node/title     title
                              :block/uid      page-uid
                              :block/children []
                              :time/create    now-ref
                              :time/edit      now-ref}
        txs                  (if page-exists?
                               []
                               [page])]
    txs))


(defmethod resolve-atomic-op-to-tx :page/rename
  [db {:op/keys [args]}]
  (let [old-name           (-> args :page/title)
        new-name           (-> args :target :page/title)
        page-eid           (common-db/e-by-av db :node/title old-name)
        page-exists?       (int? page-eid)
        now-ref            {:time/ts (utils/now-ts)}
        page               (when page-exists?
                             (common-db/get-block db [:node/title old-name]))
        linked-refs        (common-db/get-linked-refs-by-page-title db old-name)
        new-linked-refs    (common-db/map-new-refs linked-refs old-name new-name)
        updated-page       (when page-exists?
                             {:db/id      [:block/uid (:block/uid page)]
                              :node/title new-name
                              :time/edit  now-ref})
        txs                (concat [updated-page] new-linked-refs)]
    (if page-exists?
      txs
      (throw (ex-info "Page you've tried to rename doesn't exist." args)))))


(defmethod resolve-atomic-op-to-tx :page/merge
  [db {:op/keys [args]}]
  (let [from-name       (-> args :page/title)
        to-name         (-> args :target :page/title)
        linked-refs     (common-db/get-linked-refs-by-page-title db from-name)
        new-linked-refs (common-db/map-new-refs linked-refs from-name to-name)
        from-children   (common-db/get-children-uids db [:node/title from-name])
        to-children     (common-db/get-children-uids db [:node/title to-name])
        to-children'    (reduce #(order/insert %1 %2 :last nil) to-children from-children)
        reorder-map-fn  (fn [n x]
                          {:block/uid       x
                           :block/order     n
                           :block/_children [:node/title to-name]})
        reorder         (order/reorder to-children to-children' reorder-map-fn)
        ;; Move paged properties, or delete if key is already there.
        now-ref         {:time/ts (utils/now-ts)}
        from-properties (->> [:node/title from-name] (common-db/get-page db) :block/properties)
        to-property-ks  (->> [:node/title to-name] (common-db/get-page db) :block/properties keys set)
        properties      (->> from-properties
                             (mapcat (fn [[k {:block/keys [uid]}]]
                                       (if (to-property-ks k)
                                         (common-db/retract-uid-recursively-tx db uid now-ref)
                                         (position/add-property db uid {:page/title to-name
                                                                        :relation   {:page/title k}} now-ref)))))
        ;; Delete linked props that would end up duplicated on parent.
        linked-props    (->> [:node/title from-name]
                             (common-db/get-page db)
                             :block/_key
                             (map :db/id)
                             (map (partial common-db/get-parent db))
                             (mapcat (fn [{:block/keys [properties]}]
                                       (if (get properties to-name)
                                         (->> (get properties from-name)
                                              :block/uid
                                              (common-db/retract-uid-recursively-tx db now-ref))
                                         []))))
        delete-page     [:db/retractEntity [:node/title from-name]]
        new-datoms      (concat [delete-page]
                                new-linked-refs
                                reorder
                                properties
                                linked-props)]
    (log/debug ":page/merge args:" (pr-str args) ", resolved-tx:" (pr-str new-datoms))
    new-datoms))


(defmethod resolve-atomic-op-to-tx :page/remove
  [db {:op/keys [args]}]
  (log/debug "atomic-resolver: :page/remove: " (pr-str args))
  (let [{:page/keys [title]} args
        now-ref              {:time/ts (utils/now-ts)}
        page-uid             (common-db/get-page-uid db title)
        retract-blocks       (when page-uid
                               (common-db/retract-uid-recursively-tx db page-uid now-ref))
        delete-linked-refs   (when page-uid
                               (->> page-uid
                                    (vector :block/uid)
                                    (common-db/get-block db)
                                    vector
                                    (common-db/replace-linked-refs-tx db)))
        delete-linked-props  (when page-uid
                               (->> [:node/title title]
                                    (common-db/get-page db)
                                    :block/_key
                                    (map :db/id)
                                    (map (partial common-db/get-block-uid db))
                                    (mapcat (partial common-db/retract-uid-recursively-tx db now-ref))))
        tx-data              (if page-uid
                               (concat retract-blocks
                                       delete-linked-refs
                                       delete-linked-props)
                               [])]
    tx-data))


(defmethod resolve-atomic-op-to-tx :shortcut/new
  [db {:op/keys [args]}]
  (let [{:page/keys [title]} args
        titles               (common-db/get-sidebar-titles db)
        titles'              (order/insert titles title :last nil)
        reorder              (order/reorder titles titles' order/shortcut-map-fn)
        tx-data              reorder]
    tx-data))


(defmethod resolve-atomic-op-to-tx :shortcut/remove
  [db {:op/keys [args]}]
  (let [{:page/keys [title]} args
        titles               (common-db/get-sidebar-titles db)
        titles'              (order/remove titles title)
        reorder              (order/reorder titles titles' order/shortcut-map-fn)
        page-uid             (common-db/get-page-uid db title)
        remove-shortcut-tx   [:db/retract [:block/uid page-uid] :page/sidebar]
        tx-data              (conj reorder remove-shortcut-tx)]
    tx-data))


(defmethod resolve-atomic-op-to-tx :shortcut/move
  [db {:op/keys [args]}]
  (let [{title        :page/title
         ref-position :shortcut/position} args
        {relation  :relation
         ref-title :page/title}           ref-position
        titles                            (common-db/get-sidebar-titles db)
        titles'                           (order/move-within titles title relation ref-title)
        reorder                           (order/reorder titles titles' order/shortcut-map-fn)
        tx-data                           reorder]
    tx-data))


(defmethod resolve-atomic-op-to-tx :composite/consequence
  [_db composite]
  (throw (ex-info "Can't resolve Composite Graph Operation, only Atomic Graph Ops are allowed."
                  (select-keys composite [:op/type :op/trigger]))))


(defn resolve-to-tx
  "This expects either Semantic Events or Atomic Graph Ops, but not Composite Graph Ops.
  Call location should break up composites into atomic ops and call this multiple times,
  once per atomic operation."
  [db {:event/keys [type op] :as event}]
  (if (or (contains? #{:op/atomic} type)
          (:op/atomic? event))
    (resolve-atomic-op-to-tx db (if (:op/atomic? event)
                                  event
                                  op))
    (throw (ex-info "Can't resolve event, only Atomic Graph Ops are allowed." event))))


(defn resolve-transact!
  "Iteratively resolve and transact event optionally with middleware (defaults to true).
  Returns :tx-data from datascript/transact!."
  ([conn event]
   (resolve-transact! conn event true))
  ([conn {:event/keys [id] :as event} middleware?]
   (log/debug "resolve-transact! event-id:" (pr-str id))
   (let [transact! (if middleware?
                     common-db/transact-with-middleware!
                     d/transact!)]
     (utils/log-time
       (str "resolve-transact! event-id: " (pr-str id) " took")
       (if (graph-ops/atomic-composite? event)
         (let [;; Using an atom as an accumulator here isn't very kosher, but it is
               ;; the right way of observing the doseq semantics while using transact!
               tx-data (atom nil)]
           (doseq [atomic (graph-ops/extract-atomics event)
                   :let   [atomic-txs (resolve-to-tx @conn atomic)]]
             (->> (transact! conn atomic-txs)
                  :tx-data
                  (swap! tx-data concat)))
           (vec @tx-data))
         (let [txs (resolve-to-tx @conn event)]
           (:tx-data (transact! conn txs))))))))
