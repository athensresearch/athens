(ns athens.electron.db-modal
  (:require
    ["@chakra-ui/react" :refer [HStack VStack FormControl FormLabel Input Button Box Tabs Tab TabList TabPanel TabPanels Text Modal ModalOverlay VStack ModalContent ModalHeader ModalFooter ModalBody ModalCloseButton ButtonGroup]]
    [athens.electron.dialogs :as dialogs]
    [athens.electron.utils :as utils]
    [athens.subs]
    [athens.util :refer [js-event->val]]
    [re-frame.core :refer [subscribe dispatch] :as rf]
    [reagent.core :as r]))


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
                    "Current workspace location")]
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
    [:> Input {:onChange  #(swap! state assoc :input (js-event->val %))}]]
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
           [:> FormLabel "Workspace name"]
           [:> Input {:defaultValue @name
                      :onChange #(reset! name (js-event->val %))}]]
          [:> FormControl
           [:> FormLabel "Remote address"]
           [:> Input {:defaultValue @address
                      :onChange #(reset! address (js-event->val %))}]]
          [:> FormControl {:flexDirection "row"}
           [:> FormLabel "Password"]
           [:> Input {:defaultValue @password
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
         "Add Workspace"]
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
            [:> TabList {:px 2}
             [:> Tab "Open from file"]
             [:> Tab "Join remote "]
             [:> Tab "Create new"]])
          [:> TabPanels {:display "contents"}
           [:> TabPanel {:display "contents"}
            [open-local-comp loading selected-db]]
           [:> TabPanel {:display "contents"}
            [join-remote-comp]]
           [:> TabPanel {:display "contents"}
            [create-new-local state]]]]]]])))
