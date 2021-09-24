(ns eventsync.core
  (:require [flatland.ordered.map :refer [ordered-map]]))


(defn create-stage [label]
  {:label label
   :events (ordered-map)})

(defn stage-map-entry [stage]
  [(:label stage) stage])

(defn op [type label uid event noop?])

(defn update-last-operation [state op]
  (-> state
      (update :last-op op)
      (update :op-count inc)))

(defn uid-stage [state uid])

(defn stage> [labels label])

(defn stage< [labels label])

(defn previous-stage [labels labe])

(defn promotion? [state from to uid])

(defn promote [state label uid event])

;; API

(defn create-state [id labels]
  {:id       id
   :labels   labels
   :stages   (into (ordered-map) (mapv (comp stage-map-entry create-stage) labels))
   :last-op  :initialization
   :op-count 0})

(defn add [state label uid event])

(defn remove [state label uid event])

(defn log [state])

(comment
  (-> (create-state :mario [:one :two :three])
      :stages
      seq)

  )

