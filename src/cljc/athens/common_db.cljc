(ns athens.common-db
  "Common DB (Datalog) access layer.
  So we execute same code in CLJ & CLJS."
  (:refer-clojure :exclude [descendants])
  (:require
    [athens.common.logging    :as log]
    [athens.common.migrations :as migrations]
    [athens.parser            :as parser]
    [clojure.data             :as data]
    [clojure.pprint           :as pp]
    [clojure.set              :as set]
    [clojure.string           :as string]
    [clojure.walk             :as walk]
    [datascript.core          :as d])
  #?(:cljs
     (:require-macros
       [athens.common.sentry :as sentry-m :refer [wrap-span wrap-span-no-new-tx]])))


(def v1-schema
  {:schema/version      {}
   :block/uid           {:db/unique :db.unique/identity}
   :node/title          {:db/unique :db.unique/identity}
   :attrs/lookup        {:db/cardinality :db.cardinality/many}
   :block/children      {:db/cardinality :db.cardinality/many
                         :db/valueType   :db.type/ref}
   :block/refs          {:db/cardinality :db.cardinality/many
                         :db/valueType   :db.type/ref}
   ;; TODO: do we really still use it?
   :block/remote-id     {:db/unique :db.unique/identity}})


(def v2-schema
  {;; It would be nicer to have the reverse relationship here (:block/properties as a set
   ;; of children refs, instead :block/property-of as a single parent ref), but that doesn't
   ;; work with the tupleAttr below.
   :block/property-of     {:db/cardinality :db.cardinality/one
                           :db/valueType   :db.type/ref}
   ;; key is to a property what order is to a child
   :block/key             {:db/cardinality :db.cardinality/one
                           :db/valueType   :db.type/ref}
   ;; tupleAttrs are used here to ensure the relationship is unique,
   ;; that there are no repeat keys in a block.
   ;; https://github.com/tonsky/datascript/blob/master/docs/tuples.md
   ;; It could later be extended to also ensure order is unique.
   :block/property-of+key {:db/tupleAttrs [:block/property-of :block/key]
                           :db/unique     :db.unique/identity}})


(def v3-schema
  {;; Time is a unique number timestamp.
   :time/ts      {:db/unique :db.unique/identity}
   ;; Presence ids are unique strings, matching the presence name.
   :presence/id  {:db/unique :db.unique/identity}
   ;; Events have uid, and reference time, and auth.
   :event/uid    {:db/unique :db.unique/identity}
   :event/time   {:db/cardinality :db.cardinality/one
                  :db/valueType   :db.type/ref}
   :event/auth   {:db/cardinality :db.cardinality/one
                  :db/valueType   :db.type/ref}
   ;; Blocks reference events for creation and edits.
   :block/create {:db/cardinality :db.cardinality/one
                  :db/valueType   :db.type/ref}
   :block/edits  {:db/cardinality :db.cardinality/many
                  :db/valueType   :db.type/ref}})


