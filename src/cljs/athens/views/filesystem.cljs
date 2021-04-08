(ns athens.views.filesystem
  (:require
    ["@material-ui/icons/ArrowBack" :default ArrowBack]
    ["@material-ui/icons/Close" :default Close]
    ["@material-ui/icons/FolderOpen" :default FolderOpen]
    ["@material-ui/icons/MergeType" :default MergeType]
    [athens.electron :as electron]
    [athens.events :as events]
    [athens.subs]
    #_[athens.util :as util]
    [athens.views.buttons :refer [button]]
    [athens.views.modal :refer [modal-style]]
    [clojure.edn :as edn]
    [datascript.core :as d]
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


(defn file-cb
  [e transformed-db roam-db-filename]
  (let [fr   (js/FileReader.)
        file (.. e -target -files (item 0))]
    (set! (.-onload fr)
          (fn [e]
            (let [edn-data                  (.. e -target -result)
                  filename                  (.-name file)
                  db                        (edn/read-string {:readers datascript.core/data-readers} edn-data)
                  transformed-dates-roam-db (athens.events/update-roam-db-dates db)]
              (reset! roam-db-filename filename)
              (reset! transformed-db transformed-dates-roam-db))))
    (.readAsText fr file)))


(defn roam-pages
  [roam-db]
  (d/q '[:find [?pages ...]
         :in $
         :where
         [_ :node/title ?pages]]
       roam-db))


(defn merge-modal
  [open?]
  (let [close-modal         #(reset! open? false)
        transformed-roam-db (r/atom nil)
        roam-db-filename    (r/atom "")]
    (fn []
      [:div (use-style modal-style)
       [modal/modal

        {:title    [:div.modal__title
                    [:> MergeType]
                    [:h4 "Merge Roam DB"]
                    [button {:on-click close-modal}
                     [:> Close]]]

         :content  [:div (use-style modal-contents-style)
                    (if (nil? @transformed-roam-db)
                      [:<>
                       [:input {:type "file" :accept ".edn" :on-change #(file-cb % transformed-roam-db roam-db-filename)}]
                       [:div {:style {:position       "relative"
                                      :padding-bottom "56.25%"
                                      :margin         "20px 0"
                                      :width          "100%"}}
                        [:iframe {:src                   "https://www.loom.com/embed/787ed48da52c4149b031efb8e17c0939"
                                  :frameBorder           "0"
                                  :webkitallowfullscreen "true"
                                  :mozallowfullscreen    "true"
                                  :allowFullScreen       true
                                  :style                 {:position "absolute"
                                                          :top      0
                                                          :left     0
                                                          :width    "100%"
                                                          :height   "100%"}}]]]
                      (let [roam-pages   (roam-pages @transformed-roam-db)
                            shared-pages (events/get-shared-pages @transformed-roam-db)]
                        [:div {:style {:display "flex" :flex-direction "column"}}
                         [:h6 (str "Your Roam DB had " (count roam-pages)) " pages. " (count shared-pages) " of these pages were also found in your Athens DB. Press Merge to continue merging your DB."]
                         [:p {:style {:margin "10px 0 0 0"}} "Shared Pages"]
                         [:ol {:style {:max-height "400px"
                                       :width      "100%"
                                       :overflow-y "auto"}}
                          (for [x shared-pages]
                            ^{:key x}
                            [:li (str "[[" x "]]")])]
                         [button {:style    {:align-self "center"}
                                  :primary  true
                                  :on-click (fn []
                                              (dispatch [:upload/roam-edn @transformed-roam-db @roam-db-filename])
                                              (close-modal))}
                          "Merge"]]))]

         :on-close close-modal}]])))


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
