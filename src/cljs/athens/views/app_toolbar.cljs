(ns athens.views.app-toolbar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.electron :as electron]
    [athens.router :as router]
    [athens.style :refer [color]]
    [athens.subs]
    #_[athens.util :as util]
    [athens.views.buttons :refer [button]]
    [athens.views.modal :refer [modal-style]]
    [komponentit.modal :as modal]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def app-header-style
  {:grid-area "app-header"
   :-webkit-app-region "drag"
   :justify-content "flex-start"
   :background-clip "padding-box"
   :align-items "center"
   :display "grid"
   :position "absolute"
   :top "-0.25rem"
   :right 0
   :left 0
   :grid-template-columns "auto 1fr auto"
   :z-index "1000"
   :grid-auto-flow "column"
   :padding "0.25rem 0.75rem"
   ;; TODO: padding (and toolbar height) should be linked
   ;; to zoom level, so zooming out doesn't cause the buttons
   ;; to be hidden by traffic lights.
   :padding-left "80px"
   ::stylefy/manual [[:svg {:font-size "20px"}]
                     [:button {:justify-self "flex-start"}]]})


(def app-header-control-section-style
  {:display "grid"
   :grid-auto-flow "column"
   :background (:color :background-color :opacity-med)
   :backdrop-filter "blur(0.375rem)"
   :padding "0.25rem"
   :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
   :grid-gap "0.25rem"})


(def app-header-secondary-controls-style
  (merge app-header-control-section-style
         {:color (color :body-text-color :opacity-med)
          :justify-self "flex-end"
          :margin-left "auto"
          ::stylefy/manual [[:button {:color "inherit"}]]}))


(def separator-style
  {:border "0"
   :background (color :background-minus-1 :opacity-high)
   :margin-inline "20%"
   :margin-block "0"
   :inline-size "1px"
   :block-size "auto"})


(stylefy/keyframes "fade-in"
                   [:from
                    {:opacity "0"}]
                   [:to
                    {:opacity "1"}])


(def modal-contents-style
  {:display "flex"
   :padding "1.5rem"
   :flex-direction "column"
   :align-items "center"
   ::stylefy/manual [[:p {:max-width "24rem"
                          :text-align "center"}]
                     [:button {:font-size "18px"}]]})


(def features-table-style
  {:background (color :background-plus-1 :opacity-low)
   :border-radius "0.5rem"
   :margin "0.5rem auto 1.25rem"
   ::stylefy/manual [[:th {:font-weight "normal"
                           :opacity "0.75"
                           :padding-block-start "0.5rem"
                           :text-align "left"}]
                     [:th :td {:padding-inline "0.25rem"
                               :padding-block "0.125rem"}]
                     [:tr:last-child [:td {:padding-block-end "0.5rem"}]]
                     [:th:first-child :td:first-child {:padding-inline-start "1rem"}]
                     [:th:last-child :td:last-child {:padding-inline-end "1rem"}]]})


;;; Components


(defn separator
  []
  [:hr (use-style separator-style)])


(defn feature-yes
  []
  [(r/adapt-react-class mui-icons/Check) {:style {:margin "auto"
                                                  :display "block"
                                                  :color (color :confirmation-color)}}])


(defn feature-no
  []
  [(r/adapt-react-class mui-icons/Close) {:style {:margin "auto"
                                                  :display "block"
                                                  :color (color :warning-color)}}])


(defn features-table
  []
  [:table (use-style features-table-style)
   [:thead
    [:th]
    [:th "Athens"]
    [:th "Roam"]]
   [:tbody
    [:tr
     [:td "Text Editing"]
     [:td [feature-yes]]
     [:td [feature-yes]]]
    [:tr
     [:td "Bidirectional Links"]
     [:td [feature-yes]]
     [:td [feature-yes]]]
    [:tr
     [:td "Timeline (Daily Notes)"]
     [:td [feature-yes]]
     [:td [feature-yes]]]
    [:tr
     [:td "Bookmarked Pages"]
     [:td [feature-yes]]
     [:td [feature-yes]]]
    [:tr
     [:td "Todos, Kanban, etc."]
     [:td [feature-no]]
     [:td [feature-yes]]]]])


