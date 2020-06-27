(ns athens.devcards.node-page
  (:require
    [athens.db :as db]
    [athens.devcards.blocks :as blocks]
    [athens.patterns :as patterns]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [transact! pull q]]
    [re-frame.core :refer [subscribe]]))


;;; Components


(defn node-page-el
  [{:block/keys [children uid] title :node/title} editing-uid linked-refs unlinked-refs]
  [:div

   ;; Header
   [:div {:data-uid uid :class "page-header"}
    (if (= uid editing-uid)
      [:h1
       [autosize/textarea
        {:value      title
         :style      {:width "100%"}
         :auto-focus true
         :on-change  (fn [e]
                       [:transact-event [[:db/add [:block/uid uid] :node/title (.. e -target -value)]]])}]]
      [:h1 title])]

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
    (when-not (clojure.string/blank? title)
      (let [linked-ref-entids     @(q db/q-refs db/dsdb (patterns/linked title))
            unlinked-ref-entids   @(q db/q-refs db/dsdb (patterns/unlinked title))]
        [node-page-el node editing-uid linked-ref-entids unlinked-ref-entids]))))


;;; Devcards


(defcard-rg Node-Page
  "pull entity 4093: \"Hyperlink\" page"
  [node-page-component 4093])
