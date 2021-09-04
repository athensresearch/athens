(ns athens.views.pages.settings
  (:require
    ["@material-ui/icons/Check" :default Check]
    ["@material-ui/icons/NotInterested" :default NotInterested]
    [athens.db :refer [default-athens-persist]]
    [athens.util :refer [js-event->val]]
    [athens.views.buttons :refer [button]]
    [athens.views.textinput :as textinput]
    [athens.views.toggle-switch :as toggle-switch]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [re-frame.core :refer [subscribe dispatch reg-event-fx]]
    [reagent.core :as r]
    [stylefy.core :as stylefy])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


;; Styles

(def settings-wrap-style
  {:border-top      "1px solid var(--border-color)"
   :padding-top     "2rem"
   :padding-bottom  "2rem"
   :padding         "2rem 0.75rem"
   :line-height     "1.25"
   ::stylefy/manual [[:h3 {:margin 0}]
                     [:&.disabled {:opacity 0.5}]
                     [:header {:padding-bottom "1rem"}]
                     [:span.glance {:font-weight "normal"
                                    :opacity     0.7
                                    :font-size   "0.8em"
                                    :gap         "0.25em"}
                      [:svg {:vertical-align "-0.25em"
                             :font-size      "1.5em"}]]
                     [:aside {:font-size   "0.8em"
                              :padding-top "0.5rem"}]
                     [:p {:margin "0.25rem 0"}]
                     [:p:first-child {:margin-top 0}]
                     [:p:last-child {:margin-bottom 0}]
                     [:label {:display     "flex"
                              :gap         "0.5rem"
                              :align-items "center"
                              :font-weight "bold"}]]
   ::stylefy/media  {{:min-width "40em"}
                     {:display               "grid"
                      :grid-template-columns "10rem 1fr"
                      :grid-gap              "1rem"}}})


(def settings-page-styles
  {:width     "50em"
   :max-width "100%"
   :margin    "2rem auto"})


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
            (update-fn @value)

            ;; Open Collective Lambda doesn't find email
            (and (:success resp) (false? (:email_exists (:body resp))))
            (do
              (update-fn nil)
              (js/alert "No OpenCollective account was found with this email address."))

            ;; Something else, e.g. networking error
            :else
            (js/alert (str "Unexpected error" resp)))))))


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


(defn setting-wrapper
  ([children]
   [setting-wrapper {} children])
  ([config children]
   (let [{:keys [disabled] :as _props} config]
     [:div (stylefy/use-style settings-wrap-style
                              {:class [(when disabled "disabled")]}) children])))


(defn email-comp
  "Two email values. One in `init-state`, one is `value`. Only updates init-state email if valid API response. Therefore,
  user is not valid if init-state email is empty string."
  [email update-fn]
  (let [value (r/atom (:email email))]
    (fn []
      [setting-wrapper
       [:<>
        [:header
         [:h3 "Email"]
         [:span.glance (if (clojure.string/blank? email)
                         "Not set"
                         email)]]
        [:main
         [:div
          [textinput/textinput {:type        " email "
                                :placeholder " Open Collective Email "
                                :on-change   #(reset! value (.. % -target -value))
                                :value       @value}]
          [button {:primary  true
                   :disabled (not (clojure.string/blank? email))
                   :on-click #(handle-submit-email value update-fn)}
           "Submit"]
          [button {:on-click #(handle-reset-email value update-fn)}
           "Reset"]]
         [:aside
          [:p (if (clojure.string/blank? email)
                "You are using the free version of Athens. You are hosting your own data. Please be careful!"
                "Thank you for supporting Athens! Backups are coming soon.")]]]]])))


(defn monitoring-comp
  [monitoring update-fn]
  [setting-wrapper
   [:<>
    [:header
     [:h3 "Usage and Diagnostics"]
     [:span.glance (if (true? monitoring)
                     [:<>
                      [:> Check]
                      [:span "Sending usage data"]]
                     [:<>
                      [:> NotInterested]
                      [:span "Not sending usage data"]])]]
    [:main
     [:label {:style {:cursor "pointer"}}
      [toggle-switch/toggle-switch {:checked   monitoring
                                    :on-change #(handle-monitoring-click monitoring update-fn)}]
      "Send usage data and diagnostics to Athens"]
     [:aside
      [:p "Athens has never and will never look at the contents of your database."]
      [:p "Athens will never ever sell your data."]]]]])


(defn backup-comp
  [backup-time update-fn]
  [setting-wrapper
   [:<>
    [:header
     [:h3 "Backups"]
     [:span.glance (str backup-time " seconds after last edit")]]
    [:main
     [:label
      [textinput/textinput {:type         "number"
                            :defaultValue backup-time
                            :min          0
                            :step         15
                            :max          100
                            :on-blur      #(update-fn (.. % -target -value))}]
      " seconds"]
     [:aside
      [:p "Changes are saved immediately."]
      [:p (str "Athens will save a new backup " backup-time " seconds after your last edit.")]]]]])


(defn remote-backups-comp
  []
  [setting-wrapper
   {:disabled true}
   [:<>
    [:header
     [:h3 "Remote Backups"]
     [:span.glance "Coming soon to "
      [:a {:href   "https://opencollective.com/athens"
           :target "_blank"
           :rel    "noreferrer"}
       " paid users and sponsors"]]]
    [:main
     [button {:disabled true} "Backup my DB to the cloud"]]]])


(defn settings-container
  [child]
  [:div (stylefy/use-style settings-page-styles) child])


(defn remote-username-comp
  [username update-fn]
  [setting-wrapper
   [:<>
    [:header
     [:h3 "Username"]
     [:span.glance username]]
    [:main
     [textinput/textinput {:type         "text"
                           :placeholder  "Username"
                           :on-blur      #(update-fn username (js-event->val %))
                           :defaultValue username}]
     [:aside
      [:p "For now, a username is only needed if you are connected to a server."]]]]])


(defn reset-settings-comp
  [reset-fn]
  [setting-wrapper
   [:<>
    [:header
     [:h3 "Reset settings"]]
    [:main
     [button {:on-click reset-fn}
      "Reset all settings to defaults"]
     [:aside
      [:p "All settings saved between sessions will be restored to defaults."]
      [:p "Databases on disk will not be deleted, but you will need to add them to Athens again."]
      [:p "Athens will restart after reset and open the default database path."]]]]])


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
  (let [{:keys [email username monitoring backup-time]} @(subscribe [:settings])]
    [settings-container
     [:<>
      [:h1 "Settings"]
      [email-comp email #(dispatch [:settings/update :email %])]
      [monitoring-comp monitoring #(dispatch [:settings/update :monitoring %])]
      [backup-comp backup-time (fn [x]
                                 (dispatch [:settings/update :backup-time x])
                                 (dispatch [:fs/update-write-db]))]
      [remote-backups-comp]
      [remote-username-comp username (fn [current-username new-username]
                                       (dispatch [:presence/send-username current-username new-username])
                                       (dispatch [:settings/update :username new-username]))]
      [reset-settings-comp #(dispatch [:settings/reset])]]]))
