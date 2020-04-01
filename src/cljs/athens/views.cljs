(ns athens.views
  (:require
   [re-frame.core :as rf :refer [subscribe]]
   ;; [re-posh.core :as rp]
   [athens.subs]
   ))

;; (defn render-counter []
;;   (let [counter (re-frame/subscribe [:counter])]
;;     [:div
;;      [:h3 @counter]
;;      [:button {:on-click #(re-frame/dispatch [:add-counter])} "Add"]]))

(defn render-nodes []
  (let [nodes @(subscribe [:nodes])]
    [:ul
     (for [[eid title] nodes]
       ^{:key eid} [:li (str eid " " title)])]
      ))

(defn render-block [block]
 ^{:key (:db/id block)} [:div {:style {:margin-left "20px"}} (str "•   "  (:block/string block))
                         (for [b (:block/children block)]
                           (render-block b))])

(defn render-node [node-eid]
  (let [node @(subscribe [:node node-eid])
        blocks @(subscribe [:blocks node-eid])]
    [:div
     [:h1 (:node/title node)]
     [:h4 "Attributes"]
     [:ul
      (map (fn [[k v]] [:li {:key (str k ":" v)} (str k ": " v)]) node)]
     [:h4 "Blocks"]
     [:div (for [b (:block/children blocks)]
             (render-block b))
      ]]))

(defn main-panel []
  (let [author (subscribe [:user/name])]
    [:div
     [:h1 "Hello World"]
     [:p (str "from " @author)] 
     (render-node 56)
     [:h1 "All Nodes"]
     (render-nodes)
     ]))

