(ns athens.views.task.core
  (:require
    ["@chakra-ui/react"                   :refer [FormControl,
                                                  FormLabel,
                                                  FormErrorMessage,
                                                  FormHelperText
                                                  Input
                                                  Box Button ButtonGroup IconButton  MenuList MenuItem]]
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common.utils                  :as common.utils]
    [athens.db                            :as db]
    [athens.views.blocks.types            :as types]
    [athens.views.blocks.types.dispatcher :as dispatcher]
    [clojure.string                       :as str]))


;; Create a new task
(defn new-task
  [db block-uid position title description priority creator assignee due-date status project-relation]
  (->> (bfs/internal-representation->atomic-ops
         db
         [#:block{:uid        (common.utils/gen-block-uid)
                  :string     ""
                  :properties {":block/type"
                               #:block{:string "athens/task"
                                       :uid    (common.utils/gen-block-uid)}
                               ":task/id"
                               #:block{:string "TASK-1"
                                       :uid    (common.utils/gen-block-uid)}
                               ":task/title"
                               #:block{:string title
                                       :uid    (common.utils/gen-block-uid)}
                               ":task/description"
                               #:block{:string description
                                       :uid    (common.utils/gen-block-uid)}
                               ":task/priority"
                               #:block{:string priority
                                       :uid    (common.utils/gen-block-uid)}
                               ":task/creator"
                               #:block{:string creator
                                       :uid    (common.utils/gen-block-uid)}
                               ":task/assignee"
                               #:block{:string assignee
                                       :uid    (common.utils/gen-block-uid)}
                               ":task/due-date"
                               #:block{:string due-date
                                       :uid    (common.utils/gen-block-uid)}
                               ":task/status"
                               #:block{:string status
                                       :uid    (common.utils/gen-block-uid)}
                               ":comment/project-relation"
                               #:block{:string project-relation
                                       :uid    (common.utils/gen-block-uid)}}}]
         {:block/uid block-uid
          :relation  position})
       (composite/make-consequence-op {:op/type :new-type})))


;; Update the task properties
(defn update-task-properties
  [db task-block-uid new-properties-map]
  (let [task-properties       (common-db/get-block-property-document @db/dsdb [:block/uid task-block-uid])
        ops                   (concat
                                (for [[prop-name prop-value] new-properties-map]
                                  (let [[new-uid prop-ops] (graph-ops/build-property-path db task-block-uid [prop-name])
                                        save-op            (graph-ops/build-block-save-op db new-uid prop-value)]
                                    (if-not (= (get task-properties prop-name) prop-value)
                                      (conj prop-ops save-op)
                                      []))))
        updated-properties-op (composite/make-consequence-op {:op/type :update-task-properties}
                                                             ops)
        event                 (common-events/build-atomic-event updated-properties-op)]
    {:fx [[:dispatch [:resolve-transact-forward event]]]}))


;; View

(defrecord TaskView
  []

  types/BlockTypeProtocol

  (inline-ref-view
    [this block-data attr ref-uid uid callbacks with-breadcrumb?])


  (outline-view
    [this block-data block-el callbacks]
    (let [task-properties (common-db/get-block-property-document @db/dsdb [:block/uid (:block/uid block-data)])
          title           (get task-properties ":task/title")
          invalid-title?  (and (str/blank? title)
                               (not (nil? title)))
          title-id        (str (random-uuid))]
      (fn [this block-data block-el callbacks]
        [:div {:class "task_container"}
         [:> FormControl {:is-required true
                          :is-invalid  invalid-title?}
          [:> FormLabel {:html-for title-id}
           "Task Title"]
          [:> Input {:value     (or title "")
                     :id        title-id
                     :on-change (fn [e]
                                  (let [value (-> e .-target .-value)]
                                    (println "TODO: new title value: " (pr-str value))))}]
          (if invalid-title?
            [:> FormErrorMessage "Task title is required"]
            [:> FormHelperText "Please provide Task title"])]])))


  (supported-transclusion-scopes
    [this])


  (transclusion-view
    [this block-el block-uid callback transclusion-scope])


  (zoomed-in-view
    [this block-data callbacks])


  (supported-breadcrumb-styles
    [this])


  (breadcrumbs-view
    [this block-data callbacks breadcrumb-style]))


(defmethod dispatcher/block-type->protocol "athens/task" [_block-type args-map]
  (TaskView.))
