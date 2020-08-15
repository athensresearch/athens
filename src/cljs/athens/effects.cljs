(ns athens.effects
  (:require
    [athens.db :as db]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [cljs.pprint :refer [pprint]]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [posh.reagent :refer [transact!]]
    [re-frame.core :refer [dispatch reg-fx]]))


(def fs (js/require "fs"))
(def stream (js/require "stream"))



;;; Effects


(reg-fx
  :transact!
  (fn [datoms]
    (prn "TX INPUTS")
    (pprint datoms)
    (prn "TX OUTPUTS")
    (let [outputs (:tx-data (transact! db/dsdb datoms))]
      (pprint outputs)
      (def r (.. stream -Readable (from (dt/write-transit-str @db/dsdb))))
      (def w (.createWriteStream fs "./data/tmp.transit"))
      (.pipe r w))))


(reg-fx
  :reset-conn!
  (fn [new-db]
    (d/reset-conn! db/dsdb new-db)))


(reg-fx
  :local-storage/set-db!
  (fn [db]
    (js/localStorage.setItem "datascript/DB" (dt/write-transit-str db))))


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

;(reg-fx
;  :fs/write-stream
;  (fn [_]
;    (createReadStream "./package.json")
;    (createWriteStream "./package-copy.json")))
