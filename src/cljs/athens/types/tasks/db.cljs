(ns athens.types.tasks.db
  (:require
    [athens.common-db :as common-db]))


(defn get-title-block-of-task
  [db uid]
  (let [block (common-db/get-block-property-document db [:block/uid uid])]
    (get block ":task/title")))
