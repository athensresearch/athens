(ns athens.common-events.graph.ops
  "Building (including contextual resolution) Graph Ops like a boss."
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.graph.atomic    :as atomic]
    [athens.common-events.graph.composite :as composite]
    [clojure.set                          :as set]))


(defn build-page-new-op
  "Creates `:page/new` & optionally `:block/new` ops.
  If page already exists, just creates atomic `:block/new`.
  If page doesn't exist, generates composite of atomic `:page/new` & `:block/new`."
  ([_db page-title]
   (atomic/make-page-new-op page-title))
  ([db page-title block-uid]
   (let [location (common-db/compat-position db {:ref-name page-title
                                                 :relation :first})]
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


(defn build-block-split-op
  "Creates `:block/split` composite op, taking into account context."
  [db {:keys [old-block-uid new-block-uid
              string index relation]}]
  (let [save-block-op     (build-block-save-op db old-block-uid (subs string 0 index))
        new-block-op      (atomic/make-block-new-op new-block-uid {:ref-uid old-block-uid
                                                                   :relation relation})
        new-block-save-op (build-block-save-op db new-block-uid (subs string index))
        split-block-op    (composite/make-consequence-op {:op/type :block/split}
                                                         [save-block-op
                                                          new-block-op
                                                          new-block-save-op])]
    split-block-op))


(defn build-block-remove-op
  "Creates `:block/remove` op."
  [db delete-uid]
  (when (common-db/e-by-av db :block/uid delete-uid)
    (atomic/make-block-remove-op delete-uid)))


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
        ;; block/save atomic op]
        existing-string     (common-db/v-by-ea db
                                               [:block/uid merge-uid]
                                               :block/string)
        block-save-op       (build-block-save-op db merge-uid (str existing-string value))
        delete-and-merge-op (composite/make-consequence-op {:op/type :block/remove-and-merge}
                                                           [block-remove-op
                                                            block-save-op])]
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
