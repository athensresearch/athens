(ns athens.views.toolbar-presence
  (:require
    ["@material-ui/core/Popover" :as Popover]
    ["@material-ui/icons/Link" :default Link]
    [athens.style :refer [color]]
    [athens.views.buttons :refer [button]]
    [clojure.string :as str]
    [re-frame.core :refer [subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


(def m-popover (r/adapt-react-class (.-default Popover)))


;; Data

(def PALETTE
  ["#21A469",
   "#DDA74C",
   "#009FB8",
   "#0062BE"
   "yellow"
   "red"])


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

(defn avatars
  [members]
  (mapv (fn [{}])
        members))


(defn avatar-svg
  [props & children]
  [:svg (merge (use-style {:height          "1.5em"
                           :width           "1.5em"
                           :overflow        "visible"
                           ::stylefy/manual [[:circle {:stroke-width "0.2px"}]
                                             [:text {:font-weight "bold"}]]})
               props)
   children])


(defn avatar-el
  "Takes a member map for the user data.
  Optionally takes some props for things like fill."
  ([member]
   [avatar-el member {:filled true}])
  ([{:keys [username color]} {:keys [filled]}]
   (let [initials (first username)]
     [avatar-svg {:viewBox "0 0 4 4"}
      [:circle {:cx          2
                :cy          2
                :r           2
                :fill        (when filled color)
                :stroke      (when filled color)
                :fillOpacity (when-not filled 0.1)

                :strokeWidth "1px"}]
      [:text {:width      4
              :x          2
              :y          "72%"
              :font-size  "18%"
              :fill       (if filled "#fff" color)
              :textAnchor "middle"}
       initials]])))


(defn avatar-stack-el
  [& children]
  [:div (use-style {:display "grid"
                    :grid-auto-flow "column"
                    :grid-template-columns "repeat(auto-fit, 1em)"
                    :svg {:mask-image "radial-gradient(
                         1.5em 1.15em at 150% 50%
                         transparent calc(96%)
                         #000 100%)"}})
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


(defn MemberListItem
  [& children]
  [:li (use-style {:padding "8px 16px"
                   :cursor "pointer"
                   :transition "backdrop-filter 0.1s ease"})
   children])


(defn member-item-el
  [member filled?]
  [:<>
   [avatar-el member filled?]
   (:username member)])

;;&hover {
;;        :backdrop-filter "brightness(95%)"}
;;
;;
;;&active {
;;         :backdrop-filter "brightness(92%)"}
;;
;;(defn ListItem = styled.li``


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
                   [:> Link]]
                  [list-el

                   ;; On same page
                   [list-section-header-el "On This Page"]
                   (for [member same-page-members]
                     [MemberListItem
                      [member-item-el member]])

                   ;; Online, different page
                   [list-separator-el]
                   (for [member online-members]
                     [MemberListItem
                      [member-item-el member]])]]])))

