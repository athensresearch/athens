(ns athens.devcards.buttons
  (:require
   ["@material-ui/icons" :as mui-icons]
   [athens.db]
   [athens.style :refer [color]]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros [defcard-rg]]
   [garden.color :refer [darken]]
   [garden.selectors :as selectors]
   [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles

(def button-icons-style {:margin-block-start "-0.0835em"
                         :margin-block-end "-0.0835em"})

(def button-icons-not-last-child-style {:margin-inline-end "0.251em"})

(def button-icons-not-first-child-style {:margin-inline-style "0.251em"})

(def button-icons-only-child-style {:margin-inline-start "-4px"
                                    :margin-inline-end "-4px"})


(def buttons-style
  {:cursor "pointer"
   :padding          "0.375rem 0.625rem"
   :margin           "0"
   :font-family      "inherit"
   :font-size        "inherit"
   :border-radius    "4px"
   :font-weight      "500"
   :border           "none"
   :display          "inline-flex"
   :align-items      "center"
   :color            (color :body-text-color)
   :background-color "transparent"
   :transition       "all 0.075s ease"
   ::stylefy/manual [[:&:hover {:background (darken (color :panel-color :opacity-low) 10)}]
                     [:&:active
                      :&:hover:active
                      :&.active {:color (color :body-text-color)
                                 :background-color  (darken (color :panel-color :opacity-med) 10)}]
                     [:&:disabled {:color (color :body-text-color 0.3)
                                   :background-color (color :panel-color :opacity-higher)
                                   :cursor "default"}]
                     [:span {:flex "1 0 auto"
                             :text-align "left"}]
                     [:.MuiSvgIcon-root button-icons-style
                      [(selectors/& (selectors/not (selectors/last-child))) button-icons-not-last-child-style]
                      [(selectors/& (selectors/not (selectors/first-child))) button-icons-not-first-child-style]
                      [(selectors/& ((selectors/first-child (selectors/last-child)))) button-icons-only-child-style]]]})


(def buttons-primary-style
  (merge buttons-style {:color (color :link-color)
                        :background-color (color :link-color :opacity-lower)
                        ::stylefy/manual [[:&:hover {:background (color :link-color :opacity-low)}]
                                          [:&:active
                                           :&:hover:active
                                           :&.active {:color "white"
                                                      :background-color (color :link-color)}]
                                          [:&:disabled {:color (color :body-text-color 0.3)
                                                        :background-color (color :panel-color :opacity-higher)
                                                        :cursor "default"}]
                                          [:span {:flex "1 0 auto"
                                                  :text-align "left"}]
                                          [:.MuiSvgIcon-root button-icons-style
                                           [(selectors/& (selectors/not (selectors/last-child))) button-icons-not-last-child-style]
                                           [(selectors/& (selectors/not (selectors/first-child))) button-icons-not-first-child-style]
                                           [(selectors/& ((selectors/first-child (selectors/last-child)))) button-icons-only-child-style]]]}))


(def devcard-section-style {:display "grid"
                            :grid-auto-flow "column"
                            :justify-content "flex-start"
                            :align-items "center"
                            :grid-gap "1rem"
                            :padding "1rem"
                            :margin "1rem"
                            :border-top [["1px solid" (color :panel-color)]]
                            ::stylefy/manual [[:h3 {:font-weight "normal"
                                                    :font-size "16px"}]]})


;;; Components


(defn button
  [{:keys [disabled label on-click-fn style active class]}]
  [:button (use-style (merge buttons-style style) {:disabled disabled
                                                   :on-click on-click-fn
                                                   :class [class (when active "active")]})
   label])


(defn button-primary
  [{:keys [disabled label on-click-fn style active class]}]
  [:button (use-style (merge buttons-primary-style style) {:disabled disabled
                                                           :on-click on-click-fn
                                                           :class [class (when active "active")]})
   label])


;;; Devcards


(defcard-rg Button
  [:<>
   [:section (use-style devcard-section-style)
    [:h3 "Default"]
    [button {:label "Button"}]
    [button {:label [:> mui-icons/Face]}]
    [button {:label [:<>
                     [:> mui-icons/Face]
                     [:span "Button"]]}]
    [button {:label [:<>
                     [:span "Button"]
                     [:> mui-icons/ChevronRight]]}]]
   [:section (use-style devcard-section-style)
    [:h3 "Active/Pressed"]
    [button {:label "Button"
             :active true}]
    [button {:label [:> mui-icons/Face]
             :active true}]
    [button {:label [:<>
                     [:> mui-icons/Face]
                     [:span "Button"]]
             :active true}]
    [button {:label [:<>
                     [:span "Button"]
                     [:> mui-icons/ChevronRight]]
             :active true}]]
   [:section (use-style devcard-section-style)
    [:h3 "Disabled"]
    [button {:disabled true :label "Button"}]
    [button {:disabled true :label [:> mui-icons/Face]}]
    [button {:disabled true :label [:<>
                                    [:> mui-icons/Face]
                                    [:span "Button"]]}]
    [button {:disabled true :label [:<>
                                    [:span "Button"]
                                    [:> mui-icons/ChevronRight]]}]]])


(defcard-rg Button-Primary
  [:<>
   [:section (use-style devcard-section-style)
    [:h3 "Primary"]
    [button-primary {:label "Button"}]
    [button-primary {:label [:> mui-icons/Face]}]
    [button-primary {:label [:<>
                             [:> mui-icons/Face]
                             [:span "Button"]]}]
    [button-primary {:label [:<>
                             [:span "Button"]
                             [:> mui-icons/ChevronRight]]}]]
   [:section (use-style devcard-section-style)
    [:h3 "Active/Pressed"]
    [button-primary {:label "Button"
                     :active true}]
    [button-primary {:label [:> mui-icons/Face]
                     :active true}]
    [button-primary {:label [:<>
                             [:> mui-icons/Face]
                             [:span "Button"]]
                     :active true}]
    [button-primary {:label [:<>
                             [:span "Button"]
                             [:> mui-icons/ChevronRight]]
                     :active true}]]
   [:section (use-style devcard-section-style)
    [:h3 "Primary Disabled"]
    [button-primary {:disabled true :label "Button"}]
    [button-primary {:disabled true :label [:> mui-icons/Face]}]
    [button-primary {:disabled true :label [:<>
                                            [:> mui-icons/Face]
                                            [:span "Button"]]}]
    [button-primary {:disabled true :label [:<>
                                            [:span "Button"]
                                            [:> mui-icons/ChevronRight]]}]]])
