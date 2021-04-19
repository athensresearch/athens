(ns athens.views.filesystem
  (:require
    ["@material-ui/core/Tab" :default Tab]
    ["@material-ui/core/Tabs" :default Tabs]
    ["@material-ui/icons/Close" :default Close]
    ["@material-ui/icons/LibraryBooks" :default LibraryBooks]
    ["@material-ui/icons/MergeType" :default MergeType]
    [athens.electron :as electron]
    [athens.events :as events]
    [athens.subs]
    [athens.util :refer [js-event->val]]
    [athens.views.buttons :refer [button]]
    [athens.views.modal :refer [modal-style]]
    [athens.views.textinput :as textinput]
    [athens.ws-client :as ws-client]
    [cljs.reader :refer [read-string]]
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
   :width           "500px"
   ::stylefy/manual [[:p {:max-width  "24rem"
                          :text-align "center"}]
                     [:button.toggle-button {:font-size     "18px"
                                             :align-self    "flex-start"
                                             :padding-left  "0"
                                             :margin-bottom "1rem"}]
                     [:code {:word-break "break-all"}]
                     [:.MuiTabs-indicator {:background-color "var(--link-color)"}]]})


(rf/reg-event-db
  :remote-graph/set-conf
  (fn [db [_ key val]]
    (let [n-rgc (-> db :db/remote-graph-conf (assoc key val))]
      (js/localStorage.setItem "db/remote-graph-conf" n-rgc)
      (assoc db :db/remote-graph-conf n-rgc))))


(rf/reg-event-db
  :remote-graph-conf/load
  (fn [db _]
    (let [remote-conf (some->> "db/remote-graph-conf"
                               js/localStorage.getItem read-string)]
      (assoc db :db/remote-graph-conf remote-conf))))


(dispatch [:remote-graph-conf/load])


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


(defn open-local-comp
  [loading db-filepath]
  [:<>
   [:h5 {:style {:align-self "flex-start"
                 :margin-top "2em"}}
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
    [button {:primary  true
             :on-click #(electron/create-dialog! (:input @state))}
     "Browse"]]])


(defn join-remote-comp
  [remote-graph-conf]
  [:<>
   (->> [{:label       "Remote address"
          :key         :address
          :placeholder "Remote server address"}
         {:label       "Token"
          :input-type  "password"
          :key         :token
          :placeholder "Secret token"}]
        (map (fn [{:keys [label key placeholder input-type]}]
               ^{:key key}
               [:div {:style {:width  "100%" :margin-top "10px"}}
                [:h5 label]
                [:div {:style {:margin          "5px 0"
                               :display         "flex"
                               :justify-content "space-between"}}
                 [textinput/textinput {:style       {:flex-grow 1
                                                     :padding   "5px"}
                                       :type        (or input-type "text")
                                       :value       (key @remote-graph-conf)
                                       :placeholder placeholder
                                       :on-change   #(rf/dispatch [:remote-graph/set-conf key (js-event->val %)])}]]]))
        doall)
   [button {:primary  true
            :style    {:margin-top "0.5rem"}
            :on-click #(ws-client/start-socket! (assoc @remote-graph-conf
                                                       :reload-on-init? true))}
    "Join"]])


(defn window
  "If loading is true, then that means the user has opened the modal and the db was not found on the filesystem.
  If loading is false, do not allow user to exit modal, and show slightly different UI."
  []
  (let [loading           (subscribe [:loading?])
        close-modal       (fn []
                            (when-not @loading
                              (dispatch [:modal/toggle])))
        remote-graph-conf (subscribe [:db/remote-graph-conf])
        db-filepath       (subscribe [:db/filepath])
        state             (r/atom {:input     ""
                                   :remote?   (:default? @remote-graph-conf)
                                   :tab-value 0})]
    (fn []
      [:div (use-style modal-style)
       [modal/modal
        {:title    [:div.modal__title
                    [:> LibraryBooks]
                    [:h4 "Database"]
                    (when-not @loading
                      [button {:on-click close-modal} [:> Close]])]
         :content  [:div (use-style modal-contents-style)
                    [:> Tabs {:on-change (fn [_x y] (swap! state assoc :tab-value y))
                              :value     (:tab-value @state)}
                     [:> Tab {:label "Open Local"}]
                     [:> Tab {:label "Create Local"}]
                     [:> Tab {:label "Join Remote"}]]
                    (cond
                      (= 2 (:tab-value @state))
                      [join-remote-comp remote-graph-conf]

                      (= 1 (:tab-value @state))
                      [create-new-local state]

                      (= 0 (:tab-value @state))
                      [open-local-comp loading db-filepath])]
         :on-close close-modal}]])))
