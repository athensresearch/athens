(ns athens.types.tasks.view.generic-textarea
  (:require
    ["/components/Block/BlockFormInput" :refer [BlockFormInput]]
    ["@chakra-ui/react"                 :refer [Box
                                                FormControl
                                                FormErrorMessage
                                                FormLabel]]
    [athens.common-events.graph.ops     :as graph-ops]
    [athens.common.logging              :as log]
    [athens.common.utils                :as common.utils]
    [athens.reactive                    :as reactive]
    [athens.self-hosted.presence.views  :as presence]
    [athens.views.blocks.editor         :as editor]
    [clojure.string                     :as str]
    [goog.functions                     :as gfns]
    [re-frame.core                      :as rf]
    [reagent.core                       :as r]))


(defn generic-textarea-view-for-task-props
  [_parent-block-uid _prop-block-uid _prop-name _prop-title _required? _multiline?]
  (let [prop-id     (str (random-uuid))
        local-value (r/atom "")]
    (fn [parent-block-uid prop-block-uid prop-name prop-title required? multiline?]
      (let [prop-block          (reactive/get-reactive-block-document [:block/uid prop-block-uid])
            prop-str            (or (:block/string prop-block) "")
            invalid-prop-str?   (and required?
                                     (str/blank? prop-str)
                                     (not (nil? prop-str)))
            save-fn             (fn generic-textarea-view-for-task-props-save-fn
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
                                                     (fn [db uid] [(graph-ops/build-block-save-op db uid new-value)])]))
                                     (when (= ":task/title" prop-name)
                                       (rf/dispatch [:block/save {:uid    parent-block-uid
                                                                  :string new-value}])))))
            update-fn           #(do
                                   (log/debug prop-name "update-fn:" (pr-str %))
                                   (reset! local-value %))
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
        (reset! local-value prop-str)
        [:> FormControl {:is-required required?
                         :display     "contents"
                         :is-invalid  invalid-prop-str?}
         [:> FormLabel {:html-for prop-id}
          prop-title]
         [:> Box [:> BlockFormInput
                  {:isMultiline multiline?
                   :size        "sm"}
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
