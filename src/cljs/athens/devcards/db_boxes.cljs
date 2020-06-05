(ns athens.devcards.db-boxes
  (:require
    [athens.db :as db]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [datascript.core :as d]
    [devcards.core :as devcards :refer [defcard defcard-rg]]
    [garden.core :refer [css]]
    [reagent.core :as r]
    [sci.core :as sci])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


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


; there is code here that makes me weep. have to refactor this
(defn rows
  [data mode]
  (case mode
    :coll (->> data
               (map-indexed vector)
               (map (fn [x] {:item x})))

    :map (->> data
              (map (fn [[k v]] [{:item k} {:item v :attr k}])))

    :tuples (->> data
                 (map-indexed (fn [idx v]
                                (into [{:item idx}]
                                      (map (fn [x] {:item x}) v)))))

    :maps (let [hs (headings data mode)]
            (for [row-idx (-> data count range)]
              (into [{:item row-idx}]
                    (for [h (rest hs)]
                      (when-let [item (get-in data [row-idx h])]
                        {:item item
                         :attr h})))))))


;; When mode is :map or :maps we can look at the keys to determine the operation
;; When mode is :tuples, :coll or :else we might have to analyze the query


(do db/schema)


(defn update-box!
  [s]
  (swap! box-state*
         #(-> %
              (assoc :str-content s)
              (eval-box))))


(defn pull-entity-str
  [id]
  (str "(d/pull @athens/db '[*] " id ")"))


(defn cell
  [{:keys [item attr]}]
  (if (= :db/id attr)
    [:a {:on-click #(update-box! (pull-entity-str item))}
     (str item)]
    (str item)))


(defn table-view
  [data mode]
  (let [hs (headings data mode)]
    [:table
     [:tr (for [h hs] [:th (str h)])]
     (for [row (rows data mode)]
       [:tr (for [item row]
              (if item
                [:td (cell item)]
                [:td ""]))])]))


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
  (let [value (-> e .-target .-value)]
    (update-box! value)))


(defn box-component
  [box-state _]
  (let [{:keys [str-content result error]} @box-state]
      [:div
       [:textarea {:value str-content
                   :on-change handle-box-change!
                   :style {:width "100%"
                           :min-height "150px"
                           :resize :none}}]
       (if-not error
         (browser-component result)
         (error-component result))]))


(defcard-rg Browse-db-box
  box-component
  box-state*
  {:history true})
