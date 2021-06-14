(ns athens.views.app-toolbar
  (:require
    ["@material-ui/core/SvgIcon" :default SvgIcon]
    ["@material-ui/icons/BubbleChart" :default BubbleChart]
    ["@material-ui/icons/CheckCircle" :default CheckCircle]
    ["@material-ui/icons/ChevronLeft" :default ChevronLeft]
    ["@material-ui/icons/ChevronRight" :default ChevronRight]
    ["@material-ui/icons/Error" :default Error]
    ["@material-ui/icons/FileCopy" :default FileCopy]
    ["@material-ui/icons/Menu" :default Menu]
    ["@material-ui/icons/MergeType" :default MergeType]
    ["@material-ui/icons/Replay" :default Replay]
    ["@material-ui/icons/Search" :default Search]
    ["@material-ui/icons/Settings" :default Settings]
    ["@material-ui/icons/Storage" :default Storage]
    ["@material-ui/icons/Sync" :default Sync]
    ["@material-ui/icons/Today" :default Today]
    ["@material-ui/icons/ToggleOff" :default ToggleOff]
    ["@material-ui/icons/ToggleOn" :default ToggleOn]
    ["@material-ui/icons/VerticalSplit" :default VerticalSplit]
    [athens.router :as router]
    [athens.style :refer [color unzoom]]
    [athens.subs]
    [athens.util :as util :refer [app-classes]]
    [athens.views.buttons :refer [button]]
    [athens.views.filesystem :as filesystem]
    [athens.views.presence :as presence]
    [athens.ws-client :as ws]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;; Styles

(def window-toolbar-buttons-style
  {:display "flex"
   :margin-left "1rem"
   :align-self "stretch"
   :align-items "stretch"
   :color "inherit"
   ::stylefy/manual [[:&.os-windows [:button {:border-radius 0
                                              :width "48px"
                                              :min-height "32px"
                                              :display "flex"
                                              :align-items "center"
                                              :color (color :body-text-color :opacity-med)
                                              :background (color :background-minus-1)
                                              :transition "background 0.075s ease-in-out, filter 0.075s ease-in-out, color 0.075s ease-in-out"
                                              :justify-content "center"
                                              :border 0}
                                     [:svg {:font-size "16px"}]]
                      [:&.theme-light [:button:hover {:filter "brightness(92%)"}]]
                      [:&.theme-dark [:button:hover {:filter "brightness(150%)"}]]
                      [:&.theme-dark :&.theme-light [:button.close:hover {:background "#E81123" ; Windows close button background color
                                                                          :filter "none"
                                                                          :color "#fff"}]]]
                     ;; Styles for linux (Ubuntu)
                     [:&.os-linux {:display "grid"
                                   :padding "4px"
                                   :padding-right "8px"
                                   :grid-auto-flow "column"
                                   :grid-gap "4px"}
                      [:button {:position "relative"
                                :margin "auto"
                                :width "32px"
                                :height "32px"
                                :display "flex"
                                :align-items "center"
                                :background "transparent"
                                :color (color :body-text-color :opacity-med)
                                :transition "background 0.075s ease-in-out, filter 0.075s ease-in-out, color 0.075s ease-in-out"
                                :justify-content "center"
                                :border 0}
                       [:&:before {:content "''"
                                   :border-radius "1000em"
                                   :z-index -1
                                   :position "absolute"
                                   :background (color :background-plus-1)
                                   :inset "6px"}]
                       [:&.close {:color "#fff"}
                        [:&:before {:background "#555"}]] ; Ubuntu close button background color
                       [:&.minimize [:svg {:position "relative"
                                           :top "5px"}]]
                       [:svg {:font-size "12px"}]]
                      [:&.theme-light [:button:hover {:filter "brightness(92%)"}]]
                      [:&.theme-dark [:button:hover {:filter "brightness(150%)"}]]
                      [:&.is-focused ["button.close::before" {:background "#E9541F"}]]]]})


(def app-header-style
  {:grid-area "app-header"
   :justify-content "flex-start"
   :background-clip "padding-box"
   :background (color :background-plus-1)
   :color (color :body-text-color :opacity-high)
   :border-bottom "1px solid transparent"
   :align-items "center"
   :display "grid"
   :height "48px"
   :padding-left "10px"
   :grid-template-columns "auto 1fr auto"
   :transition "border-color 1s ease"
   :z-index "1070"
   :grid-auto-flow "column"
   :-webkit-app-region "drag"
   ::stylefy/manual [["&.is-fullscreen" {:height "44px"}]
                     [:svg {:font-size "20px"}]
                     [:&:hover {:transition "border-color 0.15s ease"
                                :border-bottom-color (color :body-text-color :opacity-lower)}]
                     [:button {:justify-self "flex-start"
                               :-webkit-app-region "no-drag"}]
                     ;; Windows-only styles
                     [:&.os-windows {:background (color :background-minus-1)
                                     :padding-left "10px"}]
                     ;; Mac-only styles
                     [:&.os-mac {:background (color :background-color :opacity-high)
                                 :color (color :body-text-color :opacity-med)
                                 :padding-left "88px"
                                 :padding-right "22px"
                                 :height "52px"
                                 :backdrop-filter "blur(20px)"
                                 :position "absolute"
                                 :top 0
                                 :right 0
                                 :left 0}
                      ["&.is-fullscreen" {:padding-left "22px"}]
                      ["button:not(:hover):not(.is-active)" {:background "transparent"}]]]})


(def athena-button-label-style
  {:padding-left "1rem"
   :padding-right "1rem"
   ::stylefy/media {{:max-width "800px"} {:display "none"}}})


(def app-header-control-section-style
  {:display "grid"
   :grid-auto-flow "column"
   :grid-gap "0.25rem"})


(def app-header-secondary-controls-style
  (merge app-header-control-section-style
         {:justify-self "flex-end"
          :margin-left "auto"
          ::stylefy/manual [[:button {:color "inherit"
                                      :background "inherit"}]]}))


(def separator-style
  {:border 0
   :margin-inline "0.125rem"
   :margin-block 0
   :block-size "auto"})


(def sync-icon-style
  {:background (color :background-minus-2)
   :border-radius "100%"
   :padding 0
   :margin 0
   :height "12px !important"
   :width "12px !important"})


(stylefy/keyframes "fade-in"
                   [:from
                    {:opacity 0}]
                   [:to
                    {:opacity 1}])


;; Components


(defn separator
  []
  [:hr (use-style separator-style)])


(defn app-toolbar
  []
  (let [left-open?        (subscribe [:left-sidebar/open])
        right-open?       (subscribe [:right-sidebar/open])
        route-name        (subscribe [:current-route/name])
        os                (util/get-os)
        electron?         (util/electron?)
        theme-dark        (subscribe [:theme/dark])
        remote-graph-conf (subscribe [:db/remote-graph-conf])
        socket-status     (subscribe [:socket-status])
        win-focused?      (subscribe [:win-focused?])
        win-maximized?    (subscribe [:win-maximized?])
        win-fullscreen?   (subscribe [:win-fullscreen?])
        merge-open?       (reagent.core/atom false)]
    (fn []
      [:<>
       (when @merge-open?
         [filesystem/merge-modal merge-open?])
       [:header (merge (use-style app-header-style
                                  {:class (app-classes {:os os
                                                        :electron? electron?
                                                        :theme-dark? @theme-dark
                                                        :win-focused? @win-focused?
                                                        :win-fullscreen? @win-fullscreen?
                                                        :win-maximized? @win-maximized?})})
                       (unzoom))
        [:div (use-style app-header-control-section-style)
         [button {:active @left-open?
                  :title "Toggle Navigation Sidebar"
                  :on-click #(dispatch [:left-sidebar/toggle])}
          [:> Menu]]
         [separator]
         ;; TODO: refactor to effects
         (when electron?
           [:<>
            [button {:on-click #(.back js/window.history)} [:> ChevronLeft]]
            [button {:on-click #(.forward js/window.history)} [:> ChevronRight]]
            [separator]])
         [button {:on-click router/nav-daily-notes
                  :title "Open Today's Daily Note"
                  :active   (= @route-name :home)} [:> Today]]
         [button {:on-click #(router/navigate :pages)
                  :title "Open All Pages"
                  :active   (= @route-name :pages)} [:> FileCopy]]
         [button {:on-click #(router/navigate :graph)
                  :title "Open Graph"
                  :active   (= @route-name :graph)} [:> BubbleChart]]
         ;; below is used for testing error tracking
         #_[button {:on-click #(throw (js/Error "error"))
                    :style {:border "1px solid red"}} [:> Warning]]
         [button {:on-click #(dispatch [:athena/toggle])
                  :class "athena"
                  :style    {:background "inherit"}
                  :active   @(subscribe [:athena/open])}
          [:<> [:> Search] [:span (use-style athena-button-label-style) "Find or Create a Page"]]]]

        [:div (use-style app-header-secondary-controls-style)
         (if electron?
           [:<>
            [presence/presence-popover-info]
            (when (= @socket-status :closed)
              [button
               {:onClick #(ws/start-socket!
                            (assoc @remote-graph-conf
                                   :reload-on-init? true))}
               [:<>
                [:> Replay]
                [:span "Re-connect with remote"]]])
            [button {:on-click #(swap! merge-open? not)
                     :title "Merge Roam Database"}
             [:> MergeType]]
            [button {:on-click #(router/navigate :settings)
                     :title "Open Settings"
                     :active   (= @route-name :settings)}
             [:> Settings]]

            [button {:on-click #(dispatch [:modal/toggle])
                     :title "Choose Database"}
             [:div {:style {:display "flex"}}
              [:> Storage {:style {:align-self "center"}}]
              [:div {:style {:margin-left "-10px"
                             :align-self "flex-end"}}
               (cond
                 (= @socket-status :closed)
                 [:> Error (merge (use-style sync-icon-style)
                                  {:style {:color (color :error-color)}
                                   :title "Disconnected"})]
                 (or (and (:default? @remote-graph-conf)
                          (= @socket-status :running))
                     @(subscribe [:db/synced]))
                 [:> CheckCircle (merge (use-style sync-icon-style)
                                        {:style {:color (color :confirmation-color)}
                                         :title "Synced"})]
                 :else [:> Sync (merge (use-style sync-icon-style)
                                       {:style {:color (color :highlight-color)}
                                        :title "Synchronizing..."})])]]]

            [separator]]
           [button {:style {:min-width "max-content"} :on-click #(dispatch [:get-db/init]) :primary true} "Load Test DB"])
         [button {:on-click #(dispatch [:theme/toggle])
                  :title "Toggle Color Scheme"}
          (if @theme-dark
            [:> ToggleOff]
            [:> ToggleOn])]
         [separator]
         [button {:active   @right-open?
                  :title "Toggle Sidebar"
                  :on-click #(dispatch [:right-sidebar/toggle])}
          [:> VerticalSplit {:style {:transform "scaleX(-1)"}}]]]

        (when (and (contains? #{:windows :linux} os) electron?)
          [:div (use-style window-toolbar-buttons-style
                           {:class (app-classes {:os os
                                                 :electron? electron?
                                                 :theme-dark? @theme-dark
                                                 :win-focused? @win-focused?
                                                 :win-fullscreen? @win-fullscreen?
                                                 :win-maximized? @win-maximized?})})

           ;; Minimize Button
           [:button.minimize
            {:on-click #(dispatch [:toggle-max-min-win true])
             :title "Minimize"}
            [:> SvgIcon
             [:line
              {:stroke "currentColor", :stroke-width "2", :x1 "4", :x2 "20", :y1 "11", :y2 "11"}]]]
           ;; Exit Fullscreen Button
           (if @win-fullscreen?
             [:button.exit-fullscreen
              {:on-click #(dispatch [:exit-fullscreen-win])
               :title  "Exit FullScreen"}
              [:> SvgIcon
               [:path
                {:d "M11 13L5 19M11 13V19M11 13H5"
                 :stroke "currentColor"
                 :stroke-width "2"}]
               [:path
                {:d "M13 11L19.5 4.5M13 11L13 5M13 11L19 11"
                 :stroke "currentColor"
                 :stroke-width "2"}]]]
             ;; Maximize/Restore Button
             [:button.maximize-restore
              {:on-click #(dispatch [:toggle-max-min-win false])
               :title (if @win-maximized?
                        "Restore"
                        "Maximize")}
              (if @win-maximized?
                ;; SVG Restore
                [:> SvgIcon
                 [:path {:d "M8 5H19V16H8V5Z"
                         :fill "none"
                         :stroke "currentColor"
                         :stroke-width "2"}]
                 [:path {:d "M16 17V19H5V8H7"
                         :fill "none"
                         :stroke "currentColor"
                         :stroke-width "2"}]]
                ;; SVG Maximize
                [:> SvgIcon
                 [:rect
                  {:height "14"
                   :stroke "currentColor"
                   :fill "none"
                   :stroke-width "2"
                   :width "14"
                   :x "5"
                   :y "5"}]])])
           ;; Close Button
           [:button.close
            {:on-click #(dispatch [:close-win])
             :title "Close Athens"}
            [:> SvgIcon
             [:line
              {:stroke "currentColor"
               :stroke-width "2"
               :x1 "4.44194"
               :x2 "19.4419"
               :y1 "4.55806"
               :y2 "19.5581"}]
             [:line
              {:stroke "currentColor"
               :stroke-width "2"
               :x1 "4.55806"
               :x2 "19.5581"
               :y1 "19.5581"
               :y2 "4.55806"}]]]])]])))
