(ns athens.views.pages.settings
  (:require
    ["@material-ui/icons/Check" :default Check]
    ["@material-ui/icons/NotInterested" :default NotInterested]
    ["@material-ui/icons/SettingsBackupRestore" :default SettingsBackupRestore]
    [athens.db :refer [default-keymap]]
    [athens.electron :as electron]
    [athens.keybindings :refer [mousetrap-record]]
    [athens.util :refer [js-event->val]]
    [athens.views.buttons :refer [button]]
    [athens.views.textinput :as textinput]
    [athens.views.toggle-switch :as toggle-switch]
    [athens.views.utils :refer [track-outside-click]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [goog.functions :as goog-functions]
    [re-frame.core :refer [subscribe dispatch]]
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


(defn init-email
  []
  (let [email (js/localStorage.getItem "auth/email")]
    (if (= email "null")
      ""
      email)))


(defn init-monitoring
  "Returns true if opted-out
  Monitoring = true if opted-in"
  []
  (not (.. js/window -posthog has_opted_out_capturing)))


(defn init-autosave-time
  []
  (js/Number (js/localStorage.getItem "debounce-save-time")))


(defn init-state
  []
  {:email         (init-email)
   :monitoring    (init-monitoring)
   :autosave-time (init-autosave-time)})


(defn handle-reset-email
  [s value]
  (reset! value "")
  (swap! s assoc :email "")
  (js/localStorage.setItem "auth/email" nil))


(defn handle-submit-email
  [s value]
  (let [api "https://dhx9n94ty5.execute-api.us-east-1.amazonaws.com/Prod/hello"
        email-qs "?email="
        query-url (str api email-qs @value)]
    (go (let [resp (<! (http/get query-url))]
          (cond

            ;; Open Collective Lambda finds email associated with Athens
            (and (:success resp) (true? (:email_exists (:body resp))))
            (do
              (js/localStorage.setItem "auth/email" @value)
              (swap! s assoc :email @value))

            ;; Open Collective Lambda doesn't find email
            (and (:success resp) (false? (:email_exists (:body resp))))
            (do
              (js/localStorage.setItem "auth/email" nil)
              (swap! s assoc :email "")
              (js/alert "No OpenCollective account was found with this email address."))

            ;; Something else, e.g. networking error
            :else
            (js/alert (str "Unexpected error" resp)))))))


(defn monitoring-off
  [s]
  (.. js/posthog (capture "opt-out"))
  (.. js/window -posthog opt_out_capturing)
  (js/localStorage.setItem "sentry" "off")
  (swap! s update :monitoring not))


(defn monitoring-on
  [s]
  (.. js/window -posthog opt_in_capturing)
  (.. js/posthog (capture "opt-in"))
  (js/localStorage.setItem "sentry" "on")
  (swap! s update :monitoring not))


(defn handle-monitoring-click
  [s]
  (if (:monitoring @s)
    (monitoring-off s)
    (monitoring-on s)))


(defn handle-blur-autosave-input
  [e s]
  (let [value (.. e -target -value)]
    (swap! s assoc :autosave-time value)
    (set! electron/debounce-write-db (goog-functions/debounce electron/write-db (* 1000 value)))
    (js/localStorage.setItem "debounce-save-time" value)))


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
  [s]
  (let [value (r/atom (:email @s))]
    (fn [s]
      [setting-wrapper
       [:<>
        [:header
         [:h3 "Email"]
         [:span.glance (if (clojure.string/blank? (:email @s))
                         "Not set"
                         (:email @s))]]
        [:main
         [:div
          [textinput/textinput {:type        " email "
                                :placeholder " Open Collective Email "
                                :on-change   #(reset! value (.. % -target -value))
                                :value       @value}]
          [button {:primary  true
                   :disabled (not (clojure.string/blank? (:email @s)))
                   :on-click #(handle-submit-email s value)}
           "Submit"]
          [button {:on-click #(handle-reset-email s value)}
           "Reset"]]
         [:aside
          [:p (if (clojure.string/blank? (:email @s))
                "You are using the free version of Athens. You are hosting your own data. Please be careful!"
                "Thank you for supporting Athens! Backups are coming soon.")]]]]])))


(defn monitoring-comp
  [s]
  [setting-wrapper
   [:<>
    [:header
     [:h3 "Usage and Diagnostics"]
     [:span.glance (if (true? (:monitoring @s))
                     [:<>
                      [:> Check]
                      [:span "Sending usage data"]]
                     [:<>
                      [:> NotInterested]
                      [:span "Not sending usage data"]])]]
    [:main
     [:label {:style {:cursor "pointer"}}
      [toggle-switch/toggle-switch {:checked   (:monitoring @s)
                                    :on-change #(handle-monitoring-click s)}]
      "Send usage data and diagnostics to Athens"]
     [:aside
      [:p "Athens has never and will never look at the contents of your database."]
      [:p "Athens will never ever sell your data."]]]]])


(defn autosave-comp
  [s]
  [setting-wrapper
   [:<>
    [:header
     [:h3 "AutoSave"]
     [:span.glance (str (:autosave-time @s) " seconds")]]
    [:main
     [:label
      [textinput/textinput {:type         "number"
                            :defaultValue (:autosave-time @s)
                            :min          0
                            :step         15
                            :max          100
                            :on-blur      #(handle-blur-autosave-input % s)}]
      " seconds"]
     [:aside
      [:p (str "Athens will save and create a local backup " (:autosave-time @s) " seconds after your last edit.")]]]]])


(defn backups-comp
  [_s]
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


(defn handle-user-name-change
  [e]
  (dispatch [:user/set :name (js-event->val e)])
  (js/localStorage.setItem "user/name" (js-event->val e)))


(defn remote-username-comp
  []
  (let [remote-graph-conf @(subscribe [:db/remote-graph-conf])
        remote? (:default? remote-graph-conf)]
    [setting-wrapper
     (when (not remote?) {:disabled true})
     [:<>
      [:header
       [:h3 "Username"]
       [:span.glance (:name @(subscribe [:user/current]))]]
      [:main
       [textinput/textinput {:type         "text"
                             :placeholder  "Username"
                             :disabled     (not remote?)
                             :on-blur      handle-user-name-change
                             :defaultValue (:name @(subscribe [:user/current]))}]
       [:aside
        [:p "For now, a username is only needed if you are connected to a server."]]]]]))


;; -- Keymap Settings -----------------------------------------------------------
(def display-unstable-keymap-settings?
  (r/atom (boolean (js/localStorage.getItem "display-unstable-keymap-settings"))))


;; Temporal function in window to enable this unstable feature
(set! (.-toggleUnstableKeymapSettings js/window)
      (fn []
        (let [newVal (not @display-unstable-keymap-settings?)]
          (reset! display-unstable-keymap-settings? newVal)
          (js/localStorage.setItem "display-unstable-keymap-settings" newVal))))


(def key-aliases-description
  (array-map
    :athena/toggle "Toggle athena"
    :nav/back "Navigate back"
    :nav/forward "Navigate forward"
    :nav/daily-notes "Go to daily Notes"
    :nav/pages "Go to pages"
    :nav/graph "Go to graph"
    :left-sidebar/toggle "Toggle left sidebar"
    :right-sidebar/toggle "Toggle right sidebar"
    :content/bold "Toggle bold"
    :content/italic "Toggle italics"
    :content/strikethrough "Toggle strikethrough"
    :content/highlight "Toggle highlight"
    :content/open-current-block-or-page "Open current block or page"))


(defn key-shortcut
  [key-alias]
  (let [editing? (r/atom false)
        handle-hotkey-change (fn [sequence]
                               (when @editing?
                                 (dispatch [:keymap/update key-alias (first sequence)])
                                 (reset! editing? false)))
        default-hotkey (key-alias default-keymap)
        handle-click (fn []
                       (reset! editing? true)
                       (mousetrap-record handle-hotkey-change))
        restore-default! (fn []
                           (dispatch [:keymap/restore key-alias default-keymap]))]

    (fn [key-alias]
      (let [keymap @(subscribe [:keymap])
            hotkey (key-alias keymap)
            different-from-default? (not= hotkey default-hotkey)]
        [:div {:style {:display "flex" :justify-content "space-between" :border-bottom "1px solid var(--border-color)" :padding "10px 0"}}
         [:span {:style {:font-weight 500}} (key-alias key-aliases-description)]
         [track-outside-click #(reset! editing? false)
          [:div
           [:div {:style {:display "flex" :justify-content "flex-end"}}
            [:button {:on-click handle-click
                      :style    {:display "block"}}
             hotkey]
            (when different-from-default?
              [:> SettingsBackupRestore {:style {:margin-left "8px" :cursor "pointer"} :on-click restore-default!}])]
           (when @editing? [:p {:style {:margin-top "4px"}} "Press new shortcut..."])]]]))))


(defn keymap
  []
  (let [keymap @(subscribe [:keymap])]
    [setting-wrapper
     [:<>
      [:header
       [:h3 "Keymap"]]
      [:main
       [:div (for [key-alias (keys key-aliases-description)]
               ^{:key key-alias} [key-shortcut key-alias])]]]]))


(defn page
  []
  (let [s (r/atom (init-state))]
    [settings-container
     [:<>
      [:h1 "Settings"]
      [email-comp s]
      [monitoring-comp s]
      [autosave-comp s]
      [backups-comp s]
      [remote-username-comp]
      (when @display-unstable-keymap-settings?
        [keymap])]]))
