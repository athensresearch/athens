(ns athens.common-events.resolver.atomic
  (:require
    [athens.common-db               :as common-db]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common-events.resolver  :as resolver]
    [athens.common.logging          :as log]
    [athens.common.utils            :as utils]
    [athens.dates                   :as dates]
    [clojure.pprint                 :as pp]
    [clojure.string                 :as s]))


(defmulti resolve-atomic-op-to-tx
  "Resolves ⚛️ Atomic Graph Ops to TXs."
  #(:op/type %2))


(defmethod resolve-atomic-op-to-tx :block/new
  [db {:op/keys [args]}]
  (let [{:keys [block-uid
                position]}              args
        {:keys [ref-uid
                ref-title
                relation]}              position
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
                                          :last   (->> parent-block
                                                       :block/children
                                                       (map :block/order)
                                                       (reduce max 0)
                                                       inc)
                                          :before (:block/order ref-block)
                                          :after  (inc (:block/order ref-block)))
        now                             (utils/now-ts)
        new-block                       {:block/uid    block-uid
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
  [db {:op/keys [args]}]
  (let [{:keys [block-uid
                new-string
                old-string]} args
        stored-old-string    (if-let [block-eid (common-db/e-by-av db :block/uid block-uid)]
                               (common-db/v-by-ea db block-eid :block/string)
                               "")]
    (when-not (= stored-old-string old-string)
      (print (ex-info ":block/save operation started from a stale state."
                      {:op/args           args
                       :actual-old-string stored-old-string})))
    (let [now           (utils/now-ts)
          updated-block {:block/uid    block-uid
                         :block/string new-string
                         :edit/time    now}]
      [updated-block])))


(defmethod resolve-atomic-op-to-tx :block/move
  [db {:op/keys [args]}]
  (log/debug "atomic-resolver :block/move args:" (pr-str args))
  (let [{:keys [block-uid position]}            args
        {:keys [ref-uid ref-title relation]}    position
        _valid-position                         (common-db/validate-position db position)
        _valid-block-uid                        (when (common-db/get-page-title db block-uid)
                                                  (throw (ex-info "Block to be moved is a page, cannot move pages." args)))
        ref-title->uid                          (common-db/get-page-uid db ref-title)
        ;; Pages must be referenced by title but internally we still use uids for them.
        ref-uid                                 (or ref-uid ref-title->uid)
        {old-block-order :block/order
         :as             moved-block}           (common-db/get-block db [:block/uid block-uid])
        {old-parent-block-uid :block/uid}       (common-db/get-parent db [:block/uid block-uid])
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
        ref-block-order                         (:block/order ref-block)
        ;; Up means it went from a higher number (e.g. 5) to a lower number (e.g. 2).
        up?                                     (< ref-block-order old-block-order)
        new-block-order                         (condp = relation
                                                  :first  0
                                                  :last   (->> new-parent-block
                                                               :block/children
                                                               (map :block/order)
                                                               (reduce max 0)
                                                               inc)
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

      (let [retract-from-old-parent [:db/retract [:block/uid old-parent-block-uid] :block/children [:block/uid block-uid]]
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
  (let [{:keys [block-uid]}   args
        block-exists?         (common-db/e-by-av db :block/uid block-uid)
        {removed-order :block/order
         children      :block/children
         :as           block} (when block-exists?
                                (common-db/get-block db [:block/uid block-uid]))
        parent-eid            (when block-exists?
                                (common-db/get-parent-eid db [:block/uid block-uid]))
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
                                             (apply conj (rest children)
                                                    (:block/children c-block))))
                                    acc)))
        all-uids-to-remove    (conj (set descendants-uids) block-uid)
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
                                                   from-string    (str "((" removed-uid "))")
                                                   uid->string    (->> referenced-uids
                                                                       (map (fn [uid]
                                                                              [uid (common-db/v-by-ea db [:block/uid uid] :block/string)]))
                                                                       (into {}))]
                                               (map (fn [[uid original-string]]
                                                      {:block/uid    uid
                                                       :block/string (s/replace original-string from-string removed-string)})
                                                    uid->string)))))
        has-asserts?          (seq asserts)
        retract-kids          (mapv (fn [uid]
                                      [:db/retractEntity [:block/uid uid]])
                                    descendants-uids)
        retract-entity        (when block-exists?
                                [:db/retractEntity [:block/uid block-uid]])
        retract-parents-child (when parent-uid
                                [:db/retract [:block/uid parent-uid] :block/children [:block/uid block-uid]])
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
    (log/debug ":block/remove block-uid:" (pr-str block-uid)
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
  (let [{:keys [title]} args
        page-exists?    (common-db/e-by-av db :node/title title)
        page-uid        (or (-> title dates/title-to-date dates/date-to-day :uid)
                            (utils/gen-block-uid))
        now             (utils/now-ts)
        page            {:node/title     title
                         :block/uid      page-uid
                         :block/children []
                         :create/time    now
                         :edit/time      now}
        txs             (if page-exists?
                          []
                          [page])]
    txs))


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
  "Iteratively resolve and transact event."
  [conn {:event/keys [id] :as event}]
  (log/debug "resolve-transact! event-id:" (pr-str id))
  (if (graph-ops/atomic-composite? event)
    (doseq [atomic (graph-ops/extract-atomics event)
            :let   [_ (log/debug "resolve-transact! atomic:" (with-out-str (pp/pprint atomic)))
                    atomic-txs (resolve-to-tx @conn atomic)]]
      (log/debug "resolve-transact! atomic-txs:" (with-out-str (pp/pprint atomic-txs)))
      (common-db/transact-with-middleware! conn atomic-txs))
    (let [txs (resolve-to-tx @conn event)]
      (log/debug "resolve-transact! txs:" (with-out-str (pp/pprint txs)))
      (common-db/transact-with-middleware! conn txs))))
