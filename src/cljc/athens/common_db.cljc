(ns athens.common-db
  "Common DB (Datalog) access layer.
  So we execute same code in CLJ & CLJS."
  (:require
    [athens.parser                 :as parser]
    [athens.patterns               :as patterns]
    [clojure.data                  :as data]
    [clojure.pprint                :as pprint]
    [clojure.set                   :as set]
    [clojure.string                :as string]
    #?(:clj [clojure.tools.logging :as log])
    #?(:clj  [datahike.api         :as d]
       :cljs [datascript.core      :as d])))


(defn e-by-av
  [db a v]
  (-> (d/datoms db :avet a v)
      first
      :e))


(defn v-by-ea
  [db e a]
  (-> (d/datoms db :eavt e a)
      first
      :v))


(def rules
  '[[(after ?p ?at ?ch ?o)
     [?p :block/children ?ch]
     [?ch :block/order ?o]
     [(> ?o ?at)]]
    [(between ?p ?lower-bound ?upper-bound ?ch ?o)
     [?p :block/children ?ch]
     [?ch :block/order ?o]
     [(> ?o ?lower-bound)]
     [(< ?o ?upper-bound)]]
    [(inc-after ?p ?at ?ch ?new-o)
     (after ?p ?at ?ch ?o)
     [(inc ?o) ?new-o]]
    [(dec-after ?p ?at ?ch ?new-o)
     (after ?p ?at ?ch ?o)
     [(dec ?o) ?new-o]]
    [(plus-after ?p ?at ?ch ?new-o ?x)
     (after ?p ?at ?ch ?o)
     [(+ ?o ?x) ?new-o]]
    [(minus-after ?p ?at ?ch ?new-o ?x)
     (after ?p ?at ?ch ?o)
     [(- ?o ?x) ?new-o]]
    [(siblings ?uid ?sib-e)
     [?e :block/uid ?uid]
     [?p :block/children ?e]
     [?p :block/children ?sib-e]]])


(defn inc-after
  [db eid order]
  (->> (d/q '[:find ?ch ?new-o
              :in $ % ?p ?at
              :keys db/id block/order
              :where (inc-after ?p ?at ?ch ?new-o)]
            db
            rules
            eid
            order)))


(defn dec-after
  [db eid order]
  (->> (d/q '[:find ?ch ?new-o
              :in $ % ?p ?at
              :keys db/id block/order
              :where (dec-after ?p ?at ?ch ?new-o)]
            db
            rules
            eid
            order)))


(defn get-children-uids-recursively
  "Get list of children UIDs for given block `uid` (including the root block's UID)"
  [db uid]
  (when-let [eid (e-by-av db :block/uid uid)]
    (->> eid
         (d/pull db '[:block/order :block/uid {:block/children ...}])
         (tree-seq :block/children :block/children)
         (map :block/uid))))


(defn retract-uid-recursively-tx
  "Retract all blocks of a page, including the page."
  [db uid]
  (mapv (fn [uid]
          [:db/retractEntity [:block/uid uid]])
        (get-children-uids-recursively db uid)))


(defn get-ref-ids
  [db pattern]
  (d/q '[:find [?e ...]
         :where
         [?e :block/string ?s]
         [(re-find ?regex ?s)]
         :in $ ?regex]
       db pattern))


(defn get-block
  "Fetches whole block based on `:db/id`."
  [db eid]
  (d/pull db '[:db/id
               #?(:cljs
                  :remote/db-id)
               :node/title
               :block/uid
               :block/order
               :block/string
               :block/open
               :block/refs
               :block/_refs
               {:block/children [:block/uid
                                 :block/order]}]
          eid))


(defn get-parent-eid
  "Find parent's `:db/id` of given `eid`."
  [db eid]
  (->> (d/entity db eid)
       :block/_children
       first
       :db/id))


(defn get-parent
  "Given `:db/id` find it's parent."
  [db eid]
  (get-block db (get-parent-eid db eid)))


(defn prev-sib
  [db uid prev-sib-order]
  (d/q '[:find ?sib .
         :in $ % ?target-uid ?prev-sib-order
         :where
         (siblings ?target-uid ?sib)
         [?sib :block/order ?prev-sib-order]
         [?sib :block/uid ?uid]
         [?sib :block/children ?ch]]
       db rules uid prev-sib-order))


