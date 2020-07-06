(ns athens.views.buttons
  (:require
    [athens.db]
    [athens.style :refer [color]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.color :refer [darken]]
    [garden.selectors :as selectors]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def buttons-style
  {:cursor "pointer"
   :padding          "6px 10px"
   :margin           "0"
   :font-family      "inherit"
   :font-size        "inherit"
   :border-radius    "4px"
   :font-weight      "500"
   :border           "none"
   :display          "inline-flex"
   :align-items      "center"
   :color            "rgba(50, 47, 56, 1)"
   :background-color "transparent"
   :transition       "all 0.05s ease"
   ::stylefy/mode [[:hover {:background-color "#EFEDEB"}]
                   [:active {:color "rgba(0, 117, 225)"
                             :background-color "rgba(0, 117, 225, 0.1)"}]
                   [:disabled {:color "rgba(0, 0, 0, 0.3)"
                               :background-color "#EFEDEB"
                               :cursor "default"}]]
   ::stylefy/manual [[:svg {:margin-block-start "-0.0835em"
                            :margin-block-end "-0.0835em"}
                      [(selectors/& (selectors/not (selectors/last-child))) {:margin-inline-end "0.251em"}]
                      [(selectors/& (selectors/not (selectors/first-child))) {:margin-inline-start "0.251em"}]
                      [(selectors/& ((selectors/first-child (selectors/last-child)))) {:margin-inline-start "-4px"
                                                                                       :margin-inline-end "-4px"}]]
                     [:span {:flex "1 0 auto"
                             :text-align "left"}]
                     [:&.active {:background-color (darken (color :panel-color) 10)}]]})


(def buttons-primary-style
  (merge buttons-style {:color "rgba(0, 117, 225)"
                        :background-color "rgba(0, 117, 225, 0.1)"
                        ::stylefy/mode [[:hover {:background-color "rgba(0, 117, 225, 0.25)"}]
                                        [:active {:color "white"
                                                  :background-color "rgba(0, 117, 225, 1)"}]
                                        [:disabled {:color "rgba(0, 0, 0, 0.3)"
                                                    :background-color "#EFEDEB"
                                                    :cursor "default"}]]}))


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
