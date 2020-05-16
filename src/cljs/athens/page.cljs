(ns athens.page
  (:require [athens.parser :refer [parse]]
            [re-frame.core :refer [subscribe dispatch]]
            #_[reitit.frontend.easy :as rfee]
            #_[reagent.core :as reagent]))


(defn on-block-click [uid]
  (dispatch [:navigate :page {:id uid}]))

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
             [:div.block
              ;; TODO refactor into style.cljs
              {:style {:display "flex"}
               :on-click #()}
              [:div.controls
               ;; TODO refactor into style.cljs
               {:style {:display "flex"
                        :align-items "flex-start"
                        :padding-top 5}}
               (cond
                 (and children? open) [:span.arrow-down {:on-click #(dispatch [:block/toggle-open dbid open])}]
                 (and children? (not open)) [:span.arrow-right {:on-click #(dispatch [:block/toggle-open dbid open])}]
                 :else [:span {:style {:width 10}}])
               [:span.bullet
                ;; TODO refactor into style.cljs
                {:style {:height 12
                         :width 12
                         :border-radius "50%"
                         :margin-right 5
                         :cursor "pointer"
                         :display "flex"
                         :background-color (if (not open) "lightgray" nil)
                         :vertical-align "middle"
                         :align-items "center"
                         :justify-content "center"}}
                [:span.controls
                 ;; TODO refactor into style.cljs
                 {:style {:height 5
                          :width 5
                          :border-radius "50%"
                          :cursor "pointer"
                          :display "inline-block"
                          :background-color "black"
                          :vertical-align "middle"}
                  :on-click #(on-block-click uid)}]]]
              [:span (parse string)]]
             (when open
               [:div {:style {:margin-left 20}}
                [render-blocks uid]])])))])))

                                        ; :match [[title]] or #title or #[[title]]
(defn linked-pattern [string]
  (re-pattern (str "("
                   "\\[{2}" string "\\]{2}"
                   "|" "#" string
                   "|" "#" "\\[{2}" string "\\[{2}"
                   ")")))

; also excludes [title] :(
(defn unlinked-pattern [string]
  (re-pattern (str "[^\\[|#]" string)))

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
                              :on-click #(on-block-click uid)}
                             (or string title)]))
                        @parents))]
       [:div
        {:style {:margin 0}
         :content-editable true}
        (str "• " (:block/string @node))]
       [:div
        {:style {:margin-left 20}}
        [render-blocks (:block/uid @node)]]])))

(defn node-page []
  (fn [node]
    (let [linked-refs   (subscribe [:node/refs (linked-pattern   (:node/title node))])
          unlinked-refs (subscribe [:node/refs (unlinked-pattern (:node/title node))])]
      [:div
       [:h2
        {:content-editable true}
        (:node/title node)]
       [render-blocks (:block/uid node)]
       [:div.lnk-refs-wrap
        [:h3 "Linked References"]
        [:div.lnk-refs
         (for [id (reduce into [] @linked-refs)]
           ^{:key id}
           [:div.lnk-ref
            [block-page id]])]]
       [:div.unl-refs-wrap
        [:h3 "Unlinked References"]
        [:div.unl-refs
         (for [id (reduce into [] @unlinked-refs)]
           ^{:key id}
           [:div.unl-ref
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
