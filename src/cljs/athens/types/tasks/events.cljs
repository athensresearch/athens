(ns athens.types.tasks.events
  (:require
    [athens.db :as db]
    [athens.common-db                           :as common-db]
    [athens.common-events                       :as common-events]
    [athens.common-events.graph.ops             :as graph-ops]
    [athens.common-events.bfs                   :as bfs]
    [athens.common-events.graph.composite       :as composite]
    [re-frame.core :as rf]
    [athens.types.tasks.shared :as shared]))



(defn on-update-status
  [task-uid new-status]
  (let [new-status (-> new-status shared/find-status-uid)
        new-status (str "((" new-status "))")]
    (rf/dispatch [:graph/update-in [:block/uid task-uid] [":task/status"]
                  (fn [db uid]
                    [(graph-ops/build-block-save-op db uid new-status)]
                    #_(if is-checked
                        [(graph-ops/build-block-save-op db uid (str "((" (find-status-uid "To Do") "))"))]
                        [(graph-ops/build-block-save-op db uid (str "((" (find-status-uid "Done") "))"))]))])))