(ns athens.page
  (:require [athens.parser :refer [parse]]
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

; TODO: make the regex a rule function in subs
(defn make-pattern [str]
  (re-pattern (str "\\[\\[" str "\\]\\]")))

(defn node-page [node]
  (fn [node]
    (let [linked-refs (subscribe [:linked-refs2 (make-pattern str)])]
      [:div
       [:h2 (:node/title node)]
       [render-blocks (:block/uid node)]
       [:div
        [:h3 "Linked References"]
        (for [lr @linked-refs]
          (let [{:block/keys [uid string children] :node/keys [title]} lr]
            (pr "LR" lr)
            [:div string])
          )]
       ])))


(defn block-page [node]
  (fn [node]
    (let [parents (subscribe [:block/_children2 [:block/uid (:block/uid node)]])]
      [:div
       (for [b @parents]
         (let [{:block/keys [uid string] :node/keys [title]} b]
           ^{:key uid}
           [:span {:style {:color "gray"}}            
            [:span
             [:span {:style {:cursor "pointer"}
                     :on-click #(dispatch [:navigate :page {:id uid}])}
              (or string title)]
             [:span " > "]]]))
       [:h2 (str "• " (:block/string node))]
       [:div {:style {:margin-left 20}}
        [render-blocks (:block/uid node)]]])))

(defn main []
  (let [current-route (subscribe [:current-route])]
    (fn []
      (let [node (subscribe [:node [:block/uid (-> @current-route :path-params :id)]])]
        [:div
          [:h1 "Page Panel"]
         (if (:node/title @node)
           [node-page @node]
           [block-page @node])
         ]))))
