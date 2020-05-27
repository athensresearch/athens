(ns athens.views.components.block
  (:require
    [athens.data.blocks :refer [full-data]]
    [devcards.core :as devcards :refer [defcard defcard-rg]]
    [reagent.core :as r]))


(declare block-component)


(defn block-title-component
  [*title opts]
  (when-let [t @*title]
    [:h1 t]))


(defn block-content-component
  [*content opts]
  [:p @*content])


(defn block-children-component
  [*block ks *open? *children {:keys [toggle-open] :as opts}]
  (let [children @*children]
    (when (seq children)
      (let [path (conj ks :block/open)]
        [:div
         [:button {:on-click (fn [] (toggle-open path))} (if @*open? "close" "open")]
         (if @*open?
           [:div
            [:ul
             (for [[i child] (map-indexed list children)]
               ^{:key i}
               [:li
                [:div
                 [block-component *block (conj ks :block/children i) opts]]])]])]))))


(defn block-component
  [*block ks opts]
  (let [*current-block  (r/cursor *block ks)

        *title (r/cursor *block (conj ks :node/title))
        *content (r/cursor *block (conj ks :block/string))
        *children (r/cursor *block (conj ks :block/children))
        *open? (r/cursor *block (conj ks :block/open))]
    (fn []
      [:div
       [:pre (pr-str (dissoc @*current-block :block/children))]
       [block-title-component *title opts]
       [block-content-component *content opts]
       [block-children-component *block ks *open? *children opts]])))


(defcard-rg block
  (let [data (r/atom full-data)]
    [block-component
     data
     #_[] [:block/children 0]

     {:toggle-open (fn [ks]
                     (swap! data update-in ks not))}]))
