(ns athens.views.task.core
  (:require
    ["@chakra-ui/react"                   :refer [Box,
                                                  FormControl,
                                                  FormLabel,
                                                  FormErrorMessage,
                                                  FormHelperText]]
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common.logging                :as log]
    [athens.common.utils                  :as common.utils]
    [athens.db                            :as db]
    [athens.reactive                      :as reactive]
    [athens.types.tasks.events            :as task-events]
    [athens.views.blocks.content          :as content-editor]
    [athens.views.blocks.types            :as types]
    [athens.views.blocks.types.dispatcher :as dispatcher]
    [clojure.string                       :as str]
    [goog.functions                       :as gfns]
    [re-frame.core                        :as rf]
    [reagent.core                         :as r]
    [reagent.ratom                        :as ratom]))


;; Create a new task
(defn new-task
  [db block-uid position title description priority creator assignee due-date status project-relation]
  (->> (bfs/internal-representation->atomic-ops
         db
         [#:block{:uid        (common.utils/gen-block-uid)
                  :string     ""
                  :properties {":block/type"
                               #:block{:string "[[athens/task]]"
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
                               ":task/project"
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

(defn task-title-view
  [_parent-block-uid title-block-uid]
  (let [title-id (str (random-uuid))]
    ;; TODO discover it title prop is missing, if so, create it
    ;; NOTE this really should not be the case, unless user messed block structure
    ;; nevertheless this should be general patter for all property editors
    (when-not title-block-uid
      (let []))    
    
    (fn [parent-block-uid title-block-uid]
      (let [title-block    (reactive/get-reactive-block-document [:block/uid title-block-uid])
            title          (or (:block/string title-block) "")
            local-value    (r/atom title)
            invalid-title? (and (str/blank? title)
                                (not (nil? title)))
            save-fn        (fn [e]
                             (let [new-value (-> e .-target .-value)]
                               (log/debug "title-save-fn" (pr-str new-value))
                               (reset! local-value new-value)))
            update-fn     #(do
                             (log/debug "update-fn:" (pr-str %))
                             (when-not (= title %)
                               (reset! local-value %)
                               (rf/dispatch [::task-events/save-title
                                             {:parent-block-uid parent-block-uid
                                              :title            %}])))
            idle-fn        (gfns/debounce #(do
                                             (log/debug "title-idle-fn" (pr-str @local-value))
                                             (update-fn @local-value))
                                          2000)
            read-value    local-value
            show-edit?    (r/atom true)
            state-hooks   {:save-fn       save-fn
                           :idle-fn       idle-fn
                           :update-fn     update-fn
                           :read-value    read-value
                           :show-edit?    show-edit?}]
        [:> FormControl {:is-required true
                         :is-invalid  invalid-title?}
         [:> FormLabel {:html-for title-id}
          "Task Title"]
         ;; TODO: Use actual editor
         [:> Box {:px 2
                  :mt 2
                  :minHeight "2.125em"
                  :borderRadius "sm"
                  :bg "background.attic"
                  :cursor "text"
                  :_focusWithin {:shadow "focus"}}
          [content-editor/block-content-el {:block/uid    title-block-uid
                                            }
           state-hooks]]
         #_[:> Input {:value     (or title "")
                      :id        title-id
                      :on-change (fn [e]
                                   (let [value (-> e .-target .-value)]
                                     ))}]
         (if invalid-title?
           [:> FormErrorMessage "Task title is required"]
           [:> FormHelperText "Please provide Task title"])]))))


(defrecord TaskView
  []

  types/BlockTypeProtocol

  (inline-ref-view
    [this block-data attr ref-uid uid callbacks with-breadcrumb?])


  (outline-view
    [this block-data block-el callbacks]
    (let [block-uid      (:block/uid block-data)
          reactive-block (reactive/get-reactive-block-document [:block/uid block-uid])]
      (fn [this block-data block-el callbacks]
        (let [{title-uid :block/uid} (->> reactive-block
                                          :block/properties
                                          (filter (fn [[k _v]] (= ":task/title" k)))
                                          (map second)
                                          first)]
          [:div {:class "task_container"}
           [task-title-view block-uid title-uid]]))))


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
