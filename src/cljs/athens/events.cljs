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
                                            {:when :seen? :events :api-request-error :dispatch [:alert-failure "Boot Error"] :halt? true}]}}))


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
      {:dispatch [:get-datoms]})))


(reg-event-fx
  :undo
  (fn [_ _]
    (when-let [prev (db/find-prev @db/history #(identical? @db/dsdb %))]
      {:reset-conn prev})))


(reg-event-fx
  :redo
  (fn [_ _]
    (when-let [next (db/find-next @db/history #(identical? @db/dsdb %))]
      {:reset-conn next})))




;;; dsdb events (transactions)

(reg-event-fx
  :transact-event
  (fn [_ [_ datoms]]
    {:transact datoms}))


;; Block Editing

(defn get-block
  [id]
  @(pull db/dsdb '[:db/id :block/uid :block/order {:block/children [:block/uid :block/order]}] id))


(defn get-parent
  [id]
  (let [eid (-> (d/entity @db/dsdb id)
              :block/_children
              first
              :db/id)]
    (get-block eid)))


;; find sibling block with order - 1
;; add to end of sib children. no need to reindex?
(reg-event-fx
  :indent
  (fn [_ [_ uid]]
    (let [block (get-block [:block/uid uid])
          parent (get-parent [:block/uid uid])
          older-sib (->> parent
                      :block/children
                      (filter #(= (dec (:block/order block)) (:block/order %)))
                      first
                      :db/id
                      get-block)
          new-block {:db/id (:db/id block) :block/order (count (:block/children older-sib))}
          reindex-blocks (->> (d/q '[:find ?ch ?new-order
                                     :in $ ?parent ?source-order
                                     :where
                                     [?parent :block/children ?ch]
                                     [?ch :block/order ?order]
                                     [(> ?order ?source-order)]
                                     [(dec ?order) ?new-order]]
                                @db/dsdb (:db/id parent) (:block/order block))
                           (map (fn [[id order]] {:db/id id :block/order order})))]
      {:transact [[:db/retract (:db/id parent) :block/children (:db/id block)]
                  {:db/id (:db/id older-sib) :block/children [new-block]}
                  {:db/id (:db/id parent) :block/children reindex-blocks}]})))


(reg-event-fx
  :unindent
  (fn [_ [_ uid]]
    (let [parent (get-parent [:block/uid uid])
          grandpa (get-parent (:db/id parent))
          reindex-blocks (->> (d/q '[:find ?new-order ?children
                                     :in $ ?grandpa ?parent-order
                                     :where
                                     [?grandpa :block/children ?children]
                                     [?children :block/order ?order]
                                     [(> ?order ?parent-order)]
                                     [(inc ?order) ?new-order]]
                                @db/dsdb (:db/id grandpa) (:order parent))
                           (map (fn [[order id]] {:db/id id :block/order order})))]
      {:transact [[:db/add [:block/uid uid] :block/order (inc (:block/order parent))]
                  [:db/retract (:db/id parent) :block/children [:block/uid uid]]
                  [:db/add (:db/id grandpa) :block/children [:block/uid uid]]
                  {:db/add (:db/id grandpa) :block/children reindex-blocks}]})))





;; Drag and Drop


(defn target-child
  [{:block/keys [uid order]} {parent-eid :db/id} {target-eid :db/id}]
  (let [new-parent-children (->> (d/q '[:find ?ch ?new-order
                                         :in $ ?parent ?source-order
                                         :where
                                         [?parent :block/children ?ch]
                                         [?ch :block/order ?order]
                                         [(> ?order ?source-order)]
                                         [(dec ?order) ?new-order]]
                                    @db/dsdb parent-eid order)
                              (map (fn [[id order]] {:db/id id :block/order order})))
        new-target-children (->> (d/q '[:find ?ch ?new-order
                                        :in $ ?parent
                                        :where
                                        [?parent :block/children ?ch]
                                        [?ch :block/order ?order]
                                        [(inc ?order) ?new-order]]
                                   @db/dsdb target-eid)
                              (map (fn [[id order]] {:db/id id :block/order order}))
                              (concat [{:block/uid uid :block/order 0}]))]
    [[:db/retract parent-eid :block/children [:block/uid uid]] ;; retract source from parent
     {:db/add parent-eid :block/children new-parent-children} ;; reindex parent without source
     ;; TODO: not sure why I need to use :db/id here and not :db/add
     {:db/id target-eid :block/children new-target-children}])) ;; reindex target. include source


(defn between [s t x]
  "http://blog.jenkster.com/2013/11/clojure-less-than-greater-than-tip.html"
  (if (< s t)
    (and (< s x) (< x t))
    (and (< t x) (< x s))))


(defn target-sibling-same-parent
  [source target parent]
  (let [t-order (:block/order target)
        s-order (:block/order source)
        inc-or-dec (if (> s-order t-order) inc dec)
        reindex (->> (d/q '[:find ?ch ?new-order
                            :in $ ?parent ?s-order ?t-order ?between ?inc-or-dec
                            :where
                            [?parent :block/children ?ch]
                            [?ch :block/order ?order]
                            [(?between ?s-order ?t-order ?order)]
                            [(?inc-or-dec ?order) ?new-order]]
                       @db/dsdb (:db/id parent) s-order t-order between inc-or-dec)
                  (map (fn [[eid order _uid]] {:db/id eid :block/order order}))
                  (concat [{:db/id (:db/id source) :block/order (inc t-order)}]))]
    [{:db/add (:db/id parent) :block/children reindex}]))


(defn target-sibling-diff-parent
  "source: reindex everything after source-order
 target: reindex everything after target-order"
  [source target source-parent target-parent]
  (let [source-parent-children (->> (d/q '[:find ?ch ?new-order
                                           :in $ ?parent ?source-order
                                           :where
                                           [?parent :block/children ?ch]
                                           [?ch :block/order ?order]
                                           [(> ?order ?source-order)]
                                           [(dec ?order) ?new-order]]
                                      @db/dsdb (:db/id source-parent) (:block/order source))
                                 (map (fn [[id order]] {:db/id id :block/order order})))
        target-parent-children (->> (d/q '[:find ?ch ?new-order
                                           :in $ ?parent ?target-order
                                           :where
                                           [?parent :block/children ?ch]
                                           [?ch :block/order ?order]
                                           [(> ?order ?target-order)]
                                           [(inc ?order) ?new-order]]
                                      @db/dsdb (:db/id target-parent) (:block/order target))
                                 (map (fn [[id order]] {:db/id id :block/order order}))
                                 (concat [{:db/id (:db/id source) :block/order (inc (:block/order target))}]))]
    [[:db/retract (:db/id source-parent) :block/children (:db/id source)]
     {:db/id (:db/id source-parent) :block/children source-parent-children}
     {:db/id (:db/id target-parent) :block/children target-parent-children}]))


(reg-event-fx
  :drop-bullet
  (fn-traced [_ [_ source-uid target-uid kind]]
             (let [source        (get-block [:block/uid source-uid])
                   target        (get-block [:block/uid target-uid])
                   source-parent (get-parent [:block/uid source-uid])
                   target-parent (get-parent [:block/uid target-uid])]
               (prn target)
               {:transact
                (cond
                  ;; child always has same behavior: move to first child of target
                  (= kind :child) (target-child source source-parent target)

                  ;; do nothing if target is directly above source
                  (and
                    (= source-parent target-parent)
                    (= 1 (- (:block/order source) (:block/order target))))
                  nil

                  ;; re-order blocks between source and target
                  (= source-parent target-parent)
                  (target-sibling-same-parent source target source-parent)

                  ;;; when parent is different, re-index both source-parent and target-parent
                  (not= source-parent target-parent)
                  (target-sibling-diff-parent source target source-parent target-parent)

                  :else nil)})))

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

