(ns athens.devcards.buttons
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db]
    [athens.style :refer [base-styles]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [stylefy.core :as stylefy :refer [use-style]]))


(def buttons
  {:cursor "pointer"
   :padding          "6px 10px"
   :border-radius    "4px"
   :font-weight      "500"
   :border           "none"
   :display          "inline-flex"
   :align-self       "flex-start"
   :align-items      "center"
   :color            "rgba(50, 47, 56, 1)"
   :background-color "transparent"
   ::stylefy/mode [[:hover {:background-color "#EFEDEB"}]
                   [:active {:color "rgba(0, 117, 225)"
                             :background-color "rgba(0, 117, 225, 0.1)"}]
                   [:disabled {:color "rgba(0, 0, 0, 0.3)"
                               :background-color "#EFEDEB"
                               :cursor "default"}]]
   ::stylefy/manual [[:svg {:font-size "145%"
                            :vertical-align "-0.05em"}
                      [(selectors/& (selectors/not (selectors/last-child))) {:margin-inline-end "0.251em"}]
                      [(selectors/& (selectors/not (selectors/first-child))) {:margin-inline-start "0.251em"}]]]})


(def buttons-primary
  (merge buttons {:color "rgba(0, 117, 225)"
                  :background-color "rgba(0, 117, 225, 0.1)"
                  ::stylefy/mode [[:hover {:background-color "rgba(0, 117, 225, 0.25)"}]
                                  [:active {:color "white"
                                            :background-color "rgba(0, 117, 225, 1)"}]]}))

(defn button [{:keys [disabled
                      primary]} content]
  [:button (use-style buttons {:disabled disabled}) content])


(defcard-rg Button
  [:div
   [:button (use-style buttons) [:> mui-icons/Face]]
   [:button (use-style buttons) [:span "Press Me"]]
   [:button (use-style buttons) [:> mui-icons/Face] [:span "Press Me"]]
   [:button (use-style buttons) [:span "Press Me"] [:> mui-icons/Face]]])


(defcard-rg Disabled-Button
  [button {:disabled true} "Disabled Button"])


(defcard-rg Primary-Button
  [button {:primary true} "Primary Button"])
  ;; [:button.primary (use-style buttons-primary) "Press Me"])
