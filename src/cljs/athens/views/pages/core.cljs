(ns athens.views.pages.core
  (:require
    ["@chakra-ui/react" :refer [Box]]
    [athens.util :refer [toast]]
    [athens.views.hoc.perf-mon :as perf-mon]
    [athens.views.pages.all-pages :as all-pages]
    [athens.views.pages.daily-notes :as daily-notes]
    [athens.views.pages.graph :as graph]
    [athens.views.pages.page :as page]
    [goog.functions :refer [debounce]]
    [re-frame.core :as rf]))


(def throttled-scroll
  (debounce #(rf/dispatch [:daily-note/scroll]) 25))


;; View

(defn view
  []
  (let [route-name (rf/subscribe [:current-route/name])]
    ;; TODO: create a UI to inform the player of the connection status
    (when (= @(rf/subscribe [:connection-status]) :reconnecting)
      (toast (clj->js {:status "info"
                       :title "Reconnecting to server..."})))
    [:> Box {:flex "1 1 100%"
             :class "main-content"
             :position "relative"
             :alignItems "flex-start"
             :justifyContent "stretch"
             :display "flex"
             :overflowY "auto"
             :on-scroll (when (= @route-name :home)
                          throttled-scroll)}
     (case @route-name
       :pages         [perf-mon/hoc-perfmon-no-new-tx {:span-name "pages/all-pages"}
                       [all-pages/page]]
       :page          [perf-mon/hoc-perfmon {:span-name "pages/page"}
                       [page/page]]
       :page-by-title [perf-mon/hoc-perfmon {:span-name "pages/page-by-title"}
                       [page/page-by-title]]
       :home          [perf-mon/hoc-perfmon {:span-name "pages/home-page"}
                       [daily-notes/page]]
       :graph         [perf-mon/hoc-perfmon-no-new-tx {:span-name "pages/graph"}
                       [graph/page]]
       [perf-mon/hoc-perfmon-no-new-tx {:span-name "pages/default"}
        [daily-notes/page]])]))
