(ns athens.views.jetsam
  "This is for experimentation with re-usability of block view/edit."
  (:require
    [athens.views.blocks.eitor :as editor]
    [athens.views.blocks.textarea-keydown :as txt-key-down]
    [reagent.core                         :as r]))


(defn jetsam-component
  "Experiments with embedding"
  []
  (let [value-atom        (r/atom "This [[has]] link")
        show-edit-atom?   (r/atom false)
        block-uid         "my-random-uid"
        block-o           {:block/uid      block-uid
                           ;; :block/string   @value-atom
                           :block/children []}
        save-fn           #(reset! value-atom %)
        enter-handler     (fn jetsam-enter-handler
                            [_uid _d-key-down]
                            (txt-key-down/replace-selection-with "\n"))
        tab-handler       (fn jetsam-tab-handler
                            [_uid _embed-id _d-key-down]
                            (println "tab is here"))
        backspace-handler (fn jetsam-backspace-handler
                            [_uid _value]
                            (println "backspace is here"))
        delete-handler    (fn jetsam-delete-handler
                            [_uid _d-key-down]
                            (println "delete didn't break shit this time"))
        state-hooks       {:save-fn                 #(do
                                                       (println "save-fn" (pr-str %)))
                           :update-fn               #(do
                                                       (println "update-fn" (pr-str %))
                                                       (save-fn %))
                           :idle-fn                 #(println "idle-fn" (pr-str %))
                           :read-value              value-atom
                           :show-edit?              show-edit-atom?
                           :enter-handler           enter-handler
                           :tab-handler             tab-handler
                           :backspace-handler       backspace-handler
                           :delete-handler          delete-handler
                           :default-verbatim-paste? true
                           :keyboard-navigation?    false}]
    (fn jetsam-component-render-fn
      []
      [:div {:class "jetsam block-container"
             :style {:position         "absolute"
                     :left             "25vw"
                     :top              "25vh"
                     :width            "50vw"
                     :height           "50vh"
                     :background-color "lightgreen"}}
       [editor/block-editor block-o state-hooks]])))
