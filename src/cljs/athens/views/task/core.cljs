(ns athens.views.task.core
  (:require
    ["@chakra-ui/react"                   :refer [Box,
                                                  FormControl,
                                                  FormLabel,
                                                  FormErrorMessage,
                                                  FormHelperText,
                                                  Select
                                                  HStack
                                                  VStack]]
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common.logging                :as log]
    [athens.common.utils                  :as common.utils]
    [athens.db                            :as db]
    [athens.reactive                      :as reactive]
    [athens.self-hosted.presence.views    :as presence]
    [athens.types.tasks.events            :as task-events]
    [athens.views.blocks.content          :as content-editor]
    [athens.views.blocks.types            :as types]
    [athens.views.blocks.types.dispatcher :as dispatcher]
    [clojure.string                       :as str]
    [goog.functions                       :as gfns]
    [re-frame.core                        :as rf]
    [reagent.core                         :as r]))


;; Create default task statuses configuration

(defn- internal-representation-allowed-stauses
  []
  [#:block{:uid    (common.utils/gen-block-uid)
           :string "To Do"}
   #:block{:uid    (common.utils/gen-block-uid)
           :string "Doing"}
   #:block{:uid    (common.utils/gen-block-uid)
           :string "Blocked"}
   #:block{:uid    (common.utils/gen-block-uid)
           :string "Done"}
   #:block{:uid    (common.utils/gen-block-uid)
           :string "Cancelled"}])


(defn- internal-representation-task-status-property-enum
  []
  {":property/enum" #:block{:uid      (common.utils/gen-block-uid)
                            :string   ""
                            :children (internal-representation-allowed-stauses)}})


(defn- internal-representation-task-status-page
  []
  {:page/title       ":task/status"
   :block/properties (internal-representation-task-status-property-enum)})


(defn- ensure-task-status-property-enum
  [_db _task-status-page]
  #_(let []
       ;; TODO
       ;; 3. find ":property/enum" prop of this page
       ;; 4. create if not found
       ;; 5. create default statuses
       ;; 6. return :block/uid :block/string of these
       ))


(defn- extract-allowed-statuses
  [ops]
  (let [block-saves (graph-ops/contains-op? ops :block/save)]
    (->> block-saves
         (mapcat #(graph-ops/contains-op? % :block/save))
         (map :op/args)
         (filter #(contains? #{"To Do" "Doing" "Blocked" "Done" "Cancelled"} (:block/string %))))))


(defn- create-task-status-page
  [db]
  (let [internal-repr    (internal-representation-task-status-page)
        page-create-ops  (->> (bfs/internal-representation->atomic-ops db [internal-repr] nil)
                              (composite/make-consequence-op {:op/type :create-task-statuses}))
        allowed-statuses (extract-allowed-statuses page-create-ops)]
    [allowed-statuses page-create-ops]))


(defn- create-default-allowed-statuses
  [db]
  ;; 1. find :task/status page
  (let [task-status-page (common-db/get-page-document db [:node/title ":task/status"])
        [allowed-statuses
         create-ops]     (if task-status-page
                           ;; page exists
                           (ensure-task-status-property-enum db task-status-page)
                           ;; 2. create if not found
                           (create-task-status-page db))]
    [allowed-statuses create-ops]))


;; Create a new task
(defn new-task
  [db block-uid position title description priority creator assignee due-date status _projects]
  ;; TODO verify `status` correctness
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
                               ;; NOTE Task belonging to a Project is maintained on side of a Project
                               #_#_
                               ":task/projects"
                               #:block{:string   ""
                                       :uid      (common.utils/gen-block-uid)
                                       :children (for [project projects]
                                                   #:block{:string project
                                                           :uid    (common.utils/gen-block-uid)})}}}]
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


(defn generic-textarea-view-for-task-props
  [_parent-block-uid _prop-block-uid _prop-name _prop-title _required?]
  (let [prop-id (str (random-uuid))]
    (fn [parent-block-uid prop-block-uid prop-name prop-title required?]
      (let [prop-block         (reactive/get-reactive-block-document [:block/uid prop-block-uid])
            prop-str           (or (:block/string prop-block) "")
            local-value        (r/atom prop-str)
            invalid-prop-str?  (and (str/blank? prop-str)
                                    (not (nil? prop-str)))
            save-fn            (fn [e]
                                 (let [new-value (-> e .-target .-value)]
                                   (log/debug prop-name "save-fn" (pr-str new-value))
                                   (reset! local-value new-value)))
            update-fn          #(do
                                  (log/debug prop-name "update-fn:" (pr-str %))
                                  (when-not (= prop-str %)
                                    (reset! local-value %)
                                    (cond
                                      (= prop-name ":task/title")       (rf/dispatch [::task-events/save-title
                                                                                      {:parent-block-uid parent-block-uid
                                                                                       :title            %}])
                                      (= prop-name ":task/description") (rf/dispatch [::task-events/save-description
                                                                                      {:parent-block-uid parent-block-uid
                                                                                       :description      %}])
                                      (= prop-name ":task/due-date")    (rf/dispatch [::task-events/save-title
                                                                                      {:parent-block-uid parent-block-uid
                                                                                       :due-date         %}]))))

            idle-fn            (gfns/debounce #(do
                                                 (log/debug prop-name "idle-fn" (pr-str @local-value))
                                                 (update-fn @local-value))
                                              2000)
            read-value         local-value
            show-edit?         (r/atom true)
            state-hooks        {:save-fn    save-fn
                                :idle-fn    idle-fn
                                :update-fn  update-fn
                                :read-value read-value
                                :show-edit? show-edit?}]
        [:> FormControl {:is-required required?
                         :is-invalid  invalid-prop-str?}
         [:> FormLabel {:html-for prop-id}
          prop-title]
         [:> Box {:px           2
                  :mt           2
                  :minHeight    "2.125em"
                  :borderRadius "sm"
                  :bg           "background.attic"
                  :cursor       "text"
                  :_focusWithin {:shadow "focus"}}
          ;; NOTE: we generate temporary uid for prop if it doesn't exist, so editor can work
          [content-editor/block-content-el {:block/uid (or prop-block-uid
                                                           ;; NOTE: temporary magic, stripping `:task/` 🤷‍♂️
                                                           (str "tmp-" (subs prop-name
                                                                             (.indexOf prop-name "/"))
                                                                "-uid-" (common.utils/gen-block-uid)))}
           state-hooks]
          [presence/inline-presence-el prop-block-uid]]

         (if invalid-prop-str?
           [:> FormErrorMessage
            (str prop-title " is " (if required?
                                     "required"
                                     "empty"))]
           [:> FormHelperText
            (str "Please provide " prop-title)])]))))


