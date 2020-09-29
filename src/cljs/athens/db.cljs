(ns athens.db
  (:require
    [athens.patterns :as patterns]
    [athens.util :refer [escape-str]]
    [clojure.edn :as edn]
    [datascript.core :as d]
    [posh.reagent :refer [posh! pull q]]))


;; -- Example Roam DBs ---------------------------------------------------

(def athens-url "https://raw.githubusercontent.com/athensresearch/athens/master/data/athens.datoms")
(def help-url   "https://raw.githubusercontent.com/athensresearch/athens/master/data/help.datoms")
(def ego-url    "https://raw.githubusercontent.com/athensresearch/athens/master/data/ego.datoms")


;; -- re-frame -----------------------------------------------------------

(defonce rfdb {:user                "Socrates"
               :db/filepath         nil
               :db/synced           true
               :current-route       nil
               :loading?            true
               :alert               nil
               :athena/open         false
               :athena/recent-items '()
               :devtool/open        false
               :left-sidebar/open   false
               :right-sidebar/open  false
               :right-sidebar/items {}
               ;;:dragging-global     false
               :mouse-down false
               :daily-notes/items   []
               :selected/items   []})


;; -- JSON Parsing ----------------------------------------------------

(def str-kw-mappings
  "Maps attributes from \"Export All as JSON\" to original datascript attributes."
  {"children" :block/children
   "create-email" :create/email
   "create-time" :create/time
   "edit-email" :edit/email
   "edit-time" :edit/time
   "email" :user/email
   "emoji" :ent/emoji
   "emojis" :ent/emojis
   "props" :block/props
   "string" :block/string
   "text-align" :block/text-align
   "time" nil
   "title" :node/title
   "uid" :block/uid
   "users" nil
   "heading" :block/heading})


(defn convert-key
  [k]
  (get str-kw-mappings k k))


(defn parse-hms
  "Parses JSON retrieved from Roam's \"Export all as JSON\". Not fully functional."
  [hms]
  (if (not (coll? hms))
    hms
    (map #(reduce (fn [acc [k v]]
                    (assoc acc (convert-key k) (parse-hms v)))
                  {}
                  %)
         hms)))


(defn parse-tuples
  "Parse tuples exported via method specified in https://roamresearch.com/#/app/ego/page/eJ14YtH2G."
  [tuples]
  (->> tuples
       (partition 3)            ; chunk into 3-tuples
       rest                     ; drop first tuple which is (?e ?a ?v)
       (map #(map edn/read-string %))
       (map #(cons :db/add %))))


(defn json-str-to-edn
  "Convert a JSON str to EDN. May receive JSON through an HTTP request or file upload."
  [json-str]
  (->> json-str
       (js/JSON.parse)
       (js->clj)))


(defn str-to-db-tx
  "Deserializes a JSON string into EDN and then Datoms."
  [json-str]
  (let [edn-data (json-str-to-edn json-str)]
    (if (coll? (first edn-data))
      (parse-hms edn-data)
      (parse-tuples edn-data))))


;; -- Datascript and Posh ------------------------------------------------

(def schema
  {:block/uid      {:db/unique :db.unique/identity}
   :node/title     {:db/unique :db.unique/identity}
   :attrs/lookup   {:db/cardinality :db.cardinality/many}
   :block/children {:db/cardinality :db.cardinality/many
                    :db/valueType :db.type/ref}
   :block/refs     {:db/cardinality :db.cardinality/many
                    :db/valueType :db.type/ref}})


(defonce dsdb (d/create-conn schema))


;; todo: turn into an effect
(posh! dsdb)


(defn e-by-av
  [a v]
  (-> (d/datoms @dsdb :avet a v) first :e))


(defn v-by-ea
  [e a]
  (-> (d/datoms @dsdb :eavt e a) first :v))


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
  [eid order]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at
              :where (inc-after ?p ?at ?ch ?new-o)]
            @dsdb rules eid order)))


(defn dec-after
  [eid order]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at
              :where (dec-after ?p ?at ?ch ?new-o)]
            @dsdb rules eid order)))


(defn plus-after
  [eid order x]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at ?x
              :where (plus-after ?p ?at ?ch ?new-o ?x)]
            @dsdb rules eid order x)))


(defn minus-after
  [eid order x]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at ?x
              :where (minus-after ?p ?at ?ch ?new-o ?x)]
            @dsdb rules eid order x)))


(defn not-contains?
  [coll v]
  (not (contains? coll v)))


(defn last-child?
  [uid]
  (->> (d/q '[:find ?sib-uid ?sib-o
              :in $ % ?uid
              :where
              (siblings ?uid ?sib)
              [?sib :block/uid ?sib-uid]
              [?sib :block/order ?sib-o]]
            @dsdb rules uid)
       (sort-by second)
       last
       first
       (= uid)))


