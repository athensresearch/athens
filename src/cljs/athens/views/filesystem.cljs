(ns athens.views.filesystem
  (:require
    ["@material-ui/icons/ArrowBack" :as ArrowBack]
    ["@material-ui/icons/Close" :as Close]
    ["@material-ui/icons/FolderOpen" :as FolderOpen]
    [athens.electron :as electron]
    [athens.subs]
    #_[athens.util :as util]
    [athens.views.buttons :refer [button]]
    [athens.views.modal :refer [modal-style]]
    [komponentit.modal :as modal]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


(def modal-contents-style
  {:display "flex"
   :padding "1.5rem"
   :flex-direction "column"
   :align-items "center"
   :width "400px"
   ::stylefy/manual [[:p {:max-width "24rem"
                          :text-align "center"}]
                     [:button {:font-size "18px"}]]})


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
                    [:> FolderOpen]
                    [:h4 "Filesystem"]
                    (when-not @loading
                      [button {:on-click close-modal} [:> Close]])]
         :content  [:div (use-style modal-contents-style)
                    (if (:create @state)
                      [:<>
                       [button {:style    {:align-self "start" :padding "0"}
                                :on-click #(swap! state update :create not)}
                        [:<>
                         [:> ArrowBack]
                         [:span "Back"]]]
                       [:div {:style {:display         "flex"
                                      :justify-content "space-between"
                                      :width           "100%"
                                      :margin-top      "2em"
                                      :margin-bottom   "1em"}}
                        [:label "Database Name"]
                        [:input {:value       (:input @state)
                                 :placeholder "DB Name"
                                 :on-change   #(swap! state assoc :input (.. % -target -value))}]]
                       [:div {:style {:display         "flex"
                                      :justify-content "space-between"
                                      :width           "100%"}}
                        [:label "Location"]
                        [button {:primary  true
                                 :on-click #(electron/create-dialog! (:input @state))}
                         "Browse"]]]
                      [:<>
                       [:b {:style {:align-self "flex-start"}}
                        (if @loading
                          "No DB Found At"
                          "Current Location")]
                       [:code {:style {:margin "1rem 0 2rem 0"}} @db-filepath]
                       [:div (use-style {:display         "flex"
                                         :justify-content "space-between"
                                         :align-items     "center"
                                         :width           "80%"})
                        [button {:primary  true
                                 :on-click #(electron/open-dialog!)}
                         "Open"]
                        [button {:disabled @loading
                                 :primary  true
                                 :on-click #(electron/move-dialog!)}
                         "Move"]
                        [button {:primary  true
                                 :on-click #(swap! state update :create not)}
                         "Create"]]])]
         :on-close close-modal}]])))
