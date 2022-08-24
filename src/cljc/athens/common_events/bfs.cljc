(ns athens.common-events.bfs
  (:refer-clojure :exclude [descendants])
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.graph.atomic    :as atomic]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.utils                  :as common.utils]
    [clojure.string                       :as string]
    [clojure.walk                         :as walk]))


(defn enhance-block
  [block previous parent]
  (merge block
         {:parent parent}
         {:previous (select-keys previous [:block/uid])}))


(defn parent-lookup
  [{:keys [page/title block/uid]}]
  (if title
    [:page/title title]
    [:block/uid uid]))


(defn enhance-children
  [children parent]
  ;; Partition by 2 with nil first/last elements to get each [previous current] pair.
  ;; https://stackoverflow.com/a/41925223/2116927
  (->> (concat [nil] children [nil])
       (partition 2 1)
       (map (fn [[previous current]]
              (when current (enhance-block current previous (parent-lookup parent)))))
       (remove nil?)
       vec))


(defn- enhance-props
  [properties parent]
  (->> properties
       (map (fn [[k v]] (assoc v :parent (parent-lookup parent) :key k)))
       vec))


(defn enhance-internal-representation
  "Enhance an internal representations' individual elements with a reference to parent and previous elements.
  Parents will be referenced by maps with either :page/title or :block/uid, previous will be :block/uid.
  Toplevel pages will be sorted after all toplevel blocks. "
  [internal-representation]
  (let [{blocks true
         pages  false} (group-by (comp nil? :page/title) internal-representation)
        ;; Enhance toplevel blocks as if they had a nil parent.
        ;; The first block will have neither a parent or a previous.
        blocks         (or blocks [])
        pages          (or pages [])
        blocks'        (enhance-children blocks nil)]
    (walk/postwalk
      (fn [x]
        (if (map? x)
          (let [{:block/keys [children properties]} x]
            (cond-> x
              children   (assoc :block/children (enhance-children children x))
              properties (assoc :block/properties (enhance-props properties x))))
          x))
      (concat blocks' pages))))


(defn enhanced-internal-representation->atomic-ops
  "Takes the enhanced internal representation and creates :page/new or :block/new and :block/save atomic events.
  Throws if default-position is nil and position cannot be determined."
  [db default-position {:keys [page/title previous parent key] :block/keys [uid string open?] :as eir}]
  (if title
    [(atomic/make-page-new-op title)]
    (let [[parent-type parent-id]   parent
          previous-uid              (:block/uid previous)
          prop-or-last              (if key
                                      {:page/title key}
                                      :last)
          position                  (cond
                                      ;; There's a block before this one that we can add this one after.
                                      previous-uid {:block/uid previous-uid   :relation :after}
                                      ;; There's no previous block, but we can add it to the end of the parent or as prop.
                                      parent-id    {parent-type parent-id     :relation prop-or-last}
                                      ;; There's a default place where we can drop blocks, use it.
                                      default-position default-position
                                      :else (throw (ex-info "Cannot determine position for enhanced internal representation" eir)))
          new-op                    (graph-ops/build-block-new-op db uid position)
          atomic-new-ops            (if (graph-ops/atomic-composite? new-op)
                                      (graph-ops/extract-atomics new-op)
                                      [new-op])
          save-op                   (graph-ops/build-block-save-op db uid string)
          atomic-save-ops           (if (graph-ops/atomic-composite? save-op)
                                      (graph-ops/extract-atomics save-op)
                                      [save-op])]
      (cond-> (into atomic-new-ops atomic-save-ops)
        (= open? false) (conj (atomic/make-block-open-op uid false))))))


(defn move-save-ops-to-end
  [coll]
  (let [{save true
         not-save false} (group-by #(= (:op/type %) :block/save) coll)]
    (concat [] not-save save)))


(defn add-missing-block-uids
  [internal-representation]
  (walk/postwalk
    (fn [x]
      (if (and (map? x)
               ;; looks like a block
               (or (:block/string x)
                   (:block/properties x)
                   (:block/children x)
                   (:block/open? x))
               ;; but doesn't have uid
               (not (:block/uid x)))
        ;; add it
        (assoc x :block/uid (common.utils/gen-block-uid))
        x))
    internal-representation))


(defn internal-representation->atomic-ops
  "Convert internal representation to the vector of atomic operations that would create it.
  :block/save operations are grouped at the end so that any ref'd entities are already created."
  [db internal-representation default-position]
  (when-not (or (vector? internal-representation)
                (list? internal-representation))
    (throw "Internal representation must be a vector"))
  (->> internal-representation
       add-missing-block-uids
       enhance-internal-representation
       (mapcat (partial tree-seq common-db/has-descendants? common-db/descendants))
       (map (partial enhanced-internal-representation->atomic-ops db default-position))
       flatten
       distinct
       move-save-ops-to-end
       vec))


(defn build-paste-op
  "For blocks creates `:block/new` and `:block/save` event and for page creates `:page/new`
   Arguments:
   - `db` db value
   - `uid` uid of the block where the internal representation needs to be pasted
   - `internal-representation` of the pages/blocks selected"

  ([db internal-representation]
   (composite/make-consequence-op {:op/type :block/paste} (internal-representation->atomic-ops db internal-representation nil)))
  ([db uid local-str internal-representation]
   (let [current-block-parent-uid (:block/uid (common-db/get-parent db [:block/uid uid]))
         {:block/keys [order
                       children
                       open
                       string]} (common-db/get-block db [:block/uid uid])
         ;; The parent of block depends on:
         ;; - if the current block is open and has chidren : if this is the case then we want the blocks to be pasted
         ;;   under the current block as its first children
         ;; - else the parent is the current block's parent
         current-block-parent?    (and children
                                       open)
         empty-block?             (and (string/blank? local-str)
                                       (empty? children))
         new-block-str?           (not= local-str string)
         ;; If block has a new local-str, write that
         block-save-op            (when new-block-str?
                                    (atomic/make-block-save-op uid local-str))
         ;; - If the block is empty then we delete the empty block and add new blocks. So in this case
         ;;   the block order for the new blocks is the same as deleted blocks order.
         ;; - If the block is parent then we want the blocks to be pasted as this blocks first children
         ;; - If the block is not empty then add the new blocks after the current one.
         new-block-order          (cond
                                    empty-block? order
                                    current-block-parent? 0
                                    :else (inc order))
         block-position           (cond
                                    empty-block? current-block-parent-uid
                                    current-block-parent? uid
                                    :else current-block-parent-uid)
         default-position         (common-db/compat-position db {:block/uid block-position
                                                                 :relation  new-block-order})
         ir-ops                   (internal-representation->atomic-ops db internal-representation default-position)
         remove-op                (when empty-block?
                                    (graph-ops/build-block-remove-op db uid))]
     (composite/make-consequence-op {:op/type :block/paste}
                                    (cond-> ir-ops
                                      new-block-str? (conj block-save-op)
                                      empty-block? (conj remove-op))))))


(defn db-from-repr
  [repr]
  (let [conn (common-db/create-conn)]
    (->> repr
         (build-paste-op @conn)
         common-events/build-atomic-event
         (atomic-resolver/resolve-transact! conn))
    @conn))
