(ns athens.common-events.bfs
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.graph.atomic    :as atomic]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [clojure.string                       :as string]
    [clojure.walk                         :as walk]))


(defn enhance-block
  [block previous parent]
  (merge block
         {:parent (let [page-ks (select-keys parent [:page/title])]
                    (if (empty? page-ks)
                      (select-keys parent [:block/uid])
                      page-ks))}
         {:previous (select-keys previous [:block/uid])}))


(defn enhance-children
  [children parent]
  ;; Partition by 2 with nil first/last elements to get each [previous current] pair.
  ;; https://stackoverflow.com/a/41925223/2116927
  (->> (concat [nil] children [nil])
       (partition 2 1)
       (map (fn [[previous current]]
              (when current (enhance-block current previous parent))))
       (remove nil?)
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
        (if-some [children (:block/children x)]
          (assoc x :block/children (enhance-children children x))
          x))
      (concat blocks' pages))))


(defn enhanced-internal-representation->atomic-ops
  "Takes the enhanced internal representation and creates :page/new or :block/new and :block/save atomic events.
  Throws if default-position is nil and position cannot be determined."
  [db default-position {:keys [block/uid block/string block/open? page/title previous parent] :as eir}]
  (if title
    [(atomic/make-page-new-op title)]
    (let [{parent-title :page/title
           parent-uid   :block/uid} parent
          previous-uid              (:block/uid previous)
          position                  (cond
                                      ;; There's a block before this one that we can add this one after.
                                      previous-uid {:block/uid previous-uid   :relation :after}
                                      ;; There's no previous block, but we can add it to the end of the parent.
                                      parent-title {:page/title parent-title :relation :last}
                                      parent-uid   {:block/uid parent-uid     :relation :last}
                                      ;; There's a default place where we can drop blocks, use it.
                                      default-position default-position
                                      :else (throw (ex-info "Cannot determine position for enhanced internal representation" eir)))
          save-op                   (graph-ops/build-block-save-op db uid string)
          atomic-save-ops           (if (graph-ops/atomic-composite? save-op)
                                      (graph-ops/extract-atomics save-op)
                                      [save-op])]
      (cond-> (into [(atomic/make-block-new-op uid position)] atomic-save-ops)
        (= open? false) (conj (atomic/make-block-open-op uid false))))))


(defn move-save-ops-to-end
  [coll]
  (let [{save true
         not-save false} (group-by #(= (:op/type %) :block/save) coll)]
    (concat [] not-save save)))


(defn internal-representation->atomic-ops
  "Convert internal representation to the vector of atomic operations that would create it.
  :block/save operations are grouped at the end so that any ref'd entities are already created."
  [db internal-representation default-position]
  (->> internal-representation
       enhance-internal-representation
       (mapcat (partial tree-seq :block/children :block/children))
       (map (partial enhanced-internal-representation->atomic-ops db default-position))
       flatten
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
         ;; do we need compat position? the 3 cases are X, :first, and :after. I'm not sure about the case of empty-block?
         default-position         (common-db/compat-position db {:block/uid block-position
                                                                 :relation  new-block-order})
         ir-ops                   (internal-representation->atomic-ops db internal-representation default-position)
         remove-op                (when empty-block?
                                    (graph-ops/build-block-remove-op db uid))]
     (composite/make-consequence-op {:op/type :block/paste}
                                    (cond-> ir-ops
                                      new-block-str? (conj block-save-op)
                                      empty-block? (conj remove-op))))))

