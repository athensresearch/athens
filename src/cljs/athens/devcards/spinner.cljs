(ns athens.devcards.spinner
  (:require
    [athens.db]
    [athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles

(stylefy/keyframes "appear-and-drop"
                   [:from
                    {:transform "translateY(-40%)"
                     :opacity "0"}]
                   [:to
                    {:transform "translateY(0)"
                     :opacity "var(--anim-opacity-end, 1)"}])


(stylefy/keyframes "appear"
                   [:from
                    {:opacity "0"}]
                   [:to
                    {:opacity "var(--anim-opacity-end, 1)"}])


(stylefy/keyframes "spinning"
                   [:from
                    {:transform "rotate(0deg)"}]
                   [:to
                    {:transform "rotate(1079deg)"}])


(def spinner-style
  {:--anim-opacity-end "1"
   :width "10em"
   :height "10em"
   :display "grid"
   :align-self "center"
   :margin "auto"
   :text-align "center"
   :place-items "center"
   :animation "appear 0.5s ease"
   :place-content "center"
   :grid-gap "0.5rem"})


(def spinner-progress-style
  {:width "3em"
   :height "3em"
   :border-radius "1000px"
   :border (str "1.5px solid " (color :panel-color))
   :border-top-color (color :link-color)
   :animation "spinning 3s linear infinite"})


(def spinner-message-style
  {:--anim-opacity-end (:opacity-high OPACITIES)
   :animation "appear-and-drop 1s 0.75s ease"
   :font-size "14px"
   :animation-fill-mode "both"})


;;; Components


(defn spinner
  [{:keys [message style]}]
  [:div (use-style (merge spinner-style style))
   [:div (use-style spinner-progress-style)]
   [:span (use-style spinner-message-style) (if message
                                              message
                                              "Loading...")]])


;;; Devcards


(defcard-rg Default-Spinner
  [spinner (use-style spinner-style)])


(defcard-rg Spinner-with-custom-message
  [spinner (use-style spinner-style {:message "Custom Loading Message"})])
