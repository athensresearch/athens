(ns athens.views.spinner
  (:require
    [athens.db]
    [athens.style :as style]
    [cljsjs.react]
    [cljsjs.react.dom]
    [goog.dom :refer [getElement]]
    [reagent.dom :as r-dom]
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
   :border (str "1.5px solid " (style/color :background-minus-1))
   :border-top-color (style/color :link-color)
   :animation "spinning 3s linear infinite"})


(def spinner-message-style
  {:--anim-opacity-end (:opacity-high style/OPACITIES)
   :animation "appear-and-drop 1s 0.75s ease"
   :font-size "14px"
   :animation-fill-mode "both"})


(def initial-spinner-container
  {:margin-top      "50vh"
   :transform       "translateY(-50%)"
   :display         "flex"
   :flex-direction  "column"
   :justify-content "center"
   :align-items     "center"})


;;; Components


(defn spinner-component
  [{:keys [message style]}]
  [:div (use-style (merge spinner-style style))
   [:div (use-style spinner-progress-style)]
   [:span (use-style spinner-message-style) (or message "Loading...")]])


(goog-define COMMIT_URL false)


(defn initial-spinner-component
  []
  [:div (use-style initial-spinner-container)
   (when COMMIT_URL
     [:a {:href COMMIT_URL} COMMIT_URL])
   [spinner-component]])


(defn ^:export init-spinner
  []
  (style/init)
  (r-dom/render [initial-spinner-component]
                (getElement "app")))
