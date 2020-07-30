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
               :current-route       nil
               :loading?            true
               :alert               nil
               :athena/open         false
               :athena/recent-items '()
               :devtool/open        false
               :left-sidebar/open   true
               :right-sidebar/open  false
               :right-sidebar/items {}
               ;;:dragging-global     false
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
                    :db/valueType :db.type/ref}})


(def welcome-datoms
  [{:db/id -1
    :node/title "athens/Welcome"
    :block/uid "0"
    :block/children [{:block/uid "welcome"
                      :block/string "Welcome to Athens, Open-Source Networked Thought."
                      :block/order 0}
                     {:block/uid "features"
                      :block/string "Markup Features"
                      :block/open true
                      :block/order 1
                      :block/children [{:block/uid "bold"
                                        :block/order 0
                                        :block/string "cmd-b **bold text with double asterisks**"}
                                       {:block/uid "single-backticks"
                                        :block/order 1
                                        :block/string "`mono-spaced text with backticks`"}
                                       {:block/uid "links"
                                        :block/order 2
                                        :block/string "links with double brackets: [[athens/Welcome]]"}
                                       {:block/uid "nested-links"
                                        :block/order 2
                                        :block/string "links with double brackets: [[nested [[links]]]]"}
                                       {:block/uid "hashtags"
                                        :block/order 3
                                        :block/string "or hashtags: #athens/Welcome"}
                                       {:block/uid "long-hashtags"
                                        :block/order 4
                                        :block/string "can use `#[[]]` for multi-word tags: #[[Hello Athens]]"}
                                       {:block/uid "block-refs"
                                        :block/order 5
                                        :block/string "Can reference other blocks with `(())`: ((features))"}
                                       {:block/uid "todo"
                                        :block/order 6
                                        :block/string "{{[[TODO]]}} `cmd-enter` for a TODO checkbox"}
                                       {:block/uid "done"
                                        :block/order 7
                                        :block/string "{{[[DONE]]}} `cmd-enter` again for DONE"}
                                       {:block/uid "embeds"
                                        :block/order 8
                                        :block/string "embeds with `{{[[youtube: ]]}}` and `{{``iframe: }}`"
                                        :block/children [{:block/uid "youtube"
                                                          :block/order 0
                                                          :block/string "{{[[youtube]]: https://www.youtube.com/watch?v=dQw4w9WgXcQ}}"}
                                                         {:block/uid "iframe"
                                                          :block/order 1
                                                          :block/string "{{iframe: https://www.openstreetmap.org/export/embed.html?bbox=-0.004017949104309083%2C51.47612752641776%2C0.00030577182769775396%2C51.478569861898606&layer=mapnik}}"}]}
                                       {:block/uid "images"
                                        :block/order 9
                                        :block/string "images with `![]()` ![athens-splash](https://raw.githubusercontent.com/athensresearch/athens/master/doc/athens-puk-patrick-unsplash.jpg)"}]}]}])


(defonce dsdb (d/create-conn schema))


(d/transact! dsdb welcome-datoms)


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
    [(inc-after ?p ?at ?ch ?new-o)
     (after ?p ?at ?ch ?o)
     [(inc ?o) ?new-o]]
    [(dec-after ?p ?at ?ch ?new-o)
     (after ?p ?at ?ch ?o)
     [(dec ?o) ?new-o]]])


(defn sort-block-children
  [block]
  (if-let [children (seq (:block/children block))]
    (assoc block :block/children
           (vec (sort-by :block/order (map sort-block-children children))))
    block))


(defn get-block-document
  [id]
  (->> @(pull dsdb '[:db/id :block/uid :block/string :block/open :block/order {:block/children ...} :edit/time] id)
       sort-block-children))


(defn get-node-document
  [id]
  (->> @(pull dsdb '[:db/id :node/title :block/uid :block/string :block/open :block/order :page/sidebar {:block/children ...} :edit/time] id)
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
  @(pull dsdb '[:db/id :node/title :block/uid :block/order :block/string {:block/children [:block/uid :block/order]}] id))


(defn get-parent
  [id]
  (-> (d/entity @dsdb id)
      :block/_children
      first
      :db/id
      get-block))


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
  (->> @(pull dsdb '[:block/order :block/uid {:block/children ...}] (e-by-av :block/uid uid))
       (tree-seq :block/children :block/children)
       (map :block/uid)))


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
  [query]
  (d/q '[:find [(pull ?node [:db/id :node/title :block/uid]) ...]
         :in $ ?query-pattern ?query
         :where
         [?node :node/title ?title]
         [(re-find ?query-pattern ?title)]
         [(not= ?title ?query)]] ;; ignore exact match to avoid duplicate
       @dsdb
       (re-case-insensitive query)
       query))


(defn get-root-parent-node
  [block]
  (loop [b block]
    (if (:node/title b)
      (assoc block :block/parent b)
      (recur (first (:block/_children b))))))


(defn search-in-block-content
  [query]
  (->>
    (d/q '[:find [(pull ?block [:db/id :block/uid :block/string :node/title {:block/_children ...}]) ...]
           :in $ ?query-pattern
           :where
           [?block :block/string ?txt]
           [(re-find ?query-pattern ?txt)]]
         @dsdb
         (re-case-insensitive query))
    (map get-root-parent-node)
    (mapv #(dissoc % :block/_children))))


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

;; if child, go to child 0
;; else recursively find next sibling of parent
(defn next-block-uid
  [uid]
  (let [block (->> (get-block [:block/uid uid])
                   sort-block-children)
        ch (:block/children block)
        next-block-recursive (next-sibling-block-recursively uid)]
    (cond
      ch (:block/uid (first ch))
      next-block-recursive (:block/uid next-block-recursive))))

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