(defn sort-block-children
  [block]
  (if-let [children (seq (:block/children block))]
    (assoc block :block/children
           (vec (sort-by :block/order (map sort-block-children children))))
    block))


(def block-document-pull-vector
  '[:db/id :block/uid :block/string :block/open :block/order {:block/children ...} :block/_refs])


(def node-document-pull-vector
  (-> block-document-pull-vector
      (conj :node/title :page/sidebar)))


(defn get-block-document
  [id]
  (->> @(pull dsdb block-document-pull-vector id)
       sort-block-children))


(defn get-node-document
  [id]
  (->> @(pull dsdb node-document-pull-vector id)
       sort-block-children))


(defn shape-parent-query
  "Normalize path from deeply nested block to root node."
  [pull-results]
  (->> (loop [b   pull-results
              res []]
         (if (:node/title b)
           (conj res b)
           (recur (first (:block/_children b))
                  (conj res (dissoc b :block/_children)))))
       (rest)
       (reverse)
       vec))


(defn get-parents-recursively
  [id]
  (->> @(pull dsdb '[:db/id :node/title :block/uid :block/string {:block/_children ...}] id)
       shape-parent-query))


(defn get-block
  [id]
  @(pull dsdb '[:db/id :node/title :block/uid :block/order :block/string {:block/children [:block/uid :block/order]} :block/open] id))


(defn get-parent
  [id]
  (-> (d/entity @dsdb id)
      :block/_children
      first
      :db/id
      get-block))


(defn get-older-sib
  [uid]
  (let [sib-uid   (d/q '[:find ?uid .
                         :in $ % ?target-uid
                         :where
                         (siblings ?target-uid ?sib)
                         [?target-e :block/uid ?target-uid]
                         [?target-e :block/order ?target-o]
                         [(dec ?target-o) ?prev-sib-order]
                         [?sib :block/order ?prev-sib-order]
                         [?sib :block/uid ?uid]]
                       @dsdb rules uid)
        older-sib (get-block [:block/uid sib-uid])]
    older-sib))


(defn same-parent?
  "Given a coll of uids, determine if uids are all direct children of the same parent."
  [uids]
  (let [parents (d/q '[:find ?parents
                       :in $ [?uids ...]
                       :where
                       [?e :block/uid ?uids]
                       [?parents :block/children ?e]]
                     @dsdb uids)]
    (= (count parents) 1)))


(defn deepest-child-block
  [id]
  (let [document (->> @(pull dsdb '[:block/order :block/uid {:block/children ...}] id))]
    (loop [block document]
      (if (nil? (:block/children block))
        block
        (let [ch (:block/children block)
              n  (count ch)]
          (recur (get ch (dec n))))))))


(defn get-children-recursively
  "Get list of children UIDs for given block ID (including the root block's UID)"
  [uid]
  (->> (d/pull @dsdb '[:block/order :block/uid {:block/children ...}] (e-by-av :block/uid uid))
       (tree-seq :block/children :block/children)
       (map :block/uid)))


(defn retract-uid-recursively
  "Retract all blocks of a page, including the page."
  [uid]
  (mapv (fn [uid] [:db/retractEntity [:block/uid uid]])
        (get-children-recursively uid)))


(defn re-case-insensitive
  "More options here https://clojuredocs.org/clojure.core/re-pattern"
  [query]
  (re-pattern (str "(?i)" (escape-str query))))


(defn search-exact-node-title
  [query]
  (d/q '[:find (pull ?node [:db/id :node/title :block/uid]) .
         :in $ ?query
         :where [?node :node/title ?query]]
       @dsdb
       query))


(defn search-in-node-title
  ([query] (search-in-node-title query 20))
  ([query n]
   (->> (d/q '[:find [(pull ?node [:db/id :node/title :block/uid]) ...]
               :in $ ?query-pattern ?query
               :where
               [?node :node/title ?title]
               [(re-find ?query-pattern ?title)]
               [(not= ?title ?query)]] ;; ignore exact match to avoid duplicate
             @dsdb
             (re-case-insensitive query)
             query)
        (take n))))


(defn get-root-parent-node
  [block]
  (loop [b block]
    (if (:node/title b)
      (assoc block :block/parent b)
      (recur (first (:block/_children b))))))


(defn search-in-block-content
  ([query] (search-in-block-content query 20))
  ([query n]
   (->>
     (d/q '[:find [(pull ?block [:db/id :block/uid :block/string :node/title {:block/_children ...}]) ...]
            :in $ ?query-pattern
            :where
            [?block :block/string ?txt]
            [(re-find ?query-pattern ?txt)]]
          @dsdb
          (re-case-insensitive query))
     (take n)
     (map get-root-parent-node)
     (mapv #(dissoc % :block/_children)))))


(defn get-block-refs
  [uid]
  (d/q '[:find [?refs ...]
         :in $ ?uid
         :where
         [?e :block/uid ?uid]
         [?e :block/refs ?refs]]
       @dsdb
       uid))

;; xxx 2 kinds of operations
;; write operations, it's nice to have entire block and entire parent block to make TXes
;; read operations (navigation), only need uids

;; xxx these all assume all blocks are open. have to skip closed blocks
;; TODO: focus AND set selection-start for :editing/uid

(defn prev-sibling-uid
  [uid]
  (d/q '[:find ?sib-uid .
         :in $ ?block-uid
         :where
         [?block :block/uid ?block-uid]
         [?block :block/order ?block-o]
         [?parent :block/children ?block]
         [?parent :block/children ?sib]
         [?sib :block/order ?sib-o]
         [?sib :block/uid ?sib-uid]
         [(dec ?block-o) ?prev-sib-o]
         [(= ?sib-o ?prev-sib-o)]]
       @dsdb uid))

;; if order 0, go to parent
;; if order n, go to prev siblings deepest child
(defn prev-block-uid
  [uid]
  (let [block (get-block [:block/uid uid])
        parent (get-parent [:block/uid uid])
        deepest-child-prev-sibling (deepest-child-block [:block/uid (prev-sibling-uid uid)])]
    (if (zero? (:block/order block))
      (:block/uid parent)
      (:block/uid deepest-child-prev-sibling))))


(defn next-sibling-block
  [uid]
  (d/q '[:find (pull ?sib [*]) .
         :in $ ?block-uid
         :where
         [?block :block/uid ?block-uid]
         [?block :block/order ?block-o]
         [?parent :block/children ?block]
         [?parent :block/children ?sib]
         [?sib :block/order ?sib-o]
         [?sib :block/uid ?sib-uid]
         [(inc ?block-o) ?prev-sib-o]
         [(= ?sib-o ?prev-sib-o)]]
       @dsdb uid))


(defn next-sibling-block-recursively
  [uid]
  (loop [uid uid]
    (let [sib (next-sibling-block uid)
          parent (get-parent [:block/uid uid])]
      (if (or sib (:node/title parent))
        sib
        (recur (:block/uid parent))))))


(defn next-block-uid
  "1-arity:
    if child, go to child 0
    else recursively find next sibling of parent
  2-arity:
    used for multi-block-selection; ignores child blocks"
  ([uid]
   (let [block                (->> (get-block [:block/uid uid])
                                   sort-block-children)
         ch                   (:block/children block)
         next-block-recursive (next-sibling-block-recursively uid)]
     (cond
       ch (:block/uid (first ch))
       next-block-recursive (:block/uid next-block-recursive))))
  ([uid selection?]
   (if selection?
     (let [next-block-recursive (next-sibling-block-recursively uid)]
       next-block-recursive (:block/uid next-block-recursive))
     (next-block-uid uid))))

;; history

(defonce history (atom []))
(def ^:const history-limit 10)


(defn drop-tail
  [xs pred]
  (loop [acc []
         xs  xs]
    (let [x (first xs)]
      (cond
        (nil? x) acc
        (pred x) (conj acc x)
        :else  (recur (conj acc x) (next xs))))))


(defn trim-head
  [xs n]
  (vec (drop (- (count xs) n) xs)))


(defn find-prev
  [xs pred]
  (last (take-while #(not (pred %)) xs)))


(defn find-next
  [xs pred]
  (fnext (drop-while #(not (pred %)) xs)))


(d/listen! dsdb :history
           (fn [tx-report]
             (let [{:keys [db-before db-after]} tx-report]
               (when (and db-before db-after)
                 (swap! history (fn [h]
                                  (-> h
                                      (drop-tail #(identical? % db-before))
                                      (conj db-after)
                                      (trim-head history-limit))))))))

;; -- Linked & Unlinked References ----------

(defn get-ref-ids
  [pattern]
  @(q '[:find [?e ...]
        :in $ ?regex
        :where
        [?e :block/string ?s]
        [(re-find ?regex ?s)]]
      dsdb
      pattern))


(defn merge-parents-and-block
  [ref-ids]
  (let [parents (reduce-kv (fn [m _ v] (assoc m v (get-parents-recursively v)))
                           {}
                           ref-ids)
        blocks (map (fn [id] (get-block-document id)) ref-ids)]
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


(defn get-data
  [pattern]
  (-> pattern get-ref-ids merge-parents-and-block group-by-parent seq))


(defn get-data-by-block
  [pattern]
  (-> pattern get-ref-ids merge-parents-and-block seq))


(defn get-linked-references
  [title]
  (-> title patterns/linked get-data))


(defn get-linked-references-by-block
  [title]
  (-> title patterns/linked get-data-by-block))


(defn get-unlinked-references
  [title]
  (-> title patterns/unlinked get-data))


(defn count-linked-references-excl-uid
  [title uid]
  (->> (get-linked-references-by-block title)
       (remove #(= (:block/uid %) uid))
       count))
