(ns athens.page
  (:require [athens.parser :refer [parse]]
            [re-frame.core :refer [subscribe]]
            ))

(defn render-block-string
  [string]
  (fn []
    [:div
     [:span.controls {:style {:margin-right 5}} "•"]
     (parse string)]))

(defn render-blocks [block-uid]
  (fn [block-uid]
    (let [block (subscribe [:block/children [:block/uid block-uid]])]
      [:div
       (for [ch (:block/children @block)]
         (let [{:block/keys [string uid children]} ch]
           ^{:key uid}
           [:div
            [render-block-string string]
            [:div {:style {:margin-left 20}}
             [render-blocks uid]]]))])))


(defn main []
  (let [current-route (subscribe [:current-route])]
    (fn []
      (let [node (subscribe [:node [:block/uid (-> @current-route :path-params :id)]])]
        [:div
         [:h1 "Page Panel"]
         [:h2 (:node/title @node)] ; TODO: reverse lookup... diff view for node vs block
         [render-blocks (-> @current-route :path-params :id)]
         ]))))
