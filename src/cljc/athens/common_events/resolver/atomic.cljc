(ns athens.common-events.resolver.atomic
  (:require
    [athens.common-db               :as common-db]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common-events.resolver  :as resolver]
    [athens.common.logging          :as log]
    [athens.common.utils            :as utils]
    [athens.dates                   :as dates]
    [clojure.pprint                 :as pp]
    [clojure.string                 :as s]
    [datascript.core                :as d]))


(defmulti resolve-atomic-op-to-tx
  "Resolves ⚛️ Atomic Graph Ops to TXs."
  #(:op/type %2))


(defmethod resolve-atomic-op-to-tx :block/new
  [db {:op/keys [args]}]
  (let [{:block/keys [uid position]}    args
        {relation  :relation
         ref-uid   :block/uid
         ref-title :page/title}         position
        _valid-position                 (common-db/validate-position db position)
        ref-title->uid                  (common-db/get-page-uid db ref-title)
        ;; Pages must be referenced by title but internally we still use uids for them.
        ref-uid                         (or ref-uid ref-title->uid)
        ref-parent?                     (#{:first :last} relation)
        ref-block-exists?               (int? (common-db/e-by-av db :block/uid ref-uid))
        ref-block                       (when ref-block-exists?
                                          (common-db/get-block db [:block/uid ref-uid]))
        {parent-block-uid :block/uid
         :as              parent-block} (if ref-parent?
                                          (if ref-block-exists?
                                            ref-block
                                            (throw (ex-info "Ref block does not exist" {:block/uid ref-uid})))
                                          (common-db/get-parent db [:block/uid ref-uid]))
        parent-block-exists?            (int? (common-db/e-by-av db :block/uid parent-block-uid))
        new-block-order                 (condp = relation
                                          :first  0
                                          :last   (if-let [parent-block-children (:block/children parent-block)]
                                                    (->> parent-block-children
                                                         (map :block/order)
                                                         (reduce max 0)
                                                         inc)
                                                    0)
                                          :before (:block/order ref-block)
                                          :after  (inc (:block/order ref-block)))
        now                             (utils/now-ts)
        new-block                       {:block/uid    uid
                                         :block/string ""
                                         :block/order  new-block-order
                                         :block/open   true
                                         :create/time  now
                                         :edit/time    now}
        reindex                         (if-not parent-block-exists?
                                          [new-block]
                                          (concat [new-block]
                                                  (common-db/inc-after db
                                                                       [:block/uid parent-block-uid]
                                                                       (dec new-block-order))))
        tx-data                         [{:block/uid      parent-block-uid
                                          :block/children reindex
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
  (let [{:block/keys [uid position]}            args
        {relation  :relation
         ref-uid   :block/uid
         ref-title :page/title}                 position
        _valid-position                         (common-db/validate-position db position)
        _valid-block-uid                        (when (common-db/get-page-title db uid)
                                                  (throw (ex-info "Block to be moved is a page, cannot move pages." args)))
        ref-title->uid                          (common-db/get-page-uid db ref-title)
        ;; Pages must be referenced by title but internally we still use uids for them.
        ref-uid                                 (or ref-uid ref-title->uid)
        {old-block-order :block/order
         :as             moved-block}           (common-db/get-block db [:block/uid uid])
        {old-parent-block-uid :block/uid}       (common-db/get-parent db [:block/uid uid])
        ref-parent?                             (#{:first :last} relation)
        ref-block-exists?                       (int? (common-db/e-by-av db :block/uid ref-uid))
        ref-block                               (when ref-block-exists?
                                                  (common-db/get-block db [:block/uid ref-uid]))
        {new-parent-block-uid :block/uid
         :as                  new-parent-block} (if ref-parent?
                                                  (if ref-block-exists?
                                                    ref-block
                                                    (throw (ex-info "Ref block does not exist" {:block/uid ref-uid})))
                                                  (common-db/get-parent db [:block/uid ref-uid]))
        same-parent?                            (= new-parent-block-uid old-parent-block-uid)
        ref-block-order                         (or (:block/order ref-block)
                                                    ;; When the ref-block is page we can say the order of
                                                    ;; page is -1, the consequence of this is that all the
                                                    ;; block moves would be considered as moving up when compared
                                                    ;; with this -1 order.
                                                    -1)
        ;; Up means it went from a higher number (e.g. 5) to a lower number (e.g. 2).
        up?                                     (< ref-block-order old-block-order)
        new-block-order                         (condp = relation
                                                  :first  0
                                                  :last   (if-let [parent-block-children (:block/children new-parent-block)]
                                                            (->> parent-block-children
                                                                 (map :block/order)
                                                                 (reduce max 0)
                                                                 inc)
                                                            0)
                                                  :before (cond
                                                            ;; it replaces ref block
                                                            (not same-parent?) ref-block-order
                                                            up?                ref-block-order
                                                            ;; the ref block is unmoved
                                                            :else              (dec ref-block-order))
                                                  :after  (cond
                                                            ;; the ref block is unmoved
                                                            (not same-parent?) (inc ref-block-order)
                                                            up?                (inc ref-block-order)
                                                            ;; it replaces the ref block
                                                            :else              ref-block-order))
        now                                     (utils/now-ts)
        updated-block                           (merge moved-block
                                                       {:block/order new-block-order
                                                        :edit/time   now})]
    (if same-parent?
      (let [lower-bound-index (min old-block-order new-block-order)
            upper-bound-index (max old-block-order new-block-order)
            +or-              (if up? + -)
            reindexed-parent  {:block/uid      old-parent-block-uid
                               :edit/time      now
                               :block/children (common-db/reindex-blocks-between-bounds db
                                                                                        +or-
                                                                                        [:block/uid old-parent-block-uid]
                                                                                        ;; NB: reindex-blocks-between excludes the bounds
                                                                                        ;; We need to figure out the right bounds:
                                                                                        (if up?
                                                                                          ;; block moved up to occupy lower-bound-index
                                                                                          ;; so the block previously there needs to
                                                                                          ;; be included in the reindexing.
                                                                                          (dec lower-bound-index)
                                                                                          lower-bound-index)
                                                                                        (if up?
                                                                                          upper-bound-index
                                                                                          ;; block moved down to occupy upper-bound-index
                                                                                          ;; so the block previously there needs to
                                                                                          ;; be included in the reindexing.
                                                                                          (inc upper-bound-index))
                                                                                        1)}
            tx-data           [updated-block reindexed-parent]]
        (log/debug "same-parent:\n"
                   (with-out-str
                     (pp/pprint {:lower   lower-bound-index
                                 :upper   upper-bound-index
                                 :old-bo  old-block-order
                                 :new-bo  new-block-order
                                 :up?     up?
                                 :tx-data tx-data})))
        tx-data)

      (let [retract-from-old-parent [:db/retract [:block/uid old-parent-block-uid] :block/children [:block/uid uid]]
            old-parent-reindex      (common-db/dec-after db
                                                         [:block/uid old-parent-block-uid]
                                                         old-block-order)
            old-parent-reindexed    {:block/uid      old-parent-block-uid
                                     :edit/time      now
                                     :block/children old-parent-reindex}
            new-parent-reindexed    {:block/uid      new-parent-block-uid
                                     :edit/time      now
                                     :block/children (concat [updated-block]
                                                             (common-db/inc-after db
                                                                                  [:block/uid new-parent-block-uid]
                                                                                  (dec new-block-order)))}
            tx-data                 (if (seq old-parent-reindex)
                                      [retract-from-old-parent
                                       old-parent-reindexed
                                       new-parent-reindexed]
                                      [retract-from-old-parent
                                       new-parent-reindexed])]
        (log/debug "diff-parent:\n"
                   (with-out-str
                     (pp/pprint {:old-bo  old-block-order
                                 :new-bo  new-block-order
                                 :up?     up?
                                 :tx-data tx-data})))
        tx-data))))


(defmethod resolve-atomic-op-to-tx :block/remove
  [db {:op/keys [args]}]
  ;; [x] :db/retractEntity
  ;; [x] retract children
  ;; [x] :db/retract parent's child
  ;; [x] reindex parent's children
  ;; [x] cleanup block refs
  (let [{:block/keys [uid]}   args
        block-exists?         (common-db/e-by-av db :block/uid uid)
        {removed-order :block/order
         children      :block/children
         :as           block} (when block-exists?
                                (common-db/get-block db [:block/uid uid]))
        parent-eid            (when block-exists?
                                (common-db/get-parent-eid db [:block/uid uid]))
        parent-uid            (when parent-eid
                                (common-db/v-by-ea db parent-eid :block/uid))
        reindex               (common-db/dec-after db [:block/uid parent-uid] removed-order)
        reindex?              (seq reindex)
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
                                                          title  (merge {:node/title   (s/replace title from-string removed-string)}))))
                                                    referenced-uids)))))
        has-asserts?          (seq asserts)
        retract-kids          (mapv (fn [uid]
                                      [:db/retractEntity [:block/uid uid]])
                                    descendants-uids)
        retract-entity        (when block-exists?
                                [:db/retractEntity [:block/uid uid]])
        retract-parents-child (when parent-uid
                                [:db/retract [:block/uid parent-uid] :block/children [:block/uid uid]])
        parent                (when reindex?
                                {:block/uid      parent-uid
                                 :block/children reindex})
        txs                   (when block-exists?
                                (cond-> []
                                  parent-uid   (conj retract-parents-child)
                                  reindex?     (conj parent)
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
  (let [from-name                       (-> args :page/title)
        to-name                         (-> args :target :page/title)
        linked-refs                     (common-db/get-linked-refs-by-page-title db from-name)
        new-linked-refs                 (common-db/map-new-refs linked-refs from-name to-name)
        {old-page-kids :block/children} (common-db/get-page-document db [:node/title from-name])
        existing-page-block-count       (common-db/existing-block-count db to-name)
        reindex                         (map (fn [{:block/keys [order uid]}]
                                               {:db/id           [:block/uid uid]
                                                :block/order     (+ order existing-page-block-count)
                                                :block/_children [:node/title to-name]})
                                             old-page-kids)
        delete-page                     [:db/retractEntity [:node/title from-name]]
        new-datoms                      (concat [delete-page]
                                                new-linked-refs
                                                reindex)]
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
        page-uid             (common-db/get-page-uid db title)
        reindex-shortcut-txs (->> (common-db/get-sidebar-elements db)
                                  (sort-by :page/sidebar)
                                  (map-indexed (fn [i m] (assoc m :page/sidebar i)))
                                  vec)
        add-shortcut-tx      {:block/uid    page-uid
                              :page/sidebar (or (count reindex-shortcut-txs)
                                                1)}
        tx-data              (conj reindex-shortcut-txs
                                   add-shortcut-tx)]
    tx-data))


(defmethod resolve-atomic-op-to-tx :shortcut/remove
  [db {:op/keys [args]}]
  (let [{:page/keys [title]} args
        page-uid             (common-db/get-page-uid db title)
        reindex-shortcut-txs (->> (common-db/get-sidebar-elements db)
                                  (remove #(= page-uid (:block/uid %)))
                                  (sort-by :page/sidebar)
                                  (map-indexed (fn [i m] (assoc m :page/sidebar i)))
                                  vec)
        remove-shortcut-tx    [:db/retract [:block/uid page-uid] :page/sidebar]
        tx-data               (conj reindex-shortcut-txs remove-shortcut-tx)]
    tx-data))


(defmethod resolve-atomic-op-to-tx :shortcut/move
  [db {:op/keys [args]}]
  (let [{title        :page/title
         ref-position :shortcut/position}  args
        {relation  :relation
         ref-title :page/title}            ref-position
        source-uid                         (common-db/get-page-uid db title)
        [source-order
         target-order]                     (common-db/find-source-target-order db
                                                                               title
                                                                               ref-title)
        new-source-order                   (if (and  (= :before relation)
                                                     (< source-order target-order))
                                             (dec target-order)
                                             target-order)
        new-source                         [{:block/uid    source-uid
                                             :page/sidebar new-source-order}]
        new-target-order                   (cond
                                             (and  (= :before relation)
                                                   (< source-order target-order)) target-order
                                             (= :before relation)                 (dec target-order)
                                             :else                                (inc target-order))
        inc-or-dec                         (if (and (= :before relation)
                                                    (> source-order target-order))
                                             inc
                                             dec)
        reindex                            (common-db/reindex-sidebar-after-move db
                                                                                 source-order
                                                                                 new-target-order
                                                                                 common-db/between
                                                                                 inc-or-dec)
        tx-data                            (concat new-source
                                                   reindex)]
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
    (resolver/resolve-event-to-tx db event)))


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
