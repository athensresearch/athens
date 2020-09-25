(ns athens.views.block-page
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.keybindings :refer [destruct-key-down]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color]]
    [athens.util :refer [now-ts gen-block-uid]]
    [athens.views.blocks :refer [block-el]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      KeyCodes)))


;;; Styles


(def page-style
  {:margin     "2rem auto"
   :padding    "1rem 2rem"
   :flex-basis "100%"
   :max-width  "55rem"})


(def title-style
  {:position        "relative"
   :overflow        "visible"
   :flex-grow       "1"
   :margin          "0.2em 0"
   :letter-spacing  "-0.03em"
   :word-break      "break-word"
   ::stylefy/manual [[:textarea {:display "none"}]
                     [:&:hover [:textarea {:display "block"
                                           :z-index 1}]]
                     [:textarea {:-webkit-appearance "none"
                                 :cursor             "text"
                                 :resize             "none"
                                 :transform          "translate3d(0,0,0)"
                                 :color              "inherit"
                                 :font-weight        "inherit"
                                 :padding            "0"
                                 :letter-spacing     "inherit"
                                 :position           "absolute"
                                 :top                "0"
                                 :left               "0"
                                 :right              "0"
                                 :width              "100%"
                                 :min-height         "100%"
                                 :caret-color        (color :link-color)
                                 :background         "transparent"
                                 :margin             "0"
                                 :font-size          "inherit"
                                 :line-height        "inherit"
                                 :border-radius      "0.25rem"
                                 :transition         "opacity 0.15s ease"
                                 :border             "0"
                                 :opacity            "0"
                                 :font-family        "inherit"}]
                     [:textarea:focus
                      :.is-editing {:outline "none"
                                    :z-index 3
                                    :display "block"
                                    :opacity "1"}]
                     [(selectors/+ :.is-editing :span) {:opacity 0}]]})


;;; Components

(defn handle-enter
  [e uid _]
  (let [new-uid (gen-block-uid)
        now (now-ts)]
    (.. e preventDefault)
    (dispatch [:transact [{:block/uid      uid
                           :edit/time      now
                           :block/children [{:block/order  0
                                             :block/uid    new-uid
                                             :block/open   true
                                             :block/string ""}]}]])
    (dispatch [:editing/uid new-uid])))


(defn block-page-key-down
  [e uid state]
  (let [d-event (destruct-key-down e)
        {:keys [key-code]} d-event]
    (cond
      (= key-code KeyCodes.ENTER) (handle-enter e uid state))))


(defn block-page-change
  [e _uid state]
  (let [value (.. e -target -value)]
    (swap! state assoc :string/local value)))


(defn block-page-el
  [_ _ _]
  (let [state (r/atom {:string/local    nil
                       :string/previous nil})]
    (fn [block parents editing-uid]
      (let [{:block/keys [string children uid]} block]

        (when (not= string (:string/previous @state))
          (swap! state assoc :string/previous string :string/local string))

        [:div.block-page (use-style page-style)
         ;; Parent Context
         [:span {:style {:color "gray"}}

          (->> (for [{:keys [node/title block/uid block/string]} parents]
                 [:span {:key uid :style {:cursor "pointer"} :on-click #(navigate-uid uid)} (or string title)])
               (interpose ">")
               (map (fn [x]
                      (if (= x ">")
                        [(r/adapt-react-class mui-icons/KeyboardArrowRight) (use-style {:vertical-align "middle"})]
                        x))))]

         ;; Header
         [:h1 (use-style title-style {:data-uid uid :class "block-header"})
          [autosize/textarea
           {:value       (:string/local @state)
            :class       (when (= editing-uid uid) "is-editing")
            :auto-focus  true
            :on-key-down (fn [e] (block-page-key-down e uid state))
            :on-change   (fn [e] (block-page-change e uid state))
            :on-blur     (fn [e] (athens.views.blocks/textarea-blur e uid state))}]
          [:span (:string/local @state)]]


         ;; Children
         [:div (for [child children]
                 (let [{:keys [db/id]} child]
                   ^{:key id} [block-el child]))]]))))


(defn block-page-component
  [ident]
  (let [block (db/get-block-document ident)
        parents (db/get-parents-recursively ident)
        editing-uid @(subscribe [:editing/uid])]
    [block-page-el block parents editing-uid]))
