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
    [posh.reagent :refer [transact! pull pull-many q]]
    [reagent.core :as r]
    [athens.patterns :as patterns]))


(defcard-rg Import-Styles
  [style-guide-css])


(defcard Instantiate-Dsdb)
(defonce conn (new-conn))
(posh-conn! conn)


(defcard-rg Create-Datoms
  (let [datoms [{:db/id 4291,
                 :block/uid "0MtCtwFh0",
                 :create/email "tangj1122@gmail.com",
                 :create/time 1587924500189,
                 :edit/email "tangj1122@gmail.com",
                 :edit/time 1587924500192,
                 :node/title "Datomic"}
                {:block/string "[[[[Datomic]]: [[Event Sourcing]] without the hassle]]",
                 :create/email "tangj1122@gmail.com",
                 :create/time 1587823705202,
                 :block/refs 4292,
                 :block/uid "WNkp8_jEO",
                 :block/open true,
                 :edit/time 1587999528005,
                 :db/id 4138,
                 :edit/email "tangj1122@gmail.com",
                 :block/order 0}
                {:block/string "[[[[Datomic]]: [[Event Sourcing]] without the hassle]]",
                 :create/email "tangj1122@gmail.com",
                 :create/time 1587924394698,
                 :block/refs 4292,
                 :block/uid "-ejAtqgis",
                 :block/open true,
                 :edit/time 1587924500188,
                 :db/id 4286,
                 :edit/email "tangj1122@gmail.com",
                 :block/order 1}
                {:block/string "there is no hard limit, but don't put 100 billion datoms in Datomic",
                 :create/email "tangj1122@gmail.com",
                 :create/time 1587680559303,
                 :block/uid "6S4eVeXo8",
                 :block/open true,
                 :edit/time 1587680599692,
                 :db/id 3673,
                 :edit/email "tangj1122@gmail.com",
                 :block/order 1}
                {:block/string "**there are only 4 people doing all of the dev on Datomic, Clojure**, etc combined so we are a tiny team of very experienced people using high leverage tools. I'm not sure this is directly relevant to most software teams in general (but Clojure projects do probably tend to be more that, and less big teams)",
                 :create/email "tangj1122@gmail.com",
                 :create/time 1588171319980,
                 :block/uid "zm_Ft2Iim",
                 :block/open true,
                 :edit/time 1588171339086,
                 :db/id 5008,
                 :edit/email "tangj1122@gmail.com",
                 :block/order 3}
                {:db/id          1
                 :block/uid      "uid1",
                 :node/title     "top-level page"
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
      [:span (parse-and-render string)]]                                       ;; parse-and-render will break because it uses rfee/href
     (when open?
       (for [ch (:block/children block)]
         (let [uid (:block/uid ch)]
           [:div {:style {:margin-left 28} :key uid}
            [block-component [:block/uid uid]]])))]))


(defn block-component [ident]
  "This query is long because I'm not sure how to recursively find all child blocks with all attributes
  '[* {:block/children [*]}] doesn't work


Also, why does datascript return a reaction of {:db/id nil} when pulling for [:block/uid uid]?
no results for q returns nil
no results for pull eid returns nil
  "
  (fn []
    (let [block (pull conn '[:db/id :block/string :block/uid :block/children :block/open {:block/children ...}] ident)]
      (when (:db/id @block)
        [block-el @block]))))


(defcard-rg Block
  "[:block/uid \"uid1\"]"
  [block-component [:block/uid "uid1"]])


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

(defn node-page-el [node linked-refs unlinked-refs]
  (let [{:block/keys [children] :node/keys [title]} node]
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
     ;; {:background-color "lightblue" :margin "15px 0px" :padding 5}
     [:div
      [:h4 "Linked References"]
      (for [ref linked-refs]
        ^{:key ref} [:p ref])]
     [:div
      [:h4 "Unlinked References"]
      (for [ref unlinked-refs]
        ^{:key ref} [:p ref])]]))


(def q-refs
  '[:find [?e ...]
    :in $ ?regex
    :where
    [?e :block/string ?s]
    [(re-find ?regex ?s)]])


(defn node-page-component
  "One diff between datascript and posh: we don't have pull in q for posh
  https://github.com/mpdairy/posh/issues/21"
  [ident]
  (fn []
    (let [node          (pull conn '["*" {:block/children [:block/uid]}] ident)
          title         (:node/title @node)
          linked-refs   (q q-refs conn (patterns/linked title))
          unlinked-refs (q q-refs conn (patterns/unlinked title))
          ;;merge         (subscribe [:merge-prompt]) ;; merge with alert?
          ]
      [node-page-el @node @linked-refs @unlinked-refs])))

;; TODO we shouldn't query for (un)linked refs if the query fails
;; but it should never fail?

(defcard-rg Node-Page
  "Datomic: pull [:block/uid \"0MtCtwFh0\"]"
  [node-page-component [:block/uid "0MtCtwFh0"]])

(defn shape-parent-query
  "Find path from nested block to origin node.
  Again, don't understand why query returns {:db/id nil} if no query. Why not just nil?"
  [pull-results]
  (if-not (:db/id pull-results)
    (vector)
    (->> (loop [b   pull-results
                res []]
           (if (:node/title b)
             (conj res b)
             (recur (first (:block/_children b))
               (conj res (dissoc b :block/_children)))))
      (rest)
      (reverse))))


(defn block-page-el
  [block parents]
  (let [{:block/keys [string children]} block]
    [:div
     [:span {:style {:color "gray"}}
      (interpose                                            ;; create an interpose function that can take [:> non-string elem]
        " > "
        (for [p parents]
          (let [{:keys [node/title block/uid block/string]} p]
            [:span {:key uid :style {:cursor "pointer"} :on-click #(navigate-page uid)} (or string title)])))
      ]
     [:h2 (str "• " string)]
     [:div
      (for [child children]
        (let [{:keys [db/id]} child]
          ^{:key id} [block-component id])
        )]
     ]))

(defn block-page-component
  "two queries: block+children and parents"
  [ident]
  (let [block @(pull conn '[:db/id :block/uid :block/string {:block/children ...}] ident)
        parents (->> @(pull conn '[:db/id :node/title :block/uid :block/string {:block/_children ...}] ident)
                  (shape-parent-query))]
    (block-page-el block parents)))


(defcard-rg Block-Page-Top-Level
  [block-page-component [:block/uid "uid5"]])



(defn page-el
  [])

(defn page-component
  [])

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

;;  TODO: Will be broken as long as we are using `rfee/href` to link to pages."