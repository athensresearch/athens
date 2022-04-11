(ns athens.self-hosted.presence.views
  (:require
    ["/components/Avatar/Avatar" :refer [Avatar]]
    ["/components/PresenceDetails/PresenceDetails" :refer [PresenceDetails]]
    [athens.electron.utils :as electron.utils]
    [athens.router :as router]
    [athens.self-hosted.presence.events]
    [athens.self-hosted.presence.fx]
    [athens.self-hosted.presence.subs]
    [athens.util :as util]
    [re-frame.core :as rf]
    [reagent.core :as r]))


;; Avatar

(defn user->person
  [{:keys [session-id username color]
    :page/keys [title]}]
  (when (and session-id username color)
    {:personId  session-id
     :username  username
     :color     color
     :pageTitle title}))


(defn copy-host-address-to-clipboard
  [host-address]
  (.. js/navigator -clipboard (writeText host-address))
  (rf/dispatch [:show-snack-msg {:msg "Host address copied to clipboard"
                                 :type :success}]))


(defn copy-permalink
  []
  (let [selected-db @(rf/subscribe [:db-picker/selected-db])
        url (router/create-url-with-graph-param (:id selected-db))]
    (.. js/navigator -clipboard (writeText url))
    (rf/dispatch [:show-snack-msg {:msg "Permalink copied to clipboard"
                                   :type :success}])))


(defn go-to-user-block
  [all-users js-person]
  (let [{_block-uid :block/uid
         page-uid  :page/uid}
        (->> (js->clj js-person :keywordize-keys true)
             :personId
             (get all-users))]
    (rf/dispatch (if page-uid
                   ;; TODO: if we support navigating to a block, it should be added here.
                   [:navigate :page {:id page-uid}]
                   [:show-snack-msg {:msg "User is not on any page"
                                     :type :success}]))))


(defn edit-current-user
  [js-person]
  (let [{:keys [username color]} (js->clj js-person :keywordize-keys true)]
    (rf/dispatch [:settings/update :username username])
    (rf/dispatch [:settings/update :color color])
    (rf/dispatch [:presence/send-update {:username username
                                         :color color}])))


;; Exports

(defn toolbar-presence-el
  []
  (r/with-let [selected-db            (rf/subscribe [:db-picker/selected-db])
               current-user           (rf/subscribe [:presence/current-user])
               all-users              (rf/subscribe [:presence/users-with-page-data])
               same-page              (rf/subscribe [:presence/same-page])
               diff-page              (rf/subscribe [:presence/diff-page])
               others-seq             #(->> (dissoc % (:session-id @current-user))
                                            vals
                                            (map user->person)
                                            (remove nil?))]
              (fn []
                (let [current-user'          (user->person @current-user)
                      current-page-members   (others-seq @same-page)
                      different-page-members (others-seq @diff-page)]
                  [:> PresenceDetails (merge
                                        {:current-user             current-user'
                                         :current-page-members     current-page-members
                                         :different-page-members   different-page-members
                                         :host-address             (:url @selected-db)
                                         :handle-copy-host-address copy-host-address-to-clipboard
                                         :handle-press-member      #(go-to-user-block @all-users %)
                                         :handle-update-profile    #(edit-current-user %)
                                         ;; TODO: show other states when we support them.
                                         :connection-status        "connected"}
                                        (when-not electron.utils/electron?
                                          {:handle-copy-permalink copy-permalink}))]))))


;; inline

(defn inline-presence-el
  [uid]
  (let [users (rf/subscribe [:presence/has-presence (util/embed-uid->original-uid uid)])]
    (when (seq @users)
      (into
        [:> (.-Stack Avatar)
         {:size "1.25rem"
          :maskSize "1.5px"
          :stackOrder "from-left"
          :limit 3
          :style {:zIndex 100
                  :position "absolute"
                  :right "-1.5rem"
                  :top "0.25rem"
                  :padding "0.125rem"
                  :background "var(--background-color)"}}]
        (->> @users
             (map user->person)
             (remove nil?)
             (map (fn [{:keys [personId] :as person}]
                    [:> Avatar (merge {:showTooltip false :key personId} person)])))))))

