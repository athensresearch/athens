(ns athens.events
  (:require
    [athens.db :as db]
    [athens.util :refer [now-ts gen-block-uid]]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [posh.reagent :refer [pull #_q #_pull-many]]
    [re-frame.core :refer [reg-event-db reg-event-fx]]))


;;; re-frame app-db events


(reg-event-db
  :init-rfdb
  (fn-traced [_ _]
             db/rfdb))


(reg-event-db
  :athena/toggle
  (fn [db _]
    (update db :athena/open not)))


(reg-event-db
  :athena/update-recent-items
  (fn-traced [db [_ selected-page]]
    (when (nil? ((set (:athena/recent-items db)) selected-page))
      (update db :athena/recent-items conj selected-page))))


(reg-event-db
  :devtool/toggle
  (fn [db _]
    (update db :devtool/open not)))


(reg-event-db
  :left-sidebar/toggle
  (fn [db _]
    (update db :left-sidebar/open not)))


(reg-event-db
  :right-sidebar/toggle
  (fn [db _]
    (update db :right-sidebar/open not)))


(reg-event-db
  :right-sidebar/toggle-item
  (fn [db [_ item]]
    (update-in db [:right-sidebar/items item :open] not)))


;; TODO: dec all indices > closed item
(reg-event-db
  :right-sidebar/close-item
  (fn [db [_ uid]]
    (update db :right-sidebar/items dissoc uid)))


;; TODO: change right sidebar items from map to datascript
(reg-event-fx
  :right-sidebar/open-item
  (fn-traced [{:keys [db]} [_ uid]]
    (let [block     (d/pull @db/dsdb '[:node/title :block/string] [:block/uid uid])
          new-item  (merge block {:open true :index -1})
          new-items (assoc (:right-sidebar/items db) uid new-item)
          inc-items (reduce-kv (fn [m k v] (assoc m k (update v :index inc)))
                               {}
                               new-items)
          sorted-items (into (sorted-map-by (fn [k1 k2]
                                              (compare
                                                [(get-in new-items [k1 :index]) k2]
                                                [(get-in new-items [k2 :index]) k1]))) inc-items)]
      {:db (assoc db :right-sidebar/items sorted-items)
       :dispatch (when (false? (:right-sidebar/open db))
                   [:right-sidebar/toggle])})))


;; Alerts


(reg-event-db
  :alert/set
  (fn-traced [db alert]
             (assoc db :alert alert)))


(reg-event-db
  :alert/unset
  (fn-traced [db]
             (assoc db :alert nil)))


;; Loading


(reg-event-db
  :loading/set
  (fn-traced [db]
    (assoc-in db [:loading?] true)))


(reg-event-db
  :loading/unset
  (fn-traced [db]
             (assoc-in db [:loading?] false)))


;; Event Listeners


(reg-event-db
  :editing/uid
  (fn-traced [db [_ uid]]
             (assoc db :editing/uid uid)))


(reg-event-db
  :drag-bullet
  (fn [db [_ map]]
    (assoc db :drag-bullet map)))


(reg-event-db
  :tooltip/uid
  (fn [db [_ uid]]
    (assoc db :tooltip/uid uid)))


;; Daily Notes


(reg-event-db
  :daily-notes/reset
  (fn [db _]
    (assoc db :daily-notes/items [])))


(reg-event-fx
  :daily-note/next
  (fn [{:keys [db]} [_ {:keys [uid title]}]]
    (let [new-db (update db :daily-notes/items conj uid)
          now (now-ts)]
      (if (db/e-by-av :block/uid uid)
        {:db new-db}
        {:db        new-db
         :transact! [{:db/id -1 :node/title title :block/uid uid :create/time now :edit/time now}]}))))


;;; Event Effects and Datascript Transactions


;; Import/Export


(reg-event-fx
  :boot ;; FIXME: rename to init-dsdb?
  (fn-traced [_ _]
             {:async-flow {:first-dispatch [:get-local-storage-db]
                           :rules          [{:when :seen? :events :parse-datoms :dispatch [:loading/unset] :halt? true}
                                            {:when :seen? :events :api-request-error :dispatch [:alert/set "Boot Error"] :halt? true}]}}))


(reg-event-fx
  :get-datoms
  (fn [_ _]
    {:http {:method :get
            :url db/athens-url
            :opts {:with-credentials? false}
            :on-success [:parse-datoms]
            :on-failure [:alert/set]}}))


(reg-event-fx
  :parse-datoms
  (fn [{:keys [db]} [_ json-str]]
    (let [datoms (db/str-to-db-tx json-str)
          new-db (d/db-with (d/empty-db db/schema) datoms)]
      {:reset-conn!          new-db
       :set-local-storage-db new-db
       :dispatch-n           [[:init-rfdb]
                              [:navigate (-> db :currount-route :data :name)]
                              [:loading/unset]]})))


(reg-event-fx
  :get-local-storage-db
  (fn [{:keys [db]}]
    (if-let [stored (js/localStorage.getItem "datascript/DB")]
      {:reset-conn! (dt/read-transit-str stored)
       ;;:db         (assoc (assoc db/rfdb :loading false) :current-route (:current-route db))
       :db          (merge db/rfdb {:loading false :current-route (:current-route db)})}
      {:dispatch [:get-datoms]
       :db       db/rfdb})))


;; Datascript

(reg-event-fx
  :transact
  (fn [_ [_ datoms]]
    {:transact! datoms}))


(reg-event-fx
  :page/create
  (fn [_ [_ title uid]]
    (let [now (now-ts)]
      {:transact! [{:db/id -1 :node/title title :block/uid uid :create/time now :edit/time now}]})))


(reg-event-fx
  :undo
  (fn [_ _]
    (when-let [prev (db/find-prev @db/history #(identical? @db/dsdb %))]
      {:reset-conn! prev})))


(reg-event-fx
  :redo
  (fn [_ _]
    (when-let [next (db/find-next @db/history #(identical? @db/dsdb %))]
      {:reset-conn! next})))


;; TODO: move to db
(def rules
  '[[(after ?p ?at ?ch ?o)
     [?p :block/children ?ch]
     [?ch :block/order ?o]
     [(> ?o ?at)]]
    [(inc-after ?p ?at ?ch ?new-o)
     (after ?p ?at ?ch ?o)
     [(inc ?o) ?new-o]]
    [(dec-after ?p ?at ?ch ?new-o)
     (after ?p ?at ?ch ?o)
     [(dec ?o) ?new-o]]])


(reg-event-fx
  :backspace
  (fn [_ [_ _uid]]))


;; TODO but how to set focus... especially async
(defn split-block
  [uid val sel-start]
  (let [parent (db/get-parent [:block/uid uid])
        block (db/get-block [:block/uid uid])
        head (subs val 0 sel-start)
        tail (subs val sel-start)
        new-uid (gen-block-uid)
        new-block {:db/id        -1
                   :block/order  (inc (:block/order block))
                   :block/uid    new-uid
                   :block/open   true
                   :block/string tail}
        reindex (->> (d/q '[:find ?ch ?new-o
                            :in $ % ?p ?at
                            :where (inc-after ?p ?at ?ch ?new-o)]
                       @db/dsdb rules (:db/id parent) (:block/order block))
                  (map (fn [[id order]] {:db/id id :block/order order}))
                  (concat [new-block]))]
    {:transact! [[:db/add (:db/id block) :block/string head]
                 {:db/id (:db/id parent)
                  :block/children reindex}]
     :dispatch  [:editing/uid new-uid]}))


(defn bump-up
  [uid val sel-start]
  (let [parent (db/get-parent [:block/uid uid])
        block (db/get-block [:block/uid uid])
        tail (subs val sel-start)
        new-uid (gen-block-uid)
        new-block {:db/id        -1
                   :block/order  (:block/order block)
                   :block/uid    new-uid
                   :block/open   true
                   :block/string tail}
        reindex (->> (d/q '[:find ?ch ?new-o
                            :in $ % ?p ?at
                            :where (inc-after ?p ?at ?ch ?new-o)]
                       @db/dsdb rules (:db/id parent) (inc (:block/order block)))
                  (map (fn [[id order]] {:db/id id :block/order order}))
                  (concat [new-block]))]
    {:transact! [[:db/add (:db/id block) :block/string ""]
                 {:db/id (:db/id parent) :block/children reindex}]
     :dispatch  [:editing/uid new-uid]}))


;; TODO: if enter at end of block, if block open, insert new 0th child. otherwise, add sibling (default behavior right now)
(reg-event-fx
  :enter
  (fn [_ [_ uid val sel-start]]
    (cond
      (not (zero? sel-start)) (split-block uid val sel-start)
      (empty? val) {:dispatch [:unindent uid]}
      (and (zero? sel-start) val) (bump-up uid val sel-start))))


;; TODO: no-op when indenting as the right-most child
(reg-event-fx
  :indent
  (fn [_ [_ uid]]
    (let [block (db/get-block [:block/uid uid])
          parent (db/get-parent [:block/uid uid])
          older-sib (->> parent
                      :block/children
                      (filter #(= (dec (:block/order block)) (:block/order %)))
                      first
                      :db/id
                      db/get-block)
          new-block {:db/id (:db/id block) :block/order (count (:block/children older-sib))}
          reindex-blocks (->> (d/q '[:find ?ch ?new-o
                                     :in $ % ?p ?at
                                     :where (dec-after ?p ?at ?ch ?new-o)]
                                @db/dsdb rules (:db/id parent) (:block/order block))
                           (map (fn [[id order]] {:db/id id :block/order order})))]
      {:transact! [[:db/retract (:db/id parent) :block/children (:db/id block)]
                   {:db/id (:db/id older-sib) :block/children [new-block]} ;; becomes child of older sibling block — same parent but order-1
                   {:db/id (:db/id parent) :block/children reindex-blocks}]}))) ;; reindex parent


;; TODO: no-op when user tries to unindent to a child out of current context
(reg-event-fx
  :unindent
  (fn [_ [_ uid]]
    (let [parent (db/get-parent [:block/uid uid])
          grandpa (db/get-parent (:db/id parent))
          new-block {:block/uid uid :block/order (inc (:block/order parent))}
          reindex-grandpa (->> (d/q '[:find ?ch ?new-order
                                      :in $ % ?grandpa ?parent-order
                                      :where (inc-after ?grandpa ?parent-order ?ch ?new-order)]
                                 @db/dsdb rules (:db/id grandpa) (:block/order parent))
                            (map (fn [[id order]] {:db/id id :block/order order}))
                            (concat [new-block]))]
      (when (and parent grandpa)
        {:transact! [[:db/retract (:db/id parent) :block/children [:block/uid uid]]
                     {:db/id (:db/id grandpa) :block/children reindex-grandpa}]}))))


(defn target-child
  [source source-parent target]
  (let [new-block {:block/uid (:block/uid source) :block/order 0}
        new-parent-children (->> (d/q '[:find ?ch ?new-order
                                         :in $ % ?parent ?source-order
                                         :where (dec-after ?parent ?source-order ?ch ?new-order)]
                                    @db/dsdb rules (:db/id source-parent) (:block/order source))
                              (map (fn [[id order]] {:db/id id :block/order order})))
        new-target-children (->> (d/q '[:find ?ch ?new-order
                                        :in $ % ?parent ?at
                                        :where (inc-after ?parent ?at ?ch ?new-order)]
                                   @db/dsdb rules (:dbid target) 0)
                              (map (fn [[id order]] {:db/id id :block/order order}))
                              (concat [new-block]))]
    [[:db/retract (:db/id source-parent) :block/children [:block/uid (:block/uid source)]] ;; retract source from parent
     {:db/add (:db/id source-parent) :block/children new-parent-children} ;; reindex parent without source
     {:db/id (:db/id target) :block/children new-target-children}])) ;; reindex target. include source


