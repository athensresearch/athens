(ns athens.db
  (:require
    [athens.common-db                    :as common-db]
    [athens.common-events.resolver.order :as order]
    [athens.common.logging               :as log]
    [athens.common.sentry                :refer-macros [defntrace]]
    [athens.electron.utils               :as electron.utils]
    [athens.patterns                     :as patterns]
    [athens.types.tasks.db               :as tasks-db]
    [clojure.edn                         :as edn]
    [clojure.string                      :as string]
    [datascript.core                     :as d]
    [re-frame.core                       :as rf]))


;; -- Example Roam DBs ---------------------------------------------------

(def help-url   "https://raw.githubusercontent.com/athensresearch/athens/master/data/help.datoms")
(def ego-url    "https://raw.githubusercontent.com/athensresearch/athens/master/data/ego.datoms")


;; -- seed data -----------------------------------------------------------


(def default-graph-conf
  {:hlt-link-levels  1
   :link-distance    50
   :charge-strength  -15
   :local-depth      1
   :root-links-only? false
   :orphans?         true
   :daily-notes?     true})


(def default-pallete
  #{"#DDA74C"
    "#C45042"
    "#611A58"
    "#21A469"
    "#009FB8"
    "#0062BE"})


(def greek-pantheon
  #{"Zeus"
    "Hera"
    "Poseidon"
    "Demeter"
    "Athena"
    "Apollo"
    "Artemis"
    "Ares"
    "Aphrodite"
    "Hephaestus"
    "Hermes"
    "Dionysus"
    ;; Technically not part of the Olympians, but a cool guy nonetheless.
    "Hades"
    ;; Son of either Zeus and Persephone or Hades and persephone.
    ;; Nothing to do with a recent video game at all.
    "Zagreus"})


(def default-settings
  {:email       nil
   :username    (rand-nth (vec greek-pantheon))
   :color       (rand-nth (vec default-pallete))
   :monitoring  true
   :backup-time 15})


(def default-athens-persist
  {;; Increase this number when making breaking changes to the persistence format.
   :persist/version 2
   :theme/dark      false
   :graph-conf      default-graph-conf
   :settings        default-settings})


;; -- update -------------------------------------------------------------

(defn update-legacy-to-latest
  "Add settings in the legacy format to the latest persist format."
  [latest]
  ;; This value was not saved in local storage proper, saving it there
  ;; to enable use of update-when-found.
  (js/localStorage.setItem "monitoring" (not (try (.. js/window -posthog has_opted_out_capturing)
                                                  (catch :default _ true))))
  (let [update-when-found (fn [x keyseq k xf]
                            (if-some [v (js/localStorage.getItem k)]
                              (assoc-in x keyseq (xf v))
                              x))
        str->boolean (fn [x] (= x "true"))]
    (-> latest
        (update-when-found [:settings :email] "auth/email" identity)
        (update-when-found [:settings :username] "user/name" identity)
        (update-when-found [:settings :backup-time] "debounce-save-time" js/Number)
        (update-when-found [:settings :monitoring] "monitoring" str->boolean)
        (update-when-found [:theme/dark] "theme/dark" str->boolean)
        (update-when-found [:graph-conf] "graph-conf" edn/read-string))))


(defn update-v1-to-v2
  [persisted]
  (-> persisted
      (assoc-in [:settings :color] (:color default-settings))
      (assoc :persist/version 2)))


(defn- recreate-self-hosted-dbs
  [dbs]
  (into {} (map (fn [[k {:keys [name url password] :as db}]]
                  [k (if (electron.utils/remote-db? db)
                       (electron.utils/self-hosted-db name url password)
                       db)])
                dbs)))


(defn update-v2-to-v3
  [persisted]
  (-> persisted
      (update :db-picker/all-dbs recreate-self-hosted-dbs)
      (assoc :persist/version 3)))


