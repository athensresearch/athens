(ns athens.devcards.blocks
  (:require
    [athens.devcards.db :refer [new-conn posh-conn! load-real-db-button]]
    [athens.db]
    [athens.lib.dom.attributes :refer [with-styles with-attributes]]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.router :refer [navigate-page toggle-open]]
    [athens.style :refer [style-guide-css +flex-column +flex-center]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard defcard-rg]]
    ["@material-ui/icons" :as mui-icons]
    [posh.reagent :refer [transact! pull q]]
    [reagent.core :as r]))


(defcard-rg Import-Styles
  [style-guide-css])


(defcard Instantiate-Dsdb)
(defonce conn (new-conn))
(posh-conn! conn)


(defcard-rg Create-Datoms
  (let [datoms [{:db/id          1
                 :block/uid      "uid1",
                 :node/title     "I'm a page"
                 :block/children [{:db/id          2
                                   :block/uid      "uid2",
                                   :block/open     true,
                                   :block/string   "child block - uid2"
                                   :block/children [{:db/id        4
                                                     :block/uid    "uid4"
                                                     :block/string "child block - uid4"}]}
                                  {:db/id          3
                                   :block/uid      "uid3"
                                   :block/open     false
                                   :block/string   "child block - uid3"
                                   :block/children [{:db/id        5
                                                     :block/uid    "uid5"
                                                     :block/string "child block - uid5"}]}]}]]
    [:button.primary {:on-click #(transact! conn datoms)} "Create Datoms"]))


(defcard-rg Load-Real-DB
  [load-real-db-button conn])


(defn toggle
  [dbid open?]
  (transact! conn [{:db/id dbid :block/open (not open?)}]))


(def +gray-circle
  (with-styles +flex-center
    {:height 12 :width 12 :margin-right 5 :margin-top 5 :border-radius "50%" :cursor "pointer"}))


(def +black-circle
  (with-styles {:height 5 :width 5 :border-radius "50%" :cursor "pointer" :display "inline-block"
                :background-color "black" :vertical-align "middle"}))


(comment
  "Playing around with using MaterialUI icons for circles instead of plain CSS"
  [:div +flex-center
   [:> mui-icons/FiberManualRecord (with-styles {:font-size 10 :color "lightgray"})]
   [:> mui-icons/FiberManualRecord (with-styles {:font-size 6})]])


(declare block-component)

(defn block-el [block]
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  (let [{:block/keys [uid string open children] dbid :db/id} block
        open? (and (seq children) open)
        closed? (and (seq children) (not open))]
    [:div +flex-column
     [:div {:style {:display "flex"}}
      (cond
        open? [:> mui-icons/KeyboardArrowRight {:style {:cursor "pointer"} :on-click #(toggle dbid open)}]
        closed? [:> mui-icons/KeyboardArrowDown {:style {:cursor "pointer"} :on-click #(toggle dbid open)}]
        :else [:span {:style {:width 10}}])
      [:span (with-styles +gray-circle {:background-color (if closed? "lightgray" nil)})
       [:span (with-attributes +black-circle {:on-click #(navigate-page uid)})]]
      [:span (parse-and-render string)]]                                       ;; parse-and-render
     (when open?
       (for [ch (:block/children block)]
         (let [uid (:block/uid ch)]
           [:div {:style {:margin-left 28} :key uid}
            [block-component uid]])))]))


(defn block-component [uid]
  "This query is long because I'm not sure how to recursively find all child blocks with all attributes
  '[* {:block/children [*]}] doesn't work"
  (fn []
    (let [block (pull conn
                  '[:db/id :block/string :block/uid :block/children :block/open {:block/children ...}]
                  [:block/uid uid])]
      (when (:db/id @block)
        [block-el @block]))))


(defcard-rg Block
  "[block-component \"uid2\"]"
  [block-component "uid2"])


(def enter-keycode 13)
(def esc-keycode 27)


(defn title-comp [title]
  (let [s (r/atom {:editing false
                         :current-title title})
        save! (fn [new-title]
                (swap! s assoc :editing false)
                ;;(dispatch [:node/renamed (:current-title @s) new-title])
                )
        cancel! (fn [] (swap! s assoc :editing false))]
    (fn [title]
      (if (:editing @s)
        [:input {:default-value title
                 :auto-focus true
                 :on-blur #(save! (-> % .-target .-value))
                 :on-key-down #(cond
                                 (= (.-keyCode %) enter-keycode)
                                 (save! (-> % .-target .-value))

                                 (= (.-keyCode %) esc-keycode)
                                 (cancel!)

                                 :else nil)}]
        [:h2 {:on-click (fn [_] (swap! s #(-> %
                                            (assoc :editing true)
                                            (assoc :current-title title))))}
         title]))))


;;(defn merge-prompt
;;  [{:keys [old-title new-title]}]
;;  [:div {:style {:background "red"
;;                 :color "white"}}
;;   (str "\"" new-title "\" already exists, merge pages?")
;;   [:a {:on-click #(dispatch [:node/merged old-title new-title])
;;        :style {:margin-left "30px"}}
;;    "yes"]
;;   [:a {:on-click #(dispatch [:node/merge-canceled])
;;        :style {:margin-left "30px"}}
;;    "no"]])


;;(defn main []
;;  (let [current-route (subscribe [:current-route])]
;;    (fn []
;;      (let [node (subscribe [:node [:block/uid (-> @current-route :path-params :id)]])]
;;        [:div
;;         (with-styles {:margin-left "40px" :margin-right "40px"})
;;         ;;[:h1 "Page Panel"]
;;         (if (:node/title @node)
;;           [node-page @node]
;;           ;;[block-page (:block/uid @node)]
;;           )]))))

(defn page-el
  [])

(defn page-component
  [])

(defn block-page-el
  [])

(defn block-page-component
  [])

(defn node-page-el [node]
  (let [{:block/keys [children]
         :node/keys  [title]} node]
    [:div
     ;;(when (get @merge :active false)
     ;;  [merge-prompt @merge])
     [title-comp title]
     [:div
      (for [child children]
        (let [{:keys [block/uid]} child]
          ^{:key uid}
          [block-component uid])
        )]
     [:div
      [:h3 "Linked References"]
      ;;[:div
      ;; (for [id (reduce into [] @linked-refs)]
      ;;   ^{:key id}
      ;;   [:div {:style {:background-color "lightblue" :margin "15px 0px" :padding 5}}
      ;;    [block-page id]])]
      ]
     [:div
      [:h3 "Unlinked References"]
      ;;[:div
      ;; (for [id (reduce into [] @unlinked-refs)]
      ;;   ^{:key id}
      ;;   [:div {:style {:background-color "lightblue" :margin "15px 0px" :padding 5}}
      ;;    [block-page id]])]]
      ]])
  )

(defn node-page-component
  [uid]
  (fn []
    (let [
          node (pull conn
                 '[* {:block/children [:block/uid]}]
                 [:block/uid uid])
          ;;linked-refs   (subscribe [:node/refs (patterns/linked   (:node/title node))])
          ;;unlinked-refs (subscribe [:node/refs (patterns/unlinked (:node/title node))])
          ;;merge         (subscribe [:merge-prompt])
          ]
      [node-page-el @node]
      )))


(defcard-rg Node-Page
  "Pulling [:block/uid \"uid1\"] "
  [node-page-component "uid1"])


;;  TODO: Will be broken as long as we are using `rfee/href` to link to pages."