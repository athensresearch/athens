(ns athens.views.node-page
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.parse-renderer :as parse-renderer]
    [athens.patterns :as patterns]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color]]
    [athens.views.blocks :refer [block-el]]
    [athens.views.breadcrumbs :refer [breadcrumbs-list breadcrumb]]
    [athens.views.buttons :refer [button]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as string]
    [garden.selectors :as selectors]
    [goog.functions :refer [debounce]]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [#_pull q]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]
    [tick.alpha.api :as t]))


;;; Styles


(def page-style
  {:margin "2rem auto"
   :padding "1rem 2rem"
   :flex-basis "100%"
   :max-width "55rem"})


(def title-style
  {:position "relative"
   :overflow "visible"
   :flex-grow "1"
   :margin "0.2em 0 0.2em 1rem"
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
                                    :z-index 3
                                    :display "block"
                                    :opacity "1"}]
                     [(selectors/+ :.is-editing :span) {:opacity 0}]]})


(def references-style {:margin-block "3em"})


(def references-heading-style
  {:font-weight "normal"
   :display "flex"
   :padding "0 2rem"
   :align-items "center"
   ::stylefy/manual [[:svg {:margin-right "0.25em"
                            :font-size "1rem"}]
                     [:span {:flex "1 1 100%"}]]})


(def references-list-style
  {:font-size "14px"})


(def references-group-title-style
  {:color (color :link-color)
   :margin "0 1.5rem"
   :font-weight "500"
   ::stylefy/manual [[:a:hover {:cursor "pointer"
                                :text-decoration "underline"}]]})


(def references-group-style
  {:background (color :background-minus-2 :opacity-med)
   :padding "1rem 0.5rem"
   :border-radius "4px"
   :margin "0.5em 0"})


(def reference-breadcrumbs-style
  {:font-size "12px"
   :padding "0.25rem calc(2rem - 0.5em)"})


(def references-group-block-style
  {:border-top [["1px solid " (color :border-color)]]
   :padding-block-start "1em"
   :margin-block-start "1em"
   ::stylefy/manual [[:&:first-of-type {:border-top "0"
                                        :margin-block-start "0"}]]})


;;; Helpers


(defn handler
  [val uid]
  (dispatch [:transact [[:db/add [:block/uid uid] :node/title val]]]))


(def db-handler (debounce handler 500))


(defn get-ref-ids
  [pattern]
  @(q '[:find [?e ...]
        :in $ ?regex
        :where
        [?e :block/string ?s]
        [(re-find ?regex ?s)]]
      db/dsdb
      pattern))


(defn merge-parents-and-block
  [ref-ids]
  (let [parents (reduce-kv (fn [m _ v] (assoc m v (db/get-parents-recursively v)))
                           {}
                           ref-ids)
        blocks (map (fn [id] (db/get-block-document id)) ref-ids)]
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


(defn is-timeline-page
  [uid]
  (boolean
    (try
      (let [[m d y] (string/split uid "-")]
        (t/date (string/join "-" [y m d])))
      (catch js/Object _ false))))


;;; Components


;; TODO: where to put page-level link filters?
(defn node-page-el
  [{:block/keys [children uid] title :node/title} editing-uid ref-groups timeline-page?]

  [:div (use-style page-style)

   ;; TODO: implement timeline
   ;;(when timeline-page?
   ;;  [button {:on-click-fn #(dispatch [:jump-to-timeline uid])
   ;;           :label [:<>
   ;;                   [:mui-icons Left]
   ;;                   [:span "Timeline"]]}])

   ;; Header
   [:h1 (use-style title-style {:data-uid uid :class "page-header"})
    (when-not timeline-page?
      [autosize/textarea
       {:default-value title
        :class      (when (= editing-uid uid) "is-editing")
        :auto-focus true
        :on-change  (fn [e] (db-handler (.. e -target -value) uid))}])
    (parse-renderer/parse-and-render title)]

   ;; Children
   [:div
    (for [{:block/keys [uid] :as child} children]
      ^{:key uid}
      [block-el child])]

   ;; References
   (doall
     (for [[linked-or-unlinked refs] ref-groups]
       (when (not-empty refs)
         [:section (use-style references-style {:key linked-or-unlinked})
          [:h4 (use-style references-heading-style)
           [(r/adapt-react-class mui-icons/Link)]
           [:span linked-or-unlinked]
           [button {:label    [(r/adapt-react-class mui-icons/FilterList)]
                    :disabled true}]]
          [:div (use-style references-list-style)
           (doall
             (for [[group-title group] refs]
               [:div (use-style references-group-style {:key (str "group-" group-title)})
                [:h4 (use-style references-group-title-style)
                 [:a {:on-click #(navigate-uid uid)} group-title]] ;; FIXME: use correct uid
                (doall
                  (for [{:block/keys [uid parents] :as block} group]
                    [:div (use-style references-group-block-style {:key (str "ref-" uid)})
                ;; TODO: expand parent on click
                     [block-el block]
                     (when (> (count parents) 1)
                       [breadcrumbs-list {:style reference-breadcrumbs-style}
                        [(r/adapt-react-class mui-icons/LocationOn)]
                        (doall
                          (for [{:keys [node/title block/string block/uid]} parents]
                            [breadcrumb {:key (str "breadcrumb-" uid) :on-click #(navigate-uid uid)} (or title string)]))])]))]))]])))])


(defn node-page-component
  "One diff between datascript and posh: we don't have pull in q for posh
  https://github.com/mpdairy/posh/issues/21"
  [ident]
  (let [{:keys [block/uid node/title] :as node} (db/get-node-document ident)
        editing-uid @(subscribe [:editing/uid])
        timeline-page? (is-timeline-page uid)]
    (when-not (string/blank? title)
      ;; TODO: turn ref-groups into an atom, let users toggle open/close
      (let [ref-groups [["Linked References" (-> title patterns/linked get-data)]
                        ["Unlinked References" (-> title patterns/unlinked get-data)]]]
        [node-page-el node editing-uid ref-groups timeline-page?]))))
