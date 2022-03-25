(ns athens.views.pages.core
  (:require
   ["@chakra-ui/react" :refer [Box]]
   [athens.views.hoc.perf-mon :as perf-mon]
   [athens.views.pages.all-pages :as all-pages]
   [athens.views.pages.daily-notes :as daily-notes]
   [athens.views.pages.graph :as graph]
   [athens.views.pages.page :as page]
   [athens.views.pages.settings :as settings]
   [re-frame.core :as rf]))

;; View

(defn view
  []
  (let [route-name (rf/subscribe [:current-route/name])]
    ;; TODO: create a UI to inform the player of the connection status
    (when (= @(rf/subscribe [:connection-status]) :reconnecting)
      (rf/dispatch [:alert/js "Oops! Connection Lost. Reconnecting..."]))
    [:> Box {:flex "1 1 100%"
             :position "relative"
             :gridArea "main-content"
             :alignItems "flex-start"
             :justifyContent "stretch"
             :paddingTop "3.25rem"
             :display "flex"
             :overflowY "overlay"
             :sx {"&:before" {:content "''"
                              :position "absolute"
                              :inset 0
                              :top "3.25rem"
                              "-webkit-app-region" "no-drag"}
                  "::-webkit-scrollbar" {:background "background.basement"
                                         :width "0.5rem"
                                         :height "0.5rem"}
                  "::-webkit-scrollbar-corner" {:bg "background.basement"}
                  "::-webkit-scrollbar-thumb" {:bg "background.upper"
                                               :borderRadius "full"}}
             :on-scroll (when (= @route-name :home)
                          #(rf/dispatch [:daily-note/scroll]))}
     (case @route-name
       :settings      [perf-mon/hoc-perfmon-no-new-tx {:span-name "pages/settings"}
                       [settings/page]]
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
