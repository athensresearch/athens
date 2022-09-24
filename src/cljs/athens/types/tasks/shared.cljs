(ns athens.types.tasks.shared
  (:require
    [athens.common-db                           :as common-db]
    [athens.common-events.bfs                   :as bfs]
    [athens.reactive :as reactive]
    [re-frame.core :as rf]))


;; Create default task statuses configuration

(defn internal-representation-allowed-stauses
  []
  [{:block/string "To Do"}
   {:block/string "Doing"}
   {:block/string "Blocked"}
   {:block/string "Done"}
   {:block/string "Cancelled"}])


(defn internal-representation-allowed-priorities
  []
  [{:block/string "Expedite"}
   {:block/string "P1"}
   {:block/string "P2"}
   {:block/string "P3"}
   {:block/string "Nice to have"}])


(defn find-allowed-priorities
  []
  (let [task-priority-page  (reactive/get-reactive-node-document [:node/title ":task/priority"])
        allowed-prio-blocks (-> task-priority-page
                                :block/properties
                                (get ":property/enum")
                                :block/children)
        allowed-priorities  (map #(select-keys % [:block/uid :block/string]) allowed-prio-blocks)]
    (when-not allowed-prio-blocks
      (rf/dispatch [:graph/update-in [:node/title ":task/priority"] [":property/enum"]
                    (fn [db uid]
                      (when-not (common-db/block-exists? db [:block/uid uid])
                        (bfs/internal-representation->atomic-ops db (internal-representation-allowed-priorities)
                                                                 {:block/uid uid :relation :first})))]))
    (when (seq allowed-priorities)
      allowed-priorities)))


(defn find-allowed-statuses
  []
  (let [task-status-page    (reactive/get-reactive-node-document [:node/title ":task/status"])
        allowed-stat-blocks (-> task-status-page
                                :block/properties
                                (get ":property/enum")
                                :block/children)
        allowed-statuses    (map #(select-keys % [:block/uid :block/string]) allowed-stat-blocks)]
    (when-not allowed-stat-blocks
      (rf/dispatch [:graph/update-in [:node/title ":task/status"] [":property/enum"]
                    (fn [db uid]
                      (when-not (common-db/block-exists? db [:block/uid uid])
                        (bfs/internal-representation->atomic-ops db (internal-representation-allowed-stauses)
                                                                 {:block/uid uid :relation :first})))]))
    (when (seq allowed-statuses)
      allowed-statuses)))


(defn find-status-uid
  [status]
  (->> (filter (fn [allowed-status]
                 (= status (:block/string allowed-status)))
               (find-allowed-statuses))
       first
       :block/uid))


