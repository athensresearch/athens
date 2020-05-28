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


(reg-fx
  :timeout
  (let [timers (atom {})]
    (fn [{:keys [action id event wait]}]
      (case action
        :start (swap! timers assoc id (js/setTimeout #(dispatch event) wait))
        :clear (do (js/clearTimeout (get @timers id))
                   (swap! timers dissoc id))))))


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


(defn rename-tx
  [ds old-title new-title]
  (let [eid (node-with-title ds old-title)
        blocks (referencing-blocks ds old-title)]
    (->> blocks
         (map (partial rename-refs-tx old-title new-title))
         (into [[:db/add eid :node/title new-title]]))))


(reg-event-fx
  :node/renamed
  [(rp/inject-cofx :ds)]
  (fn-traced [{:keys [db ds]} [_ old-title new-title]]
             (when (not= old-title new-title)
               (if (node-with-title ds new-title)
                 {:db (assoc db :merge-prompt {:active true
                                               :old-title old-title
                                               :new-title new-title})
                  :timeout {:action :start
                            :id :merge-prompt
                            :wait 7000
                            :event [:node/merge-canceled]}}
                 {:transact (rename-tx ds old-title new-title)}))))


(defn count-children
  [ds title]
  (d/q '[:find (count ?children) .
         :in $ ?title
         :where [?e :node/title ?title]
         [?e :block/children ?children]]
       ds title))


(defn get-children-eids
  [ds title]
  (d/q '[:find [?children ...]
         :in $ ?title
         :where [?e :node/title ?title]
         [?e :block/children ?children]]
       ds title))


(defn move-blocks-tx
  [ds from-title to-title]
  (let [block-count (count-children ds to-title)
        block-eids (get-children-eids ds from-title)]
    (mapcat (fn [eid]
              (let [order (:block/order (d/pull ds [:block/order] eid))]
                [[:db/add [:node/title to-title] :block/children eid]
                 [:db/add eid :block/order (+ order block-count)]]))
            block-eids)))


(reg-event-fx
  :node/merged
  [(rp/inject-cofx :ds)]
  (fn-traced [{:keys [db ds]} [_ primary-title secondary-title]]
             {:db (dissoc db :merge-prompt)
              :timeout {:action :clear
                        :id :merge-prompt}
              :transact (concat [[:db.fn/retractEntity [:node/title secondary-title]]]
                                (move-blocks-tx ds secondary-title primary-title)
                                (rename-tx ds primary-title secondary-title))}))


(reg-event-fx
  :node/merge-canceled
  (fn-traced [{:keys [db]} _]
             {:db (dissoc db :merge-prompt)
              :timeout {:action :clear
                        :id :merge-prompt}}))


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
