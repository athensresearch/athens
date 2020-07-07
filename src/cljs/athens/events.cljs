(ns athens.events
  (:require
    [athens.db :as db]
    [athens.util :refer [now-ts gen-block-uid]]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]))


;; -- re-frame app-db events ---------------------------------------------

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


;; Block Events
;; TODO: refactor to an effect
(defn focus-el
  [id]
  (fn []
    (if-let [el (.. js/document (getElementById id))]
      (.focus el))))


(reg-event-db
  :editing/uid
  (fn-traced [db [_ uid]]
             (js/setTimeout (focus-el (str "editable-uid-" uid)) 300)
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


;; TODO: don't use app-db, use dsdb
(reg-event-fx
  :daily-note/next
  (fn [{:keys [db]} [_ {:keys [uid title]}]]
    (let [new-db (update db :daily-notes/items conj uid)]
      (if (db/e-by-av :block/uid uid)
        {:db new-db}
        {:db        new-db
         :dispatch [:page/create title uid]}))))


;; -- event-fx and Datascript Transactions -------------------------------

;; Import/Export

(reg-event-fx
  :get-db/init
  (fn [{rfdb :db} _]
    {:db (-> db/rfdb
             (assoc :loading? true))
     :async-flow {:first-dispatch (if false
                                    [:local-storage/get-db]
                                    [:http/get-db])
                  :rules          [{:when :seen?
                                    :events :reset-conn
                                    :dispatch-n [[:loading/unset]
                                                 [:navigate (-> rfdb :current-route :data :name)]]
                                    :halt? true}]}}))


(reg-event-fx
  :http/get-db
  (fn [_ _]
    {:http {:method :get
            :url db/athens-url
            :opts {:with-credentials? false}
            :on-success [:http-success/get-db]
            :on-failure [:alert/set]}}))


(reg-event-fx
  :http-success/get-db
  (fn [_ [_ json-str]]
    (let [datoms (db/str-to-db-tx json-str)
          new-db (d/db-with (d/empty-db db/schema) datoms)]
      {:dispatch-n [[:reset-conn new-db]
                    [:local-storage/set-db new-db]]})))


(reg-event-fx
  :local-storage/get-db
  [(inject-cofx :local-storage "datascript/DB")]
  (fn [{:keys [local-storage]} _]
    {:dispatch [:reset-conn (dt/read-transit-str local-storage)]}))


(reg-event-fx
  :local-storage/set-db
  (fn [_ [_ db]]
    {:local-storage/set-db! db}))


;; Datascript

(reg-event-fx
  :transact
  (fn [_ [_ datoms]]
    {:transact! datoms}))


(reg-event-fx
  :reset-conn
  (fn [_ [_ db]]
    {:reset-conn! db}))


(reg-event-fx
  :page/create
  (fn [_ [_ title uid]]
    (let [now (now-ts)
          child-uid (gen-block-uid)
          child {:db/id -2 :create/time now :edit/time now :block/uid child-uid :block/order 0 :block/open true :block/string ""}]
      {:transact! [{:db/id -1 :node/title title :block/uid uid :create/time now :edit/time now :block/children [child]}]
       :dispatch [:editing/uid child-uid]})))


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


;; TODO: should be able to use :keys now: https://github.com/tonsky/datascript/blob/master/docs/queries.md
(defn map-order
  [blocks]
  (map (fn [[id order]] {:db/id id :block/order order}) blocks))


(defn inc-after
  [eid order]
  (->> (d/q '[:find ?ch ?new-o
              ;;:keys db/id block/order
              :in $ % ?p ?at
              :where (inc-after ?p ?at ?ch ?new-o)]
            @db/dsdb rules eid order)
       map-order))


(defn dec-after
  [eid order]
  (->> (d/q '[:find ?ch ?new-o
              :in $ % ?p ?at
              :where (dec-after ?p ?at ?ch ?new-o)]
            @db/dsdb rules eid order)
       map-order))


;; xxx 2 kinds of operations
;; write operations, it's nice to have entire block and entire parent block to make TXes
;; read operations (navigation), only need uids

;; xxx these all assume all blocks are open. have to skip closed blocks
;; TODO: focus AND set selection-start for :editing/uid



