(ns athens.views.pages.core
  (:require
    [athens.style :as style]
    [athens.views.hoc.perf-mon :as perf-mon]
    [athens.views.pages.all-pages :as all-pages]
    [athens.views.pages.daily-notes :as daily-notes]
    [athens.views.pages.graph :as graph]
    [athens.views.pages.page :as page]
    [athens.views.pages.settings :as settings]
    [re-frame.core :as rf]
    [stylefy.core :as stylefy]))


;; Styles

(def main-content-style
  {:flex "1 1 100%"
   :grid-area "main-content"
   :align-items "flex-start"
   :justify-content "stretch"
   :padding-top "2.5rem"
   :display "flex"
   :overflow-y "auto"
   ::stylefy/supports {"overflow-y: overlay"
                       {:overflow-y "overlay"}}
   ::stylefy/mode {"::-webkit-scrollbar" {:background (style/color :background-minus-1)
                                          :width "0.5rem"
                                          :height "0.5rem"}
                   "::-webkit-scrollbar-corner" {:background (style/color :background-minus-1)}
                   "::-webkit-scrollbar-thumb" {:background (style/color :background-minus-2)
                                                :border-radius "0.5rem"}}})


;; View

(defn view
  []
  (let [route-name (rf/subscribe [:current-route/name])]
    ;; TODO: create a UI to inform the player of the connection status
    (when (= @(rf/subscribe [:connection-status]) :reconnecting)
      (rf/dispatch [:alert/js "Oops! Connection Lost. Reconnecting..."]))
    [:div (stylefy/use-style main-content-style
                             {:on-scroll (when (= @route-name :home)
                                           #(rf/dispatch [:daily-note/scroll]))})
     (case @route-name
       :settings      [perf-mon/hoc-perfmon {:span-name "pages/settings"}
                       [settings/page]]
       :pages         [perf-mon/hoc-perfmon {:span-name "pages/all-pages"}
                       [all-pages/page]]
       :page          [perf-mon/hoc-perfmon {:span-name "pages/page"}
                       [page/page]]
       :page-by-title [perf-mon/hoc-perfmon {:span-name "pages/page-by-title"}
                       [page/page-by-title]]
       :home          [perf-mon/hoc-perfmon {:span-name "pages/home-page"}
                       [daily-notes/page]]
       :graph         [perf-mon/hoc-perfmon {:span-name "pages/graph"}
                       [graph/page]]
       [perf-mon/hoc-perfmon {:span-name "pages/default"}
        [daily-notes/page]])]))