(defn get-older-sib
  [db uid]
  (let [sib-uid   (d/q '[:find ?uid .
                         :in $ % ?target-uid
                         :where
                         (siblings ?target-uid ?sib)
                         [?target-e :block/uid ?target-uid]
                         [?target-e :block/order ?target-o]
                         [(dec ?target-o) ?prev-sib-order]
                         [?sib :block/order ?prev-sib-order]
                         [?sib :block/uid ?uid]]
                       db
                       rules
                       uid)
        older-sib (get-block db [:block/uid sib-uid])]
    older-sib))


(defn sort-block-children
  [block]
  (if-let [children (seq (:block/children block))]
    (assoc block :block/children
           (vec (sort-by :block/order (map sort-block-children children))))
    block))


(def block-document-pull-vector
  '[:db/id :block/uid :block/string :block/open :block/order {:block/children ...} :block/refs :block/_refs])


(def node-document-pull-vector
  (-> block-document-pull-vector
      (conj :node/title :page/sidebar)))


(defn get-page-uid-by-title
  "Finds page `:block/uid` by `page-title`."
  [db page-title]
  (d/q '[:find ?uid .
         :in $ ?title
         :where
         [?eid :node/title ?title]
         [?eid :block/uid ?uid]]
       db
       page-title))


(defn existing-block-count
  "Count is used to reindex blocks after merge."
  [db local-title]
  (count (d/q '[:find [?ch ...]
                :in $ ?t
                :where
                [?e :node/title ?t]
                [?e :block/children ?ch]]
              db local-title)))


(defn map-new-refs
  "Find and replace linked ref with new linked ref, based on title change."
  [linked-refs old-title new-title]
  (map (fn [{:block/keys [uid string] :node/keys [title]}]
         (let [[string kw] (if title
                             [title :node/title]
                             [string :block/string])
               new-str (string/replace string
                                       (patterns/linked old-title)
                                       (str "$1$3$4" new-title "$2$5"))]
           {:db/id [:block/uid uid]
            kw     new-str}))
       linked-refs))


(defn minus-after
  [db eid order x]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at ?x
              :where (minus-after ?p ?at ?ch ?new-o ?x)]
            db
            rules
            eid
            order
            x)))


(defn plus-after
  [db eid order x]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at ?x
              :where (plus-after ?p ?at ?ch ?new-o ?x)]
            db
            rules
            eid
            order
            x)))


(defn reindex-blocks-between-bounds
  [db inc-or-dec parent-eid lower-bound upper-bound n]
  #_(println "reindex block")
  (d/q '[:find ?ch ?new-order
         :keys db/id block/order
         :in $ % ?+or- ?parent ?lower-bound ?upper-bound ?n
         :where
         (between ?parent ?lower-bound ?upper-bound ?ch ?order)
         [(?+or- ?order ?n) ?new-order]]
       db
       rules
       inc-or-dec
       parent-eid
       lower-bound
       upper-bound
       n))


(defn get-page-document
  "Retrieves whole page 'document', meaning with children."
  [db eid]
  (-> db
      (d/pull node-document-pull-vector eid)
      sort-block-children))


(defn uid-and-embed-id
  [uid]
  (or (some->> uid
               (re-find #"^(.+)-embed-(.+)")
               rest vec)
      [uid nil]))


(defn nth-sibling
  "Find sibling that has order+n of current block.
  Negative n means previous sibling.
  Positive n means next sibling."
  [db uid n]
  (let [block      (get-block db [:block/uid uid])
        {:block/keys [order]} block
        find-order (+ n order)]
    (d/q '[:find (pull ?sibs [*]) .
           :in $ % ?curr-uid ?find-order
           :where
           (siblings ?curr-uid ?sibs)
           [?sibs :block/order ?find-order]]
         db rules uid find-order)))


