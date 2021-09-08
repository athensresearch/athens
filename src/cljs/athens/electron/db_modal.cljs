(ns athens.electron.db-modal
  (:require
    ["/components/Button/Button" :refer [Button]]
    ["@material-ui/icons/AddBox" :default AddBox]
    ["@material-ui/icons/Close" :default Close]
    ["@material-ui/icons/Folder" :default Folder]
    ["@material-ui/icons/Group" :default Group]
    ["@material-ui/icons/MergeType" :default MergeType]
    ["@material-ui/icons/Storage" :default Storage]
    [athens.electron.dialogs :as dialogs]
    [athens.electron.utils :as utils]
    [athens.events :as events]
    [athens.style :refer [color]]
    [athens.subs]
    [athens.util :refer [js-event->val]]
    [athens.views.modal :refer [modal-style]]
    [athens.views.textinput :as textinput]
    [clojure.edn :as edn]
    [datascript.core :as d]
    [komponentit.modal :as modal]
    [re-frame.core :refer [subscribe dispatch] :as rf]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


(def modal-contents-style
  {:display         "flex"
   :padding         "0 1rem 1.5rem 1rem"
   :flex-direction  "column"
   :align-items     "center"
   :justify-content "flex-start"
   :width           "500px"
   :height          "17em"
   ::stylefy/manual [[:p {:max-width  "24rem"
                          :text-align "center"}]
                     [:button.toggle-button {:font-size     "18px"
                                             :align-self    "flex-start"
                                             :padding-left  "0"
                                             :margin-bottom "1rem"}]
                     [:code {:word-break "break-all"}]
                     [:.MuiTabs-indicator {:background-color "var(--link-color)"}]]})


(def picker-style
  {:display         "grid"
   :grid-auto-flow "column"
   :grid-auto-columns "1fr"
   :border-radius "0.5rem"
   :flex "0 0 auto"
   :font-size "1em"
   :margin "0.25rem 0"
   :align-self "stretch"
   :overflow "hidden"
   :transition "box-shadow 0.2s ease, filter 0.2s ease"
   :background (color :background-color)
   :padding "1px"
   ::stylefy/manual [[:&:hover {}]
                     [:button {:text-align "center"
                               :appearance "none"
                               :border "0"
                               :border-radius "calc(0.5rem - 1px)"
                               :padding "0.5rem 0.5rem"
                               :color "inherit"
                               :display "flex"
                               :justify-content "center"
                               :align-items "center"
                               :position "relative"
                               :z-index "0"
                               :background "inherit"}
                      [:svg {:margin-inline-end "0.25em" :font-size "1.25em"}]
                      [:&:hover {:filter "contrast(105%)"}]
                      [:&:active {:filter "contrast(110%)"}]
                      [:&.active {:background (color :background-plus-2)
                                  :z-index "5"
                                  :box-shadow [["0 1px 5px" (color :shadow-color)]]}]]]})


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
                    [:> Button {:on-click close-modal}
                     [:> Close]]]

         :content  [:div (use-style (merge modal-contents-style))
                    (if (nil? @transformed-roam-db)
                      [:<>
                       [:input {:style {:flex "0 0 auto"} :type "file" :accept ".edn" :on-change #(file-cb % transformed-roam-db roam-db-filename)}]
                       [:div {:style {:position       "relative"
                                      :padding-bottom "56.25%"
                                      :margin         "1em 0 0"
                                      :flex "1 1 100%"
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
                         [:> Button {:style    {:align-self "center"}
                                     :is-primary  true
                                     :on-click (fn []
                                                 (dispatch [:upload/roam-edn @transformed-roam-db @roam-db-filename])
                                                 (close-modal))}
                          "Merge"]]))]

         :on-close close-modal}]])))


