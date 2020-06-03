(ns athens.devcards.db-boxes
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [datascript.core :as d]
    [devcards.core :as devcards :refer [defcard defcard-rg]]
    [reagent.core :as rg]
    [sci.core :as sci]
    [cljs-http.client :as http]
    [athens.db :as db]
    [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(def log js/console.log)


(defn trace
  [x]
  (log x) x)


(defcard "
  # An experiment in querying the datascript database")


(def schema
  {:block/uid      {:db/unique :db.unique/identity}
   :node/title     {:db/unique :db.unique/identity}
   :attrs/lookup   {:db/cardinality :db.cardinality/many}
   :block/children {:db/cardinality :db.cardinality/many
                    :db/valueType :db.type/ref}})


(defonce dsdb (d/create-conn schema))


(defonce loading? (atom true))


(when @loading?
  (go
    (let [response (<! (http/get db/athens-url
                                 {:with-credentials? false}))]
      (->> response
           :body
           db/str-to-db-tx
           (d/transact dsdb))

      (reset! loading? false))))


(def initial-box
  {:str-content
    "(d/q '[:find ?e ?title
            :where [?e :node/title ?title]]
       @athens/db)"})

(comment (d/q '[:find [(pull ?e [:node/title :block/uid]) ...]
                :where [?e :node/title ?title]]
            @athens/db))


(defonce box-state*
  (rg/atom initial-box))


(defn eval-box
  [{:keys [str-content] :as box}]
  (let [bindings {'athens/db dsdb
                  'd/q d/q
                  'd/pull d/pull
                  'd/pull-many d/pull-many}
        [ok? result] (try
                       [true (sci/eval-string str-content {:bindings bindings})]
                       (catch js/Error e [false e]))]
    (-> box
        (assoc :result result)
        (assoc :error (not ok?)))))


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



(defn rows
  [data mode]
  (case mode
    :coll (map-indexed vector data)
    :map (seq data)
    :tuples (map-indexed vector data)
    :maps (let [hs (headings data mode)]
            (for [row-idx (-> data count range)]
              (into [row-idx]
                (for [h (rest hs)]
                  (get-in data [row-idx h] "")))))))


(defn table-view
  [data mode]
  (let [hs (headings data mode)]
    [:table
     [:tr (for [h hs] [:th (str h)])]
     (for [row (rows data mode)]
       [:tr (for [cell row]
              [:td (str cell)])])]))



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

          (tuples? result)
          (table-view result :tuples)

          (map? result)
          (table-view result :map)

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
    (swap! box-state*
           #(-> %
                (assoc :str-content value)
                (eval-box)))))

(defn box-component
  []
  (let [{:keys [str-content result error]} @box-state*]
    [:div
     [:textarea {:value str-content
                 :on-change handle-box-change!
                 :style {:width "100%"
                         :min-height "150px"
                         :resize :none}}]
     (if-not error
       (browser-component result)
       (error-component result))]))


(defcard-rg box
  (do
    (swap! box-state* eval-box)
    [box-component]))
