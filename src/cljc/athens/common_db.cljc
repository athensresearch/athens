(ns athens.common-db
  "Common DB (Datalog) access layer.
  So we execute same code in CLJ & CLJS."
  (:require
    [athens.parser                 :as parser]
    [athens.patterns               :as patterns]
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
  (map (fn [{:block/keys [uid string]}]
         (let [new-str (string/replace string
                                       (patterns/linked old-title)
                                       (str "$1$3$4" new-title "$2$5"))]
           {:db/id        [:block/uid uid]
            :block/string new-str}))
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
       (merge-parents-and-block db)
       group-by-parent
       (sort-by :db/id)
       vec
       rseq))


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


(defn- extract-tag-values
  "Extracts `tag` values with `extractor-fn` from parser AST."
  [ast tag extractor-fn]
  (->> (tree-seq vector? extractor-fn ast)
       (filter vector?)
       (keep #(when (= tag (first %))
                (extractor-fn %)))
       set))


(defn- extract-page-links
  "Extracts from parser AST `:page-link`s"
  [ast]
  (extract-tag-values ast :page-link #(cond
                                        (and (vector? %)
                                             (< 2 (count %)))
                                        (nth % 2)
                                        (and (vector? %)
                                             (< 1 (count %)))
                                        (nth % 1)
                                        :else
                                        %)))


(defn linkmaker
  "Maintains linked nature of Knowledge Graph.

  Returns Datascript transactions to be transacted in order to maintain links.

  Arguments:
  - `db`: Current Datascript/Datahike DB value
  - `input-tx`: Grapth structure modifying TX, analyzed for link updates

  Named after [Keymaker](https://en.wikipedia.org/wiki/Keymaker). "
  [db input-tx]
  (try
    (let [{:keys [db-before
                  db-after
                  tx-data
                  tempids]
           :as   tx-report} (d/with db input-tx)
          ;; requirements:
          ;; *p1*: page created -> check if something refers to it, update refs
          ;; *p2*: page deleted -> do we need to update `:block/refs`, since we're deleting page entity, probably not
          ;;                       also check *b6* for all child blocks
          ;; *p3*: page rename -> find references to old page title, update blocks with new title, update refs
          ;;                      also check if something refers to new title already, update refs
          ;; *b1*: block has new page ref -> update page refs
          ;; *b2*: block doesn't have page ref anymore -> update page refs
          ;; *b3*: block has new block ref -> update target block refs
          ;; *b4*: block doesn't have block ref anymore -> update target block refs
          ;; *b5*: block created -> check *b1* & *b3*
          ;; *b6*: block deleted -> check *b2* & *b4*

          ;; *b1*
          block-eid->new-strings    (->> tx-data
                                         (filter (fn [[_eid attr _value _tx added?]]
                                                   (and added?
                                                        (= :block/string attr))))
                                         (reduce (fn [acc [eid _attr value _tx _added?]]
                                                   (assoc acc eid value))
                                                 {}))
          block-eid->old-strings    (->> tx-data
                                         (filter (fn [[_eid attr _value _tx added?]]
                                                   (and (not added?)
                                                        (= :block/string attr))))
                                         (reduce (fn [acc [eid _attr value _tx _added?]]
                                                   (assoc acc eid value))
                                                 {}))
          block-eid->old-strings    (merge block-eid->old-strings
                                           (->> (keys block-eid->new-strings)
                                                (map (fn [block-eid]
                                                       (when-let [block-string (v-by-ea db-before block-eid :block/string)]
                                                         [block-eid block-string])))
                                                (into {})))
          block-eid->new-structure  (zipmap (keys block-eid->new-strings)
                                            (map parser/parse-to-ast
                                                 (vals block-eid->new-strings)))
          block-eid->old-structure  (zipmap (keys block-eid->old-strings)
                                            (map parser/parse-to-ast
                                                 (vals block-eid->old-strings)))
          block-eid->new-page-links (zipmap (keys block-eid->new-structure)
                                            (map extract-page-links
                                                 (vals block-eid->new-structure)))
          block-eid->old-page-links (zipmap (keys block-eid->old-structure)
                                            (map extract-page-links
                                                 (vals block-eid->old-structure)))
          block-eid->page-remove    (->> (for [[block-eid old-pages] block-eid->old-page-links
                                               :let                  [new-pages (get block-eid->new-page-links
                                                                                     block-eid
                                                                                     #{})]]
                                           [block-eid (set/difference old-pages new-pages)])
                                         (remove (comp empty? second))
                                         (into {}))
          block-eid->page-add       (->> (for [[block-eid new-pages] block-eid->new-page-links
                                               :let                  [old-pages (get block-eid->old-page-links
                                                                                     block-eid
                                                                                     #{})]]
                                           [block-eid (set/difference new-pages old-pages)])
                                         (remove (comp empty? second))
                                         (into {}))
          linkmaker-info            (merge {}
                                           (when (seq block-eid->page-remove)
                                             {:retracts (mapcat (fn [[block-eid page-titles]]
                                                                  (for [page-title page-titles]
                                                                    [:db/retract
                                                                     block-eid
                                                                     :block/refs
                                                                     [:node/title page-title]]))
                                                                block-eid->page-remove)})
                                           (when (seq block-eid->page-add)
                                             {:asserts (mapcat (fn [[block-eid page-titles]]
                                                                 (for [page-title page-titles]
                                                                   [:db/add
                                                                    block-eid
                                                                    :block/refs
                                                                    [:node/title page-title]]))
                                                               block-eid->page-add)}))
          linkmaker-txs             (apply conj (into [] (:asserts linkmaker-info))
                                           (:retracts linkmaker-info))
          with-linkmaker-txs        (apply conj input-tx linkmaker-txs)]
      (println "linkmaker:"
               "\ntx-data:" (pr-str tx-data)
               "\nblock-eid->old-strings:" (pr-str block-eid->old-strings)
               "\nblock-eid->new-strings:" (pr-str block-eid->new-strings)
               "\nblock-eid->old-structure:" (pr-str block-eid->old-structure)
               "\nblock-eid->new-structure:" (pr-str block-eid->new-structure)
               "\nblock-eid->old-page-links:" (pr-str block-eid->old-page-links)
               "\nblock-eid->new-page-links:" (pr-str block-eid->new-page-links)
               "\nblock-eid->page-remote:" (pr-str block-eid->page-remove)
               "\nblock-eid->page-add:" (pr-str block-eid->page-add)
               "\nlinkmaker-info:" (pr-str linkmaker-info)
               "\nlinkmaker-txs:" (pr-str linkmaker-txs))
      with-linkmaker-txs)
    (catch #?(:cljs js/Error
              :clj Exception) e
      #?(:cljs (do
                 (js/alert (str "Software failure, sorry. Please let us know about it.\n"
                                (str e)))
                 (js/console.error "Linkmaker failure." e))
         :clj (log/error "Linkmaker failure." e)))))

