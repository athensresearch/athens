(ns athens.page
  (:require
    [athens.parser :refer [parse]]
    [athens.patterns :as patterns]
    [athens.router :refer [navigate-page toggle-open]]
    [re-frame.core :refer [subscribe dispatch]]
    #_[reagent.core :as reagent]
    #_[reitit.frontend.easy :as rfee]))


(defn render-blocks []
  (fn [block-uid]
    (let [block (subscribe [:block/children-sorted [:block/uid block-uid]])]
      [:div {:class "content-block"}
       (doall
        (for [ch (:block/children @block)]
          (let [{:block/keys [uid string open children] dbid :db/id} ch
                children? (not-empty children)]
            ^{:key uid}
            [:div
             [:div.block {:style {:display "flex"}}
              [:div.controls {:style {:display "flex" :align-items "flex-start" :padding-top 5}}
               (cond
                 (and children? open) [:span.arrow-down {:style {:width        0 :height 0
                                                                 :border-left  "5px solid transparent"
                                                                 :border-right "5px solid transparent"
                                                                 :border-top   "5px solid black"
                                                                 :cursor "pointer"
                                                                 :margin-top 4}
                                                         :on-click #(toggle-open dbid open)}]
                 (and children? (not open)) [:span.arrow-right {:style {:width        0 :height 0
                                                                        :border-top  "5px solid transparent"
                                                                        :border-bottom "5px solid transparent"
                                                                        :border-left   "5px solid black"
                                                                        :cursor "pointer"
                                                                        :margin-right 4}
                                                                :on-click #(toggle-open dbid open)}]
                 :else [:span {:style {:width 10}}])
               [:span {:style {:height         12 :width 12 :border-radius "50%" :margin-right 5
                               :cursor         "pointer" :display "flex" :background-color (if (not open) "lightgray" nil)
                               :vertical-align "middle" :align-items "center" :justify-content "center"}}
                [:span.controls {:style    {:height         5 :width 5 :border-radius "50%"
                                            :cursor         "pointer" :display "inline-block" :background-color "black"
                                            :vertical-align "middle"}
                                 :on-click #(navigate-page uid)}]]]
              [:span (parse string)]]
             (when open
               [:div {:style {:margin-left 20}}
                [render-blocks uid]])])))])))


(defn block-page []
  (fn [id]
    (let [node (subscribe [:node [:block/uid id]])
          parents (subscribe [:block/_children2 [:block/uid id]])]
      [:div
       [:span {:style {:color "gray"}}
        (interpose " > "
                   (map (fn [b]
                          (let [{:block/keys [uid string] :node/keys [title]} b]
                            ^{:key uid}
                            [:span
                             {:style {:cursor "pointer"}
                              :on-click #(navigate-page uid)}
                             (or string title)]))
                        @parents))]
       [:h2
        {:content-editable true
         :style {:margin 0}} (str "• " (:block/string @node))]
       [:div {:style {:margin-left 20}}
        [render-blocks (:block/uid @node)]]])))


(defn node-page []
  (fn [node]
    (let [linked-refs   (subscribe [:node/refs (patterns/linked   (:node/title node))])
          unlinked-refs (subscribe [:node/refs (patterns/unlinked (:node/title node))])]
      [:div
       [:h2
        {:content-editable true} (:node/title node)]
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
            [block-page id]])]]])))


(defn main []
  (let [current-route (subscribe [:current-route])]
    (fn []
      (let [node (subscribe [:node [:block/uid (-> @current-route :path-params :id)]])]
        [:div
          ;;[:h1 "Page Panel"]
         (if (:node/title @node)
           [node-page @node]
           [block-page (:block/uid @node)])]))))
