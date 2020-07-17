(ns athens.views.modal
  (:require
    [athens.db]
    [athens.style :refer [color ZINDICES DEPTH-SHADOWS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [stylefy.core :as stylefy]))


;;; Styles

(def modal-style
  {:z-index (:zindex-modal ZINDICES)
   :animation "fade-in 0.2s"
   ::stylefy/manual [[:.modal {:position "fixed"
                               :top "50vh"
                               :left "50vw"
                               :transform "translate(-50%, -50%)"
                               :border-radius "0.5rem"
                               :display "flex"
                               :flex-direction "column"
                               :background (color :background-plus-2)
                               :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]}]
                     [:modal__header {:display "contents"}] ;; Deactivate layout on the default header
                     [(selectors/> :.modal__header :button) {:display "none"}] ;; Hide default close button
                     [:.modal__title :.modal__footer {:flex "0 0 auto"
                                                      :padding "0.25rem 1rem"
                                                      :display "flex"
                                                      :align-items "center"}
                      [:&:empty {:display "none"}]]
                     [:.modal__title {:border-bottom [["1px solid " (color :border-color)]]}
                      [:button {:margin-inline-start "auto"
                                :align-self "flex-start"
                                :margin-block "0.5rem"}]]
                     [:.modal__content {:flex "1 1 100%"
                                        :overflow-y "auto"
                                        :border-top [["1px solid " (color :border-color)]]}]
                     [:.modal__footer {:display "flex"}]
                     [:.modal__backdrop {:position "fixed"
                                         :top 0
                                         :left 0
                                         :background "rgba(0,0,0,0.1)"
                                         :z-index -1
                                         :width "100vw"
                                         :height "100vh"}]]})