(defn open-local-comp
  [loading db]
  [:<>
   [:h5 {:style {:align-self "flex-start"
                 :margin-top "2em"}}
    (if @loading
      "No DB Found At"
      "Current Location")]
   [:code {:style {:margin "1rem 0 2rem 0"}} (:id db)]
   [:div (use-style {:display         "flex"
                     :justify-content "space-between"
                     :align-items     "center"
                     :width           "80%"})
    [:> Button {:is-primary  true
                :on-click #(dialogs/open-dialog!)}
     "Open"]
    [:> Button {:disabled @loading
                :is-primary  true
                :on-click #(dialogs/move-dialog!)}
     "Move"]]])


(defn create-new-local
  [state]
  [:<>
   [:div {:style {:display         "flex"
                  :justify-content "space-between"
                  :width           "100%"
                  :margin-top      "2em"
                  :margin-bottom   "1em"}}
    [:h5 "Database Name"]
    [textinput/textinput {:value       (:input @state)
                          :placeholder "DB Name"
                          :on-change   #(swap! state assoc :input (js-event->val %))}]]
   [:div {:style {:display         "flex"
                  :justify-content "space-between"
                  :width           "100%"}}
    [:h5 "New Location"]
    [:> Button {:is-primary  true
                :disabled (clojure.string/blank? (:input @state))
                :on-click #(dialogs/create-dialog! (:input @state))}
     "Browse"]]])


(defn join-remote-comp
  []
  (let [name     (r/atom "RTC")
        address  (r/atom "localhost:3010")
        password (r/atom "")]
    (fn []
      [:<>
       (->>
         [:div {:style {:width  "100%" :margin-top "10px"}}
          [:h5 "Database Name"]
          [:div {:style {:margin          "5px 0"
                         :display         "flex"
                         :justify-content "space-between"}}
           [textinput/textinput {:style       {:flex-grow 1
                                               :padding   "5px"}
                                 :type        "text"
                                 :value       @name
                                 :placeholder "DB name"
                                 :on-change   #(reset! name (js-event->val %))}]]
          [:h5 "Remote Address"]
          [:div {:style {:margin          "5px 0"
                         :display         "flex"
                         :justify-content "space-between"}}
           [textinput/textinput {:style       {:flex-grow 1
                                               :padding   "5px"}
                                 :type        "text"
                                 :value       @address
                                 :placeholder "Remote server address"
                                 :on-change   #(reset! address (js-event->val %))}]]
          [:h5 "Password"]
          [:div {:style {:margin          "5px 0"
                         :display         "flex"
                         :justify-content "space-between"}}
           [textinput/textinput {:style       {:flex-grow 1
                                               :padding   "5px"}
                                 :type        "text"
                                 :value       @password
                                 :placeholder "Password (not supported yet)"
                                 :disabled    true ; TODO: not supported yet
                                 :on-change   #(reset! password (js-event->val %))}]]]
         doall)
       [:> Button {:is-primary  true
                   :style    {:margin-top "0.5rem"}
                   :disabled (or (clojure.string/blank? @name)
                                 (clojure.string/blank? @address))
                   :on-click #(rf/dispatch [:db-picker/add-and-select-db (utils/self-hosted-db @name @address)])}
        "Join"]])))


(defn window
  "If loading is true, then that means the user has opened the modal and the db was not found on the filesystem.
  If loading is false, do not allow user to exit modal, and show slightly different UI."
  []
  (let [loading           (subscribe [:loading?])
        close-modal       (fn []
                            (when-not @loading
                              (dispatch [:modal/toggle])))
        el (.. js/document (querySelector "#app"))
        selected-db       @(subscribe [:db-picker/selected-db])
        state             (r/atom {:input     ""
                                   :tab-value 0})]
    (fn []
      (js/ReactDOM.createPortal
        (r/as-element [:div (use-style modal-style)
                       [modal/modal
                        {:title    [:div.modal__title
                                    [:> Storage]
                                    [:h4 "Database"]
                                    (when-not @loading
                                      [:> Button {:on-click close-modal} [:> Close]])]
                         :content  [:div (use-style modal-contents-style)
                                    [:div (use-style picker-style)
                                     [:button {:class (when (= 0 (:tab-value @state)) "active")
                                               :on-click (fn [] (swap! state assoc :tab-value 0))}
                                      [:> Folder]
                                      [:span "Open"]]
                                     [:button {:class (when (= 1 (:tab-value @state)) "active")
                                               :on-click (fn [] (swap! state assoc :tab-value 1))}
                                      [:> AddBox]
                                      [:span "New"]]
                                     [:button {:class (when (= 2 (:tab-value @state)) "active")
                                               :on-click (fn [] (swap! state assoc :tab-value 2))}
                                      [:> Group]
                                      [:span "Join"]]]
                                    (cond
                                      (= 2 (:tab-value @state))
                                      [join-remote-comp]

                                      (= 1 (:tab-value @state))
                                      [create-new-local state]

                                      (= 0 (:tab-value @state))
                                      [open-local-comp loading selected-db])]
                         :on-close close-modal}]])
        el))))