(defn migrate-v2-time-to-v3-time
  [conn]
  (->> (d/datoms @conn :eavt)
       (filter (comp #{:create/time :edit/time} second))
       (mapcat (fn [[e a v]]
                 [[:db/retract e a]
                  {:db/id e
                   (if (= a :create/time)
                     :block/create
                     :block/edits)
                   {:event/time {:time/ts v}}}]))
       (d/transact! conn)))


(def v1-bootstrap-schema
  {:migrations/version {:db/unique :db.unique/identity}})


(defn db-versions
  [db]
  (->> (d/datoms db :aevt :migrations/version)
       (mapv #(nth % 2))
       (concat [0])
       set))


(defn version
  [conn]
  (apply max (db-versions @conn)))


(defn set-version!
  [conn version]
  (d/transact! conn [{:migrations/version version}]))


(defn transact-schema!
  [conn schema]
  (let [current-schema (d/schema @conn)
        merged-schema  (merge current-schema schema)]
    ;; NB: there is no way to update the schema of an existing conn.
    ;; This returns a new conn, any watchers will have to be added again.
    (reset! conn (-> (d/datoms @conn :eavt)
                     (d/conn-from-datoms merged-schema)
                     deref))))


(def bootstrap-migrations
  [[1 #(when (= (version %) 0)
         (transact-schema! % v1-bootstrap-schema))]])


(def migrations
  [[1 #(transact-schema! % v1-schema)]
   [2 #(transact-schema! % v2-schema)]
   [3 (fn [conn]
        (transact-schema! conn v3-schema)
        (migrate-v2-time-to-v3-time conn))]])


(defn migrate-conn!
  [conn & {:as opts}]
  (migrations/migrate! conn migrations bootstrap-migrations version set-version! opts)
  conn)


(defn create-conn
  []
  (migrate-conn! (d/create-conn)))


(defn reset-conn!
  [conn db]
  (if (= (version conn) (version (atom db)))
    (d/reset-conn! conn db)
    ;; Migrate db before resetting conn to it dbs content.
    (->> (d/conn-from-db db)
         migrate-conn!
         deref
         (d/reset-conn! conn))))


(def empty-db
  (d/db (create-conn)))


(defn e-by-av
  [db a v]
  (-> (d/datoms db :avet a v)
      first
      :e))


(defn v-by-ea
  [db e a]
  (get (d/entity db e) a))


(defn get-sidebar-elements
  [db]
  (->> (d/q '[:find [(pull ?e [*]) ...]
              :where
              [?e :page/sidebar _]]
            db)
       (sort-by :page/sidebar)))


(defn get-sidebar-count
  [db]
  (-> (get-sidebar-elements db)
      count))


(defn get-shortcut-neighbors
  "Get the neighbors for a given shortcut page, as :before and :after keys.
  Return nil values if there is no neighbor before or after."
  [db title]
  (let [sidebar-items  (get-sidebar-elements db)
        sidebar-titles (mapv :node/title sidebar-items)
        idx            (.indexOf sidebar-titles title)
        neighbors      {:before (get sidebar-titles (dec idx))
                        :after  (get sidebar-titles (inc idx))}]
    neighbors))


(defn flip-neighbor-position
  "Flips neighbor position to undo a remove.

  --Setup--
  Page 1 <- remove shortcut
  Page 2 <- :after

  --After Remove--
  Page 2 <-

  --Undo--
  Page 1 <- restore shortcut (new)
  Page 2 <- :before"
  [{:keys [before after] :as _neighbors}]
  (cond
    after {:relation :before
           :page/title after}
    before {:relation :after
            :page/title before}))


(defn get-sidebar-titles
  [db]
  (->> (get-sidebar-elements db)
       (mapv :node/title)))


(defn find-title-from-order
  [db order]
  (->> (get-sidebar-elements db)
       (filter (fn [el]
                 (= (:page/sidebar el)
                    order)))
       (first)
       (:node/title)))


(defn find-source-target-title
  [db source-order target-order]
  (let [source-title (find-title-from-order db source-order)
        target-title (find-title-from-order db target-order)]
    [source-title target-title]))


(defn find-order-from-title
  [db title]
  (->> (get-sidebar-elements db)
       (filter (fn [el]
                 (= (:node/title el)
                    title)))
       (first)
       (:page/sidebar)))


(defn sort-block-children
  [block]
  (if-let [children (seq (:block/children block))]
    (assoc block :block/children
           (vec (sort-by :block/order (map sort-block-children children))))
    block))


(defn sort-block-properties
  [properties]
  (->> properties
       (sort-by (comp str first))
       (map second)))


(defn add-property-map
  [block]
  (let [block'     (if (:block/children block)
                     (update block :block/children (partial mapv add-property-map))
                     block)
        properties (-> block' :block/_property-of seq)]
    (if properties
      (assoc block' :block/properties
             (->> properties
                  (map (fn [block]
                         [(-> block :block/key :node/title) (add-property-map block)]))
                  (into {})))
      block')))


(defn get-block
  "Fetches whole block based on `:db/id`."
  [db eid]
  (when (d/entity db eid)
    (-> (d/pull db '[:db/id
                     :node/title
                     :block/uid
                     :block/order
                     :block/string
                     :block/open
                     :block/refs
                     :block/_refs
                     {:block/key [:node/title]}
                     {:block/_property-of [:block/uid
                                           {:block/key [:node/title]}]}
                     {:block/children [:block/uid
                                       :block/order]}]
                eid)
        sort-block-children
        add-property-map)))


(defn get-page
  "Fetches whole page based on `:db/id`."
  [db eid]
  (when (d/entity db eid)
    (-> (d/pull db '[:db/id
                     :node/title
                     :block/uid
                     :page/sidebar
                     :block/refs
                     :block/_refs
                     :block/_key
                     {:block/_property-of [:block/uid
                                           {:block/key [:node/title]}]}
                     {:block/children [:block/uid
                                       :block/order]}]
                eid)
        sort-block-children
        add-property-map)))


(defn block-exists?
  [db eid]
  (d/entity db eid))


(defn child-of-or-property-of
  [e]
  (if-let [p (:block/_children e)]
    (first p)
    (:block/property-of e)))


(defn get-parent-eid
  "Find parent's eids of given `eid`."
  [db eid]
  (-> (d/entity db eid)
      child-of-or-property-of
      (select-keys [:block/uid])
      seq
      first))


(defn get-parent
  "Given `:db/id` find it's parent."
  [db eid]
  (get-block db (get-parent-eid db eid)))


(defn get-parent-eids
  "Return parent parent eids for `eid`."
  [db eid]
  (loop [block eid
         acc []]
    (if-let [parent (get-parent-eid db block)]
      (recur parent (conj acc parent))
      acc)))


(defn get-children-uids
  "Fetches page or block sorted children uids based on eid lookup."
  [db eid]
  (when (d/entity db eid)
    (->> (d/pull db '[{:block/children [:block/uid
                                        :block/order]}]
                 eid)
         :block/children
         (sort-by :block/order)
         (mapv :block/uid))))


(defn get-property-uids
  "Fetches page or block sorted property uids based on eid lookup."
  [db eid]
  (when (d/entity db eid)
    (->> (d/pull db '[{:block/_property-of [:block/uid
                                            {:block/key [:node/title]}]}]
                 eid)
         add-property-map
         :block/properties
         sort-block-properties
         (mapv :block/uid))))


