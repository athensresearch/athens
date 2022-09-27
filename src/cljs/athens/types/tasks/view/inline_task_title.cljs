(ns athens.types.tasks.view.inline-task-title
  (:require
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common.logging          :as log]
    [athens.common.utils            :as common.utils]
    [athens.reactive                :as reactive]
    [athens.views.blocks.editor     :as editor]
    [clojure.string                 :as str]
    [goog.functions                 :as gfns]
    [re-frame.core                  :as rf]
    [reagent.core                   :as r]))


(defn inline-task-title
  [_state-hooks _parent-block-uid _prop-block-uid _prop-name _prop-title _required? _multiline?]
  (let [_prop-id    (str (random-uuid))
        local-value (r/atom "")]
    (fn [state-hooks parent-block-uid prop-block-uid prop-name _prop-title _required? _multiline?]
      (let [prop-block         (reactive/get-reactive-block-document [:block/uid prop-block-uid])
            prop-str           (:block/string prop-block "")
            _invalid-prop-str? (and (str/blank? prop-str)
                                    (not (nil? prop-str)))
            save-fn            (fn
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
            update-fn          #(do
                                  (log/debug prop-name "update-fn:" (pr-str %))
                                  (reset! local-value %))
            idle-fn            (gfns/debounce #(do
                                                 (log/debug prop-name "idle-fn" (pr-str @local-value))
                                                 (save-fn))
                                              2000)
            show-edit?         (r/atom false)
            state-hooks        (merge {:save-fn                 save-fn
                                       :idle-fn                 idle-fn
                                       :update-fn               update-fn
                                       :read-value              local-value
                                       :show-edit?              show-edit?
                                       :default-verbatim-paste? true
                                       :keyboard-navigation?    true
                                       :navigation-uid          parent-block-uid}
                                      state-hooks)]
        (reset! local-value prop-str)
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

