(ns athens.types.tasks.view
  "Views for Athens Tasks"
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
    [athens.types.core                    :as types]
    [athens.types.dispatcher              :as dispatcher]
    [athens.views.blocks.editor           :as editor]
    [clojure.string                       :as str]
    [goog.functions                       :as gfns]
    [re-frame.core                        :as rf]
    [reagent.core                         :as r]))


;; Create default task statuses configuration

(defn- internal-representation-allowed-stauses
  []
  [{:block/string "To Do"}
   {:block/string "Doing"}
   {:block/string "Blocked"}
   {:block/string "Done"}
   {:block/string "Cancelled"}])


(defn- internal-representation-allowed-priorities
  []
  [{:block/string "Expedite"}
   {:block/string "P1"}
   {:block/string "P2"}
   {:block/string "P3"}
   {:block/string "Nice to have"}])


;; Create a new task
(defn new-task
  [db block-uid position title description priority creator assignee due-date status _projects]
  ;; TODO verify `status` correctness
  (->> (bfs/internal-representation->atomic-ops
         db
         [#:block{:string     ""
                  :properties {":block/type"
                               {:block/string "[[athens/task]]"}
                               ":task/title"
                               {:block/string title}
                               ":task/description"
                               {:block/string description}
                               ":task/priority"
                               {:block/string priority}
                               ":task/creator"
                               {:block/string creator}
                               ":task/assignee"
                               {:block/string assignee}
                               ":task/due-date"
                               {:block/string due-date}
                               ":task/status"
                               {:block/string status}
                               ;; NOTE Task belonging to a Project is maintained on side of a Project
                               #_#_
                               ":task/projects"
                               #:block{:string   ""
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
  [_parent-block-uid _prop-block-uid _prop-name _prop-title _required? _multiline?]
  (let [prop-id (str (random-uuid))]
    (fn [parent-block-uid prop-block-uid prop-name prop-title required? multiline?]
      (let [prop-block          (reactive/get-reactive-block-document [:block/uid prop-block-uid])
            prop-str            (or (:block/string prop-block) "")
            local-value         (r/atom prop-str)
            invalid-prop-str?   (and (str/blank? prop-str)
                                     (not (nil? prop-str)))
            save-fn             (fn
                                  ([]
                                   (log/debug prop-name "save-fn" (pr-str @local-value))
                                   (when (#{":task/title" ":task/description" ":task/due-date"} prop-name)
                                     (rf/dispatch [:properties/update-in [:block/uid parent-block-uid] [prop-name]
                                                   (fn [db uid] [(graph-ops/build-block-save-op db uid @local-value)])])))
                                  ([e]
                                   (let [new-value (-> e .-target .-value)]
                                     (log/debug prop-name "save-fn" (pr-str new-value))
                                     (reset! local-value new-value)
                                     (when (#{":task/title"
                                              ":task/assignee"
                                              ":task/description"
                                              ":task/due-date"} prop-name)
                                       (rf/dispatch [:properties/update-in [:block/uid parent-block-uid] [prop-name]
                                                     (fn [db uid] [(graph-ops/build-block-save-op db uid new-value)])])))))
            update-fn           #(do
                                   (when-not (= prop-str %)
                                     (log/debug prop-name "update-fn:" (pr-str %))
                                     (reset! local-value %)))
            idle-fn             (gfns/debounce #(do
                                                  (log/debug prop-name "idle-fn" (pr-str @local-value))
                                                  (save-fn))
                                               2000)
            read-value          local-value
            show-edit?          (r/atom true)
            custom-key-handlers {:enter-handler (if multiline?
                                                  editor/enter-handler-new-line
                                                  (fn [_uid _d-key-down]
                                                    ;; TODO dispatch save and jump to next input
                                                    (println "TODO dispatch save and jump to next input")
                                                    (update-fn @local-value)))
                                 :tab-handler   (fn [_uid _embed-id _d-key-down]
                                                  ;; TODO implement focus on next input
                                                  (update-fn @local-value))}
            state-hooks         (merge {:save-fn                 save-fn
                                        :idle-fn                 idle-fn
                                        :update-fn               update-fn
                                        :read-value              read-value
                                        :show-edit?              show-edit?
                                        :default-verbatim-paste? true
                                        :keyboard-navigation?    false}
                                       custom-key-handlers)]
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
          [editor/block-editor {:block/uid (or prop-block-uid
                                               ;; NOTE: temporary magic, stripping `:task/` 🤷‍♂️
                                               (str "tmp-" (subs prop-name
                                                                 (inc (.indexOf prop-name "/")))
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


(defn- find-allowed-priorities
  []
  (let [task-priority-page  (reactive/get-reactive-node-document [:node/title ":task/priority"])
        allowed-prio-blocks (-> task-priority-page
                                :block/properties
                                (get ":property/enum")
                                :block/children)
        allowed-priorities  (map #(select-keys % [:block/uid :block/string]) allowed-prio-blocks)]
    (when-not allowed-prio-blocks
      (rf/dispatch [:properties/update-in [:node/title ":task/priority"] [":property/enum"]
                    (fn [db uid]
                      (when-not (common-db/block-exists? db [:block/uid uid])
                        (bfs/internal-representation->atomic-ops db (internal-representation-allowed-priorities)
                                                                 {:block/uid uid :relation :first})))]))
    (when (seq allowed-priorities)
      allowed-priorities)))


(defn- find-allowed-statuses
  []
  (let [task-status-page    (reactive/get-reactive-node-document [:node/title ":task/status"])
        allowed-stat-blocks (-> task-status-page
                                :block/properties
                                (get ":property/enum")
                                :block/children)
        allowed-statuses    (map #(select-keys % [:block/uid :block/string]) allowed-stat-blocks)]
    (when-not allowed-stat-blocks
      (rf/dispatch [:properties/update-in [:node/title ":task/status"] [":property/enum"]
                    (fn [db uid]
                      (when-not (common-db/block-exists? db [:block/uid uid])
                        (bfs/internal-representation->atomic-ops db (internal-representation-allowed-stauses)
                                                                 {:block/uid uid :relation :first})))]))
    (when (seq allowed-statuses)
      allowed-statuses)))


(defn task-priority-view
  [parent-block-uid priority-block-uid]
  (let [priority-id        (str (random-uuid))
        priority-block     (reactive/get-reactive-block-document [:block/uid priority-block-uid])
        allowed-priorities (find-allowed-priorities)
        priority-string    (:block/string priority-block "(())")
        priority-uid       (subs priority-string 2 (- (count priority-string) 2))]
    [:> FormControl {:is-required true}
     [:> HStack {:spacing "2rem"}
      [:> FormLabel {:html-for priority-id
                     :w        "9rem"}
       "Task priority"]
      [:> Select {:id          priority-id
                  :value       priority-uid
                  :placeholder "Select a priority"
                  :on-change   (fn [e]
                                 (let [new-priority (-> e .-target .-value)
                                       priority-ref (str "((" new-priority "))")]
                                   (rf/dispatch [:properties/update-in [:block/uid parent-block-uid] [":task/priority"]
                                                 (fn [db uid] [(graph-ops/build-block-save-op db uid priority-ref)])])))}
       (doall
        (for [{:block/keys [uid string]} allowed-priorities]
          ^{:key uid}
          [:option {:value uid}
            string]))]]]))


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
                                 (let [new-status (-> e .-target .-value)
                                       status-ref (str "((" new-status "))")]
                                   (rf/dispatch [:properties/update-in [:block/uid parent-block-uid] [":task/status"]
                                                 (fn [db uid] [(graph-ops/build-block-save-op db uid status-ref)])])))}
       (doall
         (for [{:block/keys [uid string]} allowed-statuses]
           ^{:key uid}
           [:option {:value uid}
            string]))]]]))


(defrecord TaskView
  []

  types/BlockTypeProtocol

  (inline-ref-view
    [_this _block-data _attr _ref-uid _uid _callbacks _with-breadcrumb?])


  (outline-view
    [_this block-data _callbacks]
    (let [block-uid (:block/uid block-data)]
      (fn [_this _block-data _callbacks]
        (let [props (-> [:block/uid block-uid] reactive/get-reactive-block-document :block/properties)
              title-uid       (-> props (get ":task/title") :block/uid)
              assignee-uid    (-> props (get ":task/assignee") :block/uid)
              priority-uid    (-> props (get ":task/priority") :block/uid)
              description-uid (-> props (get ":task/description") :block/uid)
              due-date-uid    (-> props (get ":task/due-date") :block/uid)
              ;; projects-uid  (:block/uid (find-property-block-by-key-name reactive-block ":task/projects"))
              status-uid      (-> props (get ":task/status") :block/uid)]
          [:> VStack {:spacing "0.5rem"
                      :class   "task_container"}
           [generic-textarea-view-for-task-props block-uid title-uid ":task/title" "Task Title" true false]
           [generic-textarea-view-for-task-props block-uid assignee-uid ":task/assignee" "Task Assignee" false false]
           [task-priority-view block-uid priority-uid]
           [generic-textarea-view-for-task-props block-uid description-uid ":task/description" "Task Description" false true]
           ;; Making assumption that for now we can add due date manually without date-picker.
           [generic-textarea-view-for-task-props block-uid due-date-uid ":task/due-date" "Task Due Date" false false]
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


(defmethod dispatcher/block-type->protocol "[[athens/task]]" [_k _args-map]
  (TaskView.))