(defn app-toolbar
  []
  (let [left-open?  (subscribe [:left-sidebar/open])
        right-open? (subscribe [:right-sidebar/open])
        route-name  (subscribe [:current-route/name])
        db-filepath (subscribe [:db/filepath])
        state       (r/atom {:modal nil})
        theme-dark  (subscribe [:theme/dark])
        close-modal #(swap! state assoc :modal nil)]
    (fn []
      (let [{:keys [modal]} @state]
        [:<>
         [:header (use-style app-header-style)
          [:div (use-style app-header-control-section-style)
           [button {:active   @left-open?
                    :on-click #(dispatch [:left-sidebar/toggle])}
            [:> mui-icons/Menu]]
           [separator]
           ;; TODO: refactor to effects
           [button {:on-click #(.back js/window.history)} [:> mui-icons/ChevronLeft]]
           [button {:on-click #(.forward js/window.history)} [:> mui-icons/ChevronRight]]
           [separator]
           [button {:on-click router/nav-daily-notes
                    :active   (= @route-name :home)} [:> mui-icons/Today]]
           [button {:on-click #(router/navigate :pages)
                    :active   (= @route-name :pages)} [:> mui-icons/FileCopy]]
           [button {:on-click #(dispatch [:athena/toggle])
                    :style    {:width "14rem" :margin-left "1rem" :background (color :background-minus-1)}
                    :active   @(subscribe [:athena/open])}
            [:<> [:> mui-icons/Search] [:span "Find or Create a Page"]]]]

          [:div (use-style app-header-secondary-controls-style)
           ;; Click to Open
           #_[button {:on-click #(prn "TODO")}
              [(r/adapt-react-class mui-icons/FolderOpen)
               {:style {:align-self "center"}}]]
           ;; sync UI
           #_[(r/adapt-react-class mui-icons/FiberManualRecord)
              {:style {:color      (color (if @db-synced
                                            :confirmation-color
                                            :highlight-color))
                       :align-self "center"}}]
           #_[separator]
           [button {:on-click #(swap! state assoc :modal :folder)}
            [:> mui-icons/FolderOpen]]
            ;;[:> mui-icons/Publish]]
           [separator]
           [button {:on-click #(dispatch [:theme/toggle])}
            (if @theme-dark
              [:> mui-icons/ToggleOff]
              [:> mui-icons/ToggleOn])]
           [separator]
           [button {:active   @right-open?
                    :on-click #(dispatch [:right-sidebar/toggle])}
            [:> mui-icons/VerticalSplit {:style {:transform "scaleX(-1)"}}]]]]

         (case modal
           :folder
           [:div (use-style modal-style)
            [modal/modal
             {:title    [:div.modal__title
                         [:> mui-icons/FolderOpen]
                         [:h4 "Move"]
                         [button
                          {:on-click close-modal}
                          [:> mui-icons/Close]]]
              :content  [:div (use-style modal-contents-style)

                         [:b {:style {:align-self "flex-start"}} "Current Location"]
                         [:code {:style {:margin "1rem 0 2rem 0"}} @db-filepath]
                         [:div (use-style {:display         "flex"
                                           :justify-content "space-between"
                                           :align-items     "center"
                                           :min-width       "200px"})
                          [button {:primary  true
                                   :on-click #(electron/open-dialog!)}
                           "Open"]
                          [button {:primary  true
                                   :on-click #(electron/move-dialog!)}
                           "Move"]
                          #_[button {:primary  true
                                     :on-click #(prn "Create")}
                             "Create"]]]
              :on-close close-modal}]]

           ;; always false — not supporting import modal yet
           :import
           [:div (use-style modal-style)
            [modal/modal
             {:title    [:div.modal__title [:> mui-icons/Publish] [:h4 "Import to Athens"] [button
                                                                                            {:on-click close-modal}
                                                                                            [:> mui-icons/Close]]]
              :content  [:div (use-style modal-contents-style)
                         ;; TODO: Write intro copy
                         [:p "Some helpful framing about what Athens does and what users should expect. Athens is not Roam."]
                         [features-table]
                         ;; TODO: Create browser file dialog and actually import stuff
                         [:div [button {:primary true} "Add Files"]]]
              :on-close close-modal}]]
           nil)]))))


