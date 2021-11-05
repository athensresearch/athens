(ns athens.common-events.resolver
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common.logging :as log]
    [athens.common.utils :as utils]
    [athens.patterns :as patterns]
    [clojure.string :as string]
    [datascript.core :as d]))


(defn between
  "http://blog.jenkster.com/2013/11/clojure-less-than-greater-than-tip.html"
  [s t x]
  (if (< s t)
    (and (< s x) (< x t))
    (and (< t x) (< x s))))


(defn replace-linked-refs-tx
  "For a given block, unlinks [[brackets]], #[[brackets]], #brackets, or ((brackets))."
  [db blocks]
  (let [deleted-blocks (sequence (map #(assoc % :block/pattern (patterns/linked (or (:node/title %) (:block/uid %)))))
                                 blocks)
        block-refs-ids (sequence (comp (map #(:block/pattern %))
                                       (mapcat #(common-db/get-ref-ids db %))
                                       (distinct))
                                 deleted-blocks)
        block-refs     (d/pull-many db [:db/id :block/string] block-refs-ids)]
    (into []
          (map (fn [block-ref]
                 (let [updated-content (reduce (fn [content {:keys [block/pattern block/string node/title]}]
                                                 (string/replace content pattern (or title string)))
                                               (:block/string block-ref)
                                               deleted-blocks)]
                   (assoc block-ref :block/string updated-content))))
          block-refs)))


;; TODO start using this resolution in handlers
(defmulti resolve-event-to-tx
  "Resolves `:datascript/*` event in context of existing DB into transactions."
  #(:event/type %2))


(defmethod resolve-event-to-tx :datascript/rename-page
  [db {:event/keys [id type args]}]
  (let [{:keys [uid
                old-name
                new-name]} args
        linked-refs        (common-db/get-linked-refs-by-page-title db old-name)
        new-linked-refs    (common-db/map-new-refs linked-refs old-name new-name)
        new-page           {:db/id      [:block/uid uid]
                            :node/title new-name}
        new-datoms         (concat [new-page] new-linked-refs)]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str new-datoms))
    new-datoms))


(defmethod resolve-event-to-tx :datascript/merge-page
  [db {:event/keys [id type args]}]
  (let [{:keys [uid
                old-name
                new-name]}              args
        linked-refs                     (common-db/get-linked-refs-by-page-title db old-name)
        new-linked-refs                 (common-db/map-new-refs linked-refs old-name new-name)
        {old-page-kids :block/children} (common-db/get-page-document db [:block/uid uid])
        new-parent-uid                  (common-db/get-page-uid db new-name)
        existing-page-block-count       (common-db/existing-block-count db new-name)
        reindex                         (map (fn [{:block/keys [order uid]}]
                                               {:db/id           [:block/uid uid]
                                                :block/order     (+ order existing-page-block-count)
                                                :block/_children [:block/uid new-parent-uid]})
                                             old-page-kids)
        delete-page                     [:db/retractEntity [:block/uid uid]]
        new-datoms                      (concat [delete-page]
                                                new-linked-refs
                                                reindex)]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str new-datoms))
    new-datoms))


