(ns athens.coeffects
  #_(:require
    [athens.db :as db]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [posh.reagent :refer [transact! pull #_q #_pull-many]]
    [re-frame.core :refer [dispatch reg-fx reg-event-db reg-event-fx]]))


;;; Coeffects

;;
;;(r/reg-cofx
;;  :ds
;;  (fn [coeffects _]
;;    (assoc coeffects :ds @@store)))