(defn deepest-child-block
  [db id]
  (let [document (->> (d/pull db '[:block/order :block/uid {:block/children ...}] id)
                      sort-block-children)]
    (loop [block document]
      (let [{:block/keys [children]} block
            n (count children)]
        (if (zero? n)
          block
          (recur (get children (dec n))))))))


(defn prev-block-uid
  "If order 0, go to parent.
   If order n but block is closed, go to prev sibling.
   If order n and block is OPEN, go to prev sibling's deepest child."
  [db uid]
  (let [[uid embed-id]  (uid-and-embed-id uid)
        block           (get-block db [:block/uid uid])
        parent          (get-parent db [:block/uid uid])
        prev-sibling    (nth-sibling db uid -1)
        {:block/keys    [open uid]} prev-sibling
        prev-block      (cond
                          (zero? (:block/order block)) parent
                          (false? open) prev-sibling
                          (true? open) (deepest-child-block db [:block/uid uid]))]
    (cond-> (:block/uid prev-block)
      embed-id (str "-embed-" embed-id))))


(defn same-parent?
  "Given a coll of uids, determine if uids are all direct children of the same parent."
  [db uids]
  #_(println "same parent")
  (let [parents (->> uids
                     (mapv (comp first uid-and-embed-id))
                     (d/q '[:find ?parents
                            :in $ [?uids ...]
                            :where
                            [?e :block/uid ?uids]
                            [?parents :block/children ?e]]
                          db))]
    (= (count parents) 1)))


(defn- shape-parent-query
  "Normalize path from deeply nested block to root node."
  [pull-results]
  (->> (loop [b   pull-results
              res []]
         (if (:node/title b)
           (conj res b)
           (recur (first (:block/_children b))
                  (conj res (dissoc b :block/_children)))))
       rest
       reverse
       vec))


(defn get-block-document
  [db id]
  (->> (d/pull db block-document-pull-vector id)
       sort-block-children))


