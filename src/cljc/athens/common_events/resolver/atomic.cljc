(ns athens.common-events.resolver.atomic
  (:require
    [athens.common-db                    :as common-db]
    [athens.common-events.graph.ops      :as graph-ops]
    [athens.common-events.resolver.order :as order]
    [athens.common.logging               :as log]
    [athens.common.utils                 :as utils]
    [athens.dates                        :as dates]
    [clojure.pprint                      :as pp]
    [clojure.string                      :as s]
    [datascript.core                     :as d]))


(defmulti resolve-atomic-op-to-tx
  "Resolves ⚛️ Atomic Graph Ops to TXs."
  #(:op/type %2))


(defmethod resolve-atomic-op-to-tx :block/new
  [db {:op/keys [args]}]
  (let [{:block/keys [uid position]} args
        {:keys [relation]}           position
        [ref-uid parent-uid]         (common-db/position->uid+parent db position)
        now                          (utils/now-ts)
        new-block                    {:block/uid    uid
                                      :block/string ""
                                      :block/open   true
                                      :create/time  now
                                      :edit/time    now}
        children                     (common-db/get-children-uids db [:block/uid parent-uid])
        children'                    (order/insert children uid relation ref-uid)
        reorder                      (order/reorder children children' order/block-map-fn)
        children-tx                  (concat [new-block] reorder)
        tx-data                      [{:block/uid      parent-uid
                                       :block/children children-tx
                                       :edit/time      now}]]
    tx-data))


;; This is Atomic Graph Op, there is also composite version of it
(defmethod resolve-atomic-op-to-tx :block/save
  [_db {:op/keys [args]}]
  (let [{:block/keys [uid string]} args]
    [{:block/uid    uid
      :block/string string
      :edit/time    (utils/now-ts)}]))


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
      (let [now           (utils/now-ts)
            updated-block {:block/uid  uid
                           :block/open open?
                           :edit/time  now}]
        [updated-block]))))


