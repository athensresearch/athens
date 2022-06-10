(ns athens.views.jetsam
  "This is for experimentation with re-usability of block view/edit."
  (:require
    [athens.views.blocks.content :as b-content]
    [reagent.core :as r]))


(defn jetsam-component
  "Experiments with embedding"
  []
  (let [;; some setup code
        value-atom      (r/atom "This [[has]] link")
        show-edit-atom? (r/atom false)
        last-event      (r/atom nil)
        block           {:block/uid "my-random-uid"}
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
      [:div {:class "jetsam"
             :style {:position         "absolute"
                     :left             "25vh"
                     :top              "25vh"
                     :width            "50vh"
                     :height           "50vh"
                     :background-color "lightgreen"}}
       [b-content/block-content-el block state-hooks last-event]])))


;; TODO introduce re-usable edit/view so we don't need to include individual parts of block editing experience
