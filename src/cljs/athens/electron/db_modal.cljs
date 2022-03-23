(ns athens.electron.db-modal
  (:require
    ["/components/Button/Button" :refer [Button]]
    ["@material-ui/icons/AddBox" :default AddBox]
    ["@material-ui/icons/Close" :default Close]
    ["@material-ui/icons/Folder" :default Folder]
    ["@material-ui/icons/Group" :default Group]
    ["@material-ui/icons/MergeType" :default MergeType]
    ["@material-ui/icons/Storage" :default Storage]
    ["@chakra-ui/react" :refer [Box Text Modal ModalOverlay Divider VStack Heading ModalContent ModalHeader ModalFooter ModalBody ModalCloseButton ButtonGroup]]
    ["react-dom" :as react-dom]
    [athens.electron.dialogs :as dialogs]
    [athens.electron.utils :as utils]
    [athens.events :as events]
    [athens.subs]
    [athens.util :refer [js-event->val]]
    [athens.views.textinput :as textinput]
    [clojure.edn :as edn]
    [datascript.core :as d]
    [komponentit.modal :as modal]
    [re-frame.core :refer [subscribe dispatch] :as rf]
    [reagent.core :as r]))



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
      [:> Modal {:isOpen open?
                 :onClose close-modal
                 :size "lg"}
       [:> ModalOverlay]
       [:> ModalContent
        [:> ModalHeader
         "Merge from Roam"]
        [:> ModalCloseButton]
        (if (nil? @transformed-roam-db)
          (let [inputRef (atom nil)]
            [:> ModalBody
             [:input {:ref #(reset! inputRef %)
                      :style {:display "none"}
                      :type "file"
                      :accept ".edn"
                      :on-change #(file-cb % transformed-roam-db roam-db-filename)}]
             [:> Heading {:size "md" :as "h2"} "How to merge from Roam"]
             [:> Box {:position "relative"
                      :padding-bottom "56.25%"
                      :margin         "1rem 0 0"
                      :borderRadius  "8px"
                      :overflow "hidden"
                      :flex "1 1 100%"
                      :width          "100%"}
              [:iframe {:src                   "https://www.loom.com/embed/787ed48da52c4149b031efb8e17c0939?hide_owner=true&hide_share=true&hide_title=true&hideEmbedTopBar=true"
                        :frameBorder           "0"
                        :webkitallowfullscreen "true"
                        :mozallowfullscreen    "true"
                        :allowFullScreen       true
                        :style                 {:position "absolute"
                                                :top      0
                                                :left     0
                                                :width    "100%"
                                                :height   "100%"}}]]
             [:> ModalFooter
              [:> ButtonGroup
               [:> Button
                {:onClick #(.click @inputRef)}
                "Upload database"]]]])
          (let [roam-pages   (roam-pages @transformed-roam-db)
                shared-pages (events/get-shared-pages @transformed-roam-db)]
            [:> ModalBody
             [:> Text {:size "md"} (str "Your Roam DB had " (count roam-pages)) " pages. " (count shared-pages) " of these pages were also found in your Athens DB. Press Merge to continue merging your DB."]
             [:> Divider {:my 4}]
             [:> Heading {:size "md" :as "h3"} "Shared Pages"]
             [:> VStack {:as "ol"
                         :align "stretch"
                         :maxHeight "400px"
                         :overflowY "auto"}
              (for [x shared-pages]
                ^{:key x}
                [:li [:> Text (str "[[" x "]]")]])]
             [:> ModalFooter
              [:> ButtonGroup
               [:> Button {:variant  "outline"
                           :onClick (fn []
                                      (dispatch [:upload/roam-edn @transformed-roam-db @roam-db-filename])
                                      (close-modal))}

                "Merge"]]]]))]])))


(defn open-local-comp
  [loading db]
  [:<>
   [:h5 {:style {:align-self "flex-start"
                 :margin-top "2em"}}
    (if @loading
      "No DB Found At"
      "Current Location")]
   [:code {:style {:margin "1rem 0 2rem 0"}} (:id db)]
   [:div #_ (use-style {:display         "flex"
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
         [:div {:style {:width "100%" :margin-top "10px"}}
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
                                 :type        "password"
                                 :value       @password
                                 :placeholder "Password"
                                 :disabled    false
                                 :on-change   #(reset! password (js-event->val %))}]]]
         doall)
       [:> Button {:is-primary true
                   :style      {:margin-top "0.5rem"}
                   :disabled   (or (clojure.string/blank? @name)
                                   (clojure.string/blank? @address))
                   :on-click   #(rf/dispatch [:db-picker/add-and-select-db (utils/self-hosted-db @name @address @password)])}
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
                                   :tab-value (if utils/electron? 0 2)})]
    (fn []
      (.createPortal
        react-dom
        (r/as-element [:div #_ (use-style modal-style)
                       [modal/modal
                        {:title    [:div.modal__title
                                    [:> Storage]
                                    [:h4 "Database"]
                                    (when-not @loading
                                      [:> Button {:on-click close-modal} [:> Close]])]
                         :content  [:div #_ (use-style modal-contents-style)
                                    ;; TODO: this is hacky, we're just hiding the picker and forcing
                                    ;; tab 2 for the web client. Instead we should use Stuart's
                                    ;; redesigned DB picker.
                                    (when utils/electron?
                                      [:div #_ (use-style picker-style)
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
                                        [:span "Join"]]])
                                    (cond
                                      (= 2 (:tab-value @state))
                                      [join-remote-comp]

                                      (= 1 (:tab-value @state))
                                      [create-new-local state]

                                      (= 0 (:tab-value @state))
                                      [open-local-comp loading selected-db])]
                         :on-close close-modal}]])
        el))))
