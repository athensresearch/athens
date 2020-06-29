(ns athens.devcards.devtool
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db :refer [dsdb]]
    [athens.devcards.buttons :refer [button-primary button]]
    [athens.devcards.db :refer [load-real-db-button]]
    [athens.style :refer [color DEPTH-SHADOWS]]
    [cljs.pprint :as pp]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.core.protocols :as core-p]
    [clojure.datafy :refer [nav datafy]]
    [datascript.core :as d]
    [datascript.db]
    [devcards.core :as devcards :refer [defcard-rg]]
    [komponentit.autosize :as autosize]
    [me.tonsky.persistent-sorted-set]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [reagent.ratom]
    [sci.core :as sci]
    [shadow.remote.runtime.cljs.browser]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      KeyCodes)))


;;; Styles

(def tabs-style {:border-bottom [["1px solid" (color :body-text-color :opacity-lower)]]
                 :margin-bottom "8px"})

(def container-style
  {:padding       "4px"
   :grid-area     "devtool"
   :border-top    [["2px solid" (color :body-text-color :opacity-lower)]]
   :flex-direction "column"
   :background    (color :panel-color)
   :width         "100vw"
   :max-height    "30vh"
   :overflow-y    "auto"
   :bottom        "0"
   :right         0
   :z-index       2})


(def devtool-table-style
  {:border-collapse "collapse"
   :font-size "12px"
   :font-family "IBM Plex Sans Condensed"
   :letter-spacing "-0.01em"
   :margin "8px 0 0"
   :min-width "100%"
   ::stylefy/manual [[:td {:border-top (str "1px solid " (color :panel-color))
                           :padding "2px"}]
                     [:tbody {:vertical-align "top"}]
                     [:th {:text-align "left" :padding "2px 2px"}]
                     [:tr {:transition "all 0.05s ease"}]
                     [:td:first-child :th:first-child {:padding-left "8px"}]
                     [:td:last-child :th-last-child {:padding-right "8px"}]
                     [:tbody [:tr:hover {:background (color :panel-color 0.15)
                                         :color (color :header-text-color)}]]
                     [:td>ul {:padding "0"
                              :margin "0"
                              :list-style "none"}]
                     [:td [:li {:margin "0 0 4px"
                                :padding-top "4px";
                                :border-top (str "1px solid " (color :panel-color))}]]
                     [:td [:li:first-child {:border-top "none" :margin-top "0" :padding-top "0"}]]
                     [:a {:color (color :link-color)}]
                     [:a:hover {:text-decoration "underline"}]]})


;;; Components


(def initial-state
  {:eval-str
   "(d/q '[:find [(pull ?e [*]) ...]
       :where [?e :node/title]]
    @athens/db)"
   :tx-reports []
   :active-panel :query})


(defonce state* (r/atom initial-state))