(defn sorted-prop+children-uids
  [db eid]
  (into (get-property-uids db eid)
        (get-children-uids db eid)))


(defn property-key
  [db eid]
  (->> (d/entity db eid)
       :block/key
       :node/title))


(def block-document-pull-vector
  '[:db/id :block/uid :block/string :block/open :block/order
    {:block/children ...} :block/refs :block/_refs
    {:block/key [:node/title]} {:block/_property-of ...}
    {:block/create [{:event/time [:time/ts]}
                    {:event/auth [:presence/id]}]}
    {:block/edits [{:event/time [:time/ts]}
                   {:event/auth [:presence/id]}]}])


(def node-document-pull-vector
  (-> block-document-pull-vector
      (conj :node/title :page/sidebar :block/_key)))


(defn get-block-document
  "Fetches whole block and whole children"
  [db id]
  (->> (d/pull db block-document-pull-vector id)
       sort-block-children
       add-property-map))


(defn get-block-property-document
  "Fetches whole property document for block."
  [db id]
  (-> (d/pull db [:block/_property-of] id)
      (update :block/_property-of #(mapv (fn [{:db/keys [id]}] (get-block-document db id)) %))
      add-property-map
      :block/properties))


(declare get-page-title-from-eid)


(defn get-entity-type
  "Returns the value of eids `:entity/type` prop, if any."
  [db eid]
  (let [prop-entity-type (->> (d/entity db eid)
                              :block/_property-of
                              (some (fn [e]
                                      (when (-> e :block/key :node/title (= ":entity/type"))
                                        (:block/string e)))))
        page             (get-page-title-from-eid db eid)]
    (cond
      prop-entity-type prop-entity-type
      page "page"
      :else "block")))


(defn get-block-uid
  "Finds block `:block/uid` by eid."
  [db eid]
  (-> db
      (d/entity eid)
      :block/uid))


(defn get-page-uid
  "Finds page `:block/uid` by `page-title`."
  [db page-title]
  (-> db
      (d/entity [:node/title page-title])
      :block/uid))


(defn map-new-refs
  "Find and replace linked ref with new linked ref, based on title change."
  [linked-refs old-title new-title]
  (map (fn [{:block/keys [uid string] :node/keys [title]}]
         (let [[text kw]      (if title
                                [title :node/title]
                                [string :block/string])
               has-spaces?    (string/includes? new-title " ")
               old-wrapped    (str "[[" old-title "]]")
               new-wrapped    (str "[[" new-title "]]")
               old-naked-hash (str "#" old-title)
               new-naked-hash (if has-spaces?
                                (str "#[[" new-title "]]")
                                (str "#" new-title))
               new-str        (-> text
                                  (string/replace old-wrapped new-wrapped)
                                  (string/replace old-naked-hash new-naked-hash))]
           {:db/id [:block/uid uid]
            kw     new-str}))
       linked-refs))


(defn replace-linked-refs-tx
  "For a given block, unlinks [[brackets]], #[[brackets]], #brackets, or ((brackets))."
  [db refered-blocks]
  (let [refering-blocks-ids (sequence (comp (mapcat #(:block/_refs %))
                                            (map :db/id)
                                            (distinct))
                                      refered-blocks)
        refering-blocks     (d/pull-many db [:db/id
                                             :block/string
                                             :node/title]
                                         refering-blocks-ids)]
    (into []
          (map (fn [refering-block]
                 (let [updated-string-content (reduce (fn [content {:keys [block/string node/title]}]
                                                        (when content
                                                          (string/replace content
                                                                          (if title
                                                                            (str "[[" title "]]")
                                                                            (str "((" string "))"))
                                                                          (or title string))))
                                                      (:block/string refering-block)
                                                      refered-blocks)
                       updated-title-content  (reduce (fn [content {:keys [block/string node/title]}]
                                                        (when content
                                                          (string/replace content
                                                                          (if title
                                                                            (str "[[" title "]]")
                                                                            (str "((" string "))"))
                                                                          (or title string))))
                                                      (:node/title refering-block)
                                                      refered-blocks)]
                   (cond-> refering-block
                     (seq updated-string-content) (assoc :block/string updated-string-content)
                     (seq updated-title-content)  (assoc :node/title updated-title-content)))))
          refering-blocks)))


(defn get-page-document
  "Retrieves whole page 'document', meaning with children."
  [db eid]
  (when (d/entity db eid)
    (-> db
        (d/pull node-document-pull-vector eid)
        sort-block-children
        add-property-map)))


(defn uid-and-embed-id
  [uid]
  (or (some->> uid
               (re-find #"^(.+)-embed-(.+)")
               rest vec)
      [uid nil]))


(defn nth-child
  "Find child that has order n in parent."
  [db parent-uid order]
  (d/q '[:find (pull ?child [*]) .
         :in $ ?parent-uid ?order
         :where
         [?parent :block/uid ?parent-uid]
         [?parent :block/children ?child]
         [?child :block/order ?order]]
       db parent-uid order))


(defn get-page-title
  [db uid]
  (-> db
      (d/entity [:block/uid uid])
      :node/title))


(defn get-page-title-from-eid
  [db eid]
  (-> db
      (d/entity eid)
      :node/title))


(defn get-block-string
  [db uid]
  (-> db
      (d/entity [:block/uid uid])
      :block/string))


(defn same-parent?
  "Given a coll of uids, determine if uids are all direct children of the same parent."
  [db uids]
  #_(log/debug "same parent")
  (let [parents (->> uids
                     (mapv (comp first uid-and-embed-id))
                     (d/q '[:find ?parents
                            :in $ [?uids ...]
                            :where
                            [?e :block/uid ?uids]
                            [?parents :block/children ?e]]
                          db))]
    (= (count parents) 1)))


;; TODO: support removing two string refs from the same block.
(defn retract-uid-recursively-tx
  "Retract all blocks of a page, including the page.
  Replaces block string refs to removed entities by their ref text."
  [db event-ref uid]
  (let [block                 (get-block db [:block/uid uid])
        parent                (->> [:block/uid uid] (get-parent db))
        descendants           (concat [] (:block/children block) (:block/_property-of block))
        has-descendants?      (seq descendants)
        descendants-uids      (when has-descendants?
                                (loop [acc        []
                                       to-look-at descendants]
                                  (if-let [look-at (first to-look-at)]
                                    (let [c-uid   (:block/uid look-at)
                                          c-block (get-block db [:block/uid c-uid])]
                                      (recur (conj acc c-uid)
                                             (concat (rest to-look-at)
                                                     (:block/children c-block)
                                                     (:block/_property-of c-block))))
                                    acc)))
        all-uids-to-remove    (conj (set descendants-uids) uid)
        uid->refs             (->> all-uids-to-remove
                                   (map (fn [uid]
                                          (let [block    (get-block db [:block/uid uid])
                                                rev-refs (set (:block/_refs block))]
                                            (when-not (empty? rev-refs)
                                              [uid (set rev-refs)]))))
                                   (remove nil?)
                                   (into {}))
        ref-eids              (mapcat second uid->refs)
        eids->uids            (->> ref-eids
                                   (map (fn [{id :db/id}]
                                          [id (v-by-ea db id :block/uid)]))
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
                                             (let [removed-string (v-by-ea db [:block/uid removed-uid] :block/string)
                                                   from-string    (str "((" removed-uid "))")]
                                               (map (fn [uid]
                                                      (if-not removed-string
                                                        {:block/uid uid}
                                                        (let [string (get-block-string db uid)
                                                              title  (get-page-title db uid)]
                                                          (cond-> {:block/uid uid}
                                                            string (merge {:block/string (string/replace string from-string removed-string)})
                                                            title  (merge {:node/title (string/replace title from-string removed-string)})))))
                                                    referenced-uids)))))
        has-asserts?          (seq asserts)
        retract-kids          (mapv (fn [uid]
                                      [:db/retractEntity [:block/uid uid]])
                                    (reverse descendants-uids))
        edit-parent           (when parent
                                (merge {:block/edits event-ref}
                                       (if-let [title (:node/title parent)]
                                         {:node/title title}
                                         {:block/uid (:block/uid parent)})))
        retract-entity        [:db/retractEntity [:block/uid uid]]
        txs                   (cond-> []
                                has-descendants? (into retract-kids)
                                has-asserts?     (into asserts)
                                edit-parent      (conj edit-parent)
                                true             (conj retract-entity))]
    txs))


