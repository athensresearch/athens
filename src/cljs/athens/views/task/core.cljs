(ns athens.views.task.core
  (:require [athens.common-events.bfs :as bfs]
            [athens.common.utils :as common.utils]
            [athens.common-events.graph.composite :as composite]))


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
