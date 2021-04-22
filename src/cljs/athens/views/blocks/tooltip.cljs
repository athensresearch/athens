(ns athens.views.blocks.tooltip
  (:require
    [athens.style :as style]
    [athens.util :as util]
    [stylefy.core :as stylefy]))


;; Styles

(stylefy/keyframes "tooltip-appear"
                   [:from
                    {:opacity "0"
                     :transform "scale(0)"}]
                   [:to
                    {:opacity "1"
                     :transform "scale(1)"}])


(def tooltip-style
  {:z-index (:zindex-dropdown style/ZINDICES)
   :position "absolute"
   :box-shadow [[(:64 style/DEPTH-SHADOWS) ", 0 0 0 1px " (style/color :body-text-color :opacity-lower)]]
   :flex-direction "column"
   :background-color (style/color :background-plus-1)
   :padding "0.5rem 0.75rem"
   :border-radius "0.25rem"
   :line-height "1.75rem"
   :left "0.5rem"
   :top "2rem"
   :transform-origin "0.5rem 1.5rem"
   :min-width "9rem"
   :animation "tooltip-appear .2s ease"
   :transition "background .1s ease"
   :display "table"
   :color (style/color :body-text-color :opacity-high)
   :border-spacing "0.25rem"
   ::stylefy/manual [[:div {:display "table-row"}]
                     [:b {:display "table-cell"
                          :user-select "none"
                          :text-align "right"
                          :text-transform "uppercase"
                          :font-size "12px"
                          :letter-spacing "0.1em"
                          :opacity (:opacity-med style/OPACITIES)}]
                     [:span {:display "table-cell"
                             :user-select "all"}
                      [:&:hover {:color (style/color :header-text-color)}]]
                     [:&:after {:content "''"
                                :position "absolute"
                                :top "-0.75rem"
                                :bottom "-1rem"
                                :border-radius "inherit"
                                :left "-1rem"
                                :right "-1rem"
                                :z-index -1
                                :display "block"}]]})


;; View

(defn tooltip-el
  [block state]
  (let [{:block/keys [uid order open refs] dbid :db/id} block
        {:keys [dragging tooltip]} @state]
    ;; if re-frame-10x is hidden, don't show tooltip. see style.cljs
    (when (and tooltip (not dragging) (util/re-frame-10x-open?))
      [:div (stylefy/use-style tooltip-style
                               {:class          "tooltip"
                                :on-click       (fn [e] (.. e stopPropagation))
                                :on-mouse-leave #(swap! state assoc :tooltip false)})
       [:div [:b "db/id"] [:span dbid]]
       [:div [:b "uid"] [:span uid]]
       [:div [:b "order"] [:span order]]
       [:div [:b "open"] [:span (str open)]]
       [:div [:b "refs"] [:span (str refs)]]])))
