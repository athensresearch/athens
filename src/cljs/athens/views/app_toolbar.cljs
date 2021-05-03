(ns athens.views.app-toolbar
  (:require
   ["@material-ui/core/SVGIcon" :default SVGIcon]
   ["@material-ui/icons/BubbleChart" :default BubbleChart]
   ["@material-ui/icons/ChevronLeft" :default ChevronLeft]
   ["@material-ui/icons/ChevronRight" :default ChevronRight]
   ["@material-ui/icons/FiberManualRecord" :default FiberManualRecord]
   ["@material-ui/icons/FileCopy" :default FileCopy]
   ["@material-ui/icons/LibraryBooks" :default LibraryBooks]
   ["@material-ui/icons/Menu" :default Menu]
   ["@material-ui/icons/MergeType" :default MergeType]
   ["@material-ui/icons/Replay" :default Replay]
   ["@material-ui/icons/Search" :default Search]
   ["@material-ui/icons/Settings" :default Settings]
   ["@material-ui/icons/Today" :default Today]
   ["@material-ui/icons/ToggleOff" :default ToggleOff]
   ["@material-ui/icons/ToggleOn" :default ToggleOn]
   ["@material-ui/icons/VerticalSplit" :default VerticalSplit]
   [athens.router :as router]
   [athens.style :refer [color]]
   [athens.subs]
   [athens.util :as util]
   [athens.views.buttons :refer [button]]
   [athens.views.filesystem :as filesystem]
   [athens.views.presence :as presence]
   [athens.ws-client :as ws]
   [re-frame.core :refer [subscribe dispatch]]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles

(def win-toolbar-buttons-style
  {:display "flex"
   :margin-left "1rem"
   ::stylefy/manual [[:button {:border-radius 0
                               :width "48px"
                               :height "32px"
                               :display "flex"
                               :align-items "center"
                               :background (color :background-minus-1)
                               :justify-content "center"
                               :border 0}]
                     [:&.theme-light [:button:hover {:filter "brightness(92%)"}]]
                     [:&.theme-dark [:button:hover {:filter "brightness(150%)"}]]
                     [:&.theme-dark :&.theme-light [:button.close:hover {:background "#E81123" ;; Windows close button background color
                                                                         :filter "none"
                                                                         :color "#fff"}]]]})


(def mac-app-header-style
  {:grid-area "app-header"
   :justify-content "flex-start"
   :background-clip "padding-box"
   :background (color :background-minus-1)
   :align-items "center"
   :display "grid"
   :position "absolute"
   :top "0"
   :backdrop-filter "blur(0.375rem)"
   :right 0
   :left 0
   :grid-template-columns "auto 1fr auto"
   :z-index "1070"
   :grid-auto-flow "column"
   :-webkit-app-region "drag"
   :border-bottom [["1px solid " (color :body-text-color :opacity-lower)]]
   ::stylefy/manual [[:svg {:font-size "20px"}]
                     [:button {:justify-self "flex-start"
                               :-webkit-app-region "no-drag"}]]})


(def win-app-header-style
  {:grid-area "app-header"
   :justify-content "flex-start"
   :background-clip "padding-box"
   :background (color :background-minus-1)
   :align-items "center"
   :display "grid"
   :position "absolute"
   :top "0"
   :backdrop-filter "blur(0.375rem)"
   :right 0
   :left 0
   :grid-template-columns "auto 1fr auto"
   :z-index "1070"
   :grid-auto-flow "column"
   :-webkit-app-region "drag"
   :border-bottom [["1px solid " (color :body-text-color :opacity-lower)]]
   ::stylefy/manual [[:svg {:font-size "20px"}]
                     [:button {:justify-self "flex-start"
                               :-webkit-app-region "no-drag"}]]})


(def linux-app-header-style
  {:grid-area "app-header"
   :justify-content "flex-start"
   :background-clip "padding-box"
   :background (color :background-minus-1)
   :align-items "center"
   :display "grid"
   :position "absolute"
   :top "0"
   :backdrop-filter "blur(0.375rem)"
   :right 0
   :left 0
   :grid-template-columns "auto 1fr auto"
   :z-index "1070"
   :grid-auto-flow "column"
   :-webkit-app-region "drag"
   :border-bottom [["1px solid " (color :body-text-color :opacity-lower)]]
   ::stylefy/manual [[:svg {:font-size "20px"}]
                     [:button {:justify-self "flex-start"
                               :-webkit-app-region "no-drag"}]]})


(def app-header-control-section-style
  {:display "grid"
   :grid-auto-flow "column"
   :background "inherit"
   :backdrop-filter "blur(0.375rem)"
   :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
   :grid-gap "0.25rem"
   ::stylefy/manual [[:button {:border-radius 0
                               :color "inherit"
                               :background "inherit"}]]})


(def app-header-secondary-controls-style
  (merge app-header-control-section-style
         {:color (color :body-text-color :opacity-med)
          :justify-self "flex-end"
          :margin-left "auto"
          ::stylefy/manual [[:button {:border-radius 0
                                      :color "inherit"
                                      :background "inherit"}]]}))


(def separator-style
  {:border "0"
   :background (color :background-minus-2 :opacity-high)
   :margin-inline "20%"
   :margin-block "0"
   :inline-size "1px"
   :block-size "auto"})


(stylefy/keyframes "fade-in"
                   [:from
                    {:opacity "0"}]
                   [:to
                    {:opacity "1"}])


;;; Components


(defn separator
  []
  [:hr (use-style separator-style)])


(defn app-toolbar
  []
  (let [left-open?        (subscribe [:left-sidebar/open])
        right-open?       (subscribe [:right-sidebar/open])
        route-name        (subscribe [:current-route/name])
        electron?         (util/electron?)
        theme-dark        (subscribe [:theme/dark])
        remote-graph-conf (subscribe [:db/remote-graph-conf])
        socket-status     (subscribe [:socket-status])
        win-maximized?    (subscribe [:win-maximized?])
        merge-open?       (reagent.core/atom false)]
    (fn []
      [:<>


       (when @merge-open?
         [filesystem/merge-modal merge-open?])

       [:header (use-style (cond
                             (and (= (util/get-os) :mac) (util/electron?)) mac-app-header-style
                             (and (= (util/get-os) :windows) (util/electron?)) win-app-header-style
                             (and (= (util/get-os) :linux) (util/electron?)) linux-app-header-style)
                             {:class [(if theme-dark "theme-dark" "theme-light")]})



        [:div (use-style app-header-control-section-style)
         [button {:active   @left-open?
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
                  :style    {:width "14rem" :margin-left "1rem" :background (color :background-minus-1)}
                  :active   @(subscribe [:athena/open])}
          [:<> [:> Search] [:span "Find or Create a Page"]]]]

        [:div (use-style app-header-secondary-controls-style)
         (if electron?
           [:<>
            [presence/presence-popover-info]
            [(reagent.core/adapt-react-class FiberManualRecord)
             {:style {:color (color (cond
                                      (= @socket-status :closed)
                                      :error-color

                                      (or (and (:default? @remote-graph-conf)
                                               (= @socket-status :running))
                                          @(subscribe [:db/synced]))
                                      :confirmation-color

                                      :else :highlight-color))
                      :align-self "center"}
              :title (cond
                       (= @socket-status :closed)
                       "Disconnected"

                       (or (and (:default? @remote-graph-conf)
                                (= @socket-status :running))
                           @(subscribe [:db/synced]))
                       "Synced"

                       :else "Synchronizing...")}]
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
             [:> LibraryBooks]]
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
          [:> VerticalSplit {:style {:transform "scaleX(-1)"}}]]

         (if (and (= (util/get-os) :win) (util/electron?))
           [:div (use-style win-toolbar-buttons-style
                            {:class (if @theme-dark "theme-dark" "theme-light")})
            [:button
             {:on-click #(dispatch [:toggle-max-min-win true])
              :title "Minimize"}
             [:> SVGIcon
              [:line
               {:stroke "currentColor", :stroke-width "2", :x1 "4", :x2 "20", :y1 "11", :y2 "11"}]]]

          ;; (if @is-maximized?
            [:button
             {:on-click #(dispatch [:toggle-max-min-win false])
              :title (if @win-maximized? "Restore" "Maximize")}
             (if @win-maximized?
               [:> SVGIcon
                [:path {:d "M8 5H19V16H8V5Z"
                        :fill "none" :stroke "currentColor", :stroke-width "2"}]
                [:path {:d "M16 17V19H5V8H7",                 :fill "none" :stroke "currentColor", :stroke-width "2"}]]
               [:> SVGIcon
                [:rect
                 {:height "14"
                  :stroke "currentColor"
                  :fill "none"
                  :stroke-width "2"
                  :width "14"
                  :x "5"
                  :y "5"}]])]
            ;;  )

            [:button
             {:on-click #(dispatch [:toggle-max-min-win true])
              :class "close"
              :title "Close Athens"}
             [:> SVGIcon
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
                :y2 "4.55806"}]]]])]]])))

