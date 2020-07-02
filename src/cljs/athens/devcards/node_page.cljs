(ns athens.devcards.node-page
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.devcards.blocks :refer [block-el]]
    [athens.devcards.buttons :refer [button]]
    [athens.patterns :as patterns]
    [athens.style :refer [color]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as string]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [goog.functions :refer [debounce]]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [pull q]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles

(def title-style
  {:position "relative"
   :overflow "visible"
   :flex-grow "1"
   :margin "0.2em 0"
   :letter-spacing "-0.03em"
   :word-break "break-word"
   ::stylefy/manual [[:textarea {:display "none"}]
                     [:&:hover [:textarea {:display "block"
                                           :z-index 1}]]
                     [:textarea {:-webkit-appearance "none"
                                 :cursor "text"
                                 :resize "none"
                                 :transform "translate3d(0,0,0)"
                                 :color "inherit"
                                 :font-weight "inherit"
                                 :padding "0"
                                 :letter-spacing "inherit"
                                 :position "absolute"
                                 :top "0"
                                 :left "0"
                                 :right "0"
                                 :width "100%"
                                 :min-height "100%"
                                 :caret-color (color :link-color)
                                 :background "transparent"
                                 :margin "0"
                                 :font-size "inherit"
                                 :line-height "inherit"
                                 :border-radius "4px"
                                 :transition "opacity 0.15s ease"
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:textarea:focus
                      :.is-editing {:outline "none"
                                    :z-index "10"
                                    :display "block"
                                    :opacity "1"}]
                     [(selectors/+ :.is-editing :span) {:opacity 0}]]})


;;; Helpers


(defn handler
  [val uid]
  (dispatch [:transact-event [[:db/add [:block/uid uid] :node/title val]]]))


(def db-handler (debounce handler 500))


(defn get-ref-ids
  [pattern]
  (q '[:find [?e ...]
       :in $ ?regex
       :where
       [?e :block/string ?s]
       [(re-find ?regex ?s)]]
     db/dsdb
     pattern))


(defn get-block
  [id]
  @(pull db/dsdb db/block-pull-pattern id))


(defn get-parents
  [id]
  (->> @(pull db/dsdb db/parents-pull-pattern id)
       db/shape-parent-query))


(defn merge-parents-and-block
  [ref-ids]
  (let [parents (reduce-kv (fn [m _ v] (assoc m v (get-parents v)))
                           {}
                           ref-ids)
        blocks (map (fn [id] (get-block id)) ref-ids)]
    (mapv
      (fn [block]
        (merge block {:block/parents (get parents (:db/id block))}))
      blocks)))


(defn group-by-parent
  [blocks]
  (group-by (fn [x]
              (-> x
                  :block/parents
                  first
                  :node/title))
            blocks))


(defn get-data
  [pattern]
  (-> pattern get-ref-ids merge-parents-and-block group-by-parent seq))


;;; Components


;; TODO: where to put page-level link filters?
(defn node-page-el
  [{:block/keys [children uid] title :node/title} editing-uid ref-groups]

  [:div

   ;; Header
   [:h1 (use-style title-style {:data-uid uid :class "page-header"})
    [autosize/textarea
     {:default-value title
      :class      (when (= editing-uid uid) "is-editing")
      :auto-focus true
      :on-change  (fn [e] (db-handler (.. e -target -value) uid))}]
    [:span title]]

   ;; Children
   [:div
    (for [{:block/keys [uid] :as child} children]
      ^{:key uid}
      [block-el child])]

   ;; References
   (for [[linked-or-unlinked refs] ref-groups]
     [:div {:key linked-or-unlinked}
      [:div (use-style {:display         "flex"
                        :justify-content "space-between"
                        :align-items "center"})
       [:h3 linked-or-unlinked]
       [:span
        [button {:label    [(r/adapt-react-class mui-icons/FilterList)]
                 :disabled true}]]]
      (doall
        (for [[group-title group] refs]
          [:<> {:key group-title}
           [:h4 group-title]
           (for [{:block/keys [uid parents] :as block} group]
             [:div {:key uid}
              ;; TODO: replace with breadcrumbs?
              ;; TODO: expand parent on click
              (->> (for [{:keys [node/title block/string block/uid]} parents]
                     [:span (use-style {:color "gray"} {:key uid}) (or title string)])
                   (interpose ">")
                   (map (fn [x]
                          (if (= x ">")
                            [(r/adapt-react-class mui-icons/KeyboardArrowRight) (use-style {:vertical-align "middle"})]
                            x))))
              [block-el block]])]))])])


(defn node-page-component
  "One diff between datascript and posh: we don't have pull in q for posh
  https://github.com/mpdairy/posh/issues/21"
  [ident]
  (let [node (->> @(pull db/dsdb db/node-pull-pattern ident) (db/sort-block))
        title (:node/title node)
        editing-uid @(subscribe [:editing-uid])]
    (when-not (string/blank? title)
      ;; TODO: turn ref-groups into an atom, let users toggle open/close
      (let [ref-groups [["Linked References" (-> title patterns/linked get-data)]
                        ["Unlinked References" (-> title patterns/unlinked get-data)]]]
        [node-page-el node editing-uid ref-groups]))))


;;; Devcards


(defcard-rg Node-Page
  "pull entity 4093: \"Hyperlink\" page"
  [node-page-component 4093])
