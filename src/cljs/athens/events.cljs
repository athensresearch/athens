(ns athens.events
  (:require
   [athens.db :as db]
   [athens.patterns :as patterns]
   [cljs-http.client :as http]
   [cljs.core.async :refer [go <!]]
   [clojure.string :as str]
   [datascript.core :as d]
   [day8.re-frame.async-flow-fx]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as rf :refer [dispatch reg-fx reg-event-db reg-event-fx]]
   [re-posh.core :as rp :refer [reg-event-ds]]))


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
 (fn-traced [_ [_ json-str]]
            (d/reset-conn! db/dsdb (d/empty-db db/schema)) ;; TODO: refactor to an effect
            (db/str-to-db-tx json-str)))


(reg-event-ds
 :block/toggle-open
 (fn-traced [_ [_event eid open-state]]
            [[:db/add eid :block/open (not open-state)]]))


(defn node-with-title
  [ds title]
  (d/q '[:find ?e .
         :in $ ?title
         :where [?e :node/title ?title]]
       ds title))


(defn referencing-blocks
  [ds title]
  (d/q '[:find ?e ?s
         :in $ ?regex
         :where
         [?e :block/string ?s]
         [(re-find ?regex ?s)]]
       ds (patterns/linked title)))


(defn rename-refs-tx
  [old-title new-title [eid s]]
  (let [new-s (str/replace
               s
               (patterns/linked old-title)
               (str "$1$3$4" new-title "$2$5"))]
    [:db/add eid :block/string new-s]))


(reg-event-ds
 :node/rename
 (fn-traced [ds [_ old-title new-title]]
            (let [eid (node-with-title ds old-title)
                  blocks (referencing-blocks ds old-title)]
              (->> blocks
                   (map (partial rename-refs-tx old-title new-title))
                   (into [[:db/add eid :node/title new-title]])))))


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


(defn boot-flow
  []
  {:first-dispatch
   [:get-datoms]
   :rules [{:when :seen? :events :parse-datoms :dispatch [:clear-loading] :halt? true}
           {:when :seen? :events :api-request-error :dispatch [:alert-failure "Boot Error"] :halt? true}]})


(reg-event-fx
 :boot
 (fn-traced [_ _]
            {:async-flow (boot-flow)}))