(defmethod resolve-atomic-op-to-tx :block/move
  [db {:op/keys [args]}]
  (log/debug "atomic-resolver :block/move args:" (pr-str args))
  (let [{:block/keys [uid position]} args
        _valid-block-uid             (when (common-db/get-page-title db uid)
                                       (throw (ex-info "Block to be moved is a page, cannot move pages." args)))
        {:keys [relation]}           position
        [ref-uid new-parent-uid]     (common-db/position->uid+parent db position)
        {old-parent-uid :block/uid}  (common-db/get-parent db [:block/uid uid])
        same-parent?                 (= new-parent-uid old-parent-uid)
        now                          (utils/now-ts)
        updated-block'               (if same-parent?
                                       [{:block/uid uid
                                         :edit/time now}]
                                       [[:db/retract [:block/uid old-parent-uid] :block/children [:block/uid uid]]
                                        {:block/uid      new-parent-uid
                                         :block/children [{:block/uid uid
                                                           :edit/time now}]
                                         :edit/time      now}])
        reorder                      (if same-parent?
                                       (let [children  (common-db/get-children-uids db [:block/uid old-parent-uid])
                                             children' (order/move-within children uid relation ref-uid)
                                             reorder   (order/reorder children children' order/block-map-fn)]
                                         reorder)

                                       (let [origin-children         (common-db/get-children-uids db [:block/uid old-parent-uid])
                                             destination-children    (common-db/get-children-uids db [:block/uid new-parent-uid])
                                             [origin-children'
                                              destination-children'] (order/move-between origin-children destination-children uid relation ref-uid)
                                             reorder-origin          (order/reorder origin-children origin-children' order/block-map-fn)
                                             reorder-destination     (order/reorder destination-children destination-children' order/block-map-fn)]
                                         (concat reorder-origin reorder-destination)))]
    (into updated-block' reorder)))


(defmethod resolve-atomic-op-to-tx :block/remove
  [db {:op/keys [args]}]
  ;; [x] :db/retractEntity
  ;; [x] retract children
  ;; [x] :db/retract parent's child
  ;; [x] reindex parent's children
  ;; [x] cleanup block refs
  (let [{:block/keys [uid]}   args
        block-exists?         (common-db/e-by-av db :block/uid uid)
        {children      :block/children
         :as           block} (when block-exists?
                                (common-db/get-block db [:block/uid uid]))
        parent-eid            (when block-exists?
                                (common-db/get-parent-eid db [:block/uid uid]))
        parent-uid            (when parent-eid
                                (common-db/v-by-ea db parent-eid :block/uid))
        parent-children       (common-db/get-children-uids db [:block/uid parent-uid])
        parent-children'      (order/remove parent-children uid)
        reorder               (order/reorder parent-children parent-children' order/block-map-fn)
        has-kids?             (seq children)
        descendants-uids      (when has-kids?
                                (loop [acc        []
                                       to-look-at children]
                                  (if-let [look-at (first to-look-at)]
                                    (let [c-uid   (:block/uid look-at)
                                          c-block (common-db/get-block db [:block/uid c-uid])]
                                      (recur (conj acc c-uid)
                                             (apply conj (rest to-look-at)
                                                    (:block/children c-block))))
                                    acc)))
        all-uids-to-remove    (conj (set descendants-uids) uid)
        uid->refs             (->> all-uids-to-remove
                                   (map (fn [uid]
                                          (let [block    (common-db/get-block db [:block/uid uid])
                                                rev-refs (set (:block/_refs block))]
                                            (when-not (empty? rev-refs)
                                              [uid (set rev-refs)]))))
                                   (remove nil?)
                                   (into {}))
        ref-eids              (mapcat second uid->refs)
        eids->uids            (->> ref-eids
                                   (map (fn [{id :db/id}]
                                          [id (common-db/v-by-ea db id :block/uid)]))
                                   (into {}))
        removed-uid->uid-refs (->> uid->refs
                                   (map (fn [[k refs]]
                                          [k (set
                                               (for [{eid :db/id} refs
                                                     :let         [uid (eids->uids eid)]
                                                     :when        (not (contains? all-uids-to-remove uid))]
                                                 uid))]))
                                   (remove #(empty? (second %)))
                                   (into {}))
        asserts               (->> removed-uid->uid-refs
                                   (mapcat (fn [[removed-uid referenced-uids]]
                                             (let [removed-string (common-db/v-by-ea db [:block/uid removed-uid] :block/string)
                                                   from-string    (str "((" removed-uid "))")]
                                               (map (fn [uid]
                                                      (let [string (common-db/get-block-string db uid)
                                                            title  (common-db/get-page-title db uid)]
                                                        (cond-> {:block/uid uid}
                                                          string (merge {:block/string (s/replace string from-string removed-string)})
                                                          title  (merge {:node/title (s/replace title from-string removed-string)}))))
                                                    referenced-uids)))))
        has-asserts?          (seq asserts)
        retract-kids          (mapv (fn [uid]
                                      [:db/retractEntity [:block/uid uid]])
                                    descendants-uids)
        retract-entity        (when block-exists?
                                [:db/retractEntity [:block/uid uid]])
        retract-parents-child (when parent-uid
                                [:db/retract [:block/uid parent-uid] :block/children [:block/uid uid]])
        txs                   (when block-exists?
                                (cond-> []
                                  parent-uid   (conj retract-parents-child)
                                  reorder      (into reorder)
                                  has-kids?    (into retract-kids)
                                  has-asserts? (into asserts)
                                  true         (conj retract-entity)))]
    (log/debug ":block/remove block-uid:" (pr-str uid)
               "\nblock:" (with-out-str
                            (pp/pprint block))
               "\nparent-eid:" (pr-str parent-eid)
               "\nparent-uid:" (pr-str parent-uid)
               "\nretract-kids:" (pr-str retract-kids)
               "\nresolved to txs:" (with-out-str
                                      (pp/pprint txs)))
    txs))


(defmethod resolve-atomic-op-to-tx :page/new
  [db {:op/keys [args]}]
  (let [{:page/keys [title]} args
        page-exists?         (common-db/e-by-av db :node/title title)
        page-uid             (or (-> title dates/title-to-date dates/date-to-day :uid)
                                 (utils/gen-block-uid))
        now                  (utils/now-ts)
        page                 {:node/title     title
                              :block/uid      page-uid
                              :block/children []
                              :create/time    now
                              :edit/time      now}
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
        now                (utils/now-ts)
        page               (when page-exists?
                             (common-db/get-block db [:node/title old-name]))
        linked-refs        (common-db/get-linked-refs-by-page-title db old-name)
        new-linked-refs    (common-db/map-new-refs linked-refs old-name new-name)
        updated-page       (when page-exists?
                             {:db/id      [:block/uid (:block/uid page)]
                              :node/title new-name
                              :edit/time  now})
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
        delete-page     [:db/retractEntity [:node/title from-name]]
        new-datoms      (concat [delete-page]
                                new-linked-refs
                                reorder)]
    (log/debug ":page/merge args:" (pr-str args) ", resolved-tx:" (pr-str new-datoms))
    new-datoms))


(defmethod resolve-atomic-op-to-tx :page/remove
  [db {:op/keys [args]}]
  (log/debug "atomic-resolver: :page/remove: " (pr-str args))
  (let [{:page/keys [title]} args
        page-uid             (common-db/get-page-uid db title)
        retract-blocks       (when page-uid
                               (common-db/retract-uid-recursively-tx db page-uid))
        delete-linked-refs   (when page-uid
                               (->> page-uid
                                    (vector :block/uid)
                                    (common-db/get-block db)
                                    vector
                                    (common-db/replace-linked-refs-tx db)))
        tx-data              (if page-uid
                               (concat retract-blocks
                                       delete-linked-refs)
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
