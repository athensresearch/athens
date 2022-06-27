(ns athens.views.task.core
  (:require [athens.common-events.bfs :as bfs]
            [athens.common.utils :as common.utils]
            [athens.common-events.graph.composite :as composite]
            [athens.common-db :as common-db]
            [athens.db :as db]
            [athens.common-events :as common-events]))


;; Create a new task
(defn new-task
  [db block-uid position title description priority creator assignee due-date status project-relation]
  (->> (bfs/internal-representation->atomic-ops db
                                                [#:block{:uid    (common.utils/gen-block-uid)
                                                         :string ""
                                                         :properties
                                                         {":block/type"               #:block{:string "athens/task"
                                                                                              :uid    (common.utils/gen-block-uid)}
                                                          ":task/title"               #:block{:string title
                                                                                              :uid    (common.utils/gen-block-uid)}
                                                          ":task/description"         #:block{:string description
                                                                                              :uid  (common.utils/gen-block-uid)}
                                                          ":task/priority"            #:block{:string priority
                                                                                              :uid (common.utils/gen-block-uid)}
                                                          ":task/creator"             #:block{:string creator
                                                                                              :uid (common.utils/gen-block-uid)}
                                                          ":task/assignee"            #:block{:string assignee
                                                                                              :uid (common.utils/gen-block-uid)}
                                                          ":task/due-date"            #:block{:string due-date
                                                                                              :uid (common.utils/gen-block-uid)}
                                                          ":task/status"              #:block{:string status
                                                                                              :uid (common.utils/gen-block-uid)}
                                                          ":comment/project-relation" #:block{:string project-relation
                                                                                              :uid    (common.utils/gen-block-uid)}}}]
                                                {:block/uid block-uid
                                                 :relation  position})
       (composite/make-consequence-op {:op/type :new-type})))



;; Update the task properties
;; Need to update the block string for updating the property value
;; For creation we can use block/new and to add a new property we use block/move
(defn update-task-properties
  [db task-block-uid new-properties-map]
  ;; TODO Update the block string for updating the value of a property.
  (let [task-properties       (common-db/get-block-property-document @db/dsdb [:block/uid task-block-uid])
        updated-properties    (merge task-properties new-properties-map)

        updated-properties-op (composite/make-consequence-op {:op/type :update-task-properties}
                                                             updated-properties)
        event                 (common-events/build-atomic-event updated-properties-op)]
    {:fx [[:dispatch [:resolve-transact-forward event]]]}))

