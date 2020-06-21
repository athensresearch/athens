(ns athens.effects
  (:require
    [athens.db :as db]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [posh.reagent :refer [transact!]]
    [re-frame.core :refer [dispatch reg-fx]]))


;;; Effects


(reg-fx
  :transact
  (fn [datoms]
    (transact! db/dsdb datoms)))


(reg-fx
  :reset-conn
  (fn [new-db]
    (d/reset-conn! db/dsdb new-db)))


(reg-fx
  :set-local-storage-db
  (fn [_]
    (js/localStorage.setItem "datascript/DB" (dt/write-transit-str @db/dsdb))))


(reg-fx
  :http
  (fn [{:keys [url method opts on-success on-failure]}]
    (go
      (let [http-fn (case method
                      :post http/post :get http/get
                      :put http/put :delete http/delete)
            res     (<! (http-fn url opts))
            {:keys [success body] :as all} res]
        (if success
          (dispatch (conj on-success body))
          (dispatch (conj on-failure all)))))))


(reg-fx
  :timeout
  (let [timers (atom {})]
    (fn [{:keys [action id event wait]}]
      (case action
        :start (swap! timers assoc id (js/setTimeout #(dispatch event) wait))
        :clear (do (js/clearTimeout (get @timers id))
                   (swap! timers dissoc id))))))