(defn ds-nav-impl
  [_ k v]
  (condp = k
    :db/id (d/pull @dsdb '[* :block/_children] v) ; TODO add inverse refs here
    v)) ; TODO add unique idents here as well


(defn restore-db!
  [db]
  (d/reset-conn! dsdb db {:time-travel true}))


(extend-protocol core-p/Datafiable
  cljs.core/PersistentHashMap
  (datafy [this]
    (with-meta this {`core-p/nav ds-nav-impl}))
  cljs.core/PersistentArrayMap
  (datafy [this]
    (with-meta this {`core-p/nav ds-nav-impl}))
  datascript.db/TxReport
  (datafy [this]
    (into {} this))
  datascript.db/Datom
  (datafy [this]
    (vec this))
  datascript.db/DB
  (datafy [this]
    (into {} this))
  me.tonsky.persistent-sorted-set/BTSet
  (datafy [this]
    (vec this)))


(defn data-table
  [_ _ _]
  (let [limit (r/atom 20)]
    (fn [headers rows add-nav!]
      [:div (use-style devtool-table-style)
        [:table
         [:thead
          [:tr (for [h headers]
                 ^{:key h} [:th h])]]
         [:tbody
          (doall
            (for [row (take @limit rows)]

              ^{:key row}
              [:tr (use-style {:cursor "pointer"
                               ::stylefy/mode {:hover {:background-color "#EFEDEB"}}}
                              {:on-click #(add-nav! [(first row)
                                                     (-> row meta :row-value)])})
               (for [i (range (count row))]
                 (let [cell (get row i)]
                   ^{:key (str row i cell)}
                   [:td (if (nil? cell)
                          ""
                          (pr-str cell))]))]))]] ; use the edn-viewer here as well?
       (when (< @limit (count rows))
         [:a (use-style {:cursor "pointer"}
                        {:on-click #(swap! limit + 10)})
          "Load more"])])))


; TODO add truncation of long strings here
(defn edn-viewer
  [data _]
  [:pre [:code (with-out-str (cljs.pprint/pprint data))]])


(defn coll-viewer
  [coll add-nav!]
  [data-table ["idx" "value"]
   (->> coll
        (map-indexed (fn [idx item]
                       (with-meta [idx item] {:row-value item})))
        vec)
   add-nav!])


(defn map-viewer
  [m add-nav!]
  [data-table ["key" "value"]
   (map (fn [[k v]] (with-meta [k v] {:row-value v})) m)
   add-nav!])


(defn maps-viewer
  [ms add-nav!]
  (let [headers (into ["idx"] (->> ms (mapcat keys) distinct))
        rows (map-indexed (fn [idx m]
                            (with-meta (into [idx]
                                             (for [h (rest headers)] (get m h)))
                              {:row-value m}))
                          ms)]
    [data-table headers rows add-nav!]))


(defn tuples-viewer
  [colls add-nav!]
  (let [max-count (->> colls
                       (map count)
                       (apply max))
        headers (into ["idx"] (range max-count))
        rows (map-indexed (fn [idx coll]
                            (with-meta (into [idx]
                                             (for [i (range max-count)] (get coll i)))
                              {:row-value coll})
                            colls))]
    [data-table headers rows add-nav!]))


(defn associative-not-sequential?
  [x]
  (and (associative? x)
       (not (sequential? x))))


(defn sequence-of-maps?
  [x]
  (and (sequential? x)
       (every? map? x)))


(defn tuples?
  [x]
  (and (sequential? x)
       (every? sequential? x)))


(def viewers
  [{:athens.viewer/id :athens.browser/edn
    :athens.viewer/pred (constantly true)
    :athens.viewer/fn edn-viewer}
   {:athens.viewer/id :athens.browser/coll
    :athens.viewer/pred coll?
    :athens.viewer/fn coll-viewer}
   {:athens.viewer/id :athens.browser/map
    :athens.viewer/pred associative-not-sequential?
    :athens.viewer/fn map-viewer}
   {:athens.viewer/id :athens.browser/maps
    :athens.viewer/pred sequence-of-maps?
    :athens.viewer/fn maps-viewer}
   {:athens.viewer/id :athens.browser/tuples
    :athens.viewer/pred tuples?
    :athens.viewer/fn tuples-viewer}])


(def viewer-preference
  [:athens.browser/maps
   :athens.browser/map
   :athens.browser/tuples
   :athens.browser/coll
   :athens.browser/edn])


(defn applicable-viewers
  [data]
  (->> viewers
       (filter (fn [{:keys [athens.viewer/pred]}] (pred data)))
       (map :athens.viewer/id)
       (sort-by #(.indexOf viewer-preference %))))


(def indexed-viewers
  (->> viewers
       (map (juxt :athens.viewer/id identity))
       (into {})))


(defn data-browser
  [_]
  (let [state (r/atom {:navs []})]
    (fn [data]
      (let [navs (:navs @state)
            add-nav! #(swap! state update :navs conj %)
            navved-data (reduce (fn [d [k v]] (nav (datafy d) k v))
                                data
                                navs)
            datafied-data (datafy navved-data)
            applicable-vs (applicable-viewers datafied-data)
            viewer-name (or (:viewer @state) (first applicable-vs))
            viewer (get-in indexed-viewers [viewer-name :athens.viewer/fn])]
        [:div
         [:div {:style {:display "flex"
                        :flex-direction "row"
                        :flex-wrap "no-wrap"
                        :justify-content "space-between"}}
          [:div {:style {:display "flex"
                         :flex-direction "column"
                         :flex-wrap "no-wrap"}}
           (doall
             (for [i (-> navs count range)]
               (let [nav (get navs i)]
                 ^{:key i}
                 [:a (use-style {:cursor "pointer"
                                 ::stylefy/mode {:hover {:background-color "#EFEDEB"}}}
                                {:on-click #(swap! state (fn [s]
                                                           (-> s
                                                               (update :navs subvec 0 i)
                                                               (dissoc :viewer))))})
                  (str "<< " (first nav))])))]
          [:div (use-style {:display "flex"
                            :flex-direction "row"})
           "View as: "
           (for [v applicable-vs]
             (let [click-fn #(swap! state assoc :viewer v)]
               (if (= v viewer-name)
                 ^{:key v}
                 [button-primary {:on-click-fn click-fn
                                  :label (name v)}]
                 ^{:key v}
                 [button {:on-click-fn click-fn
                          :label (name v)}])))]]
         [:div (pr-str (type navved-data))]
         (when (d/db? navved-data)
           [button-primary {:on-click-fn #(restore-db! navved-data)
                            :label "Restore this db"}])
         [viewer datafied-data add-nav!]]))))


(defn handler
  []
  (let [n (inc (:max-eid @dsdb))
        n-child (inc n)]
    (d/transact! dsdb [{:node/title     (str "Test Page " n)
                        :block/uid      (str "uid-" n)
                        :block/children [{:block/string (str "Test Block" n-child) :block/uid (str "uid-" n-child)}]}])))


(defn eval-with-sci
  [{:keys [eval-str] :as state}]
  (let [bindings {'athens/db dsdb
                  'd/pull d/pull
                  'd/q d/q
                  'd/pull-many d/pull-many
                  'd/entity d/entity}
        [ok? result] (try
                       [true (sci/eval-string eval-str {:bindings bindings})]
                       (catch js/Error e [false e]))]
    (-> state
        (assoc :result result)
        (assoc :error (not ok?)))))


(defn eval-box!
  []
  (swap! state* eval-with-sci))


(defn update-box!
  [s]
  (swap! state* assoc :eval-str s))


(defn listener
  [tx-report]
  (swap! state* update :tx-reports conj tx-report)
  (when (not (:error @state*))
    (eval-box!)))


(d/listen! dsdb :devtool listener)


(defn handle-box-change!
  [e]
  (update-box! (-> e .-target .-value)))


(defn handle-shift-return!
  [e]
  (.preventDefault e)
  (eval-box!))


(defn insert-tab
  [s pos]
  (str (subs s 0 pos) "  " (subs s pos)))


(defn handle-tab-key!
  [e]
  (let [t (.-target e)
        v (.-value t)
        pos (.-selectionStart t)]
    (.preventDefault e)
    (update-box! (insert-tab v pos))
    (set! (.-selectionEnd t) (+ 2 pos))))


(defn handle-box-key-down!
  [e]
  (let [key (.. e -keyCode)
        shift? (.. e -shiftKey)]
    (cond
      (= key KeyCodes.ENTER) (when shift? (handle-shift-return! e))
      (= key KeyCodes.TAB) (handle-tab-key! e)
      :else nil)))


(defn error-component
  [error]
  [:div {:style {:color "red"}}
   (str error)])


(defn query-component
  [{:keys [eval-str result error]}]
  [:div (use-style {:height "100%"})
   [autosize/textarea {:value eval-str
                       :on-change handle-box-change!
                       :on-key-down handle-box-key-down!
                       :style {:width "100%"
                               :min-height "150px"
                               :resize :none
                               :font-size "12px"
                               :font-family "IBM Plex Mono"}}]
   (if-not error
     [data-browser result]
     [error-component result])])


(defn txes-component
  [{:keys [tx-reports]}]
  [data-browser tx-reports])


(defn devtool-prompt-el
  []
  [button-primary {:on-click-fn #(dispatch [:toggle-devtool])
                   :label [:<>
                           [:> mui-icons/Build]
                           [:span "Toggle devtool"]]
                   :style {:font-size "11px"}}])


(defn devtool-el
  [devtool? state]
  (when devtool?
    (let [{:keys [active-panel]} @state
          switch-panel (fn [panel] (swap! state assoc :active-panel panel))]
      [:div (use-style container-style)
       [:nav (use-style tabs-style)
        [button {:on-click-fn #(switch-panel :query)
                 :label "Query"}]
        " "
        [button {:on-click-fn #(switch-panel :txes)
                 :label "Transactions"}]]
       (case active-panel
         :query [query-component @state]
         :txes [txes-component @state])])))


(defn devtool-component
  []
  (let [devtool? @(subscribe [:devtool])]
    [devtool-el devtool? state*]))


(defcard-rg Load-Real-DB
  [load-real-db-button dsdb])


(defcard-rg Create-Page
  "Press button and then search \"test\" "
  [button-primary {:on-click-fn handler
                   :label "Create Test Pages and Blocks"}])


(defcard-rg Reset-to-all-pages
  (fn []
    [button {:on-click-fn #(do (swap! state* assoc :eval-str (:eval-str initial-state))
                               (eval-box!))
             :label "Reset"}]))


(defcard-rg Devtool-box
  [:<>
   [devtool-prompt-el]
   [devtool-component]])


(comment
  (tap> (deref state*))

  nil)
