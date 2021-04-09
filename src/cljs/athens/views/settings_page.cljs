(ns athens.views.settings-page
  (:require
    ["@material-ui/icons/ToggleOff" :default ToggleOff]
    ["@material-ui/icons/ToggleOn" :default ToggleOn]
    ["@material-ui/icons/Check" :default Check]
    ["@material-ui/icons/NotInterested" :default NotInterested]
    [athens.electron :as electron]
    [athens.views.buttons :refer [button]]
    [athens.views.textinput :as textinput]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [goog.functions :as goog-functions]
    [reagent.core :as r]
    [stylefy.core :as stylefy])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


(defn opt-out
  [opted-out?]
  (.. js/posthog (capture "opt-out"))
  (.. js/window -posthog opt_out_capturing)
  (js/localStorage.setItem "sentry" "off")
  (reset! opted-out? true))


(defn opt-in
  [opted-out?]
  (.. js/window -posthog opt_in_capturing)
  (.. js/posthog (capture "opt-in"))
  (js/localStorage.setItem "sentry" "on")
  (reset! opted-out? false))


(defn handle-click
  [opted-out?]
  (if @opted-out?
    (opt-in opted-out?)
    (opt-out opted-out?)))


(defn handle-debounce-save-input
  [value debounce-time]
  (when (and (<= 0 value) (<= value 1000))
    (reset! debounce-time value)
    (set! electron/debounce-write-db (goog-functions/debounce electron/write-db (* 1000 value)))
    (js/localStorage.setItem "debounce-save-time" value)))


(defn handle-change-email
  [email value]
  (reset! email value))


(defn handle-click-email
  [email authed? sending-request]
  (let [api       "https://dhx9n94ty5.execute-api.us-east-1.amazonaws.com/Prod/hello"
        email-qs  "?email="
        query-url (str api email-qs email)]
    (reset! sending-request true)
    (go (let [resp (<! (http/get query-url))]
          (cond

            ;; Open Collective Lambda finds email associated with Athens
            (and (:success resp) (true? (:email_exists (:body resp))))
            (do
              (js/localStorage.setItem "auth/email" email)
              (js/localStorage.setItem "auth/authed?" (str true))
              (reset! authed? true))

            ;; Open Collective Lambda doesn't find email
            (and (:success resp) (false? (:email_exists (:body resp))))
            (do
              (js/localStorage.setItem "auth/email" nil)
              (js/localStorage.setItem "auth/authed?" (str false))
              (reset! authed? false)
              (js/alert "No OpenCollective account was found with this email address."))

            ;; Something else, e.g. networking error
            :else
            (js/alert (str "Unexpected error" resp)))
          (reset! sending-request false)))))


(defn init-email
  []
  (let [email (js/localStorage.getItem "auth/email")]
    (if (= email "null")
      ""
      email)))


#_(defn settings-page
    []
    (let [opted-out?          (r/atom (.. js/window -posthog has_opted_out_capturing))
          ;; pretty easy to get around this auth. Lol
          authed?             (r/atom (= (js/localStorage.getItem "auth/authed?") "true"))
          email               (r/atom (init-email))
          sending-request     (r/atom false)
          debounce-save-time! (r/atom (js/Number (js/localStorage.getItem "debounce-save-time")))]

      (fn []
        (let [submit-disabled     (or @sending-request @authed?)]

          [:div {:style {:display        "flex"
                         :margin         "0vh 5vw"
                         :width          "90vw"
                         :flex-direction "column"}}
           [:h2 "Settings"]

           (if @authed?
             [:span (str "Thank you for using and backing us, " @email " ❤️")]
             [:span "You are using the free version of Athens. You are hosting your own data. Please be careful!"])

           [:div {:style {:margin "10px 0"}}
            [:h5 "Email"]
            [:div {:style {:margin "5px 0" :display "flex" :justify-content "space-between"}}
             [:input {:style {:width "15em"} :type "email" :value @email :placeholder "Open Collective Email"
                      :on-change #(handle-change-email email (.. % -target -value))}]
             [button {:on-click #(handle-click-email @email authed? sending-request)
                      :disabled submit-disabled
                      :primary  true} "Submit"]]]

           ;; Analytics

           (if @opted-out?
             [:h5 "Opted Out of Product Usage and Error Monitoring"]
             [:h5 "Opted Into Product Usage and Error Monitoring"])
           [:div {:style {:margin "10px 0"}}
            [button {:primary  (false? @opted-out?)
                     :on-click #(handle-click opted-out?)}
             (if @opted-out?
               [:div {:style {:display "flex"}}
                [:> ToggleOn]
                [:span "\uD83D\uDE41 Opting out makes it harder to improve Athens, and for us to become sustainable."]]
               [:div {:style {:display "flex"}}
                [:> ToggleOff]
                [:span "\uD83D\uDD12 Athens will never sell your data. Athens has never and will never look at the contents of your database and what you are writing."]])]]
           [:div {:style {:margin-top "15px"}}
            [:h5 "Remote Backups"]
            [:div {:style {:margin "5px 0" :display "flex" :justify-content "space-between"}}
             [button {:disabled true}
              "Backup my DB to the cloud"]
             [:span "Coming soon to " [:a {:href "https://opencollective.com/athens" :target "_blank"}
                                       "paid Users and Sponsors"]]]]

           ;; Auto-save
           [:div {:style {:margin "20px 0"}}
            [:h5 "Auto-save"]
            [:div {:style {:display "flex" :justify-content "space-between"
                           :margin "10px 0"}}
             [:input {:style {:width "4em"}
                      :type  "number" :value @debounce-save-time! :on-change #(handle-debounce-save-input (js/Number (.. % -target -value)) debounce-save-time!)}]
             (case @debounce-save-time!
               0 [:span (str "Athens will save and create a local backup after each edit.")]
               1 [:span (str "Athens will save and create a local backup " @debounce-save-time! " second after your last edit.")]
               [:span (str "Athens will save and create a local backup " @debounce-save-time! " seconds after your last edit.")])]]]))))


(def settings-wrap-style
  {:border-top "1px solid var(--border-color)"
   :padding-top "2rem"
   :padding-bottom "2rem"
   :padding "2rem 0.75rem"
   :line-height "1.25"

   ::stylefy/manual [[:&.disabled {:opacity 0.5}]
                     [:header {:padding-bottom "1rem"}]
                     [:span.glance {:font-weight "normal"
                                    :opacity 0.7
                                    :font-size "0.8em"
                                    :gap "0.25em"}
                      [:svg {:vertical-align "-0.25em"
                             :font-size "1.5em"}]]
                     [:aside {:font-size "0.8em"
                              :padding-top "0.5rem"}]
                     [:p {:margin "0.25rem 0"}]
                     [:p:first-child {:margin-top 0}]
                     [:p:last-child {:margin-bottom 0}]
                     [:label {:dispatch "flex"
                              :gap "0.5rem"
                              :align-items "center"
                              :font-weight "bold"
                              :cursor "pointer"}]]


   ::stylefy/media {{:min-width "40em"}
                    {:display "grid"
                     :grid-template-columns "10rem 1fr"
                     :grid-gap "1rem"}}})


(defn settings-wrap
  ([children]
   [settings-wrap {} children])
  ([config children]
   (let [{:keys [disabled] :as props} config]
     [:div (stylefy/use-style settings-wrap-style
                              {:class [(when disabled "disabled")]}) children])))


(defn email-comp
  [s]
  [settings-wrap
   [:<>
    [:header
     [:h3 "Email"]
     [:span.glance (if (clojure.string/blank? (:email @s))
                     " Not set "
                     (:email @s))]]
    [:main
     [:div
      [textinput/textinput {:type         " email "
                            :placeholder  " Open Collective Email "
                            :on-change    #(reset! s (.. % -target -value))
                            :defaultValue (:email @s)}]
      [button {:primary true
               :on-click #()}
       "Submit"]
      [button {:on-click #()}
       "Reset"]]
     [:aside
      [:p (if (clojure.string/blank? (:email @s))
            "Your data is backed up. Thank you for helping support Athens!"
            "You are using the free version of Athens. You are hosting your own data. Please be careful!")]]]]])


(defn monitoring-comp
  [s]
  [settings-wrap
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
     [:label
      [:input {:type         "checkbox"
               :defaultValue (:monitoring @s)
               :on-change    #(swap! s update :monitoring not)}]
      "Send usage data and diagnostics to Athens Research"]
     [:aside
      [:p "Athens Research has never and will never look at the contents of your database. Athens Research will never ever sell your data."]]]]])


(defn autosave-comp
  [s]
  [settings-wrap
   [:<>
    [:header
     [:h3 "AutoSave"]
     [:span.glance (str (:auto-save-frequency @s) " seconds")]]
    [:main
     [:label
      [textinput/textinput {:type         "number"
                            :defaultValue (:auto-save-frequency @s)
                            :min 0
                            :max 100
                            :on-blur    #(swap! s assoc :auto-save-frequency (.. % -target -value))}]
      " seconds"]
     [:aside
      [:p (str "Athens will save and create a local backup " (:auto-save-frequency @s) " seconds after your last edit.")]]]]])


(defn backups-comp
  [s]
  [settings-wrap
   {:disabled true}
   [:<>
    [:header
     [:h3 "Remote Backups"]
     [:span.glance "Coming soon to "
      [:a {:href "https://opencollective.com/athens"
           :target "_blank"
           :rel "noreferrer"}
         " paid users and sponsors"]]]
    [:main
     [button {:disabled true} "Backup my DB to the cloud"]
     [:aside
      [:p (str "Athens will save and create a local backup " (:auto-save-frequency @s) " seconds after your last edit.")]]]]])


(def init-state
  {:email "jeff@athens.com"
   :monitoring true
   :auto-save-frequency 15})


(def settings-page-styles
  {:width "50em"
   :max-width "100%"
   :margin "2rem auto"})



(defn settings-page-wrapper
  [comp]
  [:div (stylefy/use-style settings-page-styles)
   comp])

(defn settings-page
  []
  (let [s (r/atom init-state)]
    [settings-page-wrapper
     [:<>
      [:h1 "Settings"]
      [email-comp s]
      [monitoring-comp s]
      [autosave-comp s]
      [backups-comp s]]]))
