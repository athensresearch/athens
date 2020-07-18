(ns athens.devcards.app-toolbar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.router :refer [navigate]]
    [athens.style :refer [color]]
    [athens.subs]
    [athens.views.buttons :refer [button]]
    [athens.views.modal :refer [modal-style]]
    [komponentit.modal :as modal]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def app-header-style
  {:grid-area "app-header"
   :justify-content "flex-start"
   :background-clip "padding-box"
   :align-items "center"
   :display "grid"
   :position "absolute"
   :top 0
   :right 0
   :left 0
   :grid-template-columns "auto 1fr auto"
   :z-index "1000"
   :grid-auto-flow "column"
   :padding "0.25rem 0.75rem 0.25rem 0.25rem"
   ;; :padding "0.25rem 0.75rem 0.25rem 66px" ;; Electron styling
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
  (let [left-open? (subscribe [:left-sidebar/open])
        right-open? (subscribe [:right-sidebar/open])
        current-route (subscribe [:current-route])
        import-modal-open? (r/atom false)
        route-name (-> @current-route :data :name)]

    (fn []
      [:<>
       [:header (use-style app-header-style)
        [:div (use-style app-header-control-section-style)
         [button {:active @left-open?
                  :on-click #(dispatch [:left-sidebar/toggle])}
          [:> mui-icons/Menu]]
      ;; [separator] // for Electron implementation
      ;; [button {:on-click-fn #(navigate :home)
      ;;          :label [:> mui-icons/ChevronLeft]}]
      ;; [button {:on-click-fn #(navigate :home)
      ;;          :label [:> mui-icons/ChevronRight]}]
         [separator]
         [button {:on-click #(navigate :home)
                  :active (when (= route-name :home) true)} [:> mui-icons/Today]]
         [button {:on-click #(navigate :pages)
                  :active (when (= route-name :pages) true)}
          [:> mui-icons/FileCopy]]
         [button {:on-click #(dispatch [:athena/toggle])
                  :style {:width "14rem" :margin-left "1rem" :background (color :background-minus-1)}
                  :active (when @(subscribe [:athena/open]) true)}
          [:<> [:> mui-icons/Search] [:span "Find or Create a Page"]]]]

        [:div (use-style app-header-secondary-controls-style)
         [button {:on-click #(reset! import-modal-open? true)}
          [:> mui-icons/Publish]]
         [separator]
         [button {:active @right-open?
                  :on-click #(dispatch [:right-sidebar/toggle])}
          [:> mui-icons/VerticalSplit {:style {:transform "scaleX(-1)"}}]]]]

       (when @import-modal-open?
         [:div (use-style modal-style)
          [modal/modal
           {:title [:div.modal__title [:> mui-icons/Publish] [:h4 "Import to Athens"] [button
                                                                                       {:on-click #(reset! import-modal-open? false)}
                                                                                       [:> mui-icons/Close]]]
            :content [:div (use-style modal-contents-style)
                      ;; TODO: Write intro copy
                      [:p "Some helpful framing about what Athens does and what users should expect. Athens is not Roam."]
                      [features-table]
                      ;; TODO: Create browser file dialog and actually import stuff
                      [:div [button {:primary true} "Add Files"]]]
            :on-close #(reset! import-modal-open? false)}]])])))

