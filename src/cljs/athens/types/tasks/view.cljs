(ns athens.types.tasks.view
  "Views for Athens Tasks"
  (:require
    ["/components/Block/Taskbox"                :refer [Taskbox]]
    ["/components/Icons/Icons"                  :refer [PencilIcon]]
    ["/components/ModalInput/ModalInput"        :refer [ModalInput]]
    ["/components/ModalInput/ModalInputPopover" :refer [ModalInputPopover]]
    ["/components/ModalInput/ModalInputTrigger" :refer [ModalInputTrigger]]
    ["@chakra-ui/react"                         :refer [AvatarGroup
                                                        Avatar
                                                        Box
                                                        Divider
                                                        Button
                                                        Badge
                                                        Flex
                                                        FormControl
                                                        FormLabel
                                                        HStack
                                                        Select
                                                        Text]]
    [athens.common-db                           :as common-db]
    [athens.common-events.graph.ops             :as graph-ops]
    [athens.common.utils                        :as common.utils]
    [athens.dates                               :as dates]
    [athens.parse-renderer                      :as parser]
    [athens.reactive                            :as reactive]
    [athens.router                              :as router]
    [athens.types.core                          :as types]
    [athens.types.dispatcher                    :as dispatcher]
    [athens.types.tasks.handlers                :as handlers]
    [athens.types.tasks.shared                  :as shared]
    [athens.types.tasks.view.generic-textarea   :as generic-textarea]
    [athens.types.tasks.view.inline-task-title  :as inline-task-title]
    [athens.views.blocks.editor                 :as editor]
    [clojure.string                             :as str]
    [goog.functions                             :as gfns]
    [re-frame.core                              :as rf]
    [reagent.core                               :as r]
    [tick.core                                  :as t]))


;; View

