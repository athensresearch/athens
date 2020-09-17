(ns athens.events
  (:require
    [athens.db :as db :refer [rules get-children-recursively]]
    [athens.util :refer [now-ts gen-block-uid]]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [goog.dom :refer [getElement]]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx subscribe]]))


;; -- re-frame app-db events ---------------------------------------------

(reg-event-db
  :init-rfdb
  (fn-traced [_ _]
             db/rfdb))


(reg-event-db
  :db/update-filepath
  (fn [db [_ filepath]]
    (assoc db :db/filepath filepath)))


(reg-event-db
  :db/sync
  (fn [db [_]]
    (assoc db :db/synced true)))


(reg-event-db
  :db/not-synced
  (fn [db [_]]
    (assoc db :db/synced false)))


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


(reg-event-db
  :mouse-down/set
  (fn [db _]
    (assoc db :mouse-down true)))


(reg-event-db
  :mouse-down/unset
  (fn [db _]
    (assoc db :mouse-down false)))

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


(reg-event-db
  :dragging-global/toggle
  (fn [db _]
    (update db :dragging-global not)))


(reg-event-db
  :selected/add-item
  (fn [db [_ uid]]
    (update db :selected/items conj uid)))


(reg-event-db
  :selected/remove-item
  (fn [db [_ uid]]
    (let [items (:selected/items db)]
      (assoc db :selected/items (filterv #(not= % uid) items)))))


(reg-event-db
  :selected/add-items
  (fn [db [_ uids]]
    (update db :selected/items concat uids)))


(reg-event-db
  :selected/clear-items
  (fn [db _]
    (assoc db :selected/items [])))


(defn select-up
  [selected-items]
  (let [first-item      (first selected-items)
        prev-block-uid- (db/prev-block-uid first-item)
        prev-block      (db/get-block [:block/uid prev-block-uid-])
        parent          (db/get-parent [:block/uid first-item])
        editing-uid     @(subscribe [:editing/uid])
        editing-idx     (first (keep-indexed (fn [idx x]
                                               (when (= x editing-uid)
                                                 idx))
                                             selected-items))
        n               (count selected-items)
        new-items (cond
                    ;; if prev-block is root node TODO: (OR context root), don't do anything
                    (and (zero? editing-idx) (> n 1)) (pop selected-items)
                    (:node/title prev-block) selected-items
                    ;; if prev block is parent, replace editing/uid and first item w parent; remove children
                    (= (:block/uid parent) prev-block-uid-) (let [parent-children (-> (map #(:block/uid %) (:block/children parent))
                                                                                      set)
                                                                  to-keep         (filter (fn [x] (not (contains? parent-children x)))
                                                                                          selected-items)
                                                                  new-vec         (into [prev-block-uid-] to-keep)]
                                                              new-vec)
                    :else (into [prev-block-uid-] selected-items))]
    new-items))


(reg-event-db
  :selected/up
  (fn [db [_ selected-items]]
    (assoc db :selected/items (select-up selected-items))))


(defn select-down
  [selected-items]
  (let [editing-uid @(subscribe [:editing/uid])
        editing-idx (first (keep-indexed (fn [idx x]
                                           (when (= x editing-uid)
                                             idx))
                                         selected-items))
        last-item (last selected-items)
        next-block-uid- (db/next-block-uid last-item true)]
    (cond
      (pos? editing-idx) (subvec selected-items 1)
      next-block-uid-    (conj selected-items next-block-uid-)
      :else selected-items)))


;; using a set or a hash map, we would need a secondary editing/uid to maintain the head/tail position
;; this would let us know if the operation is additive or subtractive
(reg-event-db
  :selected/down
  (fn [db [_ selected-items]]
    (assoc db :selected/items (select-down selected-items))))


;; TODO: minus-after to reindex but what about nested blocks?
;; naive: assume all items are siblings of same parent. minus-after n where n is size of selected-items
;; better:
(reg-event-fx
  :selected/delete
  (fn [{:keys [db]} [_ selected-items]]
    (let [retract-vecs (mapv (fn [uid] [:db/retractEntity [:block/uid uid]])
                             selected-items)]
      {:dispatch [:transact retract-vecs]
       :db       (assoc db :selected/items [])})))


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
    (if-let [el (getElement id)]
      (.focus el))))


(reg-event-db
  :editing/uid
  (fn-traced [db [_ uid]]
             (js/setTimeout (focus-el (str "editable-uid-" uid)) 300)
             (assoc db :editing/uid uid)))


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
      {:fx [[:dispatch [:reset-conn new-db]
             :dispatch [:local-storage/set-db new-db]]]})))


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
    {:fx [[:dispatch [:db/not-synced]]
          [:transact! datoms]]}))


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
      {:fx [[:dispatch [:transact [{:db/id -1 :node/title title :block/uid uid :create/time now :edit/time now :block/children [child]}]]]
            [:dispatch [:editing/uid child-uid]]]})))


