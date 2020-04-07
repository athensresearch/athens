(ns athens.page
  (:require [athens.parser :refer [parse]]
            [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]
            [reitit.frontend.easy :as rfee]))

(defn render-blocks [block-uid]
  (fn [block-uid]
    (let [block (subscribe [:block/children [:block/uid block-uid]])]
      [:div
       (doall
        (for [ch (:block/children @block)]
          (let [{:block/keys [uid string children]} ch]
            ^{:key uid}
            [:div
             [:span.controls {:style {:margin-right 5 :cursor "pointer"}
                              :on-click #(dispatch [:navigate :page {:id uid}])}
              "•"]
             [:span (parse string)]
             [:div {:style {:margin-left 20}}
              [render-blocks uid]]])))])))

; match [[title]] or #title or #[[title]]
(defn linked-pattern [string]
  (re-pattern (str "("
                   "\\[{2}" string "\\]{2}"
                   "|" "#" string
                   "|" "#" "\\[{2}" string "\\[{2}"
                   ")")))

; also excludes [title] :(
(defn unlinked-pattern [string]
  (re-pattern (str "[^\\[|#]" string)))

(defn block-page [id]
  (fn [id]
    (let [node (subscribe [:node [:block/uid id]])
          parents (subscribe [:block/_children2 [:block/uid id]])]
      [:div
       [:span {:style {:color "gray"} }
        (interpose " > " 
                   (map (fn [b]
                          (let [{:block/keys [uid string] :node/keys [title]} b]
                            ^{:key uid}
                            [:span
                             {:style {:cursor "pointer"}
                              :on-click #(dispatch [:navigate :page {:id uid}])}
                             (or string title)]))
                        @parents))]
       [:h2 {:style {:margin 0}} (str "• " (:block/string @node))]
       [:div {:style {:margin-left 20}}
        [render-blocks (:block/uid @node)]]])))

(defn node-page [node]
  (fn [node]
    (let [linked-refs   (subscribe [:node/refs (linked-pattern   (:node/title node))])
          unlinked-refs (subscribe [:node/refs (unlinked-pattern (:node/title node))])]
      [:div
       [:h2 (:node/title node)]
       [render-blocks (:block/uid node)]
       [:div
        [:h3 "Linked References"]
        [:div
         (for [id (reduce into [] @linked-refs)]
           ^{:key id}
           [:div {:style {:background-color "lightblue" :margin "15px 0px" :padding 5}}
            [block-page id]])]]
       [:div
        [:h3 "Unlinked References"]
        [:div
         (for [id (reduce into [] @unlinked-refs)]
           ^{:key id}
           [:div {:style {:background-color "lightblue" :margin "15px 0px" :padding 5}}
            [block-page id]])]]
       ])))

(defn main []
  (let [current-route (subscribe [:current-route])]
    (fn []
      (let [node (subscribe [:node [:block/uid (-> @current-route :path-params :id)]])]
        [:div
          [:h1 "Page Panel"]
         (if (:node/title @node)
           [node-page @node]
           [block-page (:block/uid @node)])]))))
