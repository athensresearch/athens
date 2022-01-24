(ns athens.undo
  (:refer-clojure :exclude [remove])
  (:require
    [flatland.ordered.map :refer [ordered-map]]))


(defn- sliding-assoc
  "Assoc k v into om, keeping it at max limit elements.
  Older elements are dropped.
  om should be an ordered map of size <= limit."
  [om limit k v]
  (let [om' (cond
              (contains? om k)      om
              (>= (count om) limit) (dissoc om (ffirst om))
              :else                 om)]
    (assoc om' k v)))


(def UNDO_LIMIT 20)


(defn reset
  [db]
  (assoc db
         ::undo (ordered-map)
         ::redo (ordered-map)))


(defn reset-redo
  [db]
  (assoc db ::redo (ordered-map)))


(defn count-undo
  [db]
  (count (::undo db)))


(defn push-undo
  [db k v]
  (update db ::undo sliding-assoc UNDO_LIMIT k v))


(defn pop-undo
  [db]
  (when-some [[k v] (last (::undo db))]
    [v (update db ::undo dissoc k)]))


(defn count-redo
  [db]
  (count (::redo db)))


(defn push-redo
  [db k v]
  (update db ::redo sliding-assoc UNDO_LIMIT k v))


(defn pop-redo
  [db]
  (when-some [[k v] (last (::redo db))]
    [v (update db ::redo dissoc k)]))


(defn remove
  [db k]
  (-> db
      (update ::undo dissoc k)
      (update ::redo dissoc k)))


;; TODO: consider if we need to update the undo/redo stack dbs on rollback/rollforward.
;; If we don't update, then the undo/redo will resolve over the original db the event was applied over.
;; If we update, then the undo/redo will resolve over the latest db the event was applied over.
#_(defn update-val
  [_db _k _v])