(defn delete-page
  "Retract all blocks of a page, including the page."
  [uid]
  (mapv (fn [uid] [:db/retractEntity [:block/uid uid]])
        (get-children-recursively uid)))


(reg-event-fx
  :page/delete
  (fn [_ [_ uid]]
    {:fx [[:dispatch [:transact (delete-page uid)]]]}))


(reg-event-fx
  :page/add-shortcut
  (fn [_ [_ uid]]
    (let [sidebar-ents (d/q '[:find ?e
                              :where
                              [?e :page/sidebar _]]
                            @db/dsdb)]
      {:fx [[:dispatch [:transact [{:block/uid uid :page/sidebar (count sidebar-ents)}]]]]})))


;; TODO: reindex
(reg-event-fx
  :page/remove-shortcut
  (fn [_ [_ uid]]
    {:fx [[:dispatch [:transact [[:db/retract [:block/uid uid] :page/sidebar]]]]]}))


(reg-event-fx
  :save
  (fn [_ _]
    (let [db-filepath (subscribe [:db/filepath])]
      {:fs/write! [@db-filepath (dt/write-transit-str @db/dsdb)]
       :dispatch  [:db/sync]})))


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


(defn inc-after
  [eid order]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at
              :where (inc-after ?p ?at ?ch ?new-o)]
            @db/dsdb rules eid order)))


(defn dec-after
  [eid order]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at
              :where (dec-after ?p ?at ?ch ?new-o)]
            @db/dsdb rules eid order)))


(defn plus-after
  [eid order x]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at ?x
              :where (plus-after ?p ?at ?ch ?new-o ?x)]
            @db/dsdb rules eid order x)))


(defn minus-after
  [eid order x]
  (->> (d/q '[:find ?ch ?new-o
              :keys db/id block/order
              :in $ % ?p ?at ?x
              :where (minus-after ?p ?at ?ch ?new-o ?x)]
            @db/dsdb rules eid order x)))


(reg-event-fx
  :up
  (fn [_ [_ uid]]
   ;; FIXME: specify behavior when going up would go to title or context-root
    {:dispatch [:editing/uid (or (db/prev-block-uid uid) uid)]}))


(reg-event-fx
  :left
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (or (db/prev-block-uid uid) uid)]}))


(reg-event-fx
  :down
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (or (db/next-block-uid uid) uid)]}))


(reg-event-fx
  :right
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (or (db/next-block-uid uid) uid)]}))


;; no-op if root 0th child
;; otherwise delete block and join with previous block
(defn backspace
  [uid value]
  (let [block (db/get-block [:block/uid uid])
        parent (db/get-parent [:block/uid uid])
        reindex (dec-after (:db/id parent) (:block/order block))
        prev-block-uid- (db/prev-block-uid uid)
        {prev-block-string :block/string} (db/get-block [:block/uid prev-block-uid-])]
    (cond
      (and (:node/title parent) (zero? (:block/order block))) nil
      (:block/children block) nil
      :else {:dispatch-later [{:ms 0 :dispatch [:transact [[:db/retractEntity [:block/uid uid]]
                                                           {:db/id [:block/uid prev-block-uid-] :block/string (str prev-block-string value) :edit/time (now-ts)}
                                                           {:db/id (:db/id parent) :block/children reindex}]]}
                              {:ms 10 :dispatch [:editing/uid prev-block-uid-]}]})))


(reg-event-fx
  :backspace
  (fn [_ [_ uid value]]
    (backspace uid value)))


(defn split-block
  [uid val index]
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
    {:fx [[:dispatch [:transact [{:db/id (:db/id block) :block/string head :edit/time (now-ts)}
                                 {:db/id (:db/id parent)
                                  :block/children reindex}]]]
          [:dispatch [:editing/uid new-uid]]]}))


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
    {:fx [[:dispatch [:transact [{:db/id (:db/id parent) :block/children reindex :block/string ""}]]]
          [:dispatch [:editing/uid new-uid]]]}))


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
    {:fx [[:dispatch [:transact [{:db/id          [:block/uid (:block/uid parent)]
                                  :block/children reindex}]]]
          [:dispatch [:editing/uid new-uid]]]}))


