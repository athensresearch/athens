(ns athens.common-events.bfs
  (:require
    [athens.common-events.graph.atomic :as atomic]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common-events.graph.composite :as composite]))


(defn conjoin-to-queue
  [q data-to-be-added]
  (reduce #(conj %1 %2) q data-to-be-added))


(defn get-children-uids
  [children]
  (reduce #(conj %1 (:block/uid %2)) [] children))


(defn get-individual-blocks
  "Given internal representation (basically a tree) get individual blocks from the representation.
   Walk the tree in a bfs manner, extract the individual blocks and the parent-child relationship."
  [tree]
  (loop [res []
         parent<->child {}
         q   (conjoin-to-queue clojure.lang.PersistentQueue/EMPTY
                               tree)]
    (if (seq q)
      (let [{:block/keys [uid
                          order
                          open
                          string
                          children]
             :node/keys  [title]}      (peek q)
            block                      {:block/uid    uid
                                        :block/order  order
                                        :block/open   open
                                        :block/string string
                                        ; Used for page
                                        :node/title   title}
            children-uids              (get-children-uids children)
            new-key-value              (zipmap children-uids
                                               (take (count children-uids) (cycle [uid])))
            new-q                      (pop q)]
        (recur (conj res block)
               (merge parent<->child
                      new-key-value)
               (if (seq children)
                 (conjoin-to-queue new-q children)
                 new-q)))
     [res  parent<->child])))


(defn internal-repr->atomic-ops
  "Takes the internal representation of a block and creates :block/new and :block/save atomic
   events from it."
  [db block-internal-representation parent-uid]
  (let [{block-uid    :block/uid
         block-string :block/string
         block-order  :block/order
         node-title   :node/title}   block-internal-representation
        new-page-op                  (atomic/make-page-new-op node-title
                                                              block-uid)
        new-block-op                 (atomic/make-block-new-op block-uid
                                                               parent-uid
                                                               block-order)
        block-save-op                (graph-ops/build-block-save-op db
                                                                  block-uid
                                                                  ""
                                                                  block-string)
        all-ops                      (if node-title
                                       [new-page-op]
                                       [new-block-op
                                        block-save-op])
        block-paste-op               (composite/make-consequence-op {:op-type :block/paste}
                                                                    all-ops)]
    block-paste-op))