(ns athens.views.pages.settings
  (:require
    ["@chakra-ui/react" :refer [Text Heading Box FormControl FormLabel ButtonGroup Grid Input Button Switch Modal ModalOverlay ModalContent ModalHeader ModalBody ModalCloseButton]]
    [athens.db :refer [default-athens-persist]]
    [athens.util :refer [toast]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [subscribe dispatch reg-event-fx]]
    [reagent.core :as r])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


;; Helpers

(defn handle-submit-email
  [value update-fn]
  (let [api       "https://dhx9n94ty5.execute-api.us-east-1.amazonaws.com/Prod/hello"
        email-qs  "?email="
        query-url (str api email-qs @value)]
    (go (let [resp (<! (http/get query-url))]
          (cond

            ;; Open Collective Lambda finds email associated with Athens
            (and (:success resp) (true? (:email_exists (:body resp))))
            (do (update-fn @value)
                (toast (clj->js {:title "Account connected"
                                 :status "success"})))

            ;; Open Collective Lambda doesn't find email
            (and (:success resp) (false? (:email_exists (:body resp))))
            (do
              (update-fn nil)

              (toast (clj->js {:title "Account not found"
                               :status "error"
                               :description "No OpenCollective account was found with this email address."})))

            ;; Something else, e.g. networking error
            :else
            (toast (clj->js {:title "Unknown error"
                             :status "error"
                             :description resp})))))))


(defn handle-reset-email
  [value update-fn]
  (reset! value "")
  (update-fn nil))


(defn monitoring-off
  [update-fn]
  (.. js/posthog (capture "opt-out"))
  (.. js/window -posthog opt_out_capturing)
  (js/localStorage.setItem "sentry" "off")
  (update-fn))


(defn monitoring-on
  [update-fn]
  (.. js/window -posthog opt_in_capturing)
  (.. js/posthog (capture "opt-in"))
  (js/localStorage.setItem "sentry" "on")
  (update-fn))


(defn handle-monitoring-click
  [monitoring update-fn]
  (if monitoring
    (monitoring-off (partial update-fn false))
    (monitoring-on (partial update-fn true))))


;; Components


(defn title
  [children]
  [:> Heading {:size "md"}
   children])


(defn header
  [children]
  [:> Box {:gridArea "header"} children])


(defn glance
  [children]
  [:> Box children])


(defn form
  [children]
  [:> Box {:gridArea "form"} children])


(defn help
  [children]
  [:> Text {:color "foreground.secondary"
            :gridArea "help"} children])


(defn setting-wrapper
  ([children]
   [setting-wrapper {} children])
  ([config children]
   (let [{:keys [disabled] :as _props} config]
     [:> Grid {:as "section"
               :py 7
               :gap "1rem"
               :gridTemplateColumns "12rem 1fr"
               :gridTemplateAreas "'header form'
               'header help'"
               :_first {:borderTop "none"}
               :_notFirst {:borderTop "1px solid"
                           :borderColor "separator.divider"}
               :sx {"*" {:opacity (if disabled 0.5 1)}}}
      children])))


(defn email-comp
  "Two email values. One in `init-state`, one is `value`. Only updates init-state email if valid API response. Therefore,
  user is not valid if init-state email is empty string."
  [email update-fn]
  (let [value (r/atom (:email email))]
    (fn []
      [setting-wrapper
       [:<>
        [header
         [title "OpenCollective Address"]
         [glance (if (clojure.string/blank? email)
                   "Not set"
                   email)]]
        [form
         [:<> [:> FormControl
               [:> FormLabel "Email address"]
               [:> Input {:type        " email "
                          :width        "25em"
                          :placeholder " Open Collective Email "
                          :onChange   #(reset! value (.. % -target -value))
                          :value       @value}]]
          [:> ButtonGroup {:pt 2}
           [:> Button {:isDisabled (not (clojure.string/blank? email))
                       :onClick #(handle-submit-email value update-fn)}
            "Submit"]
           [:> Button {:onClick #(handle-reset-email value update-fn)}
            "Reset"]]]]
        [help
         [:p (if (clojure.string/blank? email)
               "You are using the free version of Athens. You are hosting your own data. Please be careful!"
               "Thank you for supporting Athens! Backups are coming soon.")]]]])))


(defn monitoring-comp
  [monitoring update-fn]
  [setting-wrapper
   [:<>
    [header
     [title "Usage and Diagnostics"]]
    [form
     [:> Switch {:defaultChecked monitoring
                 :onChange #(handle-monitoring-click monitoring update-fn)}
      "Send usage data and diagnostics to Athens"]]
    [help
     [:<> [:p "Athens has never and will never look at the contents of your database."]
      [:p "Athens will never ever sell your data."]]]]])


(defn backup-comp
  [backup-time update-fn]
  [setting-wrapper
   [:<>
    [header
     [title "On-disk Backups"]]
    [form
     [:> FormControl
      [:> FormLabel "Idle time before saving new backup"]
      [:> Input {:type         "number"
                 :defaultValue backup-time
                 :width "6em"
                 :mr "0.5rem"
                 :min          0
                 :step         15
                 :max          100
                 :onBlur      #(update-fn (.. % -target -value))}]
      " seconds"]]
    [help
     [:<> [:> Text "Changes are saved immediately."]
      [:> Text (str "Athens will save a new backup " backup-time " seconds after your last edit.")]]]]])


(defn remote-backups-comp
  []
  [setting-wrapper
   {:disabled true}
   [:<>
    [header
     [title "Remote Backups"]
     [glance "Coming soon to "
      [:a {:href   "https://opencollective.com/athens"
           :target "_blank"
           :rel    "noreferrer"}
       " paid users and sponsors"]]]
    [form
     [:> Button {:isDisabled true} "Backup my DB to the cloud"]]]])


(defn reset-settings-comp
  [reset-fn]
  [setting-wrapper
   [:<>
    [header
     [title "Reset settings"]]
    [form
     [:> Button {:onClick reset-fn}
      "Reset all settings to defaults"]]
    [help
     [:<> [:> Text "All settings saved between sessions will be restored to defaults."]
      [:> Text "Databases on disk will not be deleted, but you will need to add them to Athens again."]
      [:> Text "Athens will restart after reset and open the default database path."]]]]])


(reg-event-fx
  :settings/update
  (fn [{:keys [db]} [_ k v]]
    {:db (assoc-in db [:athens/persist :settings k] v)}))


(reg-event-fx
  :settings/reset
  (fn [{:keys [db]} _]
    {:db (assoc db :athens/persist default-athens-persist)
     :dispatch [:boot]}))


(defn page
  []
  (let [{:keys [email monitoring backup-time]} @(subscribe [:settings])]
    [:> Modal {:isOpen true
               :scrollBehavior "inside"
               :onClose #(.back js/window.history)
               :size "xl"}
     [:> ModalOverlay]
     [:> ModalContent {:maxWidth "calc(100% - 8rem)"
                       :width "50rem"
                       :my "4rem"}
      [:> ModalHeader
       {:borderBottom "1px solid" :borderColor "separator.divider"}
       "Settings"
       [:> ModalCloseButton]]
      [:> ModalBody {:flexDirection "column"}
       [:<>
        [email-comp email #(dispatch [:settings/update :email %])]
        [monitoring-comp monitoring #(dispatch [:settings/update :monitoring %])]
        [backup-comp backup-time (fn [x]
                                   (dispatch [:settings/update :backup-time x])
                                   (dispatch [:fs/update-write-db]))]
        [remote-backups-comp]
        [reset-settings-comp #(dispatch [:settings/reset])]]]]]))