(defn enter
  [uid val index]
  (let [block       (db/get-block [:block/uid uid])
        parent      (db/get-parent [:block/uid uid])
        root-block? (boolean (:node/title parent))]
    (cond
      (not (zero? index)) (split-block uid val index)
      (and (empty? val) root-block?) (new-block block parent)
      (empty? val) {:dispatch [:unindent uid]}
      (and (zero? index) val) (bump-up uid))))


(reg-event-fx
  :enter
  (fn [_ [_ uid val index]]
    (enter uid val index)))


(defn indent
  "When indenting a single block:
  - retract block from parent
  - make block the last child of older sibling
  - reindex parent
  Only indent a block if it is not the zeroth block (first child).

  Uses `value` to update block/string as well. Otherwise, if user changes block string and indents, the local string
  is reset to original value, since it has not been unfocused yet (which is currently the transaction that updates the string)."
  [uid value]
  (let [block (db/get-block [:block/uid uid])
        block-zero? (zero? (:block/order block))]
    (when-not block-zero?
      (let [parent (db/get-parent [:block/uid uid])
            older-sib (->> parent
                           :block/children
                           (filter #(= (dec (:block/order block)) (:block/order %)))
                           first
                           :db/id
                           db/get-block)
            new-block {:db/id (:db/id block) :block/order (count (:block/children older-sib)) :block/string value}
            reindex (dec-after (:db/id parent) (:block/order block))]
        {:fx [[:dispatch [:transact [[:db/retract (:db/id parent) :block/children (:db/id block)]
                                     {:db/id (:db/id older-sib) :block/children [new-block]}
                                     {:db/id (:db/id parent) :block/children reindex}]]]]}))))


(reg-event-fx
  :indent
  (fn [_ [_ uid value]]
    (indent uid value)))


(defn indent-multi
  "Only indent if all blocks are siblings, and first block is not already a zeroth child (root child)."
  [uids]
  (let [blocks      (map #(db/get-block [:block/uid %]) uids)
        same-parents? (-> (map #(db/get-parent [:block/uid %]) uids)
                          set
                          count
                          (= 1))
        n-blocks    (count blocks)
        first-block (first blocks)
        last-block  (last blocks)
        block-zero? (-> first-block :block/order zero?)]
    (when (and same-parents? (not block-zero?))
      (let [parent (db/get-parent [:block/uid (:block/uid first-block)])
            older-sib (->> parent
                           :block/children
                           (filter #(= (dec (:block/order first-block)) (:block/order %)))
                           first
                           :db/id
                           db/get-block)
            n-sib (count (:block/children older-sib))
            new-blocks (map-indexed (fn [idx x]
                                      {:db/id       (:db/id x)
                                       :block/order (+ idx n-sib)})
                                    blocks)
            new-older-sib {:db/id (:db/id older-sib)
                           :block/children new-blocks}
            reindex (minus-after (:db/id parent) (:block/order last-block) n-blocks)
            new-parent {:db/id (:db/id parent) :block/children reindex}
            retracts (mapv (fn [x] [:db/retract (:db/id parent) :block/children (:db/id x)])
                           blocks)
            tx-data (conj retracts new-older-sib new-parent)]
        {:fx [[:dispatch [:transact tx-data]]]}))))


(reg-event-fx
  :indent/multi
  (fn [_ [_ uids]]
    (indent-multi uids)))


(defn unindent
  [uid value context-root-uid]
  (let [parent (db/get-parent [:block/uid uid])]
    ;; if parent is context-root or has node/title (date page), no-op
    (cond
      (:node/title parent) nil
      (= (:block/uid parent) context-root-uid) nil
      :else (let [grandpa         (db/get-parent (:db/id parent))
                  new-block       {:block/uid uid :block/order (inc (:block/order parent)) :block/string value}
                  reindex-grandpa (->> (inc-after (:db/id grandpa) (:block/order parent))
                                       (concat [new-block]))
                  new-parent      [:db/retract (:db/id parent) :block/children [:block/uid uid]]
                  new-grandpa     {:db/id (:db/id grandpa) :block/children reindex-grandpa}
                  tx-data         [new-parent new-grandpa]]
              {:fx [[:dispatch [:transact tx-data]]]}))))


(reg-event-fx
  :unindent
  (fn [{rfdb :db} [_ uid value]]
    (let [context-root-uid (get-in rfdb [:current-route :path-params :id])]
      (unindent uid value context-root-uid))))


(defn unindent-multi
  [uids context-root-uid]
  (let [parent (db/get-parent [:block/uid (first uids)])
        same-parents? (-> (map #(db/get-parent [:block/uid %]) uids)
                          set
                          count
                          (= 1))]
    (cond
      (:node/title parent) nil
      (= (:block/uid parent) context-root-uid) nil
      (not same-parents?) nil
      :else (let [grandpa         (db/get-parent (:db/id parent))
                  blocks          (map #(db/get-block [:block/uid %]) uids)
                  n-parent        (:block/order parent)
                  n-blocks        (count blocks)
                  new-blocks      (map-indexed (fn [idx uid] {:block/uid uid :block/order (+ idx (inc n-parent))})
                                               uids)
                  reindex-grandpa (->> (plus-after (:db/id grandpa) (:block/order parent) n-blocks)
                                       (concat new-blocks))
                  retracts        (mapv (fn [x] [:db/retract (:db/id parent) :block/children (:db/id x)])
                                        blocks)
                  new-grandpa     {:db/id (:db/id grandpa) :block/children reindex-grandpa}
                  tx-data         (conj retracts new-grandpa)]
              {:fx [[:dispatch [:transact tx-data]]]}))))


(reg-event-fx
  :unindent/multi
  (fn [{rfdb :db} [_ uids]]
    (let [context-root-uid (get-in rfdb [:current-route :path-params :id])]
      (unindent-multi uids context-root-uid))))


(defn drop-child
  "Order will always be 0"
  [source source-parent target]
  (let [new-source-block {:block/uid (:block/uid source) :block/order 0}
        reindex-source-parent (dec-after (:db/id source-parent) (:block/order source))
        reindex-target-parent (->> (inc-after (:dbid target) (dec 0))
                                   (concat [new-source-block]))]
    [[:db/retract (:db/id source-parent) :block/children [:block/uid (:block/uid source)]]
     {:db/id (:db/id source-parent) :block/children reindex-source-parent}
     {:db/id (:db/id target) :block/children reindex-target-parent}]))


(defn between
  "http://blog.jenkster.com/2013/11/clojure-less-than-greater-than-tip.html"
  [s t x]
  (if (< s t)
    (and (< s x) (< x t))
    (and (< t x) (< x s))))


(defn drop-above-same-parent
  "Give source block target block's order
  When source is below target, increment block orders between source and target-1
  When source is above target, decrement block order between them.
  No effect if block/orders wouldn't change: :above and s-order == t-order - 1"
  [source target parent]
  (let [s-order (:block/order source)
        t-order (:block/order target)
        no-effect? (= s-order (dec t-order))]
    (when-not no-effect?
      (let [new-source-block {:db/id (:db/id source) :block/order t-order}
            inc-or-dec       (if (> s-order t-order) inc dec)
            reindex          (->> (d/q '[:find ?ch ?new-order
                                         :keys db/id block/order
                                         :in $ ?parent ?s-order ?t-order ?between ?inc-or-dec
                                         :where
                                         [?parent :block/children ?ch]
                                         [?ch :block/order ?order]
                                         [(?between ?s-order ?t-order ?order)]
                                         [(?inc-or-dec ?order) ?new-order]]
                                       @db/dsdb (:db/id parent) s-order (dec t-order) between inc-or-dec)
                                  (concat [new-source-block]))]
        [{:db/id (:db/id parent) :block/children reindex}]))))


(defn drop-above-diff-parent
  [source target source-parent target-parent]
  (let [new-block             {:db/id (:db/id source) :block/order (:block/order target)}
        reindex-source-parent (dec-after (:db/id source-parent) (:block/order source))
        reindex-target-parent (->> (inc-after (:db/id target-parent) (dec (:block/order target)))
                                   (concat [new-block]))]
    [[:db/retract (:db/id source-parent) :block/children (:db/id source)]
     {:db/id (:db/id source-parent) :block/children reindex-source-parent}
     {:db/id (:db/id target-parent) :block/children reindex-target-parent}]))


(defn drop-below-same-parent
  "Source block's new order is target block's order.
  No effect if block/orders wouldn't change: :below and t-order == s-order - 1"
  [source parent target]
  (let [s-order (:block/order source)
        t-order (:block/order target)
        no-effect? (= (dec s-order) t-order)]
    (when-not no-effect?
      (let [new-source-block {:db/id (:db/id source) :block/order t-order}
            reindex (dec-after (:db/id parent) s-order)]
        (concat [new-source-block] reindex)))))


(defn drop-below-diff-parent
  "source block's new order is target-order + 1"
  [source source-parent target target-parent]
  (let [new-source-block {:db/id (:db/id source) :block/order (inc (:block/order target))}
        reindex-source-parent   (dec-after (:db/id source-parent) (:block/order source))]
    [[:db/retract (:db/id source-parent) :block/children (:db/id source)]
     {:db/id (:db/id source-parent) :block/children reindex-source-parent}
     {:db/id (:db/id target-parent) :block/children [new-source-block]}]))


;; TODO: don't transact when we know TXes won't change anything
(defn drop-bullet
  [source-uid target-uid kind]
  (let [source        (db/get-block [:block/uid source-uid])
        target        (db/get-block [:block/uid target-uid])
        source-parent (db/get-parent [:block/uid source-uid])
        target-parent (db/get-parent [:block/uid target-uid])
        same-parent? (= source-parent target-parent)]
    {:fx [[:dispatch
           [:transact
            (cond
              (= kind :child) (drop-child source source-parent target)
              (and (= kind :below) same-parent?) (drop-below-same-parent source source-parent target)
              (and (= kind :below) (not same-parent?)) (drop-below-diff-parent source source-parent target target-parent)
              (and (= kind :above) same-parent?) (drop-above-same-parent source target source-parent)
              (and (= kind :above) (not same-parent?)) (drop-above-diff-parent source target source-parent target-parent))]]]}))


(reg-event-fx
  :drop-bullet
  (fn-traced [_ [_ source-uid target-uid kind]]
             (drop-bullet source-uid target-uid kind)))


;; TODO: convert to tree instead of flat map (handling indentation), write tests for markdown list parsing
(reg-event-fx
  :paste
  (fn [_ [_ uid text]]
    (let [lines (clojure.string/split-lines text)
          block (db/get-block [:block/uid uid])
          {b-order :block/order} block
          parent (db/get-parent [:block/uid uid])
          {p-id :db/id} parent
          now (now-ts)
          new-datoms (map-indexed (fn [i x]
                                    (let [start (subs x 0 2)
                                          s (if (or (= start "- ")
                                                    (= start "* "))
                                              (subs x 2)
                                              x)]
                                      {:block/uid    (gen-block-uid)
                                       :create/time  now
                                       :edit/time    now
                                       :block/order  (+ 1 i b-order)
                                       :block/string s}))
                                  lines)
          reindex (plus-after p-id b-order (count lines))
          children (concat new-datoms reindex)]
      {:dispatch [:transact [{:db/id p-id :block/children children}]]})))


(defn left-sidebar-drop-above
  [s-order t-order]
  (let [source-eid (d/q '[:find ?e .
                          :in $ ?s-order
                          :where [?e :page/sidebar ?s-order]]
                        @db/dsdb s-order)
        new-source {:db/id source-eid :page/sidebar (if (< s-order t-order)
                                                      (dec t-order)
                                                      t-order)}
        inc-or-dec (if (< s-order t-order) dec inc)
        new-indices (->> (d/q '[:find ?shortcut ?new-order
                                :keys db/id page/sidebar
                                :in $ ?s-order ?t-order ?between ?inc-or-dec
                                :where
                                [?shortcut :page/sidebar ?order]
                                [(?between ?s-order ?t-order ?order)]
                                [(?inc-or-dec ?order) ?new-order]]
                              @db/dsdb s-order (if (< s-order t-order)
                                                 t-order
                                                 (dec t-order))
                              between inc-or-dec)
                         (concat [new-source]))]
    new-indices))


(reg-event-fx
  :left-sidebar/drop-above
  (fn-traced [_ [_ source-order target-order]]
             {:dispatch [:transact (left-sidebar-drop-above source-order target-order)]}))


(defn left-sidebar-drop-below
  [s-order t-order]
  (let [source-eid (d/q '[:find ?e .
                          :in $ ?s-order
                          :where [?e :page/sidebar ?s-order]]
                        @db/dsdb s-order)
        new-source {:db/id source-eid :page/sidebar t-order}
        new-indices (->> (d/q '[:find ?shortcut ?new-order
                                :keys db/id page/sidebar
                                :in $ ?s-order ?t-order ?between
                                :where
                                [?shortcut :page/sidebar ?order]
                                [(?between ?s-order ?t-order ?order)]
                                [(dec ?order) ?new-order]]
                              @db/dsdb s-order (inc t-order) between)
                         (concat [new-source]))]
    new-indices))


(reg-event-fx
  :left-sidebar/drop-below
  (fn-traced [_ [_ source-order target-order]]
             {:dispatch [:transact (left-sidebar-drop-below source-order target-order)]}))
