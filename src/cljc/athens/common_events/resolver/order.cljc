(ns athens.common-events.resolver.order
  (:refer-clojure :exclude [remove])
  (:require
    [clojure.core :as c]))


(defn remove
  "Remove x from v."
  [v x]
  (vec (c/remove #{x} v)))


(defn- insert-at
  [v x n]
  (vec (concat (take n v) [x] (drop n v))))


(defn insert
  "Insert x in v, in a position defined by relation to target.
  See athens.common-events.graph.schema for position values."
  [v x relation target]
  (let [n (when (and target (#{:before :after} relation))
            (let [n (.indexOf v target)]
              (if (= n -1)
                nil
                n)))]
    (cond
      (= relation :first)  (into [x] v)
      (= relation :last)   (into v [x])
      (and n (= relation :before)) (insert-at v x n)
      (and n (= relation :after))  (insert-at v x (inc n))
      :else v)))


(defn move-between
  "Move x from origin to destination, to a position defined by relation to target.
  See athens.common-events.graph.schema for position values.
  Returns [modified-origin modified-destination]."
  [origin destination x relation target]
  [(remove origin x) (insert destination x relation target)])


(defn move-within
  "Move x within v, to a position defined by relation to target.
  See athens.common-events.graph.schema for position values.
  Returns modified v."
  [v x relation target]
  (-> v
      (remove x)
      (insert x relation target)))


(defn block-map-fn
  [n x]
  {:block/uid x
   :block/order n})


(defn shortcut-map-fn
  [n x]
  {:node/title x
   :page/sidebar n})


(defn reorder
  "Maps each element in before and after using map-indexed over map-fn.
  Returns all elements in after that are not in before.
  Use with block-map-fn and shortcut-map-fn to obtain valid datascript
  transactions that will reorder those elements using absolute positions."
  [before after map-fn]
  (let [before' (map-indexed map-fn before)
        after'  (map-indexed map-fn after)]
    (vec (c/remove (set before') after'))))
