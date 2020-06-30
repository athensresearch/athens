(ns athens.devcards.block-page
  (:require
    [athens.db :as db]
    [athens.devcards.blocks :refer [block-el]]
    [athens.router :refer [navigate-uid]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [transact! pull]]
    [re-frame.core :refer [subscribe]]))


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
   [:div {:data-uid uid :class "block-header"}
    (if (= uid editing-uid)
      [:h1
       [autosize/textarea
        {:value       string
         :style       {:width "100%"}
         :auto-focus  true
         :on-change   (fn [e]
                        ;;(prn (.. e -target -value))
                        (transact! db/dsdb [[:db/add [:block/uid uid] :block/string (.. e -target -value)]]))}]]
      [:h1 (str "• " string)])]

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