(defn deepest-child-block
  [id]
  (let [document (->> @(posh.reagent/pull db/dsdb '[:block/order :block/uid {:block/children ...}] id))]
    (loop [block document]
      (if (nil? (:block/children block))
        block
        (let [ch (:block/children block)
              n  (count ch)]
          (recur (get ch (dec n))))))))


(defn prev-sibling-uid
  [uid]
  @(posh.reagent/q '[:find ?sib-uid .
                     :in $ ?block-uid
                     :where
                     [?block :block/uid ?block-uid]
                     [?block :block/order ?block-o]
                     [?parent :block/children ?block]
                     [?parent :block/children ?sib]
                     [?sib :block/order ?sib-o]
                     [?sib :block/uid ?sib-uid]
                     [(dec ?block-o) ?prev-sib-o]
                     [(= ?sib-o ?prev-sib-o)]]
     db/dsdb uid))

;; if order 0, go to parent
;; if order n, go to prev siblings deepest child
(defn prev-block-uid
  [uid]
  (let [block (db/get-block [:block/uid uid])
        parent (db/get-parent [:block/uid uid])
        deepest-child-prev-sibling (deepest-child-block [:block/uid (prev-sibling-uid uid)])]
    (if (zero? (:block/order block))
      (:block/uid parent)
      (:block/uid deepest-child-prev-sibling))))

(reg-event-fx
  :up
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (prev-block-uid uid)]}))

(reg-event-fx
  :left
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (prev-block-uid uid)]}))


(defn next-sibling-block
  [uid]
  (d/q '[:find (pull ?sib [*]) .
         :in $ ?block-uid
         :where
         [?block :block/uid ?block-uid]
         [?block :block/order ?block-o]
         [?parent :block/children ?block]
         [?parent :block/children ?sib]
         [?sib :block/order ?sib-o]
         [?sib :block/uid ?sib-uid]
         [(inc ?block-o) ?prev-sib-o]
         [(= ?sib-o ?prev-sib-o)]]
    @db/dsdb uid))

(defn next-sibling-block-recursively
  [uid]
  (loop [uid uid]
    (let [sib (next-sibling-block uid)
          parent (db/get-parent [:block/uid uid])]
      (if (or sib (:node/title parent))
        sib
        (recur (:block/uid parent))))))

;; if child, go to child 0
;; else recursively find next sibling of parent
(defn next-block-uid
  [uid]
  (let [block (->> (db/get-block [:block/uid uid])
                db/sort-block-children)
        ch (:block/children block)
        next-block-recursive (next-sibling-block-recursively uid)]
    (cond
      ch (:block/uid (first ch))
      next-block-recursive (:block/uid next-block-recursive))))


(reg-event-fx
  :down
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (next-block-uid uid)]}))

(reg-event-fx
  :right
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (next-block-uid uid)]}))



;; TODO: if tail, append it to prev-block, which can be older sibling or parent block
;; FIXME: why does this jump up sometimes if previous block has content?
(defn backspace
  [uid value]
  (let [block (db/get-block [:block/uid uid])
        parent (db/get-parent [:block/uid uid])
        reindex (dec-after (:db/id parent) (:block/order block))
        editing-uid (-> parent
                        :block/children
                        (get (dec (:block/order block)))
                        :block/uid)]

    (cond
      (and (:node/title parent) (zero? (:block/order block))) nil

      :else {:dispatch-n [[:transact [[:db/retractEntity [:block/uid uid]]
                                      {:db/id (:db/id parent) :block/children reindex}]]
                          [:editing/uid editing-uid]]})))


(reg-event-fx
  :backspace
  (fn [_ [_ uid value]]
    (backspace uid value)))



(defn split-block
  [uid val index state]
  (let [parent (db/get-parent [:block/uid uid])
        block (db/get-block [:block/uid uid])
        head (subs val 0 index)
        tail (subs val index)
        new-uid (gen-block-uid)
        new-block {:db/id        -1
                   :block/order  (inc (:block/order block))
                   :block/uid    new-uid
                   :block/open   true
                   :block/string tail}
        reindex (->> (inc-after (:db/id parent) (:block/order block))
                     (concat [new-block]))]
    (swap! state assoc :atom-string head) ;; TODO: bad vibes but easiest solution right now
    {:transact! [[:db/add (:db/id block) :block/string head]
                 {:db/id (:db/id parent)
                  :block/children reindex}]
     :dispatch  [:editing/uid new-uid]}))


(defn bump-up
  "If user presses enter at the start of non-empty string, push that block down and
  and start editing a new block in the position of originating block - 'bump up' "
  [uid]
  (let [parent (db/get-parent [:block/uid uid])
        block (db/get-block [:block/uid uid])
        new-uid (gen-block-uid)
        new-block {:db/id        -1
                   :block/order  (:block/order block)
                   :block/uid    new-uid
                   :block/open   true
                   :block/string ""}
        reindex (->> (inc-after (:db/id parent) (dec (:block/order block)))
                     (concat [new-block]))]
    {:transact! [{:db/id (:db/id parent) :block/children reindex :block/string ""}]
     :dispatch [:editing/uid new-uid]}))


