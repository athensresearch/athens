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
   :width "400px"})


(def database-item-style
  {:padding "1rem"
   :align-self "stretch"
   :background (color :background-plus-1 :opacity-low)
   :margin "-1rem -0.5rem 1rem"
   :border [["1px solid" (color :border-color)]]
   :border-radius "0.5rem"
   :display "grid"
   :text-align "start"
   :grid-template-areas "'icon name' 'icon path' 'icon toolbar'"
   :grid-gap "0.125rem 1rem"
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


(def db-name-style
  {:color "inherit"
   :padding "0.125rem 0.5rem"
   :font-size "1.3125em"
   :border 0
   :caret-color (color :link-color)
   :border-radius "0.25rem"
   :transition "all 0.1s ease"
   :margin-inline-start "-0.5rem"
   :margin-inline-end "1.5rem"
   :background (color :background-minus-1)
   ::stylefy/manual [[:&:read-only {:color (color :header-text-color)
                                    :background "transparent"}]]})


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
                       :renaming false
                       :input ""})]
    (fn []
      [:div (use-style modal-style)
       [modal/modal
        {:title    [:div.modal__title
                    [:h4 (if (:create @state)
                           "Create New Database"
                           "Current Database")]
                    (when-not @loading
                      [button {:on-click close-modal} [:> mui-icons/Close]])]
         :content  [:div (use-style modal-contents-style)
                    (if (:create @state)
                      [:<>
                       [:div (use-style database-item-style)
                        [:> mui-icons/LibraryBooks]
                        [:input
                         (use-style db-name-style
                                    {;; TODO: Autofocus doesn't seem to be working
                                     :autoFocus true
                                     :value "{Default database name}"})]
                        [:p "{Default database location}"]
                        [:div (use-style database-item-toolbar-style)
                         [button {:disabled @loading
                                  :on-click #(electron/move-dialog!)}
                          "Move"]]]
                       ;;  Displaying current database
                       [:div (use-style {:display         "flex"
                                         :justify-content "space-between"
                                         :align-items     "center"
                                         :width           "80%"})
                        [button {:on-click #(swap! state update :create not)}
                         [:<>
                          [:> mui-icons/ArrowBack]
                          [:span "Back"]]]
                        [button {:primary true
                                 :on-click #(swap! state update :create not)}
                         [:<>
                          [:> mui-icons/Add]
                          [:span "Create"]]]]]

                      [:<>
                      ;;  [:b {:style {:align-self "flex-start"}}
                      ;;   (if @loading
                      ;;     "No DB Found At"
                      ;;     "Current Location")]
                        ;; Displaying current database
                       [:div (use-style database-item-style)
                        [:> mui-icons/LibraryBooks]
                        [:input
                         (use-style db-name-style
                                    {:read-only (not (:renaming @state))
                                     ;; TODO: Autofocus doesn't seem to be working
                                     :autoFocus (:renaming @state)
                                     :value (nth (reverse (str/split @db-filepath #"/")) 1)})]
                        [:p (->> (reverse (str/split @db-filepath #"/")) (drop 2) (reverse) (str/join "/"))]
                        [:div (use-style database-item-toolbar-style)
                         [button {:disabled @loading
                                  :on-click #(swap! state update :renaming not)}
                          "Rename"]
                         [:span "•"]
                         [button {:disabled @loading
                                  :on-click #(electron/move-dialog!)}
                          "Move"]]]
                       ;;  Displaying current database
                       [:div (use-style {:display         "flex"
                                         :justify-content "space-between"
                                         :align-items     "center"
                                         :width           "80%"})
                        [button {:primary true
                                 :on-click #(electron/open-dialog!)}
                         [:<>
                          [:> mui-icons/FolderOpen]
                          [:span "Open"]]]
                        [button {:primary true
                                 :on-click #(swap! state update :create not)}
                         [:<>
                          [:> mui-icons/Add]
                          [:span "New"]]]]])]
         :on-close close-modal}]])))
