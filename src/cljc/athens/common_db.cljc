(ns athens.common-db
  "Common DB (Datalog) access layer.
  So we execute same code in CLJ & CLJS."
  (:require
    [athens.parser                 :as parser]
    [athens.patterns               :as patterns]
    [clojure.set                   :as set]
    [clojure.string                :as string]
    [clojure.data                  :as data]
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


(defn replace-linked-refs-tx
  "For a given title, unlinks [[brackets]], #[[brackets]], and #brackets."
  [db title]
  (let [pattern (patterns/linked title)]
    (->> pattern
         (get-ref-ids db)
         (d/pull-many db [:db/id :block/string])
         (mapv (fn [x]
                 (let [new-str (string/replace (:block/string x) pattern title)]
                   (assoc x :block/string new-str)))))))


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


(defn get-parent
  "Given `:db/id` find it's parent."
  [db eid]
  (->> (d/entity db eid)
       :block/_children
       first
       :db/id
       (get-block db)))


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


(defn get-page-document
  "Retrieves whole page 'document', meaning with children."
  [db eid]
  (-> db
      (d/pull node-document-pull-vector eid)
      sort-block-children))


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
  (update-refs-tx [:block/uid "one"] #{[:node/title "foo"]} #{[:block/uid "bar"] [:node/title "baz"]})
  )


(defn block-refs-as-lookup-refs [db eid-or-lookup-ref]
  (when-some [ent (d/entity db eid-or-lookup-ref)]
    (into #{} (comp (mapcat second)
                    (map :db/id)
                    (map (partial eid->lookup-ref db)))
          (d/pull db '[:block/refs] (:db/id ent)))))

(defn string-as-lookup-refs [db string]
  (into #{} (comp (mapcat string->lookup-refs)
                  (map (partial eid->lookup-ref db))
                  (remove nil?))
        [string]))


(defn- parseable-string-datom [[eid attr value]]
  (when (#{:block/string :node/title} attr)
    [eid value]))

(defn linkmaker-error-handler [e input-tx]
  #?(:cljs (do
             (js/alert (str "Software failure, sorry. Please let us know about it.\n"
                            (str e)))
             (js/console.error "Linkmaker failure." e))
     :clj (do (log/error "Linkmaker failure." e)))
  ;; Return the original, un-modified, input tx so that transactions can still move forward.
  ;; We can always run linkmaker again later over all strings if we think the db is not correctly linked.
  input-tx)

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
                                     (mapcat (fn [[eid string]]
                                               (let [lookup-ref (eid->lookup-ref db eid)
                                                     before     (block-refs-as-lookup-refs db lookup-ref)
                                                     after      (string-as-lookup-refs db string)]
                                                 (update-refs-tx lookup-ref before after)))))
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
     (let [{:keys [db-before
                   db-after
                   tx-data]}  (d/with db input-tx)
           linkmaker-txs      (into []
                                    (comp (keep parseable-string-datom)
                                          (mapcat (fn [[eid string]]
                                                    (let [lookup-ref (eid->lookup-ref db-after eid)
                                                          before     (block-refs-as-lookup-refs db-before lookup-ref)
                                                          after      (string-as-lookup-refs db-after string)]
                                                      (update-refs-tx lookup-ref before after)))))
                                    tx-data)
           with-linkmaker-txs (apply conj input-tx linkmaker-txs)]
       #_(println "linkmaker:"
                "\ntx-data:" (with-out-str (clojure.pprint/pprint tx-data))
                "\nlinkmaker-txs:" (with-out-str (clojure.pprint/pprint linkmaker-txs)))
       with-linkmaker-txs)
     (catch #?(:cljs :default
               :clj Exception) e
       (linkmaker-error-handler e [])))))