(defn new-block
  "Add a new-block after block"
  [block parent]
  (let [new-uid   (gen-block-uid)
        new-block {:block/order  (inc (:block/order block))
                   :block/uid    new-uid
                   :block/open   true
                   :block/string ""}
        reindex (->> (inc-after (:db/id parent) (:block/order block))
                     (concat [new-block]))]
    {:dispatch-n [[:transact [{:db/id          [:block/uid (:block/uid parent)]
                               :block/children reindex}]]
                  [:editing/uid new-uid]]}))


(defn enter
  [uid val index state]
  (let [block       (db/get-block [:block/uid uid])
        parent      (db/get-parent [:block/uid uid])
        root-block? (boolean (:node/title parent))]
    (cond
      (not (zero? index)) (split-block uid val index state)
      (and (empty? val) root-block?) (new-block block parent)
      (empty? val) {:dispatch [:unindent uid]}
      (and (zero? index) val) (bump-up uid))))


(reg-event-fx
  :enter
  (fn [_ [_ uid val index state]]
    (enter uid val index state)))


(defn indent
  [uid]
  (let [block (db/get-block [:block/uid uid])
        parent (db/get-parent [:block/uid uid])
        older-sib (->> parent
                       :block/children
                       (filter #(= (dec (:block/order block)) (:block/order %)))
                       first
                       :db/id
                       db/get-block)
        new-block {:db/id (:db/id block) :block/order (count (:block/children older-sib))}
        reindex-blocks (->> (dec-after (:db/id parent) (:block/order block)))]
    {:transact! [[:db/retract (:db/id parent) :block/children (:db/id block)]
                 {:db/id (:db/id older-sib) :block/children [new-block]} ;; becomes child of older sibling block — same parent but order-1
                 {:db/id (:db/id parent) :block/children reindex-blocks}]}))


(reg-event-fx
  :indent
  (fn [_ [_ uid]]
    (indent uid)))


;; TODO: no-op when user tries to unindent to a child out of current context
(defn unindent
  [uid]
  (let [parent (db/get-parent [:block/uid uid])
        grandpa (db/get-parent (:db/id parent))
        new-block {:block/uid uid :block/order (inc (:block/order parent))}
        reindex-grandpa (->> (inc-after (:db/id grandpa) (:block/order parent))
                             (concat [new-block]))]
    (when (and parent grandpa)
      {:transact! [[:db/retract (:db/id parent) :block/children [:block/uid uid]]
                   {:db/id (:db/id grandpa) :block/children reindex-grandpa}]})))


(reg-event-fx
  :unindent
  (fn [_ [_ uid]]
    (unindent uid)))


(defn target-child
  [source source-parent target]
  (let [new-block {:block/uid (:block/uid source) :block/order 0}
        new-parent-children (->> (dec-after (:db/id source-parent) (:block/order source)))
        new-target-children (->> (inc-after (:dbid target) 0)
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
                     map-order
                     (concat [new-block]))]
    [{:db/add (:db/id parent) :block/children reindex}]))


(defn target-sibling-diff-parent
  [source target source-parent target-parent]
  (let [new-block {:db/id (:db/id source) :block/order (inc (:block/order target))}
        source-parent-children (->> (d/q '[:find ?ch ?new-order
                                           :in $ % ?parent ?source-order
                                           :where (dec-after ?parent ?source-order ?ch ?new-order)]
                                         @db/dsdb rules (:db/id source-parent) (:block/order source))
                                    map-order)
        target-parent-children (->> (inc-after (:db/id target-parent) (:block/order target))
                                    (concat [new-block]))]
    [[:db/retract (:db/id source-parent) :block/children (:db/id source)]
     {:db/id (:db/id source-parent) :block/children source-parent-children} ;; reindex source
     {:db/id (:db/id target-parent) :block/children target-parent-children}])) ;; reindex target


(defn drop-bullet
  [source-uid target-uid kind]
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
       (not= source-parent target-parent) (target-sibling-diff-parent source target source-parent target-parent))}))


(reg-event-fx
  :drop-bullet
  (fn-traced [_ [_ source-uid target-uid kind]]
             (drop-bullet source-uid target-uid kind)))

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

