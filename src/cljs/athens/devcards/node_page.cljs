(ns athens.devcards.node-page
  (:require
    [athens.db :as db]
    [athens.devcards.blocks :as blocks]
    [athens.patterns :as patterns]
    [athens.style :refer [color]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as string]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [goog.functions :refer [debounce]]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [pull q]]
    [re-frame.core :refer [dispatch subscribe]]
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


;;; Helpers


(defn handler
  [val uid]
  (dispatch [:transact-event [[:db/add [:block/uid uid] :node/title val]]]))


(def db-handler (debounce handler 500))


;;; Components


(defn node-page-el
  [{:block/keys [children uid] title :node/title} editing-uid linked-refs unlinked-refs]
  [:div

   ;; Header
   [:h1 (use-style title-style {:data-uid uid :class "page-header"})
    [autosize/textarea
     {:default-value title
      :class      (when (= editing-uid uid) "is-editing")
      :auto-focus true
      :on-change  (fn [e] (db-handler (.. e -target -value) uid))}]
    [:span title]]

   [:div
    (for [child children]
      ^{:key (:db/id child)} [blocks/block-el child])]
   ;; TODO references
   [:div
    [:h4 "Linked References"]
    (for [ref linked-refs]
      ^{:key ref} [:p ref])]
   [:div
    [:h4 "Unlinked References"]
    (for [ref unlinked-refs]
      ^{:key ref} [:p ref])]])


(defn node-page-component
  "One diff between datascript and posh: we don't have pull in q for posh
  https://github.com/mpdairy/posh/issues/21"
  [ident]
  (let [node (->> @(pull db/dsdb db/node-pull-pattern ident) (db/sort-block))
        title (:node/title node)
        editing-uid @(subscribe [:editing-uid])]
    (when-not (string/blank? title)
      (let [linked-ref-entids     @(q db/q-refs db/dsdb (patterns/linked title))
            unlinked-ref-entids   @(q db/q-refs db/dsdb (patterns/unlinked title))]
        [node-page-el node editing-uid linked-ref-entids unlinked-ref-entids]))))


;;; Devcards


(defcard-rg Node-Page
  "pull entity 4093: \"Hyperlink\" page"
  [node-page-component 4093])
