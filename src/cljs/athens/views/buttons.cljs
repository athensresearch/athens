(ns athens.views.buttons
  (:require
    [athens.db]
    [athens.style :refer [color]]
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
  {:margin-inline-start "-0.25rem"
   :margin-inline-end "-0.25rem"})


(def buttons-style
  {:cursor           "pointer"
   :padding          "0.375rem 0.625rem"
   :margin           "0"
   :font-family      "inherit"
   :font-size        "inherit"
   :border-radius    "0.25rem"
   :font-weight      "500"
   :border           "none"
   :display          "inline-flex"
   :align-items      "center"
   :color            (color :body-text-color)
   :background-color "transparent"
   :transition       "all 0.075s ease"
   ::stylefy/manual [[:&:hover {:background (color :body-text-color :opacity-lower)}]
                     [:&:active
                      :&:hover:active
                      :&.is-active {:color (color :body-text-color)
                                    :background (color :body-text-color :opacity-lower)}]
                     [:&:active
                      :&:hover:active
                      :&:active.is-active {:background (color :body-text-color :opacity-low)}]
                     [:&:disabled :&:disabled:active {:color (color :body-text-color :opacity-low)
                                                      :background (color :body-text-color :opacity-lower)
                                                      :cursor "default"}]
                     [:span {:flex "1 0 auto"
                             :text-align "left"}]
                     [:kbd {:margin-inline-start "1rem"
                            :font-size "85%"}]
                     [:svg button-icons-style
                      [(selectors/& (selectors/not (selectors/last-child))) button-icons-not-last-child-style]
                      [(selectors/& (selectors/not (selectors/first-child))) button-icons-not-first-child-style]
                      [(selectors/& ((selectors/first-child (selectors/last-child)))) button-icons-only-child-style]]
                     [:&.is-primary {:color (color :link-color)
                                     :background (color :link-color :opacity-lower)}
                      [:&:hover {:background (color :link-color :opacity-low)}]
                      [:&:active
                       :&:hover:active
                       :&.is-active {:color "white"
                                     :background (color :link-color)}]
                      [:&:disabled :&:disabled:active {:color (color :body-text-color :opacity-low)
                                                       :background (color :body-text-color :opacity-lower)
                                                       :cursor "default"}]]]})


;;; Components

(stylefy/class "button" buttons-style)


(defn button
  "Keep button interface as close to vanilla hiccup as possible.
  Dissoc :style :active and :class because we don't want to merge them in directly.
  Can pass in a :key prop to make react happy, as a :key or ^{:key}. Just works"
  ([children] [button {} children])
  ([{:keys [style active primary class] :as props} children]
   (let [props- (dissoc props :style :active :primary :class)]
     [:button (use-style (merge buttons-style style)
                         (merge props- {:class (vec (flatten [(when active "is-active") (when primary "is-primary") class]))}))
      children])))
