(ns athens.types.tasks.handlers
  (:require
    [athens.common-events.graph.ops             :as graph-ops]
    [athens.types.tasks.shared :as shared]
    [re-frame.core :as rf]))


(defn update-task-status
  [task-uid new-status]
  (let [new-status (-> new-status shared/find-status-uid)
        new-status (str "((" new-status "))")]
    (rf/dispatch [:graph/update-in [:block/uid task-uid] [":task/status"]
                  (fn [db uid]
                    [(graph-ops/build-block-save-op db uid new-status)]
                    #_(if is-checked
                        [(graph-ops/build-block-save-op db uid (str "((" (find-status-uid "To Do") "))"))]
                        [(graph-ops/build-block-save-op db uid (str "((" (find-status-uid "Done") "))"))]))])))