(defn- find-allowed-statuses
  []
  (let [task-status-page    (reactive/get-reactive-node-document [:node/title ":task/status"])
        allowed-stat-blocks (-> task-status-page
                                :block/properties
                                (get ":property/enum")
                                :block/children)
        allowed-statuses    (map #(select-keys % [:block/uid :block/string]) allowed-stat-blocks)]
    (println "task status page\n"
             (pr-str task-status-page))
    (if (seq allowed-statuses)
      allowed-statuses
      (let [[statuses ops] (create-default-allowed-statuses @db/dsdb)
            event          (common-events/build-atomic-event ops)]
        (rf/dispatch [:resolve-transact-forward event])
        statuses))))


(defn task-status-view
  [parent-block-uid status-block-uid]
  (let [status-id        (str (random-uuid))
        status-block     (reactive/get-reactive-block-document [:block/uid status-block-uid])
        allowed-statuses (find-allowed-statuses)
        status-string    (:block/string status-block "(())")
        status-uid       (subs  status-string 2 (- (count status-string) 2))]
    [:> FormControl {:is-required true}
     [:> HStack {:spacing "2rem"}
      [:> FormLabel {:html-for status-id
                     :w        "9rem"}
       "Task Status"]
      [:> Select {:id          status-id
                  :value       status-uid
                  :placeholder "Select a status"
                  :on-change   (fn [e]
                                 (let [new-status (-> e .-target .-value)]
                                   (rf/dispatch [::task-events/save-status
                                                 {:parent-block-uid parent-block-uid
                                                  :status           (str "((" new-status "))")}])))}
       (doall
         (for [{:block/keys [uid string]} allowed-statuses]
           ^{:key uid}
           [:option {:value uid}
            string]))]]]))


(defn- find-property-block-by-key-name
  [entity-block prop-name]
  (->> entity-block
       :block/properties
       (filter (fn [[k _v]] (= prop-name k)))
       (map second)
       first))


(defrecord TaskView
  []

  types/BlockTypeProtocol

  (inline-ref-view
    [_this _block-data _attr _ref-uid _uid _callbacks _with-breadcrumb?])


  (outline-view
    [_this block-data _callbacks]
    (let [block-uid (:block/uid block-data)]
      (fn [_this _block-data _callbacks]
        (let [reactive-block  (reactive/get-reactive-block-document [:block/uid block-uid])
              title-uid       (:block/uid (find-property-block-by-key-name reactive-block ":task/title"))
              description-uid (:block/uid (find-property-block-by-key-name reactive-block ":task/description"))
              due-date-uid    (:block/uid (find-property-block-by-key-name reactive-block ":task/due-date"))
              ;; projects-uid  (:block/uid (find-property-block-by-key-name reactive-block ":task/projects"))
              status-uid      (:block/uid (find-property-block-by-key-name reactive-block ":task/status"))]
          [:> VStack {:spacing "2rem"
                      :class   "task_container"}
           [generic-textarea-view-for-task-props block-uid title-uid ":task/title" "Task Title" true]
           [generic-textarea-view-for-task-props block-uid description-uid ":task/description" "Task Description" false]
           ;; Making assumption that for now we can add due date manually without date-picker.
           [generic-textarea-view-for-task-props block-uid due-date-uid ":task/due-date" "Task Due Date" false]
           [task-status-view block-uid status-uid]]))))


  (supported-transclusion-scopes
    [_this])


  (transclusion-view
    [_this _block-el _block-uid _callback _transclusion-scope])


  (zoomed-in-view
    [_this _block-data _callbacks])


  (supported-breadcrumb-styles
    [_this])


  (breadcrumbs-view
    [_this _block-data _callbacks _breadcrumb-style]))


(defmethod dispatcher/block-type->protocol "[[athens/task]]" [_block-type _args-map]
  (TaskView.))
