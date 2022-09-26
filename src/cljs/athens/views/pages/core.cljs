(ns athens.views.pages.core
  (:require
    [athens.util :refer [toast]]
    [athens.views.hoc.perf-mon :as perf-mon]
    [athens.views.pages.all-pages :as all-pages]
    [athens.views.pages.daily-notes :as daily-notes]
    [athens.views.pages.graph :as graph]
    [athens.views.pages.page :as page]
    [re-frame.core :as rf]))


;; View

(defn view
  []
  (let [route-name (rf/subscribe [:current-route/name])]
    ;; TODO: create a UI to inform the player of the connection status
    (when (= @(rf/subscribe [:connection-status]) :reconnecting)
      (toast (clj->js {:status "info"
                       :title "Reconnecting to server..."})))
    [:<>
     (case @route-name
       :pages         [perf-mon/hoc-perfmon-no-new-tx {:span-name "pages/all-pages"}
                       [all-pages/page]]
       :page          [perf-mon/hoc-perfmon {:span-name "pages/page"}
                       [:f> page/page]]
       :page-by-title [perf-mon/hoc-perfmon {:span-name "pages/page-by-title"}
                       [:f> page/page-by-title]]
       :home          [perf-mon/hoc-perfmon {:span-name "pages/home-page"}
                       [:f> daily-notes/page]]
       :graph         [perf-mon/hoc-perfmon-no-new-tx {:span-name "pages/graph"}
                       [graph/page]]
       [perf-mon/hoc-perfmon-no-new-tx {:span-name "pages/default"}
        [:f> daily-notes/page]])]))
