(ns athens.events
  (:require
    [athens.db :as db]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [posh.reagent :refer [pull #_q #_pull-many]]
    [re-frame.core :refer [reg-event-db reg-event-fx]]))


;;; Events


;; app-db events


(reg-event-db
  :init-rfdb
  (fn-traced [_ _]
             db/rfdb))


(reg-event-db
  :toggle-athena
  (fn [db _]
    (assoc db :athena (not (:athena db)))))


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


(reg-event-db
  :editing-uid
  (fn-traced [db [_ uid]]
             (assoc db :editing-uid uid)))


(reg-event-db
  :drag-bullet
  (fn [db [_ map]]
    (assoc db :drag-bullet map)))


(reg-event-db
  :tooltip-uid
  (fn [db [_ uid]]
    (assoc db :tooltip-uid uid)))



;;; event effects


(reg-event-fx
  :boot
  (fn-traced [_ _]
             {:db         db/rfdb
              :async-flow {:first-dispatch [:get-local-storage-db]
                           :rules          [{:when :seen? :events :parse-datoms :dispatch [:clear-loading] :halt? true}
                                            {:when :seen? :events :api-request-error :dispatch [:alert-failure "Boot Error"] :halt? true}
                                            ]}}))


(reg-event-fx
  :get-datoms
  (fn [_ _]
    {:http {:method :get
            :url db/athens-url
            :opts {:with-credentials? false}
            :on-success [:parse-datoms]
            :on-failure [:alert-failure]}}))

;; FIXME? I reset db/dsdb and store its value in localStorage in the same step. How do we ensure the order of operations is correct?
(reg-event-fx
  :parse-datoms
  (fn [_ [_ json-str]]
    (let [datoms (db/str-to-db-tx json-str)
          new-db (d/db-with (d/empty-db db/schema) datoms)]
      {:reset-conn new-db
       :set-local-storage-db nil})))


(reg-event-fx
  :get-local-storage-db
  (fn [{:keys [db]}]
    (if-let [stored (js/localStorage.getItem "datascript/DB")]
      {:reset-conn (dt/read-transit-str stored)
       :db         (assoc db :loading false)}
      {:dispatch [:get-datoms]})
    ))



;; WIP: dsdb events (transactions)

(defn reindex
  [blocks]
  (->> blocks
    (sort-by :block/order)
    (map-indexed (fn [i x] (assoc x :block/order i)))
    vec))


(defn reindex-parent
  [source parent]
  (->> parent
    :block/children
    (remove #(= (:block/uid %) source))
    reindex))


(defn reindex-target
  [_source target]
  (let [target-entity @(pull db/dsdb '[* {:block/children [:db/id :block/order]}] [:block/uid target])]
    (->> target-entity
      :block/children
      ;;(cons {:block/uid source :block/order -1})
      (cons {:db/id 2349 :block/order -1})
      reindex)))


(defn get-parent
  "takes in a block string and returns a parent with its children"
  [uid]
  (let [parent-eid (-> @(pull db/dsdb '[:block/_children] [:block/uid uid])
                     :block/_children
                     first
                     :db/id)]
    @(pull db/dsdb '[:db/id {:block/children [:db/id :block/uid :block/order]}] parent-eid)))


(reg-event-fx
  :drop-bullet
  (fn-traced [_ [_ {:keys [source target _kind]}]]
             (let [parent (get-parent source)
                   parent-children (reindex-parent source parent)
                   _target-children (reindex-target source target)]
               {:transact [{:db/add [:block/uid source] :block/children parent-children}
                           [:db/retract (:db/id parent) :block/children [:block/uid source]]

                           ;; FIXME: for some reason unable to transact multiple children
                           ;; Get error: Error: Lookup ref should contain 2 elements: [1 2 3 4]
                           ;;{:db/add [:block/uid target] :block/children target-children}
                           ]})))



;;;; TODO: delete the following logic when re-implementing title merge

;;(defn node-with-title
;;  [ds title]
;;  (d/q '[:find ?e .
;;         :in $ ?title
;;         :where [?e :node/title ?title]]
;;       ds title))
;;
;;
;;(defn referencing-blocks
;;  [ds title]
;;  (d/q '[:find ?e ?s
;;         :in $ ?regex
;;         :where
;;         [?e :block/string ?s]
;;         [(re-find ?regex ?s)]]
;;       ds (patterns/linked title)))
;;
;;
;;(defn rename-refs-tx
;;  [old-title new-title [eid s]]
;;  (let [new-s (str/replace
;;                s
;;                (patterns/linked old-title)
;;                (str "$1$3$4" new-title "$2$5"))]
;;    [:db/add eid :block/string new-s]))
;;
;;
;;(defn rename-tx
;;  [ds old-title new-title]
;;  (let [eid (node-with-title ds old-title)
;;        blocks (referencing-blocks ds old-title)]
;;    (->> blocks
;;         (map (partial rename-refs-tx old-title new-title))
;;         (into [[:db/add eid :node/title new-title]]))))
;;
;;
;;(reg-event-fx
;;  :node/renamed
;;  [(rp/inject-cofx :ds)]
;;  (fn-traced [{:keys [db ds]} [_ old-title new-title]]
;;             (when (not= old-title new-title)
;;               (if (node-with-title ds new-title)
;;                 {:db (assoc db :merge-prompt {:active true
;;                                               :old-title old-title
;;                                               :new-title new-title})
;;                  :timeout {:action :start
;;                            :id :merge-prompt
;;                            :wait 7000
;;                            :event [:node/merge-canceled]}}
;;                 {:transact (rename-tx ds old-title new-title)}))))
;;
;;
;;(defn count-children
;;  [ds title]
;;  (d/q '[:find (count ?children) .
;;         :in $ ?title
;;         :where [?e :node/title ?title]
;;         [?e :block/children ?children]]
;;       ds title))
;;
;;
;;(defn get-children-eids
;;  [ds title]
;;  (d/q '[:find [?children ...]
;;         :in $ ?title
;;         :where [?e :node/title ?title]
;;         [?e :block/children ?children]]
;;       ds title))
;;
;;
;;(defn move-blocks-tx
;;  [ds from-title to-title]
;;  (let [block-count (count-children ds to-title)
;;        block-eids (get-children-eids ds from-title)]
;;    (mapcat (fn [eid]
;;              (let [order (:block/order (d/pull ds [:block/order] eid))]
;;                [[:db/add [:node/title to-title] :block/children eid]
;;                 [:db/add eid :block/order (+ order block-count)]]))
;;            block-eids)))
;;
;;
;;(reg-event-fx
;;  :node/merged
;;  [(rp/inject-cofx :ds)]
;;  (fn-traced [{:keys [db ds]} [_ primary-title secondary-title]]
;;             {:db (dissoc db :merge-prompt)
;;              :timeout {:action :clear
;;                        :id :merge-prompt}
;;              :transact (concat [[:db.fn/retractEntity [:node/title secondary-title]]]
;;                                (move-blocks-tx ds secondary-title primary-title)
;;                                (rename-tx ds primary-title secondary-title))}))
;;
;;
;;(reg-event-fx
;;  :node/merge-canceled
;;  (fn-traced [{:keys [db]} _]
;;             {:db (dissoc db :merge-prompt)
;;              :timeout {:action :clear
;;                        :id :merge-prompt}}))

