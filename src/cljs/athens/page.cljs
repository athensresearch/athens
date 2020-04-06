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


(defn main []
  (let [current-route (subscribe [:current-route])]
    (fn []
      (let [node (subscribe [:node [:block/uid (-> @current-route :path-params :id)]])]
        [:div
         ;; [:h1 "Page Panel"]
         (if (:node/title @node)
           [:div
            [:h2 (:node/title @node)]
            [render-blocks (-> @current-route :path-params :id)]]
           [:div
            (let [parents (subscribe [:parents [:block/uid (:block/uid @node)]])]
              [:div {:style {:color "gray"}}
               (for [b @parents]
                 (let [{:block/keys [uid string] :node/keys [title]} b]
                   ^{:key uid}
                   [:span
                    [:span {:style {:cursor "pointer"}
                            :on-click #(dispatch [:navigate :page {:id uid}])}
                     (or string title)]
                    [:span " > "]]))])
            [:h2 (str "• " (:block/string @node))]
            [:div {:style {:margin-left 20}}
             [render-blocks (-> @current-route :path-params :id)]]])
         ]))))
