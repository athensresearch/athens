(ns athens.views.filesystem
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.electron :as electron]
    [athens.style :refer [color]]
    [athens.subs]
    #_[athens.util :as util]
    [athens.views.buttons :refer [button]]
    [athens.views.modal :refer [modal-style]]
    [clojure.string :as str]
    [komponentit.modal :as modal]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


(def modal-contents-style
  {:display "flex"
   :padding "1.5rem"
   :flex-direction "column"
   :align-items "center"
   :width "400px" })


(def database-item-style
  {:padding "1rem 0"
   :align-self "stretch"
   :background (color :background-plus-1 :opacity-low)
   :margin "-0.5rem 0 1.5rem"
   :border [["1px solid" (color :border-color)]]
   :border-radius "0.5rem"
   :display "grid"
   :text-align "start"
   :grid-template-areas "'icon name' 'icon path' 'icon toolbar'"
   :grid-gap "0.125rem 0"
   ::stylefy/manual [[:h4 {:grid-area "name"
                           :margin "0"}]
                     [:p {:grid-area "path"
                          :margin "0"
                          :font-size "0.85em"
                          :color (color :body-text-color :opacity-high)}]
                     [:svg {:grid-area "icon"
                            :margin "auto"
                            :font-size "2.5rem"
                            :grid-row "1 / -1"}]]})

(def database-item-toolbar-style
  {:padding-top "0.5rem"
   :grid-area "toolbar"
   :display "grid"
   :margin-right "auto"
   :grid-auto-flow "column"
   :grid-gap "0.25rem"
   :color (color :body-text-color :opacity-low)
   ::stylefy/manual [[:button {:font-size "0.85em"
                               :padding "0.5em"}]]})


(defn window
  "If loading is true, then that means the user has opened the modal and the db was not found on the filesystem.
  If loading is false, do not allow user to exit modal, and show slightly different UI."
  []
  (let [loading (subscribe [:loading?])
        close-modal (fn []
                      (when-not @loading
                        (dispatch [:modal/toggle])))
        db-filepath (subscribe [:db/filepath])
        state (r/atom {:create false
                       :input ""})]
    (fn []
      [:div (use-style modal-style)
       [modal/modal
        {:title    [:div.modal__title
                    [:h4 "Database"]
                    (when-not @loading
                      [button {:on-click close-modal} [:> mui-icons/Close]])]
         :content  [:div (use-style modal-contents-style)
                    (if (:create @state)
                      [:<>
                       [button {:style    {:align-self "start" :padding "0"}
                                :on-click #(swap! state update :create not)}

                        [:<>
                         [:> mui-icons/ArrowBack]
                         [:span "Back"]]]
                       [:div {:style {:display         "flex"
                                      :justify-content "space-between"
                                      :width           "100%"
                                      :margin-top      "2em"
                                      :margin-bottom   "1em"}}
                        [:label "Database Name"]
                        [:input {:value       (:input @state)
                                 :placeholder "DB Name"
                                 :required true
                                 :on-change   #(swap! state assoc :input (.. % -target -value))}]]
                       [:div {:style {:display         "flex"
                                      :justify-content "space-between"
                                      :width           "100%"}}
                        [:label "Location"]
                        [button {:primary  true
                                 :on-click #(electron/create-dialog! (:input @state))}
                         "Browse"]]]
                      [:<>
                      ;;  [:b {:style {:align-self "flex-start"}}
                      ;;   (if @loading
                      ;;     "No DB Found At"
                      ;;     "Current Location")]
                       ;;  Displaying current database
                       [:div (use-style database-item-style)
                        [:> mui-icons/LibraryBooks]
                        [:h4 (nth (reverse (str/split @db-filepath #"/")) 1)]
                        [:p (->> (reverse (str/split @db-filepath #"/")) (drop 2) (reverse) (str/join "/"))]
                        [:div (use-style database-item-toolbar-style)
                         [button {:disabled @loading
                                  :on-click #(electron/move-dialog!)}
                          "Rename"]
                         [:span "•"]
                         [button {:disabled @loading
                                  :on-click #(electron/move-dialog!)}
                          "Relocate"]]]
                       ;;  Displaying current database
                       [:div (use-style {:display         "flex"
                                         :justify-content "space-between"
                                         :align-items     "center"
                                         :width           "80%"})
                        [button {:primary true
                                 :on-click #(electron/open-dialog!)}
                         "Open Database"]
                        [button {:primary true
                                 :on-click #(swap! state update :create not)}
                         "New Database"]]])]
         :on-close close-modal}]])))
