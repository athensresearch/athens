(ns athens.views.pages.core
  (:require
    ["@chakra-ui/react" :refer [Box]]
    [athens.util :refer [toast]]
    [athens.views.hoc.perf-mon :as perf-mon]
    [athens.views.pages.all-pages :as all-pages]
    [athens.views.pages.daily-notes :as daily-notes]
    [athens.views.pages.graph :as graph]
    [athens.views.pages.page :as page]
    [athens.views.pages.settings :as settings]
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
             :gridArea "main-content"
             :alignItems "flex-start"
             :justifyContent "stretch"
             :display "flex"
             :overflowY "auto"
             :sx {"@supports (overflow-y: overlay)" {:overflowY "overlay"}
                  :maskImage "linear-gradient(to bottom,
                  transparent,
                  #000000cc 1rem, 
                  black 1.5rem, 
                  black calc(100vh - 5rem), 
                  #000000f0 calc(100vh - 4rem), 
                  #00000088 100vh)"
                  ".os-mac &" {:maskImage "linear-gradient(to bottom,
                  transparent var(--app-header-height),
                  black calc(var(--app-header-height) + 2rem),
                  black 5rem, 
                  black calc(100vh - 5rem), 
                  #000000f0 calc(100vh - 4rem), 
                  #00000088 100vh)"}
                  "&:before" {:content "''"
                              :position "fixed"
                              :zIndex "-1"
                              :inset 0
                              :pointerEvents "none"
                              :top "3.25rem"
                              :WebkitAppRegion "no-drag"}
                  "::WebkitScrollbar" {:background "background.basement"
                                       :width "0.5rem"
                                       :height "0.5rem"}
                  "::WebkitScrollbar-corner" {:bg "background.basement"}
                  "::WebkitScrollbar-thumb" {:bg "background.upper"
                                             :borderRadius "full"}
                  "> .node-page:first-of-type, .block-page:first-of-type" {:mt 0
                                                                           :pt "var(--app-header-height)"}}
             :on-scroll (when (= @route-name :home)
                          throttled-scroll)}
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
