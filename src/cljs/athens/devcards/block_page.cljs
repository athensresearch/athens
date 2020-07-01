(ns athens.devcards.block-page
  (:require
    [athens.db :as db]
    [athens.devcards.blocks :refer [block-el]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [transact! pull]]
    [re-frame.core :refer [subscribe]]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles

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
                                 :border-radius "4px"
                                 :transition "opacity 0.15s ease"
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:textarea:focus
                      :.is-editing {:outline "none"
                                    :z-index "10"
                                    :display "block"
                                    :opacity "1"}]
                     [(selectors/+ :.is-editing :span) {:opacity 0}]]})


;;; Components


;; TODO: replace " > " with an icon. Get a TypeError when doing this, though. Maybe same problem as "->" issue in Athena results
(defn block-page-el
  [{:block/keys [string children uid]} parents editing-uid]

  [:div
   ;; Parent Context
   [:span {:style {:color "gray"}}
    (interpose
      " > "
      (for [p parents]
        (let [{:keys [node/title block/uid block/string]} p]
          [:span {:key uid :style {:cursor "pointer"} :on-click #(navigate-uid uid)} (or string title)])))]


   ;; Header
   [:h1 (use-style title-style {:data-uid uid :class "block-header"})
    [autosize/textarea
     {:value      string
      :class       (when (= editing-uid uid) "is-editing")
      :auto-focus true
      :on-change  (fn [e]
                    (transact! db/dsdb [[:db/add [:block/uid uid] :block/string (.. e -target -value)]]))}]
    [:span string]]


   ;; Children
   [:div (for [child children]
           (let [{:keys [db/id]} child]
             ^{:key id} [block-el child]))]])


(defn block-page-component
  [ident]
  (let [block   @(pull db/dsdb db/block-pull-pattern ident)
        parents (->> @(pull db/dsdb db/parents-pull-pattern ident)
                     (db/shape-parent-query))
        editing-uid @(subscribe [:editing-uid])]
    ;;(prn block parents)
    [block-page-el block parents editing-uid]))


;;; Devcards


(defcard-rg Block-Page
  "pull entity 2347: a block within Athens FAQ"
  [block-page-component 2347])
