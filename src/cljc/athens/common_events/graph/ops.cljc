(ns athens.common-events.graph.ops
  "Building (including contextual resolution) Graph Ops like a boss."
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.graph.atomic    :as atomic]
    [athens.common-events.graph.composite :as composite]
    [athens.common.utils                  :as common.utils]
    [athens.parser.structure              :as structure]
    [clojure.set                          :as set]))


(defn build-location-op
  "Creates composite op with `:page/new` for any missing page in location.
  If no page creation is needed, returns original op."
  [db location original-op]
  (let [parent-page-title (-> location :page/title)
        prop-page-title   (-> location :relation :page/title)
        create-parent?    (and parent-page-title (not (common-db/e-by-av db :node/title parent-page-title)))
        create-prop?      (and prop-page-title (not (common-db/e-by-av db :node/title prop-page-title)))]
    (if (or create-parent? create-prop?)
      (composite/make-consequence-op {:op/type (:op/type original-op)}
                                     (cond-> []
                                       create-parent? (conj (atomic/make-page-new-op parent-page-title))
                                       create-prop?   (conj (atomic/make-page-new-op prop-page-title))
                                       true           (conj original-op)))
      original-op)))


(defn build-page-new-op
  "Creates `:page/new` & optionally `:block/new` ops.
  If page already exists, just creates atomic `:block/new`.
  If page doesn't exist, generates composite of atomic `:page/new` & `:block/new`."
  ([_db page-title]
   (atomic/make-page-new-op page-title))
  ([db page-title block-uid]
   (let [location (common-db/compat-position db {:page/title page-title
                                                 :relation  :first})]
     (->> (atomic/make-block-new-op block-uid location)
          (build-location-op db location)))))


(defn build-page-rename-op
  "Creates `:page/rename` & optionally `:page/new` ops."
  [db title-from title-to]
  (let [links-old      (common-db/find-page-links title-from)
        links-new      (common-db/find-page-links title-to)
        just-new       (set/difference links-new links-old)
        new-titles     (remove #(seq (common-db/get-page-uid db %))
                               just-new)
        atomic-pages   (when-not (empty? new-titles)
                         (into []
                               (for [title new-titles]
                                 (build-page-new-op db title))))
        atomic-rename  (atomic/make-page-rename-op title-from title-to)
        page-rename-op (if (empty? atomic-pages)
                         atomic-rename
                         (composite/make-consequence-op {:op/type :page/rename}
                                                        (conj atomic-pages
                                                              atomic-rename)))]
    page-rename-op))


(defn build-block-new-op
  [db block-uid location]
  (->> (atomic/make-block-new-op block-uid location)
       (build-location-op db location)))


(defn build-block-move-op
  [db block-uid position]
  (->> (atomic/make-block-move-op block-uid position)
       (build-location-op db position)))


(defn build-block-save-op
  "Creates `:block/save` op, taking into account context.
  So it might be a composite or atomic event, depending if new page link is present and if pages exist."
  [db block-uid string]
  (let [old-string      (common-db/get-block-string db block-uid)
        links-in-old    (common-db/find-page-links old-string)
        links-in-new    (common-db/find-page-links string)
        link-diff       (set/difference links-in-new links-in-old)
        new-page-titles (remove #(seq (common-db/get-page-uid db %))
                                link-diff)
        atomic-pages    (when-not (empty? new-page-titles)
                          (into []
                                (for [title new-page-titles]
                                  (build-page-new-op db title))))
        atomic-save     (atomic/make-block-save-op block-uid string)
        block-save-op   (if (empty? atomic-pages)
                          atomic-save
                          (composite/make-consequence-op {:op/type :block/save}
                                                         (conj atomic-pages
                                                               atomic-save)))]
    block-save-op))


(defn build-block-remove-op
  "Creates `:block/remove` op."
  [db delete-uid]
  (when (common-db/e-by-av db :block/uid delete-uid)
    (atomic/make-block-remove-op delete-uid)))


(defn build-block-merge-with-updated-op
  "Creates `:block/remove` & `:block/save` ops."
  [db remove-uid merge-uid value update-value]
  (let [;; block/remove atomic op
        block-remove-op     (build-block-remove-op db
                                                   remove-uid)
        block-save-op       (build-block-save-op db merge-uid (str update-value value))
        children-to-move    (:block/children (common-db/get-block db [:block/uid remove-uid]))
        block-move-ops      (into []
                                  (for [{block-uid :block/uid} children-to-move]
                                    (atomic/make-block-move-op block-uid {:block/uid merge-uid
                                                                          :relation  :last})))
        delete-and-merge-op (composite/make-consequence-op {:op/type :block/remove-merge-update}
                                                           (concat (if (seq block-move-ops)
                                                                     block-move-ops
                                                                     [])
                                                                   [block-remove-op
                                                                    block-save-op]))]
    delete-and-merge-op))


