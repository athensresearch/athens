(ns athens.blocks)


(defn sort-block
  [block]
  (if-let [children (seq (:block/children block))]
    (assoc block :block/children
           (sort-by :block/order (map sort-block children)))
    block))