(defn between
  "http://blog.jenkster.com/2013/11/clojure-less-than-greater-than-tip.html"
  [s t x]
  (if (< s t)
    (and (< s x) (< x t))
    (and (< t x) (< x s))))


(defn target-sibling-same-parent
  [source target parent]
  (let [t-order (:block/order target)
        s-order (:block/order source)
        new-block {:db/id (:db/id source) :block/order (inc t-order)}
        inc-or-dec (if (> s-order t-order) inc dec)
        reindex (->> (d/q '[:find ?ch ?new-order
                            :in $ ?parent ?s-order ?t-order ?between ?inc-or-dec
                            :where
                            [?parent :block/children ?ch]
                            [?ch :block/order ?order]
                            [(?between ?s-order ?t-order ?order)]
                            [(?inc-or-dec ?order) ?new-order]]
                       @db/dsdb (:db/id parent) s-order t-order between inc-or-dec)
                  (map (fn [[id order]] {:db/id id :block/order order}))
                  (concat [new-block]))]
    [{:db/add (:db/id parent) :block/children reindex}]))


(defn target-sibling-diff-parent
  [source target source-parent target-parent]
  (let [new-block {:db/id (:db/id source) :block/order (inc (:block/order target))}
        source-parent-children (->> (d/q '[:find ?ch ?new-order
                                           :in $ % ?parent ?source-order
                                           :where (dec-after ?parent ?source-order ?ch ?new-order)]
                                      @db/dsdb rules (:db/id source-parent) (:block/order source))
                                 (map (fn [[id order]] {:db/id id :block/order order})))
        target-parent-children (->> (d/q '[:find ?ch ?new-order
                                           :in $ % ?parent ?target-order
                                           :where (inc-after ?parent ?target-order ?ch ?new-order)]
                                      @db/dsdb rules (:db/id target-parent) (:block/order target))
                                 (map (fn [[id order]] {:db/id id :block/order order}))
                                 (concat [new-block]))]
    [[:db/retract (:db/id source-parent) :block/children (:db/id source)]
     {:db/id (:db/id source-parent) :block/children source-parent-children} ;; reindex source
     {:db/id (:db/id target-parent) :block/children target-parent-children}])) ;; reindex target


(reg-event-fx
  :drop-bullet
  (fn-traced [_ [_ source-uid target-uid kind]]
             (let [source        (db/get-block [:block/uid source-uid])
                   target        (db/get-block [:block/uid target-uid])
                   source-parent (db/get-parent [:block/uid source-uid])
                   target-parent (db/get-parent [:block/uid target-uid])]
               {:transact!
                (cond
                  ;; child always has same behavior: move to first child of target
                  (= kind :child) (target-child source source-parent target)
                  ;; do nothing if target is directly above source
                  (and (= source-parent target-parent)
                    (= 1 (- (:block/order source) (:block/order target)))) nil
                  ;; re-order blocks between source and target
                  (= source-parent target-parent) (target-sibling-same-parent source target source-parent)
                  ;;; when parent is different, re-index both source-parent and target-parent
                  (not= source-parent target-parent) (target-sibling-diff-parent source target source-parent target-parent))})))

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

