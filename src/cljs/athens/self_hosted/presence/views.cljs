(ns athens.self-hosted.presence.views
  (:require
   ["@material-ui/core/Popover" :as Popover]
   ["@material-ui/icons/Link" :default Link]
   [athens.style :as style]
   [athens.db :as db]
   [athens.views.buttons :refer [button]]
   [athens.self-hosted.presence.events]
   [athens.self-hosted.presence.subs]
   [athens.self-hosted.presence.fx]
   [athens.self-hosted.presence.utils :as utils]
   [clojure.string :as str]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))


(def m-popover (r/adapt-react-class (.-default Popover)))


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



(def ^:private avatar-stack-style
  {:display "flex"
   ::stylefy/manual [[:svg {:width "1.5rem"
                            :height "1.5rem"}
                      ; In a stack, each sequential item sucks in the spacing
                      ; from the item before it
                      ["&:not(:first-child)" {:margin-left "-0.8rem"}]
                      ; All but the last get a slice masked out for readability
                      ;
                      ; I'm not clear on why 1.55rem / 1.1rem work in this case
                      ; It'd be nice to have a simpler masking method
                      ; or a better-constructed string with some documentation
                      ["&:not(:last-child)" {:mask-image "radial-gradient(1.55rem 1.1rem at 160% 50%, transparent calc(96%), #000 100%)"
                                             :-webkit-mask-image "radial-gradient(1.55rem 1.1rem at 160% 50%, transparent calc(96%), #000 100%)"}]]]})


(defn- avatar-stack-el
  [& children]
  [:div (use-style avatar-stack-style)
   children])


;; List

(defn- list-el
  [& children]
  [:ul (use-style {:padding        0
                   :margin         0
                   :display        "flex"
                   :flex-direction "column"
                   :list-style     "none"})
   children])


(defn- list-header-el
  [& children]
  [:header (use-style {:border-bottom "1px solid #ddd"
                       :padding "0.25rem 0.5rem"
                       :display "flex"
                       :justify-content "space-between"
                       :align-items "center"})
   children])



(defn- list-section-header-el
  [& children]
  [:li (use-style {:font-size "12px"
                   :font-weight "bold"
                   :opacity "0.5"
                   :padding "1rem 1rem 0.25rem"})
   children])


(defn- list-header-url-el
  [& children]
  [:span (use-style {:font-size     "12px"
                     :font-weight   "700"
                     :display       "inline-block"
                     :opacity       "0.5"
                     :padding       "0.5rem"
                     :user-select   "all"
                     :margin-right  "1em"
                     :flex          "1 1 100%"
                     :white-space   "nowrap"
                     :text-overflow "hidden"})
   children])


(defn- list-separator-el
  []
  [:li (use-style {:margin "0.5rem 0 0.5rem 1rem"
                   :border-bottom "1px solid #ddd"})])


(def ^:private member-list-item-style
  {:padding "0.375rem 1rem"
   :display "flex"
   :font-size "14px"
   :align-items "center"
   :font-weight "600"
   :color (style/color :body-text-color :opacity-higher)
   :transition "backdrop-filter 0.1s ease"
   :cursor "default"
   ::stylefy/manual [[:svg {:margin-right "0.25rem"}]]})
   ;; turn off interactive button stylings until we implement interactions like "jump" or "follow"
                     ;;[:&:hover {:background (style/color :body-text-color :opacity-lower)}]
                     ;;[:&:active
                     ;; :&:hover:active
                     ;; :&.is-active {:color (style/color :body-text-color)
                     ;;               :background (style/color :body-text-color :opacity-lower)}]
                     ;;[:&:active
                     ;; :&:hover:active
                     ;; :&:active.is-active {:background (style/color :body-text-color :opacity-low)}]
                     ;;[:&:disabled :&:disabled:active {:color (style/color :body-text-color :opacity-low)
                     ;;                                 :background (style/color :body-text-color :opacity-lower)
                     ;;                                 :cursor "default"}]]})



(defn- member-item-el
  [user props]
  [:li (use-style member-list-item-style #_{:on-click #(prn user)})
   [avatar-el user props]
   (:username user)])


;; Exports
(defn toolbar-presence-el
  []
  (r/with-let [ele (r/atom nil)]
    (let [users (rf/subscribe [:presence/users-with-page-data])
          same-page-users (rf/subscribe [:presence/same-page])
          diff-page-users (rf/subscribe [:presence/diff-page])
          current-route-name (rf/subscribe [:current-route/name])]
      [:<>

       ;; Preview
       [button {:on-click #(reset! ele (.-currentTarget %))}
        [avatar-stack-el
         (cond

           (= @current-route-name :page)
           [:<>
            ;; same page
            (for [[username user] @same-page-users]
              ^{:key username}
              [avatar-el user {:filled true}])
            ;; diff page but online
            (for [[username user] @diff-page-users]
              ^{:key username}
              [avatar-el user {:filled false}])]

           ;;; TODO: capture what page user is scrolled to on Daily Notes
           ;(= @current-route-name :home)
           ;[:div "TODO"]

           ;; default to showing all users
           :else (for [[username user] @users]
                   ^{:key username}
                   [avatar-el user {:filled false}]))]]

       ;; Dropdown
       [m-popover
        {:open            (boolean (and @ele))
         :anchorEl        @ele
         :onClose         #(reset! ele nil)
         :anchorOrigin    #js{:vertical   "bottom"
                              :horizontal "center"}
         :transformOrigin #js{:vertical   "top"
                              :horizontal "center"}}
        [list-header-el
         [list-header-url-el "ath.ns/34op5fds0a"]
         [button [:> Link]]]

        [list-el
         ;; On same page

         (when-not (empty? @same-page-users)
           [:<>
            [list-section-header-el "On This Page"]
            (for [[username user] @same-page-users]
              ^{:key username}
              [member-item-el user {:filled true}])
            [list-separator-el]])

         ;; Online, different page
         (for [[username user] @diff-page-users]
           ^{:key username}
           [member-item-el user {:filled false}])]]])))


;; inline

(defn inline-presence-el
  [uid]
  (let [inline-present? (rf/subscribe [:presence/has-presence uid])]
    (when @inline-present?
      [avatar-el @inline-present?])))