(defn task-priority-view
  [parent-block-uid priority-block-uid]
  (let [priority-id        (str (random-uuid))
        priority-block     (reactive/get-reactive-block-document [:block/uid priority-block-uid])
        allowed-priorities (shared/find-allowed-priorities)
        priority-string    (:block/string priority-block "(())")
        priority-uid       (subs priority-string 2 (- (count priority-string) 2))]
    [:> FormControl {:display "contents"}
     [:> FormLabel {:html-for priority-id}
      "Priority"]
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


(defn- task-status-view-v2
  [_task-uid _status-uid]
  (let [status-options (->> (shared/find-allowed-statuses)
                            (map (fn [{:block/keys [string]}]
                                   string)))
        status         (r/atom nil)]
    (fn task-status-view-v2-render
      [task-uid status-uid]
      (let [status-block (reactive/get-reactive-block-document [:block/uid status-uid])]
        (reset! status (:block/string status-block))
        ^{:key @status}
        [:> Taskbox {:status   @status
                     :options  status-options
                     :position "relative"
                     :top      "0.2em"
                     :onClick  #(.stopPropagation %)
                     :onChange #(handlers/update-task-status task-uid %)}]))))


(defn task-el
  [_this block-data _callbacks _is-ref?]
  (let [block-uid (:block/uid block-data)]
    (fn [_this _block-data callbacks]
      (let [block            (-> [:block/uid block-uid] reactive/get-reactive-block-document)
            props            (-> block :block/properties)
            title-uid        (-> props (get ":task/title") :block/uid)
            assignee-uid     (-> props (get ":task/assignee") :block/uid)
            priority-uid     (-> props (get ":task/priority") :block/uid)
            _description-uid (-> props (get ":task/description") :block/uid)
            _creator-uid     (-> props (get ":task/creator") :block/uid)
            due-date-uid     (-> props (get ":task/due-date") :block/uid)
            creator          (-> (:block/create block) :event/auth :presence/id)
            time             (-> (:block/create block) :event/time :time/ts)
            created-date     (when time
                               (-> time
                                   t/instant
                                   t/date
                                   (dates/get-day 0)
                                   :title))
            status-uid       (-> props
                                 (get ":task/status")
                                 :block/string
                                 (common-db/strip-markup "((" "))"))
            title            (-> props (get ":task/title") :block/string)
            assignee         (-> props (get ":task/assignee") :block/string (common-db/strip-markup "[[" "]]"))
            priority         (-> [:block/uid  (-> props
                                                  (get ":task/priority")
                                                  :block/string
                                                  (common-db/strip-markup "((" "))"))]
                                 (reactive/get-reactive-block-document)
                                 :block/string)
            description      (-> props (get ":task/description") :block/string)
            due-date         (-> props
                                 (get ":task/due-date")
                                 :block/string
                                 (common-db/strip-markup "[[" "]]"))
            show-assignee?     true
            show-description?  false
            show-priority?     true
            show-creator?      false
            show-created-date? false
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
         [task-status-view-v2 block-uid status-uid]
         [:> Box {:flex       "1 1 100%"
                  :lineHeight "base"
                  :cursor     "text"}
          [inline-task-title/inline-task-title
           callbacks
           block-uid
           title-uid
           ":task/title"
           "Title"
           true
           false]]
         [:> ModalInput {:placement "left-start"
                         :isLazy    true}
          [:> ModalInputTrigger
           [:> Button {:size       "xs"
                       :alignSelf "flex-start"
                       :flex       "1 0 auto"
                       :variant    "ghost"
                       :onClick    #(.. % stopPropagation)
                       :lineHeight "unset"
                       :whiteSpace "unset"
                       :height     "var(--control-height)"
                       :px         2}

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
               (when (and show-assignee? assignee)
                 [:> AvatarGroup
                  [:> Avatar {:size "xs" :name assignee}]])
               (when (and show-due-date? due-date)
                 [:> Text {:fontSize "xs"} due-date])])

            ;; provenance
            [:> Flex {:gap 1 :align "center"}
             (when (and show-creator? creator)
               [:> AvatarGroup
                [:> Avatar {:size "xs" :name creator}]])
             (when (and show-created-date? created-date)
               [:> Text {:fontSize "xs"} created-date])]
            [:> PencilIcon {:color "foreground.secondary"}]]]
          [:> ModalInputPopover {:popoverContentProps
                                 {:display             "grid"
                                  :onClick             #(.. % stopPropagation)
                                  :gridTemplateColumns "max-content 1fr"
                                  :gap                 2
                                  :py                  2
                                  :px                  4
                                  :maxWidth            "20em"}}
           [:> HStack {:gridColumn "1 / -1" :align "flex-start"}
            [:> Text {:fontSize  "sm"
                      :noOfLines 2
                      :color     "foreground.secondary"}
             title]]
           [:> Divider {:gridColumn "1 / -1"}]
           [task-priority-view block-uid priority-uid]
           [generic-textarea/generic-textarea-view-for-task-props block-uid assignee-uid ":task/assignee" "Assignee" false false]
           ;; Making assumption that for now we can add due date manually without date-picker.
           [generic-textarea/generic-textarea-view-for-task-props block-uid due-date-uid ":task/due-date" "Due Date" false false]
           [:> Divider {:gridColumn "1 / -1"}]
           [:> Text {:color "foreground.secondary" :fontSize "sm"} "Created by"]
           [:> Flex {:align "center"} [:> Avatar {:size "2xs" :marginInlineEnd 1 :name creator}] [:> Text {:fontSize "sm" :noOfLines 0} creator]]
           [:> Text {:color "foreground.secondary" :fontSize "sm"} "Created"]
           [:> Text {:fontSize "sm"} created-date]]]]))))


(defn task-ref-el
  [ref-uid]
  (let [{:block/keys [properties]} (reactive/get-reactive-block-document [:block/uid ref-uid])
        title                      (-> properties
                                       (get ":task/title")
                                       :block/string)
        status-uid                 (-> properties
                                       (get ":task/status")
                                       :block/string
                                       (common-db/strip-markup "((" "))"))]
    [:> Flex {:display   "inline-flex"
              :align     "baseline"
              :bg        "transparent"
              :transitionProperty "colors"
              :transitionDuration "fast"
              :borderRadius "2px"
              :transitionTimingFunction "ease-in-out"
              :sx        {"WebkitBoxDecorationBreak" "clone"
                          "&:has(.task-title:hover)"
                          {:textDecoration    "none"
                           :borderBottomColor "transparent"
                           :bg                "ref.background"}}
              :alignSelf "baseline"
              :gap       1}
     [task-status-view-v2 ref-uid status-uid]
     [:> Button {:variant "unstyled"
                 :className "task-title"
                 :fontWeight "normal"
                 :whiteSpace "normal"
                 :minWidth "0"
                 :display "inline"
                 :sx {"WebkitBoxDecorationBreak" "clone"
                      ".block" {"WebkitBoxDecorationBreak" "clone",
                                :borderBottomWidth "1px"
                                :borderBottomStyle "solid"
                                :borderBottomColor "ref.foreground"}
                      ":hover .block" {:borderBottomColor "transparent"}}
                 :textAlign "start"
                 :justifyContent "start"
                 :height "auto"
                 :borderRadius "none"
                 :lineHeight        "1.4"
                 :cursor            "alias"
                 :onClick           (fn [e]
                                      (.. e stopPropagation)
                                      (let [shift? (.-shiftKey e)]
                                        (rf/dispatch [:reporting/navigation {:source :pr-task-ref
                                                                             :target :task
                                                                             :pane   (if shift?
                                                                                       :right-pane
                                                                                       :main-pane)}])
                                        (router/navigate-uid ref-uid e)))}
      [parser/parse-and-render title]]]))


(defn zoomed-in-view-el
  [_this block-data _callbacks]
  (let [parent-block-uid    (:block/uid block-data)
        props               (:block/properties (reactive/get-reactive-block-document [:block/uid parent-block-uid]))
        title-uid           (-> props (get ":task/title") :block/uid)
        title-block         (reactive/get-reactive-block-document [:block/uid title-uid])
        title-str           (or (:block/string title-block) "")
        local-value         (r/atom title-str)
        _invalid-prop-str?  (and (str/blank? title-str)
                                 (not (nil? title-str)))
        save-fn             (fn
                              ([]
                               (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [":task/title"]
                                             (fn [db uid] [(graph-ops/build-block-save-op db uid @local-value)])]))
                              ([e]
                               (let [new-value (-> e .-target .-value)]
                                 (reset! local-value new-value)
                                 (rf/dispatch [:graph/update-in [:block/uid parent-block-uid] [":task/title"]
                                               (fn [db uid] [(graph-ops/build-block-save-op db uid new-value)])])
                                 (rf/dispatch [:block/save {:uid    parent-block-uid
                                                            :string new-value}]))))
        update-fn           #(reset! local-value %)
        idle-fn             (gfns/debounce #(do
                                              (save-fn))
                                           2000)
        read-value          local-value
        show-edit?          (r/atom true)
        custom-key-handlers {:enter-handler (fn [_uid _d-key-down]
                                              ;; TODO dispatch save and jump to next input
                                              (println "TODO dispatch save and jump to next input")
                                              (update-fn @local-value))}
        state-hooks         (merge {:save-fn                 save-fn
                                    :idle-fn                 idle-fn
                                    :update-fn               update-fn
                                    :read-value              read-value
                                    :show-edit?              show-edit?
                                    :default-verbatim-paste? true
                                    :keyboard-navigation?    true
                                    :navigation-uid          parent-block-uid
                                    ;; TODO here we add styles
                                    :style                   {}}
                                   custom-key-handlers)]
    [editor/block-editor {:block/uid (or title-uid
                                         ;; NOTE: temporary magic, stripping `:task/` 🤷‍♂️
                                         (str "tmp-" (subs (or ":task/title" "")
                                                           (inc (.indexOf (or ":task/title" "") "/")))
                                              "-uid-" (common.utils/gen-block-uid)))}
     state-hooks]))


(defrecord TaskView
  []

  types/BlockTypeProtocol

  (text-view
    [_this block-data _attr]
    (str "Task: " (:block/uid block-data)))


  (inline-ref-view
    [_this _block-data _attr ref-uid _uid _callbacks _with-breadcrumb?]
    (task-ref-el ref-uid))


  (outline-view
    [_this block-data callbacks]
    [task-el _this block-data callbacks false])


  (supported-transclusion-scopes
    [_this]
    #{:embed})


  (transclusion-view
    [this _block-el block-uid callbacks transclusion-scope]
    (let [supported-trans (types/supported-transclusion-scopes this)]
      (if-not (contains? supported-trans transclusion-scope)
        (throw (ex-info (str "Invalid transclusion scope: " (pr-str transclusion-scope)
                             ". Supported transclusion types: " (pr-str supported-trans))
                        {:supported-transclusion-scopes supported-trans
                         :provided-transclusion-scope   transclusion-scope}))
        (let [block (reactive/get-reactive-block-document [:block/uid block-uid])]
          [task-el this block callbacks true]))))


  (zoomed-in-view
    [_this block-data _callbacks]
    [zoomed-in-view-el _this block-data _callbacks])


  (supported-breadcrumb-styles
    [_this]
    #{:string})


  (breadcrumbs-view
    [_this _block-data _callbacks _breadcrumb-style]))


(defmethod dispatcher/block-type->protocol "[[athens/task]]" [_k _args-map]
  (TaskView.))
