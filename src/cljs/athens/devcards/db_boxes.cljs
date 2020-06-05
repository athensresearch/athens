(ns athens.devcards.db-boxes
  (:require
    [athens.db :as db]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [datascript.core :as d]
    [devcards.core :as devcards :refer [defcard defcard-rg]]
    [garden.core :refer [css]]
    [reagent.core :as r]
    [sci.core :as sci])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


(def key-code->key
  {8   :backspace
   9   :tab
   13  :return})


(def log js/console.log)


(defn trace
  [x]
  (log x) x)


(defcard "
  # An experiment in browsing the datascript database")


(def initial-box
  {:str-content
   "(d/q '[:find [(pull ?e [*]) ...]
       :where [?e :node/title]]
    @athens/db)"})


(defonce box-state*
  (r/atom initial-box))


(defn eval-box
  [{:keys [str-content] :as box}]
  (let [bindings {'athens/db db/dsdb
                  'd/q d/q
                  'd/pull d/pull
                  'd/pull-many d/pull-many}
        [ok? result] (try
                       [true (sci/eval-string str-content {:bindings bindings})]
                       (catch js/Error e [false e]))]
    (-> box
        (assoc :result result)
        (assoc :error (not ok?)))))


(defn eval-box!
  []
  (swap! box-state* eval-box))


(defn update-box!
  [s]
  (swap! box-state* assoc :str-content s))


(defn update-and-eval-box!
  [s]
  (swap! box-state*
         #(-> %
              (assoc :str-content s)
              (eval-box))))


(defn load-real-db!
  [conn]
  (go
    (let [res (<! (http/get db/athens-url {:with-credentials? false}))
          {:keys [success body]} res]
      (if success
        (do (d/transact! conn (db/str-to-db-tx body))
            (swap! box-state* eval-box))
        (js/alert "Failed to retrieve data from GitHub")))))


(defn load-real-db-button
  [conn]
  (let [pressed? (r/atom false)
        handler (fn []
                  (swap! pressed? not)
                  (load-real-db! conn))]
    (fn []
      [:button {:disabled @pressed? :on-click handler} "Load Real Data"])))


(defcard-rg Load-Real-DB
  "Downloads the ego db. Takes a few seconds."
  [load-real-db-button db/dsdb])


(defcard-rg Modify-Devcards
  "Increase width to 90% for table"
  [:style (css [:.com-rigsomelight-devcards-container {:width "90%"}])])


(defn headings
  [data mode]
  (case mode
    :coll ["idx" "val"]
    :map ["key" "val"]
    :tuples (into ["idx"] (->> data
                               (map count)
                               (apply max)
                               range))
    :maps (into ["idx"] (->> data
                             (mapcat keys)
                             (distinct)))))


(defn coll-rows
  [coll]
  (let [row (fn [[idx value]]
              [{:value idx
                :heading "idx"
                :idx idx}
               {:value value
                :heading "val"
                :idx idx}])]
    (->> coll
         (map-indexed vector)
         (map row))))


(defn map-rows
  [m]
  (let [row (fn [[k v]]
              [{:value k
                :heading "key"
                :idx k}
               {:value v
                :attr k
                :heading "val"
                :idx k}])]
    (map row m)))


; still not very clean
(defn tuple-rows
  [tuples]
  (let [row (fn [[idx values]]
              (into
                [{:value idx
                  :heading "idx"
                  :idx idx}]
                (map-indexed
                  (fn [heading value]
                    {:value value
                     :heading (str heading)
                     :idx idx})
                  values)))]
    (->> tuples
         (map-indexed vector)
         (map row))))


(defn maps-rows
  [ms]
  (let [hs (headings ms :maps)]
    (for [idx (-> ms count range)]
      (into [{:value idx
              :heading "idx"
              :idx idx}]
            (for [h (rest hs)]
              {:value (get-in ms [idx h])
               :attr h
               :heading (str h)
               :idx idx})))))


(defn rows
  [data mode]
  (case mode
    :coll (coll-rows data)
    :map (map-rows data)
    :tuples (tuple-rows data)
    :maps (maps-rows data)))


;; When mode is :map or :maps we can look at the keys to determine the operation
;; When mode is :tuples, :coll or :else we might have to analyze the query


(defn attr-unique?
  [attr]
  (contains? (get db/schema attr) :db/unique))


(defn attr-many?
  [attr]
  (= (get-in db/schema [attr :db/cardinality])
     :db.cardinality/many))


(defn attr-ref?
  [attr]
  (= (get-in db/schema [attr :db/valueType])
     :db.type/ref))


(defn pull-entity-str
  ([id]
   (str "(d/pull @athens/db '[*] " id ")"))
  ([attr id]
   (str "(d/pull @athens/db '[*] [" attr " " (pr-str id) "])")))


(defn cell
  [{:keys [value attr id]}]
  (if value
    (cond
      (= :db/id attr)
      [:a {:on-click #(update-and-eval-box! (pull-entity-str (or id value)))
           :style {:cursor :pointer}}
       (str value)]

      (attr-unique? attr)
      [:a {:on-click #(update-and-eval-box! (pull-entity-str attr value))
           :style {:cursor :pointer}}
       (str value)]

      (and (attr-many? attr)
           (attr-ref? attr))
      [:ul (for [v value]
             ^{:key v}
             [:li (cell {:value v
                         :attr :db/id
                         :id (:db/id v)})])]

      (attr-many? attr)
      [:ul (for [v value]
             ^{:key v}
             [:li (cell {:value v})])]

      :else
      (str value))
    ""))


(defn table-view
  [data mode]
  (let [hs (headings data mode)]
    [:table
     [:thead
      [:tr (for [h hs]
             ^{:key (str "heading-" h)}
             [:th (str h)])]]
     [:tbody
      (for [row (rows data mode)]
        ^{:key (str "row-" (-> row first :idx))}
        [:tr (for [{:keys [idx heading] :as c} row]
               ^{:key (str idx heading)}
               [:td (cell c)])])]]))


(defn coll-of-maps?
  [x]
  (and (coll? x)
       (every? map? x)))


(defn tuples?
  [x]
  (and (coll? x)
       (every? coll? x)))


(defn browser-component
  [result]
  [:div (cond

          (coll-of-maps? result)
          (table-view result :maps)

          (map? result)
          (table-view result :map)

          (tuples? result)
          (table-view result :tuples)

          (coll? result)
          (table-view result :coll)

          :else
          (str result))])


(defn error-component
  [error]
  [:div {:style {:color "red"}}
   (str error)])


(defn handle-box-change!
  [e]
  (update-box! (-> e .-target .-value)))


(defn handle-return-key!
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
    ;(set! (.-value t) (insert-tab v pos))
    (set! (.-selectionEnd t) (+ 2 pos))))


(defn handle-box-key-down!
  [e]
  (let [key-code (.-keyCode e)
        shift? (.-shiftKey e)
        k (key-code->key key-code)]
    (case k
      :return (when shift?
                (handle-return-key! e))
      :tab (handle-tab-key! e)
      nil)))


(defn box-component
  [box-state _]
  (let [{:keys [str-content result error]} @box-state]
    [:div
     [:textarea {:value str-content
                 :on-change handle-box-change!
                 :on-key-down handle-box-key-down!
                 :style {:width "100%"
                         :min-height "150px"
                         :resize :none}}]
     (if-not error
       (browser-component result)
       (error-component result))]))


(defcard-rg Reset-to-all-pages
  (fn []
    [:button {:on-click #(update-and-eval-box! (:str-content initial-box))}
     "Reset"]))


(defcard-rg Browse-db-box
  box-component
  box-state*
  {:history true})
