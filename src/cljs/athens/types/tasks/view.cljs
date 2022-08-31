(ns athens.types.tasks.view
  "Views for Athens Tasks"
  (:require
    ["/components/Block/BlockFormInput"         :refer [BlockFormInput]]
    ["/components/Block/Taskbox"                :refer [Taskbox]]
    ["/components/Icons/Icons"                  :refer [CheckmarkIcon]]
    ["/components/ModalInput/ModalInput"        :refer [ModalInput]]
    ["/components/ModalInput/ModalInputPopover" :refer [ModalInputPopover]]
    ["/components/ModalInput/ModalInputTrigger" :refer [ModalInputTrigger]]
    ["@chakra-ui/react"                         :refer [FormControl
                                                        FormLabel
                                                        Text
                                                        Flex
                                                        MenuGroup
                                                        AvatarGroup
                                                        Avatar
                                                        Box
                                                        Divider
                                                        MenuOptionGroup
                                                        MenuItemOption
                                                        Button
                                                        Badge
                                                        FormErrorMessage
                                                        Select
                                                        HStack]]
    [athens.common-db                           :as common-db]
    [athens.common-events                       :as common-events]
    [athens.common-events.bfs                   :as bfs]
    [athens.common-events.graph.composite       :as composite]
    [athens.common-events.graph.ops             :as graph-ops]
    [athens.common.logging                      :as log]
    [athens.common.utils                        :as common.utils]
    [athens.dates                               :as dates]
    [athens.db                                  :as db]
    [athens.reactive                            :as reactive]
    [athens.self-hosted.presence.views          :as presence]
    [athens.types.core                          :as types]
    [athens.types.dispatcher                    :as dispatcher]
    [athens.views.blocks.editor                 :as editor]
    [clojure.string                             :as str]
    [goog.functions                             :as gfns]
    [re-frame.core                              :as rf]
    [reagent.core                               :as r]
    [tick.core                                  :as t]))


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
                               #_#_":task/projects"
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
                                  (let [[new-uid prop-ops] (graph-ops/build-path db task-block-uid [prop-name])
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
                                     (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [prop-name]
                                                   (fn [db uid] [(graph-ops/build-block-save-op db uid @local-value)])])))
                                  ([e]
                                   (let [new-value (-> e .-target .-value)]
                                     (log/debug prop-name "save-fn" (pr-str new-value))
                                     (reset! local-value new-value)
                                     (when (#{":task/title"
                                              ":task/assignee"
                                              ":task/description"
                                              ":task/due-date"} prop-name)
                                       (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [prop-name]
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
                                                    (when (= ":task/assignee"
                                                             prop-name)
                                                      (rf/dispatch [:notification-for-assigned-task parent-block-uid @local-value]))
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
                         :display "contents"
                         :is-invalid  invalid-prop-str?}
         [:> FormLabel {:html-for prop-id}
          prop-title]
         [:> Box [:> BlockFormInput
                  {:isMultiline multiline?}
                  ;; NOTE: we generate temporary uid for prop if it doesn't exist, so editor can work
                  [editor/block-editor {:block/uid (or prop-block-uid
                                                       ;; NOTE: temporary magic, stripping `:task/` 🤷‍♂️
                                                       (str "tmp-" (subs prop-name
                                                                         (inc (.indexOf prop-name "/")))
                                                            "-uid-" (common.utils/gen-block-uid)))}
                   state-hooks]
                  [presence/inline-presence-el prop-block-uid]]

          (when invalid-prop-str?
            [:> FormErrorMessage {:gridColumn 2}
             (str prop-title " is " (if required?
                                      "required"
                                      "empty"))]
            #_ [:> FormHelperText {:gridColumn 2}
                (str "Please provide " prop-title)])]]))))


