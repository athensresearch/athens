(ns athens.self-hosted.presence.views
  (:require
    ["/components/PresenceDetails/PresenceDetails" :refer [PresenceDetails]]
    [athens.self-hosted.presence.events]
    [athens.self-hosted.presence.fx]
    [athens.self-hosted.presence.subs]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;; Avatar


(defn- avatar-svg
  [props & children]
  [:svg (merge (use-style {:height          "1.5em"
                           :width           "1.5em"
                           :overflow        "hidden"
                           :border-radius   "1000em"}
                          {:class "user-avatar"})
               props)
   children])


(defn- avatar-el
  "Takes a member map for the user data.
  Optionally takes some props for things like fill."
  ([member]
   [avatar-el member {:filled true}])
  ([{:keys [username color]} {:keys [filled]}]
   (let [initials (if (string? username)
                    (subs username 0 2)
                    "")]
     [avatar-svg {:viewBox "0 0 24 24"
                  :vectorEffect "non-scaling-stroke"}
      [:circle {:cx          12
                :cy          12
                :r           12
                :fill        color
                :stroke      color
                :fillOpacity (when-not filled 0.1)
                :strokeWidth (if filled 0 "3px")
                :key "circle"}]
      [:text {:width      24
              :x          12
              :y          16.5
              :font-size  14
              :font-weight 600
              :fill       (if filled "#fff" color)
              :textAnchor "middle"
              :key "text"}
       initials]])))


(defn user->person
  [{:keys [username color]}]
  ;; TODO: have a real notion of user-id, not just username.
  {:personId username
   :username  username
   :color     color})


(defn copy-host-address-to-clipboard
  [host-address]
  (.. js/navigator -clipboard (writeText host-address))
  (rf/dispatch [:show-snack-msg {:msg "Host address copied to clipboard"
                                 :type :success}]))


(defn go-to-user-block
  [all-users js-person]
  (let [{_block-uid :block/uid
         page-uid  :page/uid}
        (->> (js->clj js-person :keywordize-keys true)
             :username
             (get all-users))]
    (rf/dispatch (if page-uid
                   ;; TODO: if we support navigating to a block, it should be added here.
                   [:navigate :page {:id page-uid}]
                   [:show-snack-msg {:msg "User is not on any page"
                                     :type :success}]))))


(defn edit-current-user
  [current-username js-person]
  (let [{:keys [username color]} (js->clj js-person :keywordize-keys true)]
    (rf/dispatch [:settings/update :username username])
    ;; Change the color of the old name immediately, then wait for the
    ;; rename to happen in the server.
    (rf/dispatch [:presence/update-color current-username color])
    (rf/dispatch [:presence/send-rename current-username username])))


;; Exports

(defn toolbar-presence-el
  []
  (r/with-let [selected-db            (rf/subscribe [:db-picker/selected-db])
               current-user           (rf/subscribe [:presence/current-user])
               all-users              (rf/subscribe [:presence/users-with-page-data])
               same-page              (rf/subscribe [:presence/same-page])
               diff-page              (rf/subscribe [:presence/diff-page])
               others-seq             #(->> (dissoc % (:username @current-user))
                                            vals
                                            (map user->person))
               current-page-members   (others-seq @same-page)
               different-page-members (others-seq @diff-page)]
              [:> PresenceDetails {:current-user              (user->person @current-user)
                                   :current-page-members      current-page-members
                                   :different-page-members    different-page-members
                                   :host-address              (:url @selected-db)
                                   :handle-press-host-address copy-host-address-to-clipboard
                                   :handle-press-member       #(go-to-user-block @all-users %)
                                   :handle-update-profile     #(edit-current-user (:username @current-user) %)
                                   ;; TODO: show other states when we support them.
                                   :connection-status         "connected"}]))


;; inline

(defn inline-presence-el
  [uid]
  (let [inline-present? (rf/subscribe [:presence/has-presence uid])]
    (when @inline-present?
      [avatar-el @inline-present?])))
