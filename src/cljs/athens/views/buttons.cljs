(ns athens.views.buttons
  (:require
    [athens.db]
    [athens.style :refer [color cssv]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def button-icons-style
  {:margin-block-start "-0.0835em"
   :margin-block-end "-0.0835em"})


(def button-icons-not-last-child-style {:margin-inline-end "0.251em"})

(def button-icons-not-first-child-style {:margin-inline-style "0.251em"})


(def button-icons-only-child-style
  {:margin-inline-start "-4px"
   :margin-inline-end "-4px"})


(def buttons-style
  {:cursor           "pointer"
   :padding          "0.375rem 0.625rem"
   :margin           "0"
   :font-family      "inherit"
   :font-size        "inherit"
   :border-radius    "4px"
   :font-weight      "500"
   :border           "none"
   :display          "inline-flex"
   :align-items      "center"
   :color            (cssv "link-color")
   :background-color "transparent"
   :transition       "all 0.075s ease"
   ::stylefy/manual [[:&:hover {:background (color :body-text-color :opacity-lower)}]
                     [:&:active
                      :&:hover:active
                      :&.is-active {:color (cssv "body-text-color")
                                    :background-color  (color :body-text-color :opacity-low)}]
                     [:&:disabled :&:disabled:active {:color (color :body-text-color 0.3)
                                                      :background-color (color :body-text-color :opacity-lower)
                                                      :cursor "default"}]
                     [:span {:flex "1 0 auto"
                             :text-align "left"}]
                     [:.MuiSvgIcon-root button-icons-style
                      [(selectors/& (selectors/not (selectors/last-child))) button-icons-not-last-child-style]
                      [(selectors/& (selectors/not (selectors/first-child))) button-icons-not-first-child-style]
                      [(selectors/& ((selectors/first-child (selectors/last-child)))) button-icons-only-child-style]]]})


(def buttons-primary-style
  (merge buttons-style {:color (cssv "link-color")
                        :background-color (color :link-color :opacity-lower)
                        ::stylefy/manual [[:&:hover {:background (color :link-color :opacity-low)}]
                                          [:&:active
                                           :&:hover:active
                                           :&.is-active {:color "white"
                                                         :background-color (cssv "link-color")}]
                                          [:&:disabled :&:disabled:active {:color (color :body-text-color 0.3)
                                                                           :background-color (color :body-text-color :opacity-lower)
                                                                           :cursor "default"}]
                                          [:span {:flex "1 0 auto"
                                                  :text-align "left"}]
                                          [:.MuiSvgIcon-root button-icons-style
                                           [(selectors/& (selectors/not (selectors/last-child))) button-icons-not-last-child-style]
                                           [(selectors/& (selectors/not (selectors/first-child))) button-icons-not-first-child-style]
                                           [(selectors/& ((selectors/first-child (selectors/last-child)))) button-icons-only-child-style]]]}))


;;; Components


(defn button
  "Creates a button control"
  [{:keys [disabled label on-click-fn style active class]}]
  [:button (use-style (merge buttons-style style) {:disabled disabled
                                                   :on-click on-click-fn
                                                   :class [class (when active "is-active")]})
   label])


(defn button-primary
  "Creates a button control"
  [{:keys [disabled label on-click-fn style active class]}]
  [:button (use-style (merge buttons-primary-style style) {:disabled disabled
                                                           :on-click on-click-fn
                                                           :class [class (when active "is-active")]})
   label])
