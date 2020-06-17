(ns athens.page
  (:require
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.patterns :as patterns]
    [athens.router :refer [navigate-page toggle-open]]
    [athens.style :refer [color OPACITIES]]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as reagent]
    [stylefy.core :as stylefy :refer [use-style]]))


;; STYLES

(def page-style {:margin "0 40px"})


(def title-style
  {:font-size "38px"
   :font-weight "500"
   :line-height "49px"
   :background "transparent"
   :font-family "inherit"
   :letter-spacing "-0.03em"
   :min-height "49px"
   :-webkit-appearance "none"
   :resize "none"
   :display "block"
   :width "100%"
   :border "none"
   :padding "0"
   :margin "0.2em 0"
   ::stylefy/mode [[:focus {:outline "none"
                            :color (color :body-text-color)
                            :opacity (:opacity-high OPACITIES)}]]})


(defn render-blocks
  []
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
               [:span (parse-and-render string)]]
              (when open
                [:div {:style {:margin-left 20}}
                 [render-blocks uid]])])))])))


(defn block-page
  []
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
       [:h2 {:style {:margin 0}}
        (str "• " (:block/string @node))]
       [:div {:style {:margin-left 20}}
        [render-blocks (:block/uid @node)]]])))


(def enter-keycode 13)
(def esc-keycode 27)


;; (defn title-comp
;;   [title]
;;   (let [s (reagent/atom {:editing false
;;                          :current-title title})
;;         save! (fn [new-title]
;;                 (swap! s assoc :editing false)
;;                 (dispatch [:node/renamed (:current-title @s) new-title]))
;;         cancel! (fn [] (swap! s assoc :editing false))]
;;     (fn [title]
;;       (if (:editing @s)
;;         [:input {:default-value title
;;                  :auto-focus true
;;                  :on-blur #(save! (-> % .-target .-value))
;;                  :on-key-down #(cond
;;                                  (= (.-keyCode %) enter-keycode)
;;                                  (save! (-> % .-target .-value))

;;                                  (= (.-keyCode %) esc-keycode)
;;                                  (cancel!)

;;                                  :else nil)}]
;;         [:h2 {:on-click (fn [_]
;;                           (swap! s #(-> %
;;                                         (assoc :editing true)
;;                                         (assoc :current-title title))))}
;;          title]))))

(defn title-comp
  [title]
  (let [s (reagent/atom {:editing false
                         :title title})
        save! (fn [new-title]
                (swap! s assoc :editing false)
                (dispatch [:node/renamed (:current-title @s) new-title]))
        cancel! (fn [] (swap! s assoc :editing false))]
    (fn []
      (if (:editing @s)
        [autosize/textarea (use-style title-style {:value (:title @s)
                                                   :auto-focus true
                                                   :on-change (fn [e] (swap! s assoc :title (.. e -target -value)))
                                                   :on-blur (fn [_]
                                                              #(save! (-> % .-target .-value))
                                                              (swap! s assoc :editing false))
                                                   :on-key-down (fn [e]
                                                                  (cond
                                                                    (= (.-keyCode e) enter-keycode)
                                                                    (doall
                                                                      (.preventDefault e)
                                                                      (save! (-> e .-target .-value)))

                                                                    (= (.-keyCode e) esc-keycode)
                                                                    (cancel!)

                                                                    :else nil))})]
        [:h1 (use-style title-style {:on-click (fn [_]
                                                 (swap! s #(-> %
                                                               (assoc :editing true)
                                                               (assoc :current-title title))))})
         (:title @s)]))))


(defn merge-prompt
  [{:keys [old-title new-title]}]
  [:div {:style {:background "red"
                 :color "white"}}
   (str "\"" new-title "\" already exists, merge pages?")
   [:a {:on-click #(dispatch [:node/merged old-title new-title])
        :style {:margin-left "30px"}}
    "yes"]
   [:a {:on-click #(dispatch [:node/merge-canceled])
        :style {:margin-left "30px"}}
    "no"]])


(defn node-page
  []
  (fn [node]
    (let [linked-refs   (subscribe [:node/refs (patterns/linked   (:node/title node))])
          unlinked-refs (subscribe [:node/refs (patterns/unlinked (:node/title node))])
          merge         (subscribe [:merge-prompt])]
      [:div
       (when (get @merge :active false)
         [merge-prompt @merge])
       [title-comp (:node/title node)]
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


(defn main
  []
  (let [current-route (subscribe [:current-route])]
    (fn []
      (let [node (subscribe [:node [:block/uid (-> @current-route :path-params :id)]])]
        [:div (use-style page-style)
         (if (:node/title @node)
           [node-page @node]
           [block-page (:block/uid @node)])]))))
