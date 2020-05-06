(ns athens.events
  (:require
    [athens.db :as db]
    [datascript.core :as d]
    [re-frame.core :as rf :refer [dispatch reg-fx reg-event-db reg-event-fx reg-sub]]
    [re-posh.core :as rp :refer [reg-event-ds]]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [day8.re-frame.async-flow-fx]))

(reg-event-db
  :init-rfdb
  (fn-traced [_ _]
    db/rfdb))

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

(reg-event-fx
  :get-datoms
  (fn [_ _]
    {:http {:method :get
            :url db/athens-url
            :opts {:with-credentials? false}
            :on-success [:parse-datoms]
            :on-failure [:alert-failure]}}))

(reg-event-ds
  :parse-datoms
  (fn-traced [_ [event json-str]]
             (d/reset-conn! db/dsdb (d/empty-db db/schema)) ;; TODO: refactor to an effect
             (db/str-to-db-tx json-str)))

(reg-event-db
  :alert-failure
  (fn-traced [db error]
    (assoc-in db [:errors] error)))

(reg-event-db
  :clear-errors
  (fn-traced [db]
             (assoc-in db [:errors] {})))

(reg-event-db
  :clear-loading
  (fn-traced [db]
    (assoc-in db [:loading] false)))

(defn boot-flow []
  {:first-dispatch
          [:get-datoms]
   :rules [{:when :seen? :events :parse-datoms :dispatch [:clear-loading] :halt? true}
           {:when :seen? :events :api-request-error :dispatch [:alert-failure "Boot Error"] :halt? true}]})

(reg-event-fx
  :boot
  (fn-traced [_ _]
    {:async-flow (boot-flow)}))