(ns athens.electron.db-modal
  (:require
    ["@chakra-ui/react" :refer [HStack VStack FormControl FormLabel Input Button Box Tabs Tab TabList TabPanel TabPanels Text Modal ModalOverlay Divider VStack Heading ModalContent ModalHeader ModalFooter ModalBody ModalCloseButton ButtonGroup]]
    [athens.electron.dialogs :as dialogs]
    [athens.electron.utils :as utils]
    [athens.events :as events]
    [athens.subs]
    [athens.util :refer [js-event->val]]
    [clojure.edn :as edn]
    [datascript.core :as d]
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
      [:> Modal {:isOpen @open?
                 :onClose close-modal
                 :closeOnOverlayClick false
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
               [:> Button {:onClick (fn []
                                      (dispatch [:upload/roam-edn @transformed-roam-db @roam-db-filename])
                                      (close-modal))}

                "Merge"]]]]))]])))


(defn form-container
  [content footer]
  [:> Box {:as "form"
           :display "contents"}
   [:> Box {:p 5 :pt 4}
    content]
   [:> ModalFooter {:borderTop "1px solid"
                    :borderColor "separator.divider"
                    :p 2
                    :pr 5} footer]])


(defn open-local-comp
  [loading db]
  [form-container
   [:> FormControl {:isReadOnly true}
    [:> FormLabel (if @loading
                    "No DB Found At"
                    "Current database location")]
    [:> HStack
     [:> Text {:as "output"
               :borderRadius "md"
               :cursor "default"
               :bg "background.floor"
               :color "foreground.secondary"
               :flex "1 1 100%"
               :py 1.5
               :px 2.5
               :display "flex"}
      (:id db)]
     [:> Button {:isDisabled @loading
                 :size "sm"
                 :onClick #(dialogs/move-dialog!)}
      "Move"]]]
   [:> ButtonGroup
    [:> Button {:onClick #(dialogs/open-dialog!)}
     "Open from file"]]])


(defn create-new-local
  [state]
  [form-container
   [:> FormControl
    [:> FormLabel "Name"]
    [:> Input {:value (:input @state)
               :onChange  #(swap! state assoc :input (js-event->val %))}]]
   [:> ButtonGroup
    [:> Button {:value (:input @state)
                :isDisabled (clojure.string/blank? (:input @state))
                :onClick #(dialogs/create-dialog! (:input @state))}
     "Choose folder"]]])


(defn join-remote-comp
  []
  (let [name     (r/atom "")
        address  (r/atom "")
        password (r/atom "")]
    (fn []
      [form-container
       (->>
         [:> VStack {:spacing 4}
          [:> FormControl
           [:> FormLabel "Database name"]
           [:> Input {:value @name
                      :onChange #(reset! name (js-event->val %))}]]
          [:> FormControl
           [:> FormLabel "Remote address"]
           [:> Input {:value @address
                      :onChange #(reset! address (js-event->val %))}]]
          [:> FormControl {:flexDirection "row"}
           [:> FormLabel "Password"]
           [:> Input {:value @password
                      :type "password"
                      :onChange #(reset! password (js-event->val %))}]]]
         doall)
       [:> ButtonGroup
        [:> Button {:type "submit"
                    :isDisabled (or (clojure.string/blank? @name)
                                    (clojure.string/blank? @address))
                    :onClick   #(rf/dispatch [:db-picker/add-and-select-db (utils/self-hosted-db @name @address @password)])}
         "Join"]]])))


(defn window
  "If loading is true, then that means the user has opened the modal and the db was not found on the filesystem.
  If loading is false, do not allow user to exit modal, and show slightly different UI."
  []
  (let [loading           (subscribe [:loading?])
        close-modal       (fn []
                            (when-not @loading
                              (dispatch [:modal/toggle])))
        selected-db       @(subscribe [:db-picker/selected-db])
        state             (r/atom {:input ""})]
    (fn []
      [:> Modal {:isOpen loading
                 :motionPreset "scale"
                 :onClose close-modal}
       [:> ModalOverlay]
       [:> ModalContent
        [:> ModalHeader
         "Add Database"]
        (when-not @loading
          [:> ModalCloseButton])
        [:> ModalBody {:display "contents"}
         ;; TODO: this is hacky, we're just hiding the picker and forcing
         ;; tab 2 for the web client. Instead we should use Stuart's
         ;; redesigned DB picker.
         [:> Tabs {:isFitted true
                   :display "contents"
                   :defaultIndex (if utils/electron? 0 1)}
          (when utils/electron?
            [:> TabList
             [:> Tab "Open Local"]
             [:> Tab "Join Remote"]
             [:> Tab "Create New"]])
          [:> TabPanels {:display "contents"}
           [:> TabPanel {:display "contents"}
            [open-local-comp loading selected-db]]
           [:> TabPanel {:display "contents"}
            [join-remote-comp]]
           [:> TabPanel {:display "contents"}
            [create-new-local state]]]]]]])))