;; Will need this in future, see Stuart's comment for better understanding here https://discord.com/channels/708122962422792194/1008156785791742002/1009102458695450654
#_(defn inline-task-title
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
                                       (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [prop-name]
                                                     (fn [db uid] [(graph-ops/build-block-save-op db uid @local-value)])])))
                                    ([e]
                                     (let [new-value (-> e .-target .-value)]
                                       (log/debug prop-name "save-fn" (pr-str new-value))
                                       (reset! local-value new-value)
                                       (when (#{":task/title"
                                                ":task/assignee"
                                                ":task/description"
                                                ":task/due-date"} prop-name)
                                         (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [prop-name]
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
              show-edit?          (r/atom false)
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
           [:> BlockFormInput
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


(defn inline-task-title-2
  [_parent-block-uid _prop-block-uid _prop-name _prop-title _required? _multiline?]
  (let [_prop-id (str (random-uuid))]
    (fn [parent-block-uid prop-block-uid prop-name _prop-title _required? multiline?]
      (let [prop-block          (reactive/get-reactive-block-document [:block/uid prop-block-uid])
            prop-str            (or (:block/string prop-block) "")
            local-value         (r/atom prop-str)
            _invalid-prop-str?  (and (str/blank? prop-str)
                                     (not (nil? prop-str)))
            save-fn             (fn
                                  ([]
                                   (log/debug prop-name "save-fn" (pr-str @local-value))
                                   (when (#{":task/title" ":task/description" ":task/due-date"} prop-name)
                                     (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [prop-name]
                                                   (fn [db uid] [(graph-ops/build-block-save-op db uid @local-value)])])))
                                  ([e]
                                   (let [new-value (-> e .-target .-value)]
                                     (log/debug prop-name "save-fn" (pr-str new-value))
                                     (reset! local-value new-value)
                                     (when (#{":task/title"
                                              ":task/assignee"
                                              ":task/description"
                                              ":task/due-date"} prop-name)
                                       (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [prop-name]
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
            show-edit?          (r/atom false)
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
        [editor/block-editor {:block/uid (or prop-block-uid
                                             ;; NOTE: temporary magic, stripping `:task/` 🤷‍♂️
                                             (str "tmp-" (subs (or prop-name "")
                                                               (inc (.indexOf (or prop-name "") "/")))
                                                  "-uid-" (common.utils/gen-block-uid)))}
         state-hooks]
        #_ [:> FormControl {:is-required required?
                            :is-invalid  invalid-prop-str?}
            [:> FormLabel {:html-for prop-id}
             prop-title]
            [:> BlockFormInput
             ;; NOTE: we generate temporary uid for prop if it doesn't exist, so editor can work

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
      (rf/dispatch [:graph/update-in [:node/title ":task/priority"] [":property/enum"]
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
      (rf/dispatch [:graph/update-in [:node/title ":task/status"] [":property/enum"]
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
    [:> FormControl {:is-required true
                     :display "contents"}
     [:> FormLabel {:html-for priority-id}
      "Task priority"]
     [:> Box [:> Select {:id          priority-id
                         :value       priority-uid
                         :size "sm"
                         :placeholder "Select a priority"
                         :on-change   (fn [e]
                                        (let [new-priority (-> e .-target .-value)
                                              priority-ref (str "((" new-priority "))")]
                                          (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [":task/priority"]
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
    [:> FormControl {:is-required true
                     :display "contents"}
     [:> FormLabel {:html-for status-id}
      "Task Status"]
     [:> Box [:> Select {:id          status-id
                         :value       status-uid
                         :placeholder "Select a status"
                         :size "sm"
                         :on-change   (fn [e]
                                        (let [new-status (-> e .-target .-value)
                                              status-ref (str "((" new-status "))")]
                                          (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [":task/status"]
                                                        (fn [db uid] [(graph-ops/build-block-save-op db uid status-ref)])])))}
              (doall
                (for [{:block/keys [uid string]} allowed-statuses]
                  ^{:key uid}
                  [:option {:value uid}
                   string]))]]]))


;; NOTE: not used right now
(defn task-status-menugroup
  [parent-block-uid status-block-uid]
  (let [;; status-id        (str (random-uuid))
        status-block     (reactive/get-reactive-block-document [:block/uid status-block-uid])
        allowed-statuses (find-allowed-statuses)
        status-string    (:block/string status-block "(())")
        status-uid       (subs  status-string 2 (- (count status-string) 2))
        on-choose-item   (fn [status]
                           (let [new-status status
                                 status-ref (str "((" new-status "))")]
                             (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [":task/status"]
                                           (fn [db uid] [(graph-ops/build-block-save-op db uid status-ref)])])))]
    [:> MenuGroup
     [:> MenuOptionGroup {:defaultValue status-uid
                          :type "radio"
                          :onChange on-choose-item}
      (doall
        (for [{:block/keys [uid string]} allowed-statuses]
          ^{:key uid}
          [:> MenuItemOption {:value uid
                              :py 0
                              :icon (r/as-element [:> CheckmarkIcon])}
           string]))]]))


(defn find-status-uid
  [status]
  (->> (filter (fn [allowed-status]
                 (= status (:block/string allowed-status)))
               (find-allowed-statuses))
       first
       :block/uid))


;; NOTE: not used right now
(defn on-update-status
  [task-uid new-status]
  (prn "UPDATE" task-uid new-status (find-status-uid new-status))
  (let [new-status (-> new-status find-status-uid)
        new-status (str "((" new-status "))")]
    (rf/dispatch [:graph/update-in [:block/uid task-uid] [":task/status"]
                  (fn [db uid]
                    [(graph-ops/build-block-save-op db uid new-status)]
                    #_(if is-checked
                        [(graph-ops/build-block-save-op db uid (str "((" (find-status-uid "To Do") "))"))]
                        [(graph-ops/build-block-save-op db uid (str "((" (find-status-uid "Done") "))"))]))])))


;; NOTE: not used right now
(defn is-checked-fn
  [status]
  (contains? #{"Done" "Cancelled"} status))


(defn task-el
  [_this block-data _callbacks _is-ref?]
  (let [block-uid (:block/uid block-data)]
    (fn [_this _block-data _callbacks]
      (let [block           (-> [:block/uid block-uid] reactive/get-reactive-block-document)
            props           (-> block :block/properties)
            title-uid       (-> props (get ":task/title") :block/uid)
            assignee-uid    (-> props (get ":task/assignee") :block/uid)
            priority-uid    (-> props (get ":task/priority") :block/uid)
            description-uid (-> props (get ":task/description") :block/uid)
            creator-uid     (-> props (get ":task/creator") :block/uid)
            due-date-uid    (-> props (get ":task/due-date") :block/uid)
            ;; projects-uid  (:block/uid (find-property-block-by-key-name reactive-block ":task/projects"))
            ;; status-uid      (-> props (get ":task/status") :block/uid)
            ;; map the :string key of the return from (find-allowed-statuses) into a vector
            status-options  (->> (find-allowed-statuses)
                                 (map (fn [{:block/keys [string]}]
                                        string)))
            creator         (-> (:block/create block) :event/auth :presence/id)
            time            (-> (:block/create block) :event/time :time/ts)
            created-date    (-> time
                                t/instant
                                t/date
                                (dates/get-day 0)
                                :title)
            status          (-> (common-db/get-block @db/dsdb [:block/uid  (-> props
                                                                               (get ":task/status")
                                                                               :block/string
                                                                               (common-db/strip-markup "((" "))"))])
                                :block/string)
            _title          (-> props (get ":task/title") :block/string)
            assignee        (-> props (get ":task/assignee") :block/string (common-db/strip-markup "[[" "]]"))
            priority        (-> (common-db/get-block @db/dsdb [:block/uid  (-> props
                                                                               (get ":task/priority")
                                                                               :block/string
                                                                               (common-db/strip-markup "((" "))"))])
                                :block/string)
            creator         creator
            description     (-> props (get ":task/description") :block/string)
            created-date    created-date
            due-date        (-> props
                                (get ":task/due-date")
                                :block/string
                                (common-db/strip-markup "[[" "]]"))

            show-assignee?     true
            show-description?  true
            show-priority?     true
            show-creator?      true
            show-created-date? true
            _show-status?      true
            show-due-date?     true]
        [:> HStack {:spacing                  1
                    :gridArea                 "content"
                    :borderRadius             "md"
                    :alignItems               "baseline"
                    :transitionProperty       "colors"
                    :transitionDuration       "fast"
                    :transitionTimingFunction "ease-in-out"
                    :overflow                 "hidden"
                    :align                    "stretch"}
         [:> Taskbox {:status   status
                      :options  status-options
                      :position "relative"
                      :top      "0.2em"
                      :onChange #(on-update-status block-uid %)}]
         [:> Box {:flex       "1 1 100%"
                  :py         1
                  :cursor     "text"
                  :lineHeight 1.4}
          [inline-task-title-2 block-uid title-uid ":task/title" "Title" true false]]
         [:> ModalInput {:placement "left-start"
                         :isLazy    true}
          [:> ModalInputTrigger
           [:> Button {:size         "sm"
                       :flex         "1 0 auto"
                       :variant      "outline"
                       :borderRadius "full"
                       :onClick      #(.. % stopPropagation)
                       :lineHeight   "unset"
                       :whiteSpace   "unset"
                       :px           2
                       :py           1}
            ;; description
            (when (and show-description? description)
              [:> Text {:fontSize "sm" :flexGrow 1 :flexBasis "100%" :m 0 :py 1 :lineHeight 1.4 :color "foreground.secondary"}
               description])
            ;; tasking/assignment
            (when (and show-priority? priority)
              [:> Badge {:size "sm" :variant "primary"}
               priority])
            (when (or due-date assignee)
              [:> Flex {:gap 1 :align "center"}
               [:> Text {:fontSize "xs"} "Due"]
               (when (and show-assignee? assignee)
                 [:> AvatarGroup {:size "xs"}
                  [:> Avatar {:name assignee}]])
               (when (and show-due-date? due-date)
                 [:> Text {:fontSize "xs"} due-date])])
            ;; provenance
            [:> Flex {:gap 1 :align "center"}
             (when (and show-creator? creator)
               [:> AvatarGroup {:size "xs"}
                [:> Avatar {:name creator}]])
             (when (and show-created-date? created-date)
               [:> Text {:fontSize "xs"} created-date])]]]
          [:> ModalInputPopover {:popoverContentProps {:display             "grid"
                                                       :onClick             #(.. % stopPropagation)
                                                       :gridTemplateColumns "max-content 1fr"
                                                       :gap                 2
                                                       :p                   4
                                                       :maxWidth            "20em"}}
           [generic-textarea-view-for-task-props block-uid description-uid ":task/description" "Description" false true]
           [:> Divider {:gridColumn "1 / -1"}]
           [task-priority-view block-uid priority-uid]
           [generic-textarea-view-for-task-props block-uid assignee-uid ":task/assignee" "Assignee" false false]
           ;; Making assumption that for now we can add due date manually without date-picker.
           [generic-textarea-view-for-task-props block-uid due-date-uid ":task/due-date" "Due Date" false false]
           [:> Text creator-uid]]]]))))


(defrecord TaskView
  []

  types/BlockTypeProtocol

  (inline-ref-view
    [_this _block-data _attr _ref-uid _uid _callbacks _with-breadcrumb?]
    (let [block (reactive/get-reactive-block-document [:block/uid _ref-uid])]
      [:> Flex {:display "inline-flex"  :gap 1} [:> Taskbox] [:> Text (:block/string block)]]))


  (outline-view
    [_this block-data _callbacks]
    [task-el _this block-data _callbacks false])


  (supported-transclusion-scopes
    [_this])


  (transclusion-view
    [_this _block-el block-uid _callback _transclusion-scope]
    (let [block (reactive/get-reactive-block-document [block-uid])]
      [task-el _this block true]))


  (zoomed-in-view
    [_this _block-data _callbacks])


  (supported-breadcrumb-styles
    [_this]
    #{:string})


  (breadcrumbs-view
    [_this _block-data _callbacks _breadcrumb-style]))


(defmethod dispatcher/block-type->protocol "[[athens/task]]" [_k _args-map]
  (TaskView.))
