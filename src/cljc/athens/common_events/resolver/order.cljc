(ns athens.common-events.resolver.order
  (:refer-clojure :exclude [remove])
  (:require
    [clojure.core :as c]))


(defn remove
  [v x]
  (vec (c/remove #{x} v)))


(defn- insert-at
  [v x n]
  (vec (concat (take n v) [x] (drop n v))))


(defn insert
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
  [from to x relation target]
  [(remove from x) (insert to x relation target)])


(defn move-within
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
  [before after map-fn]
  (let [before' (map-indexed map-fn before)
        after'  (map-indexed map-fn after)]
    (vec (c/remove (set before') after'))))
