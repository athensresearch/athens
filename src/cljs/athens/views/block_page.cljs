(ns athens.views.block-page
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color]]
    [athens.views.blocks :refer [block-el db-on-change]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def page-style
  {:margin "2rem auto"
   :padding "1rem 2rem"
   :flex-basis "100%"
   :max-width "55rem"})


(def title-style
  {:position "relative"
   :overflow "visible"
   :flex-grow "1"
   :margin "0.2em 0"
   :letter-spacing "-0.03em"
   :word-break "break-word"
   ::stylefy/manual [[:textarea {:display "none"}]
                     [:&:hover [:textarea {:display "block"
                                           :z-index 1}]]
                     [:textarea {:-webkit-appearance "none"
                                 :cursor "text"
                                 :resize "none"
                                 :transform "translate3d(0,0,0)"
                                 :color "inherit"
                                 :font-weight "inherit"
                                 :padding "0"
                                 :letter-spacing "inherit"
                                 :position "absolute"
                                 :top "0"
                                 :left "0"
                                 :right "0"
                                 :width "100%"
                                 :min-height "100%"
                                 :caret-color (color :link-color)
                                 :background "transparent"
                                 :margin "0"
                                 :font-size "inherit"
                                 :line-height "inherit"
                                 :border-radius "0.25rem"
                                 :transition "opacity 0.15s ease"
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:textarea:focus
                      :.is-editing {:outline "none"
                                    :z-index 3
                                    :display "block"
                                    :opacity "1"}]
                     [(selectors/+ :.is-editing :span) {:opacity 0}]]})


;;; Components


(defn block-page-el
  [{:block/keys [string children uid]} parents editing-uid]

  [:div (use-style page-style)
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
     {:default-value string
      :class (when (= editing-uid uid) "is-editing")
      :auto-focus true
      :on-change  (fn [e] (db-on-change (.. e -target -value) uid))}]
    [:span string]]


   ;; Children
   [:div (for [child children]
           (let [{:keys [db/id]} child]
             ^{:key id} [block-el child]))]])


(defn block-page-component
  [ident]
  (let [block   (db/get-block-document ident)
        parents (db/get-parents-recursively ident)
        editing-uid @(subscribe [:editing/uid])]
    [block-page-el block parents editing-uid]))
