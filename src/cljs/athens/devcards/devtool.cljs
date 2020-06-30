(ns athens.devcards.devtool
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db :refer [dsdb]]
    [athens.devcards.buttons :refer [button-primary button]]
    [athens.devcards.db :refer [load-real-db-button]]
    [athens.devcards.textinput :refer [textinput-style]]
    [athens.style :refer [color]]
    [cljs.pprint :as pp]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.core.protocols :as core-p]
    [clojure.datafy :refer [nav datafy]]
    [datascript.core :as d]
    [datascript.db]
    [devcards.core :as devcards :refer [defcard-rg]]
    [garden.color :refer [darken]]
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


(def container-style
  {:grid-area     "devtool"
   :flex-direction "column"
   :background    (color :panel-color)
   :position      "relative"
   :width         "100vw"
   :height        "33vh"
   :display "flex"
   :overflow-y    "auto"
   :right         0
   :z-index       2})


(def tabs-style
  {:padding "0 8px"
   :flex "0 0 auto"
   :background (darken (color :panel-color) 5)
   :display "flex"
   :align-items "stretch"
   :justify-content "space-between"
   ::stylefy/manual [[:button {:border-radius "0"}]]})


(def tabs-section-style
  {:display "flex"
   :align-items "stretch"})


(def panels-style
  {:overflow-y "auto"
   :padding "8px"})


(def current-location-style
  {:display "flex"
   :align-items "center"
   :flex "1 1 100%"
   :font-size "14px"
   :border-bottom [["1px solid" (darken (color :panel-color) 10)]]})


(def current-location-name-style
  {:font-weight "bold"
   :font-size "inherit"
   :margin-block "0"
   :margin-inline-start "1em"
   :margin-inline-end "1em"})


(def current-location-controls-style {:margin-inline-start "1em"})


(def devtool-table-style
  {:border-collapse "collapse"
   :font-size "12px"
   :font-family "IBM Plex Sans Condensed"
   :letter-spacing "-0.01em"
   :margin "8px 0 0"
   :border-spacing "0"
   :min-width "100%"
   ::stylefy/manual [[:td {:border-top [["1px solid " (darken (color :panel-color) 5)]]
                           :padding "2px"}]
                     [:tbody {:vertical-align "top"}]
                     [:th {:text-align "left" :padding "2px 2px" :white-space "nowrap"}]
                     [:tr {:transition "all 0.05s ease"}]
                     [:td:first-child :th:first-child {:padding-left "8px"}]
                     [:td:last-child :th-last-child {:padding-right "8px"}]
                     [:tbody [:tr:hover {:cursor "pointer"
                                         :background (darken (color :panel-color) 2.5)
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


(def edn-viewer-style {:font-size "12px"})


(def query-input-style
  (merge textinput-style {:width "100%"
                          :min-height "40px"
                          :font-size "12px"
                          :background (color :app-bg-color)
                          :font-family "IBM Plex Mono"}))


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
      [:div
       [:table (use-style devtool-table-style)
        [:thead
         [:tr (for [h headers]
                ^{:key h} [:th h])]]
        [:tbody
         (doall
           (for [row (take @limit rows)]

             ^{:key row}
             [:tr {:on-click #(add-nav! [(first row)
                                         (-> row meta :row-value)])}
              (for [i (range (count row))]
                (let [cell (get row i)]
                  ^{:key (str row i cell)}
                  [:td (if (nil? cell)
                         ""
                         (pr-str cell))]))]))]] ; use the edn-viewer here as well?
       (when (< @limit (count rows))
         [button-primary {:on-click-fn #(swap! limit + 10)
                          :style {:width "100%"
                                  :justify-content "center"
                                  :margin "4px 0"}
                          :label "Load More"}])])))


; TODO add truncation of long strings here
(defn edn-viewer
  [data _]
  [:pre (use-style edn-viewer-style) [:code (with-out-str (cljs.pprint/pprint data))]])


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
                        :align-items "stretch"
                        :justify-content "space-between"}}
          [:div (use-style current-location-style)
           (doall
             (for [i (-> navs count range)]
               (let [nav (get navs i)]
                 ^{:key i}
                 [button {:label [:<> [:> mui-icons/ChevronLeft] [:span (first nav)]]
                          :style {:padding "2px 4px"}
                          :on-click-fn #(swap! state (fn [s]
                                                       (-> s
                                                           (update :navs subvec 0 i)
                                                           (dissoc :viewer))))}])))
           [:h3 (use-style current-location-name-style) (pr-str (type navved-data))]
           [:div (use-style current-location-controls-style)
            [:span "View as "]
            (for [v applicable-vs]
              (let [click-fn #(swap! state assoc :viewer v)]
                ^{:key v}
                [button {:on-click-fn click-fn
                         :active (= v viewer-name)
                         :label (name v)}]))]]]
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
   [autosize/textarea (use-style query-input-style
                                 {:value eval-str
                                  :resize "none"
                                  :on-change handle-box-change!
                                  :on-key-down handle-box-key-down!})]
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


(defn devtool-close-el
  []
  [button {:on-click-fn #(dispatch [:toggle-devtool])
           :label [:> mui-icons/Clear]}])


(defn devtool-el
  [devtool? state]
  (when devtool?
    (let [{:keys [active-panel]} @state
          switch-panel (fn [panel] (swap! state assoc :active-panel panel))]
      [:div (use-style container-style)
       [:nav (use-style tabs-style)
        [:div (use-style tabs-section-style)
         [button {:on-click-fn #(switch-panel :query)
                  :active (true? (= active-panel :query))
                  :label [:<> [:> mui-icons/ShortText] [:span "Query"]]}]
         [button {:on-click-fn #(switch-panel :txes)
                  :active (true? (= active-panel :txes))
                  :label [:<> [:> mui-icons/History] [:span "Transactions"]]}]]
        [devtool-close-el]]
       [:div (use-style panels-style)
        (case active-panel
          :query [query-component @state]
          :txes [txes-component @state])]])))


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
