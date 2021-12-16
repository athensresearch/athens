(ns athens.interceptors
  (:require
    [athens.util :as util]
    [re-frame.core :as rf]))


(def persist-db
  "Saves the :athens/persist key in db to persistent storage.
  This special key is used for two main reasons:
  - performance, by using identical instead of map comparison
  - clarity, to make it obvious on access that it will be persisted"
  (rf/->interceptor
    :id    :persist
    :after (fn [{:keys [coeffects effects] :as context}]
             (let [k      :athens/persist
                   before (-> coeffects :db k)
                   after  (-> effects :db k)]
               (when (and after (not (identical? before after)))
                 (util/local-storage-set! k after)))
             context)))


(rf/reg-global-interceptor persist-db)
