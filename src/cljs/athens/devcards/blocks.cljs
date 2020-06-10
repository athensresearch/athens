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


(defn handler
  []
  (transact! conn [{:db/id 1
                    :block/uid    "uid1",
                    :block/open   true,
                    :block/string "first block"
                    :block/children [{:db/id 2
                                      :block/uid    "uid2",
                                      :block/open   true,
                                      :block/string "child block 2"
                                      :block/children [{:db/id 5
                                                        :block/uid    "uid5"
                                                        :block/open   false
                                                        :block/string "child block 5"}]}
                                     {:db/id 3
                                      :block/uid    "uid3"
                                      :block/open   false
                                      :block/string "child block 3"
                                      :block/children [{:db/id 4
                                                        :block/uid    "uid4"
                                                        :block/open   false
                                                        :block/string "child block 4"}]}]}]))


(defcard-rg Create-Page
  "Load some datoms"
  [:button.primary {:on-click handler} "Create Page"])


(defcard-rg Load-Real-DB
  [load-real-db-button conn])

(declare block-component)


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
      [:span string]]                                       ;; parse-and-render
     (when open?
       (for [ch (:block/children block)]
         (let [uid (:block/uid ch)]
           [:div {:style {:margin-left 28} :key uid}
            [block-component uid]])))]))


(defn block-component [uid]
  (fn []
    (let [block (pull conn
                  ;; This query is long because I'm not sure how to recursively find all child blocks with all attributes
                  ;; '[* {:block/children [*]}] doesn't work
                  '[:db/id :block/string :block/uid :block/children :block/open {:block/children ...}]
                  [:block/uid uid])]
      (when (:db/id @block)
        [block-el @block]))))

(defcard-rg Block
  [block-component "uid1"])