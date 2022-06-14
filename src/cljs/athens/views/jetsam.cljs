(ns athens.views.jetsam
  "This is for experimentation with re-usability of block view/edit."
  (:require
    [athens.views.blocks.content :as b-content]
    #_[athens.views.blocks.core :as b-core]
    #_[athens.views.blocks.editor :as b-editor]
    [reagent.core :as r]))


(defn jetsam-component
  "Experiments with embedding"
  []
  (let [;; some setup code
        value-atom      (r/atom "This [[has]] link")
        show-edit-atom? (r/atom false)
        last-event      (r/atom nil)
        block-uid       "my-random-uid"
        block-o         {:block/uid      block-uid
                         :block/string   @value-atom
                         :block/children []}
        save-fn         #(reset! value-atom %)
        state-hooks     {:save-fn    #(do
                                        (println "save-fn" (pr-str %)))
                         :update-fn  #(do
                                        (println "update-fn" (pr-str %))
                                        (save-fn %))
                         :idle-fn    #(println "idle-fn" (pr-str %))
                         :read-value value-atom
                         :show-edit? show-edit-atom?}]
    (fn jetsam-component-render-fn
      []
      [:div {:class "jetsam block-container"
             :style {:position         "absolute"
                     :left             "25vw"
                     :top              "25vh"
                     :width            "50vw"
                     :height           "50vh"
                     :background-color "lightgreen"}}
       #_[b-editor/editor-component
        b-core/block-el ;; for rendering children ;; TODO only part of editor because of recursive nature of children rendering
        block-o 
        false ;; children? ;; TODO only part of editor because of recursive nature of children rendering
        [] ;; linked ref data -> TODO seems like part of chrome, not an editor
        block-o ;; TODO investigate what's the role of this sanitization
        state-hooks ;; DONE for sure needed to save and read `:block/string` value
        {} ;; options
        ]
       #_[b-editor/editor-component
        block-o
        state-hooks ;; DONE for sure needed to save and read `:block/string` value
        {} ;; options
        ]
       [b-content/block-content-el block-o state-hooks last-event]])))


;; TODO introduce re-usable edit/view so we don't need to include individual parts of block editing experience