(defn build-block-remove-merge-op
  "Creates `:block/remove` & `:block/save` ops.
  Arguments:
  - `db` db value
  - `remove-uid` `:block/uid` to delete
  - `merge-uid` `:block/uid` to merge (postfix) `value` to
  - `value`: string to be postfixed to `:block/string` of `merge-uid`"
  [db remove-uid merge-uid value]
  (let [;; block/remove atomic op
        block-remove-op     (build-block-remove-op db
                                                   remove-uid)
        ;; block/save atomic op
        existing-string     (common-db/v-by-ea db
                                               [:block/uid merge-uid]
                                               :block/string)
        block-save-op       (build-block-save-op db merge-uid (str existing-string value))
        children-to-move    (:block/children (common-db/get-block db [:block/uid remove-uid]))
        block-move-ops      (into []
                                  (for [{block-uid :block/uid} children-to-move]
                                    (atomic/make-block-move-op block-uid {:block/uid merge-uid
                                                                          :relation  :last})))
        delete-and-merge-op (composite/make-consequence-op {:op/type :block/remove-and-merge}
                                                           (concat (if (seq block-move-ops)
                                                                     block-move-ops
                                                                     [])
                                                                   [block-remove-op
                                                                    block-save-op]))]
    delete-and-merge-op))


(defn atomic-composite?
  [event]
  (or
    ;; semantic event
    (and (= :op/atomic (:event/type event))
         (= :composite/consequence (-> event :event/op :op/type)))
    ;; atomic graph op
    (and (contains? event :op/atomic?)
         (not (:op/atomic? event)))))


(defn extract-atomics
  [operation]
  (into []
        (mapcat (fn [consequence]
                  (if (:op/atomic? consequence)
                    [consequence]
                    ;; this is plain recursion, maybe do loop recur instead
                    (extract-atomics consequence)))
                (or (:op/consequences operation)
                    (-> operation :event/op :op/consequences)
                    [(or (:event/op operation)
                         operation)]))))


(defn contains-op?
  [op op-type]
  (let [atomics  (extract-atomics op)
        filtered (filter #(= op-type (:op/type %)) atomics)]
    (seq filtered)))


(defn- split-props-from-blocks
  [db uids]
  (let [group-f #(if (common-db/property-key db [:block/uid %])
                   :props
                   :blocks)
        {:keys [props blocks]} (group-by group-f uids)]
    [props blocks]))


(defn block-move-chain
  [db target-uid source-uids first-rel]
  (let [[prop-uids block-uids] (split-props-from-blocks db source-uids)]
    (composite/make-consequence-op {:op/type :block/move-chain}
                                   (concat (for [uid prop-uids]
                                             (->> (common-db/drop-prop-position db uid target-uid first-rel)
                                                  (atomic/make-block-move-op uid)))

                                           (when (seq block-uids)
                                             [(atomic/make-block-move-op (first block-uids)
                                                                         {:block/uid target-uid
                                                                          :relation first-rel})])
                                           (for [[one two] (partition 2 1 block-uids)]
                                             (atomic/make-block-move-op two
                                                                        {:block/uid one
                                                                         :relation :after}))))))


(defn build-block-split-op
  "Creates `:block/split` composite op, taking into account context.
  If old-block has children, pass them on to new-block.
  If old-block is open or closed, pass that state on to new-block.
  Ignores both behaviours above if old-block is a property."
  [db {:keys [old-block-uid new-block-uid
              string index relation
              navigation-uid]}]
  (let [save-block-op      (build-block-save-op db old-block-uid (subs string 0 index))
        new-block-op       (atomic/make-block-new-op new-block-uid {:block/uid (or navigation-uid
                                                                                   old-block-uid)
                                                                    :relation  relation})
        new-block-save-op  (build-block-save-op db new-block-uid (subs string index))
        {:block/keys [open key]} (common-db/get-block db [:block/uid old-block-uid])
        children           (when-not key
                             (common-db/get-children-uids db [:block/uid old-block-uid]))
        children?          (seq children)
        move-children-op   (when children?
                             (block-move-chain db new-block-uid children :first))
        close-new-block-op (when children?
                             (atomic/make-block-open-op new-block-uid open))
        split-block-op     (composite/make-consequence-op {:op/type :block/split}
                                                          (cond-> [save-block-op
                                                                   new-block-op
                                                                   new-block-save-op]
                                                            children? (conj move-children-op)
                                                            children? (conj close-new-block-op)))]
    split-block-op))


(defn ops->new-page-titles
  "Reduces Graph Ops into a set of titles of newly created pages."
  [ops]
  (let [page-new-ops (contains-op? ops :page/new)
        new-titles   (->> page-new-ops
                          (map :op/args)
                          (map :page/title)
                          set)]
    new-titles))


(defn ops->new-block-uids
  "Reduces Graph Ops into a set of block/uids of newly created blocks."
  [ops]
  (let [block-new-ops (contains-op? ops :block/new)
        new-uids      (->> block-new-ops
                           (map :op/args)
                           (map :block/new)
                           set)]
    new-uids))


(defn structural-diff
  "Calculates removed and added links (block refs & page links)"
  [db ops]
  (let [block-save-ops       (contains-op? ops :block/save)
        page-new-ops         (contains-op? ops :page/new)
        page-rename-ops      (contains-op? ops :page/rename)
        new-blocks           (->> block-save-ops
                                  (map #(select-keys (:op/args %)
                                                     [:block/uid :block/string])))
        new-block-uids       (->> new-blocks
                                  (map :block/uid)
                                  set)
        new-page-titles      (->> (concat page-new-ops page-rename-ops)
                                  (map #(or (get-in (:op/args %) [:target :page/title])
                                            (get-in (:op/args %) [:page/title]))))
        old-page-titles      (->> page-rename-ops
                                  (map :page/title)
                                  set)
        new-block-structures (->> new-blocks
                                  (map :block/string)
                                  (map structure/structure-parser->ast))
        new-title-structures (->> new-page-titles
                                  (map structure/structure-parser->ast))
        old-block-strings    (->> new-block-uids
                                  (map #(common-db/get-block-string db %)))
        old-title-structures (->> old-page-titles
                                  (map structure/structure-parser->ast))
        old-structures       (->> old-block-strings
                                  (map structure/structure-parser->ast))
        links                (fn [structs names renames]
                               (->> structs
                                    (mapcat (partial tree-seq vector? identity))
                                    (filter #(and (vector? %)
                                                  (contains? names (first %))))
                                    (map #(vector (get renames (first %) (first %))
                                                  (-> % second :string)))
                                    set))
        old-links            (links (concat old-structures old-title-structures)
                                    #{:page-link :hashtag :block-ref}
                                    {:hashtag :page-link})
        new-links            (links (concat new-block-structures new-title-structures)
                                    #{:page-link :hashtag :block-ref}
                                    {:hashtag :page-link})
        removed-links        (set/difference old-links new-links)
        added-links          (set/difference new-links old-links)]
    [removed-links added-links]))


