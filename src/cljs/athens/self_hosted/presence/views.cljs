(ns athens.self-hosted.presence.views
  (:require
    ["/components/Avatar/Avatar" :refer [Avatar]]
    ["/components/PresenceDetails/PresenceDetails" :refer [PresenceDetails]]
    [athens.self-hosted.presence.events]
    [athens.self-hosted.presence.fx]
    [athens.self-hosted.presence.subs]
    [re-frame.core :as rf]
    [reagent.core :as r]))


;; Avatar



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
    (rf/dispatch [:settings/update :color color])
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
               settings               (rf/subscribe [:settings])
               others-seq             #(->> (dissoc % (:username @current-user))
                                            vals
                                            (map user->person))]
              (fn []
                (let [current-user'          (user->person (or @current-user
                                                               ;; TODO: this is only needed because we don't
                                                               ;; have real ids, so it's possible while changing
                                                               ;; name that the current-user does not match the
                                                               ;; user in settings for a while since there's
                                                               ;; no way to track it.
                                                               (select-keys @settings [:username :color])))
                      current-page-members   (others-seq @same-page)
                      different-page-members (others-seq @diff-page)]
                  [:> PresenceDetails {:current-user              current-user'
                                       :current-page-members      current-page-members
                                       :different-page-members    different-page-members
                                       :host-address              (:url @selected-db)
                                       :handle-press-host-address copy-host-address-to-clipboard
                                       :handle-press-member       #(go-to-user-block @all-users %)
                                       :handle-update-profile     #(edit-current-user (:username @current-user) %)
                                       ;; TODO: show other states when we support them.
                                       :connection-status         "connected"}]))))


;; inline

(defn inline-presence-el
  [uid]
  (let [users (rf/subscribe [:presence/has-presence uid])]
    (when (seq @users)
      (into
       [:> (.-Stack Avatar)
        {:size "1.25rem"
         :maskSize "1px"
         :stackOrder "from-left"
         :limit 3
         :style {:transform "translateX(calc(-100% + 1rem)) translateY(0.35rem)"
                 :padding "0.125rem"
                 :background "var(--background-color)"}}]
       (map (fn [x] [:> Avatar (merge {:showTooltip false :key (:username x)} x)]) @users)))))

