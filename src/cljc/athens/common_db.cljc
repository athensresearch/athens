(ns athens.common-db
  "Common DB (Datalog) access layer.
  So we execute same code in CLJ & CLJS."
  (:require
    [athens.patterns               :as patterns]
    [clojure.string                :as string]
    #?(:clj [clojure.tools.logging :as log])
    #?(:clj  [datahike.api         :as d]
       :cljs [datascript.core      :as d])))


(defn e-by-av
  [db a v]
  (-> (d/datoms db :avet a v)
      first
      :e))


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


(defn linkmaker
  "Maintains linked nature of Knowledge Graph.

  Returns Datascript transactions to be transacted in order to maintain links.

  Arguments:
  - `db`: Current Datascript/Datahike DB value
  - `input-tx`: Grapth structure modifying TX, analyzed for link updates

  Named after [Keymaker](https://en.wikipedia.org/wiki/Keymaker). "
  [db input-tx]
  (try
    (let [tx-report (d/with db input-tx)
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
          ])
    (catch #?(:cljs js/Error
              :clj Exception) e
      #?(:cljs (do
                 (js/alert (str "Software failure, sorry. Please let us know about it.\n"
                                (str e)))
                 (js/console.error "Linkmaker failure." e))
         :clj (log/error "Linkmaker failure." e)))))
