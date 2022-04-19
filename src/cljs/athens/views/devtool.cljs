(ns athens.views.devtool
  (:require
    ["/components/Icons/Icons" :refer [XmarkIcon ChevronLeftIcon]]
    ["@chakra-ui/react" :refer [Box IconButton Button Table Thead Tbody Th Tr Td Input ButtonGroup]]
    [athens.config :as config]
    [athens.db :as db :refer [dsdb]]
    [cljs.pprint :as pp]
    [clojure.core.protocols :as core-p]
    [clojure.datafy :refer [nav datafy]]
    [datascript.core :as d]
    [datascript.db]
    [me.tonsky.persistent-sorted-set]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [reagent.ratom]
    [sci.core :as sci])
  (:import
    (goog.events
      KeyCodes)))


;; Components


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
      [:> Box {:width "100%"}
       [:> Table {:width "100%"}
        [:> Thead
         [:> Tr (for [h headers]
                  ^{:key h} [:> Th h])]]
        [:> Tbody
         (doall
           (for [row (take @limit rows)]

             ^{:key row}
             [:> Tr {:on-click #(add-nav! [(first row)
                                           (-> row meta :row-value)])}
              (for [i (range (count row))]
                (let [cell (get row i)]
                  ^{:key (str row i cell)}
                  [:> Td (if (nil? cell)
                           ""
                           (pr-str cell))]))]))]] ; use the edn-viewer here as well?
       (when (< @limit (count rows))
         [:> Button {:onClick #(swap! limit + 10)
                     :width "100%"}
          "Load More"])])))


;; TODO add truncation of long strings here
(defn edn-viewer
  [data _]
  [:> Box {:as "pre" :fontSize "12px"} [:code (with-out-str (pp/pprint data))]])


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
        [:> Box
         [:> Box {:display "flex"
                  :flexDirection "row"
                  :flexWrap "no-wrap"
                  :alignItems "stretch"
                  :justifyContent "space-between"}
          [:> Box
           (doall
             (for [i (-> navs count range)]
               (let [nav (get navs i)]
                 ^{:key i}
                 [:> Button {:style {:padding "0.125rem 0.25rem"}
                             :on-click #(swap! state (fn [s]
                                                       (-> s
                                                           (update :navs subvec 0 i)
                                                           (dissoc :viewer))))}
                  [:<> [:> ChevronLeftIcon] [:span (first nav)]]])))
           [:h3 (pr-str (type navved-data))]
           [:div
            [:span "View as "]
            (for [v applicable-vs]
              (let [click-fn #(swap! state assoc :viewer v)]
                ^{:key v}
                [:> Button {:on-click click-fn
                            :is-pressed (= v viewer-name)}
                 (name v)]))]]]
         (when (d/db? navved-data)
           [:> Button {:on-click #(restore-db! navved-data)
                       :is-primary true}
            "Restore this db"])
         [viewer datafied-data add-nav!]]))))


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


;; Only run the listener in dev mode, not in prod. The listener slows things
;; down a lot. For example it makes the enter key take ~300ms rather than ~100ms,
;; according to the Chrome devtools flamegraph.
(when config/debug?
  (d/listen! dsdb :devtool/open listener))


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
  [:div {:style {:height "100%"}}
   [:> Input {:value eval-str
              :width "100%"
              :minHeight "2.5rem"
              :fontSize "12px"
              :background "background.basement"
              :fontFamily "code"
              :resize "none"
              :on-change handle-box-change!
              :on-key-down handle-box-key-down!}]
   (if-not error
     [data-browser result]
     [error-component result])])


(defn txes-component
  [{:keys [tx-reports]}]
  [data-browser tx-reports])


(defn devtool-close-el
  []
  [:> IconButton {:onClick #(dispatch [:devtool/toggle])}
   [:> XmarkIcon]])


(defn devtool-el
  [devtool? state]
  (when devtool?
    (let [{:keys [active-panel]} @state
          switch-panel (fn [panel] (swap! state assoc :active-panel panel))]
      [:> Box {:gridArea      "devtool"
               :flexDirection "column"
               :background    "background.basement"
               :position      "relative"
               :width         "100vw"
               :height        "33vh"
               :display       "flex"
               :overflowY     "auto"
               :right         0
               :zIndex        2}
       [:> ButtonGroup
        [:> Button {:onClick #(switch-panel :query)
                    :isActive (= active-panel :query)}
         "Query"]
        [:> Button {:onClick #(switch-panel :txes)
                    :mr "auto"
                    :isActive (= active-panel :txes)}
         "Transactions"]

        [devtool-close-el]]
       [:> Box {:overflowY "auto"
                :padding "0.5rem"}
        (case active-panel
          :query [query-component @state]
          :txes [txes-component @state])]])))


(defn devtool-component
  []
  (let [devtool? @(subscribe [:devtool/open])]
    [devtool-el devtool? state*]))