(defn update-persisted
  "Updates persisted to the latest format."
  [{:keys [:persist/version] :as persisted}]
  ;; Anything saved under the :athens/persist key will be automatically
  ;; persisted and loaded between sessions.
  (if-not version
    ;; Legacy is updated to latest directly by cherry-picking data
    ;; from local storage into the latest format.
    (update-legacy-to-latest default-athens-persist)
    ;; Data saved in previous versions of the current format need to be updated.
    (let [v< #(< version %)]
      (cond-> persisted
        ;; Update persisted by applying each update fn incrementally.
        (v< 2) update-v1-to-v2
        (v< 3) update-v2-to-v3))))


;; -- re-frame -----------------------------------------------------------

(defonce rfdb {:db/synced           true
               :db/mtime            nil
               :current-route       nil
               :loading?            true
               :modal               false
               :alert               nil
               :win-maximized?      false
               :win-fullscreen?     false
               :win-focused?        true
               :athena/open         false
               :athena/recent-items '()
               ;; todo: some value initialization like athens/persist
               ;; :right-sidebar/width 32
               :mouse-down          false
               :daily-notes/items   []
               :selection           {:items []}
               :help/open?          false
               :zoom-level          0
               :fs/watcher          nil
               :presence            {}
               :connection-status   :disconnected
               :comment/show-comments? true})


(defn init-app-db
  [persisted]
  (merge rfdb {:athens/persist (update-persisted persisted)}))


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

(defonce dsdb (common-db/create-conn))


(defn e-by-av
  [a v]
  (-> (d/datoms @dsdb :avet a v) first :e))


(defn v-by-ea
  [e a]
  (-> (d/datoms @dsdb :eavt e a) first :v))


(defn uid-and-embed-id
  [uid]
  (or (some->> uid
               (re-find #"^(.+)-embed-(.+)")
               rest vec)
      [uid nil]))


(defn sort-block-children
  [block]
  (if-let [children (seq (:block/children block))]
    (assoc block :block/children
           (vec (sort-by :block/order (map sort-block-children children))))
    block))


(defntrace shape-parent-query
  "Normalize path from deeply nested block to root node."
  [pull-results]
  (->> (loop [b   pull-results
              res []]
         (cond
           ;; There's no page in these pull results, log and exit.
           (nil? b)        (do
                             (log/warn "No parent found in" (pr-str pull-results))
                             [])
           ;; Found the page.
           (:node/title b) (conj res b)
           ;; Recur with the parent.
           :else           (recur (or (first (:block/_children b))
                                      (:block/property-of b))
                                  (conj res (dissoc b :block/_children :block/property-of)))))
       (rest)
       (reverse)
       vec))


(defntrace get-parents-recursively
  [id]
  (when (d/entity @dsdb id)
    (->> (d/pull @dsdb '[:db/id :node/title :block/uid :block/string
                         {:block/edits [{:event/time [:time/ts]}]}
                         {:block/property-of ...}
                         {:block/_children ...}]
                 id)
         shape-parent-query)))


(defntrace get-root-parent-page
  "Returns the root parent page or returns the block because this block is a page."
  [uid]
  ;; make sure block first exists
  (when-let [block (d/entity @dsdb [:block/uid uid])]
    (let [opt1 (first (get-parents-recursively [:block/uid uid]))]
      (or opt1 block))))


(defntrace get-block
  [id]
  (common-db/get-block @dsdb id))


(defntrace get-parent
  [id]
  (-> (common-db/get-parent-eid @dsdb id)
      get-block))


(defntrace deepest-child-block
  [id]
  (let [db @dsdb]
    (loop [uid (common-db/get-block-uid db id)]
      (let [eid      [:block/uid uid]
            open     (-> db (d/entity eid) :block/open)
            props?   @(rf/subscribe [:feature-flags/enabled? :properties])
            children (if props?
                       (common-db/sorted-prop+children-uids db eid)
                       (common-db/get-children-uids db eid))]
        (if (or (zero? (count children))
                (not open))
          (common-db/get-block db eid)
          (recur (last children)))))))


(defntrace search-exact-node-title
  [query]
  (d/entity @dsdb [:node/title query]))


(defn search-in-node-title
  ([query] (search-in-node-title query 20 false))
  ([query n] (search-in-node-title query n false))
  ([query n exclude-exact-match?]
   (if (string/blank? query)
     (vector)
     (let [exact-match            (when exclude-exact-match? query)
           case-insensitive-query (patterns/re-case-insensitive query)]
       (sequence
         (comp
           (filter (every-pred
                     #(re-find case-insensitive-query (:v %))
                     #(not= exact-match (:v %))))
           (take n)
           (map #(d/entity @dsdb (:e %))))
         (d/datoms @dsdb :aevt :node/title))))))


(defn get-root-parent-node-from-block
  [db {:keys [block/uid] :as block}]
  (cond
    (= "[[athens/comment]]" (common-db/get-entity-type db [:block/uid uid]))
    (let [commented-block (-> block
                              ;; comments are a child of :comments/threads
                              :block/_children first
                              ;; :comments/threads is a prop on the commented block
                              :block/property-of
                              ;; get rid of the extra ascendant data on it though
                              (dissoc :block/_children :block/property-of))]
      (assoc block
             :block/parent commented-block
             :block-search/navigate-uid (:block/uid commented-block)))

    :else
    (loop [b block]
      (cond
        (:node/title b)        (assoc block :block/parent b)
        (:block/_children b)   (recur (first (:block/_children b)))
        (:block/property-of b) (recur (:block/property-of b))
        ;; protect against orphaned nodes
        :else                  nil))))


(defn search-in-block-content
  ([query] (search-in-block-content query 20))
  ([query n]
   (if (string/blank? query)
     (vector)
     (let [db                     @dsdb
           case-insensitive-query (patterns/re-case-insensitive query)
           block-search-result    (->>
                                    (d/datoms db :aevt :block/string)
                                    (sequence
                                      (comp
                                        (filter #(re-find case-insensitive-query (:v %)))
                                        (take n)
                                        (map #(:e %))))
                                    (d/pull-many db '[:db/id :block/uid :block/string :node/title
                                                      {:block/_children ...} {:block/property-of ...}])
                                    (sequence
                                      (comp
                                        (keep (partial get-root-parent-node-from-block db))
                                        (map #(dissoc % :block/_children :block/property-of)))))]
       block-search-result))))


(defn sibling-uids
  [uid]
  (let [props-enabled? @(rf/subscribe [:feature-flags/enabled? :properties])
        eid            (->> [:block/uid uid]
                            get-parent
                            :block/uid
                            (vector :block/uid))]
    (if props-enabled?
      (common-db/sorted-prop+children-uids @dsdb eid)
      (common-db/get-children-uids @dsdb eid))))


(defn nth-sibling
  "Find sibling that has relation to current block.
  Relation can be :before or :after."
  ([uid relation]
   (-> (sibling-uids uid)
       (order/get relation uid)
       (->> (vector :block/uid))
       get-block))
  ([siblings uid relation]
   (-> siblings
       (order/get relation uid)
       (->> (vector :block/uid))
       get-block)))


(defntrace prev-block-uid
  "If first sibling, go to parent (if not a page).
   If block is closed, go to prev sibling.
   If block is OPEN, go to prev sibling's deepest child."
  [uid]
  (let [[uid embed-id]                (uid-and-embed-id uid)
        siblings                      (sibling-uids uid)
        oldest-sibling?               (= uid (first siblings))
        parent                        (when oldest-sibling?
                                        (get-parent [:block/uid uid]))
        parent-type                   (when parent
                                        (common-db/get-entity-type @dsdb [:block/uid (:block/uid parent)]))
        prev-sibling                  (nth-sibling siblings uid :before)
        {:block/keys      [open children]
         prev-sibling-uid :block/uid} prev-sibling
        prev-sibling-children?        (seq children)
        prev-sibling-type             (when prev-sibling-uid
                                        (common-db/get-entity-type @dsdb [:block/uid prev-sibling-uid]))
        prev-sibling-task?            (= "[[athens/task]]" prev-sibling-type)
        deepest-child                 (when open
                                        (deepest-child-block [:block/uid prev-sibling-uid]))
        deepest-child-type            (when deepest-child
                                        (common-db/get-entity-type @dsdb [:block/uid (:block/uid deepest-child)]))
        prev-block                    (cond
                                        (and oldest-sibling?
                                             (= "[[athens/task]]" parent-type))         (tasks-db/get-title-block-of-task @dsdb (:block/uid parent))
                                        oldest-sibling?                                 parent
                                        (and prev-sibling-task? prev-sibling-children?) deepest-child
                                        prev-sibling-task?                              (tasks-db/get-title-block-of-task @dsdb prev-sibling-uid)
                                        (false? open)                                   prev-sibling
                                        (= "[[athens/task]]" deepest-child-type)        (tasks-db/get-title-block-of-task @dsdb (:block/uid deepest-child))
                                        (true? open)                                    deepest-child)
        prev-block-uid                (:block/uid prev-block)]
    (when (and prev-block-uid
               (not (:node/title prev-block)))
      (cond-> prev-block-uid
        embed-id (str "-embed-" embed-id)))))


(defntrace next-sibling-recursively
  "Search for next sibling. If not there (i.e. is last child), find sibling of parent.
  If parent is root, go to next sibling."
  [uid]
  (loop [uid uid]
    (let [sib    (nth-sibling uid :after)
          parent (get-parent [:block/uid uid])
          {node :node/title}   (get-block [:block/uid uid])]
      (if (or sib (:node/title parent) node)
        sib
        (recur (:block/uid parent))))))


(defn next-block-uid
  "1-arity:
    if open and children, go to first sibling
    else recursively find next sibling of parent
  2-arity:
    used for multi-block-selection; ignores child blocks"
  ([uid]
   (let [[uid embed-id]            (uid-and-embed-id uid)
         props-enabled?            @(rf/subscribe [:feature-flags/enabled? :properties])
         props+children            (if props-enabled?
                                     (common-db/sorted-prop+children-uids @dsdb [:block/uid uid])
                                     (common-db/get-children-uids @dsdb [:block/uid uid]))
         {:block/keys [open]
          node        :node/title} (get-block [:block/uid uid])
         next-block-recursive      (next-sibling-recursively uid)
         next-entity-type          (when next-block-recursive
                                     (common-db/get-entity-type @dsdb [:block/uid (:block/uid next-block-recursive)]))
         next-child+prop-uid       (first props+children)
         next-child+prop-type      (when next-child+prop-uid
                                     (common-db/get-entity-type @dsdb [:block/uid next-child+prop-uid]))
         next-block                (cond
                                     (and next-block-recursive
                                          (= "[[athens/task]]" next-entity-type))
                                     (tasks-db/get-title-block-of-task @dsdb (:block/uid next-block-recursive))

                                     (and next-child+prop-uid
                                          (= "[[athens/task]]" next-child+prop-type))
                                     (tasks-db/get-title-block-of-task @dsdb next-child+prop-uid)

                                     (and (or open node)
                                          next-child+prop-uid)
                                     (get-block [:block/uid next-child+prop-uid])

                                     next-block-recursive
                                     next-block-recursive)]
     #_(log/debug "next-block-uid:" (pr-str {:next-block-recursive-uid (:block/uid next-block-recursive)
                                             :next-entity-type         next-entity-type}))
     (cond-> (:block/uid next-block)

       ;; only go to next block if it's part of current embed scheme
       (and embed-id (js/document.querySelector (str "#editable-uid-" (:block/uid next-block) "-embed-" embed-id)))
       (str "-embed-" embed-id))))
  ([uid selection?]
   (if selection?
     (let [[o-uid embed-id]     (uid-and-embed-id uid)
           next-block-recursive (next-sibling-recursively o-uid)]
       (cond-> (:block/uid next-block-recursive)

         ;; only go to next block if it's part of current embed scheme
         (and embed-id (js/document.querySelector (str "#editable-uid-" (:block/uid next-block-recursive) "-embed-" embed-id)))
         (str "-embed-" embed-id)))
     (next-block-uid uid))))


(defntrace get-sorted-children
  [uid db]
  (when uid
    (try
      (->> (d/pull db [{:block/children [:block/uid :block/order :block/open]}] [:block/uid uid])
           sort-block-children
           :block/children)
      (catch :default _))))


(defn get-first-child-uid
  [uid db]
  (-> (get-sorted-children uid db)
      first
      :block/uid))


(defn get-last-child-uid
  [parent-uid db]
  (let [{:block/keys [uid open]} (-> (get-sorted-children parent-uid db) last)]
    (cond
      (not uid) parent-uid
      open      (recur uid db)
      :else     uid)))


;; -- Linked & Unlinked References ----------

(defntrace get-ref-ids
  [unlinked-f]
  (d/q '[:find [?e ...]
         :in $ ?unlinked-f
         :where
         [?e :block/string ?s]
         [(?unlinked-f ?s)]]
       @dsdb
       unlinked-f))


(defn merge-parents-and-block
  [ref-ids]
  (let [parents (reduce-kv (fn [m _ v] (assoc m v (get-parents-recursively v)))
                           {}
                           ref-ids)
        blocks (map (fn [id] (get-block id)) ref-ids)]
    (mapv
      (fn [block]
        (merge block {:block/parents (get parents (:db/id block))}))
      blocks)))


(defn group-by-parent
  [blocks]
  (group-by (fn [x]
              (let [parent (-> x
                               :block/parents
                               first)]
                [(:node/title parent) (->> parent :block/edits (map (comp :time/ts :event/time)) sort last (or 0))]))
            blocks))


(defn eids->groups
  [eids]
  (->> eids
       merge-parents-and-block
       group-by-parent
       (sort-by #(-> % first second))
       (map #(vector (ffirst %) (second %)))
       vec
       rseq))


(defntrace get-unlinked-references
  "For node-page references UI."
  [title]
  (-> (partial patterns/contains-unlinked? title) get-ref-ids merge-parents-and-block group-by-parent seq))


;; -- save ------------------------------------------------------------


(defn transact-state-for-uid
  "uid -> Current block
   new-string -> new `:block/string` value
   source -> reporting source"
  [uid new-string source]
  (rf/dispatch [:block/save {:uid    uid
                             :string new-string
                             :source source}]))