(defn dissoc-on-match
  [m [k f]]
  (if (f m)
    (dissoc m k)
    m))


(defn get-internal-representation
  "Returns internal representation for eid in db."
  [db eid]
  (when (d/entity db eid)
    (let [rename-ks          {:block/open :block/open?
                              :node/title :page/title}
          remove-ks          [:db/id :page/sidebar :block/order
                              :block/refs :block/_refs
                              :block/key :block/_key
                              :block/_property-of
                              :block/create :block/edits]
          remove-ks-on-match [[:block/open? :block/open?]
                              [:block/uid   :page/title]]]
      ;; NB: get-page-document retrieves all keys in get-block-document as well
      (->> (get-page-document db eid)
           (walk/postwalk-replace rename-ks)
           (walk/prewalk (fn [node]
                           (if (map? node)
                             (as-> node n
                                   (apply dissoc n remove-ks)
                                   (reduce dissoc-on-match n remove-ks-on-match))
                             node)))))))


(defn get-linked-refs-by-page-title
  [db page-title]
  (when (d/entity db [:node/title page-title])
    (->> (d/pull db '[* :block/_refs] [:node/title page-title])
         :block/_refs
         (mapv :db/id)
         (mapv #(d/pull db '[:db/id :node/title :block/uid :block/string] %)))))


(def all-pages-pull-vector
  [:block/uid :node/title
   {:block/edits [{:event/time [:time/ts]}]}
   {:block/create [{:event/time [:time/ts]}]}
   ;; Get all block refs, we need them to count totals.
   ;; Without specifying a limit pull will only return first 1000.
   ;; https://docs.datomic.com/on-prem/query/pull.html#limit-option
   [:block/_refs :limit nil]])


(defn get-all-pages
  [db]
  (->> (d/datoms db :aevt :node/title)
       (map first)
       (d/pull-many db all-pages-pull-vector)))


(defn compat-position
  "Build a position by coercing incompatible arguments into compatible ones.
  uid to a page will instead use that page's title.
  Integer relation will be converted to :first if 0, or :after (with matching uid) if not.
  Relative positions to properties will be converted to :first on the parent.
  Accepts the `{:block/uid <parent-uid> :relation <integer>}` old format based on order number.
  Output position will be athens.common-events.graph.schema/child-position for the first block,
  and athens.common-events.graph.schema/sibling-position for others.
  It's safe to use a position that does not need coercing of any arguments, like the output formats."
  [db {:keys [relation block/uid page/title] :as pos}]
  (let [[coerced-ref-uid
         coerced-relation] (cond
                             (integer? relation)
                             (if (= relation 0)
                               [nil :first]
                               (let [parent-uid (or uid (get-page-uid db title))
                                     prev-uid   (:block/uid (nth-child db parent-uid (dec relation)))]
                                 (if prev-uid
                                   [prev-uid :after]
                                   ;; Can't find the previous block, just put it on last.
                                   [nil :last])))
                             (and uid
                                  (#{:before :after} relation)
                                  (property-key db [:block/uid uid]))
                             [(second (get-parent-eid db [:block/uid uid])) :first])
        coerced-title      (when (not title)
                             (get-page-title db (or coerced-ref-uid uid)))
        new-pos            (when (or coerced-ref-uid coerced-relation coerced-title)
                             (merge
                               {:relation (or coerced-relation relation)}
                               (if-let [title' (or coerced-title title)]
                                 {:page/title title'}
                                 {:block/uid (or coerced-ref-uid uid)})))]
    (or new-pos pos)))


(defn get-position
  "Get the position for block-uid in db.
  Position will be athens.common-events.graph.schema/child-position for the first block,
  and athens.common-events.graph.schema/sibling-position or /property-position for others."
  [db block-uid]
  (let [{:block/keys [order key]
         :db/keys    [id]} (get-block db [:block/uid block-uid])
        parent-uid         (->> id (get-parent db) :block/uid)
        position           (compat-position db {:block/uid parent-uid
                                                :relation  (or order {:page/title (:node/title key)})})]
    position))


(defn validate-position
  [db {:keys [relation block/uid page/title] :as position}]
  (let [title->uid (get-page-uid db title)
        uid->title (get-page-title db uid)
        key       (:page/title relation)
        block     (get-block db [:block/uid (or uid title->uid)])
        keys      (->> block :block/properties keys set)]
    ;; Fail on error conditions.
    (when-some [fail-msg (cond
                           (and uid uid->title)
                           "Location uid is a page, location must use title instead."

                           ;; TODO: this could be idempotent instead and create the page.
                           (and title (not title->uid))
                           (str "Location title does not exist:" title)

                           (and uid (not (e-by-av db :block/uid uid)))
                           (str "Location uid does not exist:" uid)

                           ;; TODO: this could be idempotent and instead overwrite the name.
                           (and key (keys key))
                           (str "Location already contains key: " key)

                           (and (#{:before :after} relation) (:block/key block))
                           (str "Location is a property, cannot use :after/:before relation."))]
      (throw (ex-info fail-msg position)))))


(defn position->uid+parent
  [db {:keys [relation block/uid page/title] :as position}]
  ;; Validate the position itself before determining the parent.
  (validate-position db position)
  (let [;; Pages must be referenced by title but internally we still use uids for them.
        uid                     (or uid (get-page-uid db title))
        {parent-uid :block/uid} (if (or (#{:first :last} relation)
                                        (:page/title relation))
                                  ;; We already know the blocks exists because of validate-position
                                  (get-block db [:block/uid uid])
                                  (if-let [parent (get-parent db [:block/uid uid])]
                                    parent
                                    (throw (ex-info "Ref block does not have parent" {:block/uid uid}))))]
    [uid parent-uid]))


(defn drop-prop-position
  [db uid target-uid rel]
  (let [target-uid' (if (#{:first :last} rel)
                      target-uid
                      (:block/uid (get-parent db [:block/uid target-uid])))
        k           (property-key db [:block/uid uid])]
    (compat-position db {:block/uid target-uid'
                         :relation  {:page/title k}})))


(defn orphan-block-uids
  [db]
  (d/q '[:find ?uid
         :where
         [?e :block/uid ?uid]
         [(missing? $ ?e :node/title)]
         [(missing? $ ?e :block/_children)]
         [(missing? $ ?e :block/property-of)]]
       db))


(defn breadcrumb-string
  [db uid]
  (let [{:block/keys [key string]
         :keys       [node/title]} (d/entity db [:block/uid uid])
        prop                       (:node/title key)
        prop-fragment              (when prop (str ": " prop " - "))]
    (or title (str prop-fragment string))))


(defn has-descendants?
  [{:block/keys [children properties]}]
  (or children properties))


(defn descendants
  [{:block/keys [children properties]}]
  (concat children properties))


(defn time-range
  [db eid]
  (let [all-times (->> (d/pull db '[{:block/edits [{:event/time [:time/ts]}]}
                                    {:block/children ...}
                                    {:block/_property-of ...}]
                               eid)
                       (tree-seq has-descendants? descendants)
                       (mapcat :block/edits)
                       (map (comp :time/ts :event/time))
                       sort)]
    [(first all-times) (last all-times)]))


(defn extract-tag-values
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
  (when (and (string? s)
             (string/starts-with? s start)
             (string/ends-with? s end))
    (subs s (count start) (- (count s) (count end)))))


(defn string->lookup-refs
  "Given string s, compute the set of refs expressed as Datalog lookup refs."
  [s]
  (let [ast           (parser/structure-parse-to-ast s)
        block-lookups (into #{}
                            (map (fn [uid] [:block/uid uid]))
                            (extract-tag-values ast #{:block-ref} identity #(-> % second :string)))
        page-lookups  (into #{}
                            (map (fn [title] [:node/title title]))
                            (extract-tag-values ast #{:page-link :hashtag} identity #(-> % second :string)))]
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
  [[eid attr value _time added?]]
  (when (and added? (#{:block/string :node/title} attr))
    [eid value]))


(defn find-page-links
  [s]
  (->> (string->lookup-refs s)
       (filter #(= :node/title (first %)))
       (map second)
       (into #{})))


(defn linkmaker-error-handler
  [e input-tx]
  (log/error e "❌ Linkmaker failure.")
  (log/debug "Linkmaker original TX:\n" (with-out-str
                                          (pp/pprint input-tx)))
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
  - `db`: Current Datascript DB value
  - `input-tx` (optional): Graph structure modifying TX, analyzed for link updates

  If `input-tx` is provided, linkmaker will only update links related to that tx.
  If `input-tx` is not provided, all links in the db are checked for updates.

  Named after [Keymaker](https://en.wikipedia.org/wiki/Keymaker). "

  ([db]
   (try
     (let [datoms        (d/datoms db :eavt)
           linkmaker-txs (into []
                               (comp (keep parseable-string-datom)
                                     (mapcat (partial update-refs db)))
                               datoms)]
       #_(log/debug "linkmaker:"
                    "\nall:" (with-out-str (pp/pprint datoms))
                    "\nlinkmaker-txs:" (with-out-str (pp/pprint linkmaker-txs)))
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
       #_(log/debug "linkmaker:"
                    "\ninput-tx:" (with-out-str (pp/pprint input-tx))
                    "\ntx-data:" (with-out-str (pp/pprint tx-data))
                    "\nlinkmaker-txs:" (with-out-str (pp/pprint linkmaker-txs))
                    "\nwith-linkmaker-txs:" (with-out-str (pp/pprint with-linkmaker-txs)))
       with-linkmaker-txs)
     (catch #?(:cljs :default
               :clj Exception) e
       (linkmaker-error-handler e input-tx)))))


(defn fix-block-order
  [{:block/keys [children] :as parent-block}]
  (let [sorted-kids  (->> children
                          (sort-by #(vector (:block/order %)
                                            (:block/uid %))))
        indexed-kids (map-indexed vector sorted-kids)
        block-fixes  (keep (fn [[idx {:block/keys [uid order]}]]
                             (when-not (= idx order)
                               {:block/uid   uid
                                :block/order idx}))
                           indexed-kids)]
    #_(log/debug "indexed-kids:" (with-out-str
                                   (pp/pprint indexed-kids))
                 "\nblock-fixes:" (with-out-str
                                    (pp/pprint block-fixes)))
    (when-not (empty? block-fixes)
      (log/error "\nNeeded to fix block-order:\n" (with-out-str
                                                    (pp/pprint block-fixes))
                 "\nOf parent:\n" (with-out-str
                                    (pp/pprint parent-block))))
    block-fixes))


(defn keep-block-order
  "Checks for `:block/order` violations and generates fixing TXs.

  Arguments: whatever it takes"
  [{:keys [db-before db-after tx-data]}]
  (let [mod-eids       (->> tx-data
                            (keep (fn [[eid attr]]
                                    (when (= :block/order attr)
                                      (eid->lookup-ref db-after eid))))
                            set)
        old-parents    (when db-before
                         (->> mod-eids
                              (map #(get-parent-eid db-before %))
                              (remove #(string/blank? (v-by-ea db-after % :block/uid)))
                              set))
        new-parents    (->> mod-eids
                            (map #(get-parent-eid db-after %))
                            set)
        both-parents   (set/union old-parents new-parents)
        parents-blocks (->> both-parents
                            (remove nil?)
                            (map #(get-block db-after %)))
        new-violations (doall
                         (remove #(or (empty? %)
                                      (nil? (:block/uid %)))
                                 (mapcat fix-block-order parents-blocks)))]
    #_(log/debug "keep-block-order:"
                 "\ntx-data:" (with-out-str
                                (pp/pprint tx-data))
                 "\nmod-eids:" (pr-str mod-eids)
                 "\nold-parents:" (pr-str old-parents)
                 "\nnew-parents:" (pr-str new-parents)
                 "\nparents-blocks:\n" (with-out-str
                                         (pp/pprint parents-blocks))
                 "\nnew-violations:" (pr-str new-violations))
    new-violations))


(defn orderkeeper-error
  [ex input-tx]
  (log/error ex "❌ Orderkeeper failure.")
  (log/debug "Orderkeeper original TX:\n" (with-out-str
                                            (pp/pprint input-tx)))
  input-tx)


(defn orderkeeper
  "Maintains the order in Knowledge Graph.

  Returns Datascript transactions to be transacted in order to maintain order.

  Arguments:
  - `db`: Current Datascript DB value
  - `input-tx`: (optional): Graph structure modifying TX, analyzed for `:block/order` mistakes

  If `input-tx` is provided, orderkeeper will only update `:block/order` related to that TX.
  If `input-tx` is not provided, all `:block/order` will be checked."
  ([db]
   (try
     (let [datoms          (d/datoms db :eavt)
           orderkeeper-txs (into []
                                 (keep-block-order {:db-after  db
                                                    :tx-data   datoms}))]
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


(defn block-uid-nil-eater-error
  [ex input-tx]
  (log/error ex "❌ `:block/uid nil` eater error")
  input-tx)


(defn block-uid-nil-eater
  "Eats (removes) all block with `:block/order` nil"
  ([db]
   (block-uid-nil-eater db []))
  ([db input-tx]
   (try
     (let [tx-report        (d/with db input-tx)
           violating-db-ids (d/q '[:find ?eid
                                   :keys db/id
                                   :where [?eid :block/order]
                                   (not [?eid :block/uid])]
                                 (:db-after tx-report))
           retractions      (into []
                                  (for [{eid :db/id} violating-db-ids]
                                    (do
                                      (log/warn "block-uid-nil-eater, have to remove :db/id" eid)
                                      [:db/retractEntity eid])))
           with-eater       (into (vec input-tx) retractions)]
       with-eater)
     (catch #?(:cljs :default
               :clj Exception) e
       (block-uid-nil-eater-error e input-tx)))))


(defn tx-with-middleware
  [db tx-data]
  #?(:cljs
     (as-> tx-data $
           ;; Hasn't really found any problems in a while, and
           ;; does a full index scan so it's pretty slow.
           #_(wrap-span "block-uid-nil-eater"
                      (block-uid-nil-eater db $))
           (wrap-span "linkmaker"
                      (linkmaker db $))
           (wrap-span "orderkeeper"
                      (orderkeeper db $)))
     :clj (->> tx-data
               #_(block-uid-nil-eater db)
               (linkmaker db)
               (orderkeeper db))))


(defn transact-with-middleware!
  "Transact tx-data enriched with middleware txs into conn."
  [conn tx-data]
  ;; 🎶 Sia "Cheap Thrills"
  (let [processed-tx-data #?(:cljs (wrap-span "tx-with-middleware"
                                              (tx-with-middleware @conn tx-data))
                             :clj (tx-with-middleware @conn tx-data))]
    #?(:cljs (wrap-span "ds/transact!"
                        (d/transact! conn processed-tx-data))
       :clj (d/transact! conn processed-tx-data))))


(defn health-check
  [conn]
  ;; NB: these could be events as well, and then we wouldn't always rerun them.
  ;; But rerunning them after replaying all events helps us find events that produce
  ;; states that need fixing.
  (log/info "Knowledge graph health check...")
  (let [linkmaker-txs       #?(:cljs (wrap-span-no-new-tx "linkmaker"
                                                          (linkmaker @conn))
                               :clj (linkmaker @conn))
        orderkeeper-txs     #?(:cljs (wrap-span-no-new-tx "orderkeeper"
                                                          (orderkeeper @conn))
                               :clj (orderkeeper @conn))
        block-nil-eater-txs #?(:cljs (wrap-span-no-new-tx "nil-eater"
                                                          (block-uid-nil-eater @conn))
                               :clj (block-uid-nil-eater @conn))]
    (when-not (empty? linkmaker-txs)
      (log/warn "linkmaker fixes#:" (count linkmaker-txs))
      (log/info "linkmaker fixes:" (pr-str linkmaker-txs))
      #?(:cljs (wrap-span-no-new-tx "transact linkmaker"
                                    (d/transact! conn linkmaker-txs))
         :clj (d/transact! conn linkmaker-txs)))
    (when-not (empty? orderkeeper-txs)
      (log/warn "orderkeeper fixes#:" (count orderkeeper-txs))
      (log/info "orderkeeper fixes:" (pr-str orderkeeper-txs))
      #?(:cljs (wrap-span-no-new-tx "transact orderkeeper"
                                    (d/transact! conn orderkeeper-txs))
         :clj (d/transact! conn orderkeeper-txs)))
    (when-not (empty? block-nil-eater-txs)
      (log/warn "block-uid-nil-eater fixes#:" (count block-nil-eater-txs))
      (log/info "block-uid-nil-eater fixes:" (pr-str block-nil-eater-txs))
      #?(:cljs (wrap-span-no-new-tx "transact nil-eater"
                                    (d/transact! conn block-nil-eater-txs))
         :clj (d/transact! conn block-nil-eater-txs)))
    (log/info "✅ Knowledge graph health check.")))
