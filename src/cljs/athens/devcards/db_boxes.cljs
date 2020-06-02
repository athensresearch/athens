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

(comment
  (go
    (let [response (<! (http/get db/athens-url
                                 {:with-credentials? false}))]
      (->> response
           :body
           db/str-to-db-tx
           (d/transact dsdb))))
  nil)


(def empty-box
  {:str-content ""})


(defonce box-state*
  (rg/atom empty-box))


(defcard box-state* box-state*)


(defn eval-box
  [{:keys [str-content] :as box}]
  (let [result (try (sci/eval-string str-content
                                     {:bindings {'athens/db dsdb
                                                 'q d/q}})
                    (catch js/Error e
                      (log e)
                      "error"))]
    (assoc box :result result)))


(defn browser
  [result]
  (if (seq result)
    [:div (for [item result]
            [:div (str item)])]
    [:div (str result)]))


(defn result-wrapper
  []
  (let [err* (rg/atom nil)]
    (rg/create-class
      {:component-did-catch (fn [err info]
                              (reset! err* [err info]))
       :reagent-render (fn [result]
                         (if (nil? @err*)
                           [browser result]
                           (let [[_ info] @err*]
                             [:div
                              [:code (str info)]])))})))


(defn handle-box-change!
  [e]
  (let [value (-> e .-target .-value)]
    (swap! box-state*
           #(-> %
                (assoc :str-content value)
                (eval-box)))))

(defn box-component
  []
  (let [{:keys [str-content result]} @box-state*]
    [:div
     [:textarea {:value str-content
                 :on-change handle-box-change!
                 :style {:width "100%"
                         :min-height "150px"
                         :resize :none}}]
     [result-wrapper result]]))


(defcard-rg box
  (do
    (swap! box-state* eval-box)
    [box-component]))
