(ns athens.common-events.graph.ops
  "Building (including contextual resolution) Graph Ops like a boss."
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.graph.atomic    :as atomic]
    [athens.common-events.graph.composite :as composite]
    [athens.parser.structure              :as structure]
    [clojure.set                          :as set]))


(defn build-page-new-op
  "Creates `:page/new` & optionally `:block/new` ops.
  If page already exists, just creates atomic `:block/new`.
  If page doesn't exist, generates composite of atomic `:page/new` & `:block/new`."
  ([_db page-title]
   (atomic/make-page-new-op page-title))
  ([db page-title block-uid]
   (let [location (common-db/compat-position db {:page/title page-title
                                                 :relation  :first})]
     (if (common-db/e-by-av db :node/title page-title)
       (atomic/make-block-new-op block-uid location)
       (composite/make-consequence-op {:op/type :page/new}
                                      [(atomic/make-page-new-op page-title)
                                       (atomic/make-block-new-op block-uid location)])))))


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


(defn block-move-chain
  [target-uid source-uids first-rel]
  (composite/make-consequence-op {:op/type :block/move-chain}
                                 (concat [(atomic/make-block-move-op (first source-uids)
                                                                     {:block/uid target-uid
                                                                      :relation first-rel})]
                                         (doall
                                           (for [[one two] (partition 2 1 source-uids)]
                                             (atomic/make-block-move-op two
                                                                        {:block/uid one
                                                                         :relation :after}))))))


(defn build-block-split-op
  "Creates `:block/split` composite op, taking into account context.
  If old-block has children, pass them on to new-block.
  If old-block is open or closed, pass that state on to new-block."
  [db {:keys [old-block-uid new-block-uid
              string index relation]}]
  (let [save-block-op      (build-block-save-op db old-block-uid (subs string 0 index))
        new-block-op       (atomic/make-block-new-op new-block-uid {:block/uid old-block-uid
                                                                    :relation  relation})
        new-block-save-op  (build-block-save-op db new-block-uid (subs string index))
        {:block/keys [open]} (common-db/get-block db [:block/uid old-block-uid])
        children           (common-db/get-children-uids db [:block/uid old-block-uid])
        children?          (seq children)
        move-children-op   (when children?
                             (block-move-chain new-block-uid children :first))
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
