(ns athens.views.toolbar-presence
  (:require
    ["@material-ui/core/Popover" :as Popover]
    ["@material-ui/icons/Link" :default Link]
    [athens.style :as style]
    [athens.views.buttons :refer [button buttons-style]]
    [clojure.string :as str]
    [re-frame.core :refer [subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


(def m-popover (r/adapt-react-class (.-default Popover)))


;; Data

(def PALETTE
  ["#DDA74C"
   "#C45042"
   "#611A58"
   "#21A469"
   "#009FB8"
   "#0062BE"])


(def NAMES
  ["Zeus"
   "Poseidon"
   "Hera"
   "Demeter"
   "Athena"
   "Apollo"])
   ;;"Artemis"
   ;;"Ares"
   ;;"Aphrodite"
   ;;"Hephaestus"
   ;;"Hermes"
   ;;"Hestia"
   ;;"Dionysus"
   ;;"Hades"])


(def BLOCK-UIDS
  ["" ;; on page, not block
   "51c3580f5" ;; poseidon
   "ed9f20b26" ;; way down
   "8b66a56f3" ;; different page
   "4135c0ecb" ;; different page on a block
   ""])


(def MEMBERS
  (mapv
    (fn [username color uid]
      {:username username :color color :block/uid uid})
    NAMES PALETTE BLOCK-UIDS))


;; Avatar

(defn avatar-svg
  [props & children]
  [:svg (merge (use-style {:height          "1.5em"
                           :width           "1.5em"
                           :overflow        "visible"
                           ::stylefy/manual [[:text {:font-weight "bold"}]]})
               props)
   children])


(defn avatar-el
  "Takes a member map for the user data.
  Optionally takes some props for things like fill."
  ([member]
   [avatar-el member {:filled true}])
  ([{:keys [username color]} {:keys [filled]}]
   (let [initials (first username)]
     [avatar-svg {:viewBox "0 0 24 24"
                  :vectorEffect "non-scaling-stroke"}
      [:circle {:cx          12
                :cy          12
                :r           12
                :fill        color
                :stroke      color
                :fillOpacity (when-not filled 0.1)
                :strokeWidth (if filled 0 "1px")
                :key "circle"}]
      [:text {:width      24
              :x          12
              :y          "72%"
              :font-size  16
              :fill       (if filled "#fff" color)
              :textAnchor "middle"
              :key "text"}
       initials]])))



(def avatar-stack-style
  {:display "grid"
   :grid-auto-flow "column"
   :grid-template-columns "repeat(auto-fit, 1em)"
   ::stylefy/manual [[:svg ["&:last-child" {:margin-right "-1.25rem"}]]]})


(defn avatar-stack-el
  [& children]
  [:div (use-style avatar-stack-style)
   children])


;; List

(defn list-el
  [& children]
  [:ul (use-style {:padding        0
                   :margin         0
                   :display        "flex"
                   :flex-direction "column"
                   :list-style     "none"})
   children])


(defn list-header-el
  [& children]
  [:header (use-style {:border-bottom "1px solid #ddd"
                       :padding "4px 8px"
                       :display "flex"
                       :justify-content "space-between"
                       :align-items "center"})
   children])



(defn list-section-header-el
  [& children]
  [:li (use-style {:font-size "12px"
                   :font-weight "bold"
                   :opacity "0.5"
                   :padding "16px 16px 4px"})
   children])


(defn list-header-url-el
  [& children]
  [:span (use-style {:font-size     "12px"
                     :font-weight   "700"
                     :display       "inline-block"
                     :opacity       "0.5"
                     :padding       "8px"
                     :margin-right  "1em"
                     :flex          "1 1 100%"
                     :white-space   "nowrap"
                     :text-overflow "hidden"})
   children])


(defn list-separator-el
  []
  [:li (use-style {:margin "5px 0 6px 16px"
                   :border-bottom "1px solid #ddd"})])


(def member-list-item-style
  {:padding "6px 16px"
   :display "flex"
   :font-size "14px"
   :align-items "center"
   :font-weight "600"
   :color (style/color :body-text-color :opacity-higher)
   :transition "backdrop-filter 0.1s ease"
   ;;:cursor "pointer"
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



;; event
:presence/ping

;; re-frame db
{:presence/users {"user-id-1" {:username  "Zeus"
                               :block/uid "asd123"
                               :page/uid  "page-1"}}}


(defn member-item-el
  [member filled?]
  [:li (use-style member-list-item-style #_{:on-click #(prn member)})
   [avatar-el member filled?]
   (:username member)])


(defn toolbar-presence
  []
  (r/with-let [ele (r/atom nil)]
              (let [same-page-members (take 3 MEMBERS)
                    online-members    (drop 3 MEMBERS)]
                [:<>

                 ;; Preview
                 [button {:on-click #(reset! ele (.-currentTarget %))}
                  [:<>
                   [avatar-stack-el
                    (for [member same-page-members]
                      [avatar-el member])]]]

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
                   [list-section-header-el "On This Page"]
                   (for [member same-page-members]
                     [member-item-el member {:filled true}])

                   ;; Online, different page
                   [list-separator-el]
                   (for [member online-members]
                     [member-item-el member {:filled false}])]]])))