(defn throw-unknown-k
  [k]
  (throw (str "Key " k " must be either string or ::first/::last.")))


(defn- new-prop
  [db [a v :as uid-or-eid] next-uid k]
  (let [uid?     (-> uid-or-eid vector? not)
        uid      (if uid?
                   uid-or-eid
                   (common-db/get-block-uid db uid-or-eid))
        title    (or (common-db/get-page-title db uid)
                     (and (= a :node/title) v))
        ;; here too
        position (merge {:relation (cond
                                     (= ::first k) :first
                                     (= ::last k)  :last
                                     (string? k)   {:page/title k}
                                     :else         (throw-unknown-k k))}
                        (if title
                          {:page/title title}
                          {:block/uid uid}))]
    (build-block-new-op db next-uid position)))


(defn build-path
  "Return uid at ks path and operations to create path, if needed, as [uid ops].
  uid can be a string or a datascript eid.
  ks can be properties names as strings, or ::first/::last for children."
  ([db uid ks]
   (build-path db uid ks []))
  ([db uid-or-eid [k & ks] ops]
   (if-not k
     [uid-or-eid ops]
     (let [uid?       (-> uid-or-eid vector? not)
           block      (common-db/get-block db (if uid?
                                                [:block/uid uid-or-eid]
                                                uid-or-eid))
           next-block (cond
                        (= ::first k) (-> block :block/children first)
                        (= ::last k)  (-> block :block/children last)
                        (string? k)   (-> block :block/properties (get k))
                        :else         (throw-unknown-k k))
           next-uid   (or (:block/uid next-block)
                          (common.utils/gen-block-uid))
           ops'       (cond-> ops
                        (not next-block) (conj (new-prop db uid-or-eid next-uid k)))]
       (recur db next-uid ks ops')))))


(defn get-path
  "Return uid at ks path."
  [db uid-or-eid [k & ks]]
  (if-not (and uid-or-eid k)
    uid-or-eid
    (let [uid?       (-> uid-or-eid vector? not)
          block      (common-db/get-block db (if uid?
                                               [:block/uid uid-or-eid]
                                               uid-or-eid))
          next-block (cond
                       (= ::first k) (-> block :block/children first)
                       (= ::last k)  (-> block :block/children last)
                       (string? k)   (-> block :block/properties (get k))
                       :else         (throw-unknown-k k))
          next-uid   (:block/uid next-block)]
      (recur db next-uid ks))))