(def block-document-pull-vector-for-copy
  '[:block/uid :block/string :block/open :block/order {:block/children ...}])


(defn get-block-document-for-copy
  [db eid]
  (->> (d/pull db block-document-pull-vector-for-copy eid)
       sort-block-children))


(defn get-parents-recursively
  [db eid]
  (->> (d/pull db '[:db/id :node/title :block/uid :block/string {:block/_children ...}] eid)
       shape-parent-query))


(defn merge-parents-and-block
  [db ref-ids]
  (let [parents (reduce-kv (fn [m _ v] (assoc m v (get-parents-recursively db v)))
                           {}
                           ref-ids)
        blocks (map (fn [id] (get-block-document db id)) ref-ids)]
    (mapv
      (fn [block]
        (merge block {:block/parents (get parents (:db/id block))}))
      blocks)))


(defn group-by-parent
  [blocks]
  (group-by (fn [x]
              (-> x
                  :block/parents
                  first
                  :node/title))
            blocks))


(defn get-linked-refs-by-page-title
  [db page-title]
  (->> (d/pull db '[* :block/_refs] [:node/title page-title])
       :block/_refs
       (mapv :db/id)
       (mapv #(d/pull db '[:db/id :node/title :block/uid :block/string] %))))


(defn get-data
  [db pattern]
  (->> pattern
       (get-ref-ids db)
       (merge-parents-and-block db)
       group-by-parent
       seq))


(defn get-unlinked-references
  "For node-page references UI."
  [db title]
  (->> title
       patterns/unlinked
       (get-data db)))


(defn get-all-pages
  [db]
  (->> (d/q '[:find [?e ...]
              :where
              [?e :node/title ?t]]
            db)
       (d/pull-many db '[* :block/_refs])))


(defn not-contains?
  [coll v]
  (not (contains? coll v)))


(defn get-children-not-in-selected-uids
  [db target-block-uid selected-uids]
  (d/q '[:find ?children-uid ?o
         :keys block/uid block/order
         :in $ % ?target-uid ?not-contains? ?source-uids
         :where
         (siblings ?target-uid ?children-e)
         [?children-e :block/uid ?children-uid]
         [(?not-contains? ?source-uids ?children-uid)]
         [?children-e :block/order ?o]]
       db
       rules
       target-block-uid
       not-contains? (set selected-uids)))


(defn- extract-tag-values
  "Extracts `tag` values from `children-fn` children with `extractor-fn` from parser AST."
  [ast tag-selector children-fn extractor-fn]
  (->> (tree-seq vector? children-fn ast)
       (filter vector?)
       (keep #(when (tag-selector (first %))
                (extractor-fn %)))
       set))


(defn strip-markup
  "Remove `start` and `end` from s if present.
   Returns nil if markup was not present."
  [s start end]
  (when (and (string/starts-with? s start)
             (string/ends-with? s end))
    (subs s (count start) (- (count s) (count end)))))


(defn string->lookup-refs
  "Given string s, compute the set of refs expressed as Datalog lookup refs."
  [s]
  (let [ast (parser/parse-to-ast s)
        block-ref-str->uid #(strip-markup % "((" "))")
        page-ref-str->title #(or (strip-markup % "#[[" "]]")
                                 (strip-markup % "[[" "]]")
                                 (strip-markup % "#" ""))
        block-lookups (into #{}
                            (map (fn [uid] [:block/uid uid]))
                            (extract-tag-values ast #{:block-ref} identity #(-> % second :from block-ref-str->uid)))
        page-lookups (into #{}
                           (map (fn [title] [:node/title title]))
                           (extract-tag-values ast #{:page-link :hashtag} identity #(-> % second :from page-ref-str->title)))]
    (set/union block-lookups page-lookups)))


(defn eid->lookup-ref
  "Return the :block/uid based lookup ref for entity eid in db.
   eid can be either an entity id or a lookup ref.
   Returns nil if there's no entity, or if entity does not have :block/uid."
  [db eid]
  (-> (d/entity db eid)
      (select-keys [:block/uid])
      seq
      first))


(defn update-refs-tx
  "Return the tx that will update lookup ref's :block/refs from before to after.
   Both before and after should be sets of lookup refs."
  [lookup-ref before after]
  (let [[only-before only-after] (data/diff before after)
        to-tx (fn [type ref] [type lookup-ref :block/refs ref])]
    (set (concat (map (partial to-tx :db/retract) only-before)
                 (map (partial to-tx :db/add) only-after)))))


(comment
  (string->lookup-refs "one [[two]] ((three)) #four #[[five [[six]]]]")
  (parser/parse-to-ast "one [[two]] ((three)) #four #[[five [[six]]]]")
  (update-refs-tx [:block/uid "one"] #{[:node/title "foo"]} #{[:block/uid "bar"] [:node/title "baz"]}))


(defn block-refs-as-lookup-refs
  [db eid-or-lookup-ref]
  (when-some [ent (d/entity db eid-or-lookup-ref)]
    (into #{} (comp (mapcat second)
                    (map :db/id)
                    (map (partial eid->lookup-ref db)))
          (d/pull db '[:block/refs] (:db/id ent)))))


(defn string-as-lookup-refs
  [db string]
  (into #{} (comp (mapcat string->lookup-refs)
                  (map (partial eid->lookup-ref db))
                  (remove nil?))
        [string]))


(defn- parseable-string-datom
  [[eid attr value]]
  (when (#{:block/string :node/title} attr)
    [eid value]))


(defn linkmaker-error-handler
  [e input-tx]
  #?(:cljs (js/console.error "Linkmaker failure." e)
     :clj  (log/error "Linkmaker failure." e))
  ;; Return the original, un-modified, input tx so that transactions can still move forward.
  ;; We can always run linkmaker again later over all strings if we think the db is not correctly linked.
  ;; TODO(reporting): report the error type, without any identifiable information.
  input-tx)


(defn update-refs
  "Returns updated refs for eid.
   Returns nil if eid is no longer in db."
  [db [eid string]]
  (when-let [lookup-ref (eid->lookup-ref db eid)]
    (let [before     (block-refs-as-lookup-refs db lookup-ref)
          after      (string-as-lookup-refs db string)]
      (update-refs-tx lookup-ref before after))))


(defn linkmaker
  "Maintains the linked nature of Knowledge Graph.

  Returns Datascript transactions to be transacted in order to maintain links.

  Arguments:
  - `db`: Current Datascript/Datahike DB value
  - `input-tx` (optional): Graph structure modifying TX, analyzed for link updates

  If `input-tx` is provided, linkmaker will only update links related to that tx.
  If `input-tx` is not provided, all links in the db are checked for updates.

  Named after [Keymaker](https://en.wikipedia.org/wiki/Keymaker). "

  ([db]
   (try
     (let [linkmaker-txs (into []
                               (comp (keep parseable-string-datom)
                                     (mapcat (partial update-refs db)))
                               (d/datoms db :eavt))]
       #_(println "linkmaker:"
               "\nall:" (with-out-str (clojure.pprint/pprint (d/datoms db :eavt)))
               "\nlinkmaker-txs:" (with-out-str (clojure.pprint/pprint linkmaker-txs)))
       linkmaker-txs)
     (catch #?(:cljs :default
               :clj Exception) e
       (linkmaker-error-handler e []))))

  ([db input-tx]
   (try
     (let [{:keys [db-after
                   tx-data]}  (d/with db input-tx)
           linkmaker-txs      (into []
                                    (comp (keep parseable-string-datom)
                                          ;; Use db-after for the before-refs to ensure retracted
                                          ;; entities are already removed, otherwise we get
                                          ;; entity-missing errors from trying to retract refs
                                          ;; with lookup-refs to missing entities.
                                          (mapcat (partial update-refs db-after)))
                                    tx-data)
           with-linkmaker-txs (into (vec input-tx) linkmaker-txs)]
       #_(println "linkmaker:"
                "\ninput-tx:" (with-out-str (pprint/pprint input-tx))
                "\ntx-data:" (with-out-str (pprint/pprint tx-data))
                "\nlinkmaker-txs:" (with-out-str (pprint/pprint linkmaker-txs))
                "\nwith-linkmaker-txs:" (with-out-str (pprint/pprint with-linkmaker-txs)))
       with-linkmaker-txs)
     (catch #?(:cljs :default
               :clj Exception) e
       (linkmaker-error-handler e [])))))


(defn fix-block-order
  [{:block/keys [children]}]
  (let [sorted-kids  (->> children
                          (sort-by #(vector (:block/order %)
                                            (:block/uid %))))
        indexed-kids (map-indexed vector sorted-kids)
        block-fixes  (keep (fn [[idx {:block/keys [uid order]}]]
                             (when-not (= idx order)
                               {:block/uid   uid
                                :block/order idx}))
                           indexed-kids)]
    #_(println "indexed-kids:" (with-out-str
                               (pprint/pprint indexed-kids))
             "\nblock-fixes:" (with-out-str
                                (pprint/pprint block-fixes)))
    (when-not (empty? block-fixes)
      #?(:cljs (js/console.error "Needed to fix block-order" (with-out-str
                                                               (pprint/pprint block-fixes)))
         :clj (log/error "Needed to fix block-order" (with-out-str
                                                       (pprint/pprint block-fixes)))))
    block-fixes))


(defn keep-block-order
  "Checks for `:block/order` violations and generates fixing TXs.

  Arguments: whatever it takes"
  [{:keys [db-before db-after tx-data]}]
  (let [mod-eids       (->> tx-data
                            (keep (fn [[eid attr]]
                                    (when (= :block/order attr)
                                      eid)))
                            set)
        old-parents    (->> mod-eids
                            (map #(get-parent-eid db-before %))
                            set)
        new-parents    (->> mod-eids
                            (map #(get-parent-eid db-after %))
                            set)
        both-parents   (set/union old-parents new-parents)
        parents-blocks (->> both-parents
                            (remove nil?)
                            (map #(get-block db-after %)))
        new-violations (doall
                         (remove empty?
                                 (mapcat fix-block-order parents-blocks)))]
    #_(println "keep-block-order:"
             "\ntx-data:" (with-out-str
                            (pprint/pprint tx-data))
             "\nmod-eids:" (pr-str mod-eids)
             "\nold-parents:" (pr-str old-parents)
             "\nnew-parents:" (pr-str new-parents)
             "\nparents-blocks:\n" (with-out-str
                                     (pprint/pprint parents-blocks))
             "\nnew-violations:" (pr-str new-violations))
    new-violations))


(defn orderkeeper-error
  [ex input-tx]
  (println "orderkeeper, error" (pr-str ex)
           "\ninput-tx:" (with-out-str
                           (pprint/pprint input-tx))))


(defn orderkeeper
  "Maintains the order in Knowledge Graph.

  Returns Datascript transactions to be transacted in order to maintain order.

  Arguments:
  - `db`: Current Datascript/Datahike DB value
  - `input-tx`: (optional): Graph structure modifying TX, analyzed for `:block/order` mistakes

  If `input-tx` is provided, orderkeeper will only update `:block/order` related to that TX.
  If `input-tx` is not provided, all `:block/order` will be checked."
  ([db]
   (try
     (let [orderkeeper-txs (into []
                                 (keep-block-order {:db-before []
                                                    :db-after  db
                                                    :tx-data   (d/datoms db :eavd)}))]
       orderkeeper-txs)
     (catch #?(:cljs :default
               :clj Exception) e
       (orderkeeper-error e [])
       [])))

  ([db input-tx]
   (try
     (let [tx-report            (d/with db input-tx)
           orderkeeper-txs      (into []
                                      (keep-block-order tx-report))
           with-orderkeeper-txs (into (vec input-tx) orderkeeper-txs)]
       with-orderkeeper-txs)
     (catch #?(:cljs :default
               :clj Exception) e
       (orderkeeper-error e input-tx)
       input-tx))))