(defmethod resolve-event-to-tx :datascript/delete-page
  [db {:event/keys [id type args]}]
  (let [{uid :uid}         args
        ;; NOTE: common DB query? find page title by page uid?
        title              (ffirst
                             (d/q '[:find ?title
                                    :where
                                    [?e :node/title ?title]
                                    [?e :block/uid ?uid]
                                    :in $ ?uid]
                                  db uid))
        retract-blocks     (common-db/retract-uid-recursively-tx db uid)
        delete-linked-refs (->> (common-db/get-page-uid db title)
                                (vector :block/uid)
                                (common-db/get-block db)
                                vector
                                (replace-linked-refs-tx db))
        tx-data            (concat retract-blocks
                                   delete-linked-refs)]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/block-save
  [_db {:event/keys [id type args]}]
  (let [{:keys [uid
                new-string
                add-time?]} args
        new-block-string     {:db/id        [:block/uid uid]
                              :block/string new-string}
        block-with-time      (if add-time?
                               (assoc new-block-string :edit/time (utils/now-ts))
                               new-block-string)
        tx-data              [block-with-time]]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/new-block
  [db {:event/keys [id type args]}]
  (let [{:keys [parent-uid
                block-order
                new-uid]} args
        new-block         {:db/id        -1
                           :block/uid    new-uid
                           :block/string ""
                           :block/order  (inc block-order)
                           :block/open   true}
        reindex           (concat [new-block]
                                  (common-db/inc-after db [:block/uid parent-uid] block-order))
        tx-data           [{:block/uid      parent-uid
                            :block/children reindex}]]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/add-child
  [db {:event/keys [id type args]}]
  (let [{:keys [parent-uid
                new-uid
                add-time?]} args
        new-child         {:db/id        -1
                           :block/uid    new-uid
                           :block/string ""
                           :block/order  0
                           :block/open   true}
        child-with-time   (if add-time?
                            (assoc new-child :edit/time (utils/now-ts))
                            new-child)
        reindex           (concat [child-with-time]
                                  (common-db/inc-after db [:block/uid parent-uid] -1))
        new-block         {:block/uid      parent-uid
                           :block/children reindex}
        tx-data           [new-block]]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/open-block-add-child
  [db {:event/keys [id type args]}]
  (let [{:keys [parent-uid
                new-uid]} args
        open-block-tx     [:db/add [:block/uid parent-uid] :block/open true]
        ;; delegate add-child-tx creation
        add-child-tx      (resolve-event-to-tx db
                                               (common-events/build-add-child-event -1
                                                                                    parent-uid
                                                                                    new-uid))
        tx-data           (into [open-block-tx] add-child-tx)]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/split-block
  [db {:event/keys [id type args]}]
  (let [{:keys [uid
                value
                index
                new-uid]}             args
        parent                        (common-db/get-parent db [:block/uid uid])
        block                         (common-db/get-block db [:block/uid uid])
        {:block/keys [order
                      children
                      open]
         :or         {children []
                      open     true}} block
        head                          (subs value 0 index)
        tail                          (subs value index)
        retracts                      (mapv (fn [child]
                                              [:db/retract (:db/id block) :block/children (:db/id child)])
                                            children)
        next-block                    {:db/id          -1
                                       :block/order    (inc order)
                                       :block/uid      new-uid
                                       :block/open     open
                                       :block/children children
                                       :block/string   tail}
        reindex                       (->> (common-db/inc-after db (:db/id parent) order)
                                           (concat [next-block]))
        new-block                     {:db/id (:db/id block) :block/string head}
        new-parent                    {:db/id (:db/id parent) :block/children reindex}
        tx-data                       (conj retracts new-block new-parent)]
    (log/warn "DEPRECATED!" "event-id:" id ", type:" type ", args:" (pr-str args)
              ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/page-add-shortcut
  [db {:event/keys [id type args]}]
  (let [{:keys [uid]}        args
        reindex-shortcut-txs (->> (d/q '[:find [(pull ?e [*]) ...]
                                         :where
                                         [?e :page/sidebar _]]
                                       db)
                                  (sort-by :page/sidebar)
                                  (map-indexed (fn [i m] (assoc m :page/sidebar i)))
                                  vec)
        add-shortcut-tx      {:block/uid uid :page/sidebar (or (count reindex-shortcut-txs) 1)}
        tx-data              (conj reindex-shortcut-txs add-shortcut-tx)]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/page-remove-shortcut
  [db {:event/keys [id type args]}]
  (let [{:keys [uid]}        args
        reindex-shortcut-txs (->> (d/q '[:find [(pull ?e [*]) ...]
                                         :where
                                         [?e :page/sidebar _]]
                                       db)
                                  (remove #(= uid (:block/uid %)))
                                  (sort-by :page/sidebar)
                                  (map-indexed (fn [i m] (assoc m :page/sidebar i)))
                                  vec)
        remove-shortcut-tx    [:db/retract [:block/uid uid] :page/sidebar]
        tx-data               (conj reindex-shortcut-txs remove-shortcut-tx)]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/left-sidebar-drop-above
  [db {:event/keys [id type args]}]
  (let [{:keys [source-order target-order]}  args
        source-eid  (d/q '[:find ?e .
                           :in $ ?source-order
                           :where [?e :page/sidebar ?source-order]]
                         db source-order)
        new-source  {:db/id source-eid :page/sidebar (if (< source-order target-order)
                                                       (dec target-order)
                                                       target-order)}
        inc-or-dec  (if (< source-order target-order) dec inc)
        tx-data     (->> (d/q '[:find ?shortcut ?new-order
                                :keys db/id page/sidebar
                                :in $ ?source-order ?target-order ?between ?inc-or-dec
                                :where
                                [?shortcut :page/sidebar ?order]
                                [(?between ?source-order ?target-order ?order)]
                                [(?inc-or-dec ?order) ?new-order]]
                              db source-order (if (< source-order target-order)
                                                target-order
                                                (dec target-order))
                              between inc-or-dec)
                         (concat [new-source]))]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/left-sidebar-drop-below
  [db {:event/keys [id type args]}]
  (let [{:keys [source-order target-order]}  args
        source-eid (d/q '[:find ?e .
                          :in $ ?source-order
                          :where [?e :page/sidebar ?source-order]]
                        db source-order)
        new-source {:db/id source-eid :page/sidebar target-order}
        tx-data (->> (d/q '[:find ?shortcut ?new-order
                            :keys db/id page/sidebar
                            :in $ ?source-order ?target-order ?between
                            :where
                            [?shortcut :page/sidebar ?order]
                            [(?between ?source-order ?target-order ?order)]
                            [(dec ?order) ?new-order]]
                          db source-order (inc target-order) between)
                     (concat [new-source]))]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/selected-delete
  [db {:event/keys [id type args]}]
  ;; We know that we only need to dec indices after the last block. The former blocks
  ;; are necessarily going to remove all tail children, meaning we only need to be
  ;; concerned with the last N blocks that are selected, adjacent siblings, to
  ;; determine the minus-after value.
  (let [{:keys [uids]}                    args
        selected-sibs-of-last             (->> uids
                                               (d/q '[:find ?sib-uid ?o
                                                      :in $ ?uid [?selected ...]
                                                      :where
                                                      ;; get all siblings of the last block
                                                      [?e :block/uid ?uid]
                                                      [?p :block/children ?e]
                                                      [?p :block/children ?sib]
                                                      [?sib :block/uid ?sib-uid]
                                                      ;; filter selected
                                                      [(= ?sib-uid ?selected)]
                                                      [?sib :block/order ?o]]
                                                    db
                                                    (last uids))
                                               (sort-by second))
        [uid order]                       (last selected-sibs-of-last)
        parent-eid                        (:db/id (common-db/get-parent db [:block/uid uid]))
        n                                 (count selected-sibs-of-last)
        ;; Since the selection is always contiguous, there's only one parent (the last one) whose children need to be reindexed.
        reindex                           (common-db/minus-after db
                                                                 parent-eid
                                                                 order
                                                                 n)
        retracted-vec                     (mapcat #(common-db/retract-uid-recursively-tx db %) uids)
        block-refs-replace                (->> uids
                                               (map #(common-db/get-block db [:block/uid %]))
                                               (replace-linked-refs-tx db))
        tx-data                           (concat retracted-vec
                                                  reindex
                                                  block-refs-replace)]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


;; resetting ds-conn re-computes all subs and is the cause
;;    for huge delays when undo/redo is pressed
;; here is another alternative strategy for undo/redo
;; Core ideas are inspired from here https://tonsky.me/blog/datascript-internals/
;;    1. db-before + tx-data = db-after
;;    2. DataScript DB contains only currently relevant datoms.
;; 1 is the math that is happening here when you undo/redo but in a much more performant way
;; 2 asserts that even if you are reapplying same txn over and over only relevant ones will be present
;;    - and overall size of transit file does not increase
;; Note: only relevant txns(ones that user deliberately made, not undo/redo ones) go into history
;; Note: Once session is lost(App is closed) edit history is also lost
;; Also cmd + z -> cmd + z -> edit something --- future(cmd + shift + z) doesn't work
;;    - as there is no logical way to assert the future when past has changed hence history is reset
;;    - very similar to intelli-j edit model or any editor's undo/redo mechanism for that matter
(defmethod resolve-event-to-tx :datascript/undo-redo
  [db-history {:event/keys [args]}]
  (let [{:keys [redo?]}  args
        [tx-m-id datoms] (cond->> @db-history
                           redo? reverse
                           true (some (fn [[tx bool datoms]]
                                        (and ((if redo? not (complement not)) bool) [tx datoms]))))]

    (reset! db-history (->> @db-history
                            (map (fn [[tx-id bool datoms]]
                                   (if (= tx-id tx-m-id)
                                     [tx-id redo? datoms]
                                     [tx-id bool datoms])))
                            doall))

    (cond->> datoms
      (not redo?) reverse
      true (map (fn [datom]
                  (let [[id attr val _txn sig?] #?(:clj datom
                                                   :cljs (vec datom))]
                    [(cond
                       (and sig? (not redo?)) :db/retract
                       (and (not sig?) (not redo?)) :db/add
                       (and sig? redo?) :db/add
                       (and (not sig?) redo?) :db/retract)
                     id attr val])))
      ;; Caveat -- we need a way to signal history watcher if this txn is relevant
      ;;     - send a dummy datom, this will get added to user's data
      ;;     - we can easily filter it out while writing to fs but it will have a perf penalty
      ;;     - Unless we are exporting transit to a common format, this can stay(only one datom -- point 2 mentioned above)
      ;;           - although a filter while exporting is more strategic -- once in a while op, compared to fs write(very frequent)
      true (concat [[:db/add "new" :from-undo-redo true]]))))


(defmethod resolve-event-to-tx :datascript/block-open
  [_db {:event/keys [id type args]}]
  (let [{:keys [block-uid
                open?]}      args
        new-block-state      [:db/add     [:block/uid block-uid]
                              :block/open open?]
        tx-data              [new-block-state]]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/delete-only-child
  [db {:event/keys [id type args]}]
  (let [{:keys [uid]} args
        block       (common-db/get-block db [:block/uid uid])
        parent      (common-db/get-parent db [:block/uid uid])
        reindex     (common-db/dec-after db (:db/id parent) (:block/order block))
        new-parent  {:db/id (:db/id parent) :block/children reindex}
        retract     [:db/retractEntity (:db/id block)]
        tx-data     [retract new-parent]]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/delete-merge-block
  [db {:event/keys [id type args]}]
  (let [{:keys [uid value]} args
        block               (common-db/get-block db [:block/uid uid])
        {:block/keys
         [children]
         :or {children []}} block
        parent              (common-db/get-parent db [:block/uid uid])
        prev-block-uid      (common-db/prev-block-uid db uid)
        prev-block          (common-db/get-block db [:block/uid prev-block-uid]) ; TODO prev-block-uid can be nil
        new-prev-block      {:db/id          [:block/uid prev-block-uid]
                             :block/string   (str (:block/string prev-block) value)
                             :block/children children}
        retracts            (mapv (fn [x] [:db/retract (:db/id block) :block/children (:db/id x)]) children)
        retract-block       [:db/retractEntity (:db/id block)]
        reindex             (common-db/dec-after db (:db/id parent) (:block/order block))
        block-refs-replace  (->> (common-db/get-block db [:block/uid uid])
                                 vector
                                 (replace-linked-refs-tx db))
        new-parent          {:db/id (:db/id parent) :block/children reindex}
        tx-data             (into (conj retracts retract-block new-prev-block new-parent)
                                  block-refs-replace)]
    (log/debug "event-id:" id ", type:" type ", args:" (pr-str args)
               ", resolved-tx:" (pr-str tx-data))
    tx-data))
