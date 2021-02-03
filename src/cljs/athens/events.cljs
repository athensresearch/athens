(ns athens.events
  (:require
    [athens.db :as db :refer [retract-uid-recursively inc-after dec-after plus-after minus-after]]
    [athens.keybindings :as keybindings]
    [athens.style :as style]
    [athens.util :refer [now-ts gen-block-uid]]
    [clojure.string :as string]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx subscribe]]))


;; -- re-frame app-db events ---------------------------------------------

(reg-event-db
  :init-rfdb
  (fn-traced [_ _]
             db/rfdb))


(reg-event-fx
  :db/update-filepath
  (fn [{:keys [db]} [_ filepath]]
    {:db (assoc db :db/filepath filepath)
     :local-storage/set! ["db/filepath" filepath]}))


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
  :right-sidebar/set-width
  (fn [db [_ width]]
    (assoc db :right-sidebar/width width)))


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
    (let [{:right-sidebar/keys [items]} db]
      (cond-> (update db :right-sidebar/items dissoc uid)
        (= 1 (count items)) (assoc :right-sidebar/open false)))))


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


(reg-event-fx
  :editing/uid
  (fn [{:keys [db]} [_ uid index]]
    {:db            (assoc db :editing/uid uid)
     :editing/focus [uid index]}))


(reg-event-fx
  :editing/target
  (fn [{:keys [db]} [_ target]]
    (let [uid (-> (.. target -id)
                  (string/split "editable-uid-")
                  second)]
      {:db (assoc db :editing/uid uid)})))


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


(defn delete-selected
  "We know that we only need to dec indices after the last block. The former blocks are necessarily going to remove all
  tail children, meaning we only need to be concerned with the last N blocks that are selected, adjacent siblings, to
  determine the minus-after value."
  [selected-items]
  (let [last-item (last selected-items)
        selected-sibs-of-last (->> (d/q '[:find ?sib-uid ?o
                                          :in $ ?uid [?selected ...]
                                          :where
                                          ;; get all siblings of the last block
                                          [?e :block/uid ?uid]
                                          [?p :block/children ?e]
                                          [?p :block/children ?sib]
                                          [?sib :block/uid ?sib-uid]
                                          ;; filter selected
                                          [(= ?sib-uid ?selected)]
                                          [?sib :block/order ?o]]
                                        @db/dsdb last-item selected-items)
                                   (sort-by second))
        [uid order] (last selected-sibs-of-last)
        parent (db/get-parent [:block/uid uid])
        n (count selected-sibs-of-last)]
    (minus-after (:db/id parent) order n)))


(reg-event-fx
  :selected/delete
  (fn [{:keys [db]} [_ selected-items]]
    (let [retract-vecs (mapcat #(retract-uid-recursively %) selected-items)
          reindex-last-selected-parent (delete-selected selected-items)
          tx-data (concat retract-vecs reindex-last-selected-parent)]
      {:dispatch [:transact tx-data]
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


;; Use native js/alert rather than custom UI alert
(reg-event-fx
  :alert/js
  (fn [_ [_ message]]
    {:alert/js! message}))


;; Modal


(reg-event-db
  :modal/toggle
  (fn [db _]
    (update db :modal not)))


;; Loading

(reg-event-db
  :loading/set
  (fn-traced [db]
             (assoc-in db [:loading?] true)))


(reg-event-db
  :loading/unset
  (fn-traced [db]
             (assoc-in db [:loading?] false)))


(reg-event-db
  :tooltip/uid
  (fn [db [_ uid]]
    (assoc db :tooltip/uid uid)))


;; Daily Notes

(reg-event-db
  :daily-notes/reset
  (fn [db _]
    (assoc db :daily-notes/items [])))


(reg-event-db
  :daily-notes/add
  (fn [db [_ uid]]
    (assoc db :daily-notes/items [uid])))


(reg-event-fx
  :daily-note/prev
  (fn [{:keys [db]} [_ {:keys [uid title]}]]
    (let [new-db (update db :daily-notes/items (fn [items]
                                                 (into [uid] items)))]
      (if (db/e-by-av :block/uid uid)
        {:db new-db}
        {:db        new-db
         :dispatch [:page/create title uid]}))))


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


(reg-event-fx
  :local-storage/set-theme
  [(inject-cofx :local-storage "theme/dark")]
  (fn [{:keys [local-storage db]} _]
    (let [is-dark (= "true" local-storage)
          theme   (if is-dark style/THEME-DARK style/THEME-LIGHT)]
      {:db          (assoc db :theme/dark is-dark)
       :stylefy/tag [":root" (style/permute-color-opacities theme)]})))


(reg-event-fx
  :theme/toggle
  (fn [{:keys [db]} _]
    (let [dark?    (:theme/dark db)
          new-dark (not dark?)
          theme    (if dark? style/THEME-LIGHT style/THEME-DARK)]
      {:db                 (assoc db :theme/dark new-dark)
       :local-storage/set! ["theme/dark" new-dark]
       :stylefy/tag        [":root" (style/permute-color-opacities theme)]})))


;; Datascript



(reg-event-fx
  :transact
  (fn [_ [_ tx-data]]
    ;; always stay synced for now because auto-saving
    (let [synced? @(subscribe [:db/synced])]
      {:fx [(when synced? [:dispatch [:db/not-synced]])
            [:dispatch [:save]]
            [:transact! tx-data]]})))


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


(reg-event-fx
  :page/delete
  (fn [_ [_ uid title]]
    (let [retract-blocks     (retract-uid-recursively uid)
          delete-linked-refs (db/replace-linked-refs title)
          tx-data            (concat retract-blocks
                                     delete-linked-refs)]
      {:fx [[:dispatch [:transact tx-data]]]})))


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
      {:fs/write!  [@db-filepath (dt/write-transit-str @db/dsdb)]})))


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


(reg-event-fx
  :up
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (or (db/prev-block-uid uid) uid)]}))


(reg-event-fx
  :down
  (fn [_ [_ uid]]
    {:dispatch [:editing/uid (or (db/next-block-uid uid) uid)]}))


(defn backspace
  "If root and 0th child, 1) if value, no-op, 2) if blank value, delete only block.
  No-op if parent is missing.
  No-op if parent is prev-block and block has children.
  No-op if prev-sibling-block has children.
  Otherwise delete block and join with previous block
  If prev-block has children"
  [uid value]
  (let [block           (db/get-block [:block/uid uid])
        {:block/keys [children order] :or {children []}} block
        parent          (db/get-parent [:block/uid uid])
        reindex         (dec-after (:db/id parent) (:block/order block))
        prev-block-uid- (db/prev-block-uid uid)
        prev-block      (db/get-block [:block/uid prev-block-uid-])
        prev-sib-order  (dec (:block/order block))
        prev-sib        (d/q '[:find ?sib .
                               :in $ % ?target-uid ?prev-sib-order
                               :where
                               (siblings ?target-uid ?sib)
                               [?sib :block/order ?prev-sib-order]
                               [?sib :block/uid ?uid]
                               [?sib :block/children ?ch]]
                             @db/dsdb db/rules uid prev-sib-order)
        prev-sib        (db/get-block prev-sib)
        retract-block  [:db/retractEntity (:db/id block)]
        new-parent     {:db/id (:db/id parent) :block/children reindex}]
    (cond
      (not parent) nil
      (and (empty? children) (:node/title parent) (zero? order) (clojure.string/blank? value)) (let [tx-data [retract-block new-parent]]
                                                                                                 {:dispatch-n [[:transact tx-data]
                                                                                                               [:editing/uid nil]]})
      (and (not-empty children) (not-empty (:block/children prev-sib))) nil
      (and (not-empty children) (= parent prev-block)) nil
      :else (let [retracts       (mapv (fn [x] [:db/retract (:db/id block) :block/children (:db/id x)]) children)
                  new-prev-block {:db/id          [:block/uid prev-block-uid-]
                                  :block/string   (str (:block/string prev-block) value)
                                  :block/children children}
                  tx-data        (conj retracts retract-block new-prev-block new-parent)]
              {:dispatch-later [{:ms 0 :dispatch [:transact tx-data]}
                                {:ms 10 :dispatch [:editing/uid prev-block-uid- (count (:block/string prev-block))]}]}))))


(reg-event-fx
  :backspace
  (fn [_ [_ uid value]]
    (backspace uid value)))


(defn split-block
  [uid val index]
  (let [parent     (db/get-parent [:block/uid uid])
        block      (db/get-block [:block/uid uid])
        {:block/keys [order children open] :or {children []}} block
        head       (subs val 0 index)
        tail       (subs val index)
        new-uid    (gen-block-uid)
        retracts   (mapv (fn [x] [:db/retract (:db/id block) :block/children (:db/id x)])
                         children)
        next-block  {:db/id          -1
                     :block/order    (inc order)
                     :block/uid      new-uid
                     :block/open     open
                     :block/children children
                     :block/string   tail}
        reindex    (->> (inc-after (:db/id parent) order)
                        (concat [next-block]))
        new-block  {:db/id (:db/id block) :block/string head}
        new-parent {:db/id (:db/id parent) :block/children reindex}
        tx-data    (conj retracts new-block new-parent)]
    {:fx [[:dispatch [:transact tx-data]]
          [:dispatch [:editing/uid new-uid]]]}))


(defn split-block-to-children
  "Takes a block uid, its value, and the index to split the value string.
  It sets the value of the block to the head of (subs val 0 index)
  It then creates a new child block with the tail of the string set as its value and sets editing to that block."
  [uid val index]
  (let [block (db/get-block [:block/uid uid])
        head (subs val 0 index)
        tail (subs val index)
        new-uid (gen-block-uid)
        new-block {:db/id        -1
                   :block/order  0
                   :block/uid    new-uid
                   :block/open   true
                   :block/string tail}
        reindex (->> (inc-after (:db/id block) -1)
                     (concat [new-block]))]
    {:fx [[:dispatch [:transact [{:db/id (:db/id block) :block/string head :edit/time (now-ts)}
                                 {:db/id (:db/id block)
                                  :block/children reindex}]]]
          [:dispatch [:editing/uid new-uid]]]}))


(reg-event-fx
  :split-block-to-children
  (fn [_ [_ uid val index]]
    (split-block-to-children uid val index)))


(defn bump-up
  "If user presses enter at the start of non-empty string, push that block down and
  and start editing a new block in the position of originating block - 'bump up' "
  [uid]
  (let [parent    (db/get-parent [:block/uid uid])
        block     (db/get-block [:block/uid uid])
        new-uid   (gen-block-uid)
        new-block {:db/id        -1
                   :block/order  (:block/order block)
                   :block/uid    new-uid
                   :block/open   true
                   :block/string ""}
        reindex   (->> (inc-after (:db/id parent) (dec (:block/order block)))
                       (concat [new-block]))]
    {:fx [[:dispatch [:transact [{:db/id (:db/id parent) :block/children reindex}]]]
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


(defn add-child
  [block]
  (let [{p-eid :db/id} block
        new-uid   (gen-block-uid)
        new-child {:block/uid new-uid :block/string "" :block/order 0 :block/open true}
        reindex   (->> (inc-after p-eid -1)
                       (concat [new-child]))
        new-block {:db/id p-eid :block/children reindex}
        tx-data   [new-block]]
    {:fx [[:dispatch [:transact tx-data]]
          [:dispatch [:editing/uid new-uid]]]}))


(reg-event-fx
  :enter/add-child
  (fn [_ [_ block]]
    (add-child block)))


(reg-event-fx
  :enter/split-block
  (fn [_ [_ uid val index]]
    (split-block uid val index)))


(reg-event-fx
  :enter/bump-up
  (fn [_ [_ uid]]
    (bump-up uid)))


(reg-event-fx
  :enter/new-block
  (fn [_ [_ block parent]]
    (new-block block parent)))


(defn enter
  "- If block is open, has children, and caret at end, create new child
  - If block is CLOSED, has children, and caret at end, add a sibling block.
  - If value is empty and a root block, add a sibling block.
  - If caret is not at start, split block in half.
  - If block has children and is closed, if at end, just add another child.
  - If block has children and is closed and is in middle of block, split block.
  - If value is empty, unindent.
  - If caret is at start and there is a value, create new block below but keep same block index."
  [rfdb uid d-key-down]
  (let [block            (db/get-block [:block/uid uid])
        parent           (db/get-parent [:block/uid uid])
        root-block?      (boolean (:node/title parent))
        context-root-uid (get-in rfdb [:current-route :path-params :id])
        {:keys [value start]} d-key-down
        event            (cond
                           (and (:block/open block)
                                (not-empty (:block/children block))
                                (= start (count value)))
                           [:enter/add-child block]

                           (and (not (:block/open block))
                                (not-empty (:block/children block))
                                (= start (count value)))
                           [:enter/new-block block parent]

                           (and (empty? value)
                                (or (= context-root-uid (:block/uid parent))
                                    root-block?))
                           [:enter/new-block block parent]

                           (not (zero? start))
                           [:enter/split-block uid value start]

                           (empty? value)
                           [:unindent uid d-key-down context-root-uid]

                           (and (zero? start) value)
                           [:enter/bump-up uid])]
    {:dispatch event}))


(reg-event-fx
  :enter
  (fn [{rfdb :db} [_ uid d-event]]
    (enter rfdb uid d-event)))


(defn indent
  "When indenting a single block:
  - retract block from parent
  - make block the last child of older sibling
  - reindex parent
  Only indent a block if it is not the zeroth block (first child).

  Uses `value` to update block/string as well. Otherwise, if user changes block string and indents, the local string
  is reset to original value, since it has not been unfocused yet (which is currently the transaction that updates the string)."
  [uid d-key-down]
  (let [{:keys [value start end]} d-key-down
        block       (db/get-block [:block/uid uid])
        block-zero? (zero? (:block/order block))]
    (when-not block-zero?
      (let [parent        (db/get-parent [:block/uid uid])
            older-sib     (db/get-older-sib uid)
            new-block     {:db/id (:db/id block) :block/order (count (:block/children older-sib)) :block/string value}
            reindex       (dec-after (:db/id parent) (:block/order block))
            retract       [:db/retract (:db/id parent) :block/children (:db/id block)]
            new-older-sib {:db/id (:db/id older-sib) :block/children [new-block] :block/open true}
            new-parent    {:db/id (:db/id parent) :block/children reindex}
            tx-data       [retract new-older-sib new-parent]]
        {:dispatch            [:transact tx-data]
         :set-cursor-position [uid start end]}))))


(reg-event-fx
  :indent
  (fn [_ [_ uid d-event]]
    (indent uid d-event)))


(defn indent-multi
  "Only indent if all blocks are siblings, and first block is not already a zeroth child (root child).

  older-sib is the current older-sib, before indent happens, AKA the new parent.
  new-parent is current parent, not older-sib. new-parent becomes grandparent.
  Reindex parent, add blocks to end of older-sib."
  [uids]
  (let [blocks       (map #(db/get-block [:block/uid %]) uids)
        same-parent? (db/same-parent? uids)
        n-blocks     (count blocks)
        first-block  (first blocks)
        last-block   (last blocks)
        block-zero?  (-> first-block :block/order zero?)]
    (when (and same-parent? (not block-zero?))
      (let [parent        (db/get-parent [:block/uid (first uids)])
            older-sib     (db/get-older-sib (first uids))
            n-sib         (count (:block/children older-sib))
            new-blocks    (map-indexed (fn [idx x] {:db/id (:db/id x) :block/order (+ idx n-sib)})
                                       blocks)
            new-older-sib {:db/id (:db/id older-sib) :block/children new-blocks :block/open true}
            reindex       (minus-after (:db/id parent) (:block/order last-block) n-blocks)
            new-parent    {:db/id (:db/id parent) :block/children reindex}
            retracts      (mapv (fn [x] [:db/retract (:db/id parent) :block/children (:db/id x)])
                                blocks)
            tx-data       (conj retracts new-older-sib new-parent)]
        {:fx [[:dispatch [:transact tx-data]]]}))))


(reg-event-fx
  :indent/multi
  (fn [_ [_ uids]]
    (indent-multi uids)))


(defn unindent
  "If parent is context-root or has node/title (date page), no-op.
  Otherwise, block becomes direct older sibling of parent (parent-order +1). reindex parent and grandparent.
   - inc-after for grandparent
   - dec-after for parent"
  [uid d-key-down context-root-uid]
  (let [parent (db/get-parent [:block/uid uid])
        {:keys [value start end]} d-key-down]
    (cond
      (:node/title parent) nil
      (= (:block/uid parent) context-root-uid) nil
      :else (let [block           (db/get-block [:block/uid uid])
                  grandpa         (db/get-parent (:db/id parent))
                  new-block       {:block/uid uid :block/order (inc (:block/order parent)) :block/string value}
                  reindex-grandpa (->> (inc-after (:db/id grandpa) (:block/order parent))
                                       (concat [new-block]))
                  reindex-parent  (dec-after (:db/id parent) (:block/order block))
                  new-parent      {:db/id (:db/id parent) :block/children reindex-parent}
                  retract         [:db/retract (:db/id parent) :block/children [:block/uid uid]]
                  new-grandpa     {:db/id (:db/id grandpa) :block/children reindex-grandpa}
                  tx-data         [retract new-parent new-grandpa]]
              {:dispatch            [:transact tx-data]
               :set-cursor-position [uid start end]}))))


(reg-event-fx
  :unindent
  (fn [{rfdb :db} [_ uid d-event]]
    (let [context-root-uid (get-in rfdb [:current-route :path-params :id])]
      (unindent uid d-event context-root-uid))))


(defn unindent-multi
  "Do not do anything if root block child or if blocks are not siblings.
  Otherwise, retract and assert new parent for each block, and reindex parent and grandparent."
  [uids context-root-uid]
  (let [parent (db/get-parent [:block/uid (first uids)])
        same-parent? (db/same-parent? uids)]
    (cond
      (:node/title parent) nil
      (= (:block/uid parent) context-root-uid) nil
      (not same-parent?) nil
      :else (let [grandpa         (db/get-parent (:db/id parent))
                  blocks          (map #(db/get-block [:block/uid %]) uids)
                  o-parent        (:block/order parent)
                  n-blocks        (count blocks)
                  last-block      (last blocks)
                  reindex-parent  (minus-after (:db/id parent) (:block/order last-block) n-blocks)
                  new-parent      {:db/id (:db/id parent) :block/children reindex-parent}
                  new-blocks      (map-indexed (fn [idx uid] {:block/uid uid :block/order (+ idx (inc o-parent))})
                                               uids)
                  reindex-grandpa (->> (plus-after (:db/id grandpa) (:block/order parent) n-blocks)
                                       (concat new-blocks))
                  retracts        (mapv (fn [x] [:db/retract (:db/id parent) :block/children (:db/id x)])
                                        blocks)
                  new-grandpa     {:db/id (:db/id grandpa) :block/children reindex-grandpa}
                  tx-data         (conj retracts new-parent new-grandpa)]
              {:fx [[:dispatch [:transact tx-data]]]}))))


(reg-event-fx
  :unindent/multi
  (fn [{rfdb :db} [_ uids]]
    (let [context-root-uid (get-in rfdb [:current-route :path-params :id])]
      (unindent-multi uids context-root-uid))))


(defn drop-child
  "Order will always be 0"
  [source source-parent target]
  (let [new-source-block      {:block/uid (:block/uid source) :block/order 0}
        reindex-source-parent (dec-after (:db/id source-parent) (:block/order source))
        reindex-target-parent (inc-after (:db/id target) -1)
        retract               [:db/retract (:db/id source-parent) :block/children [:block/uid (:block/uid source)]]
        new-source-parent     {:db/id (:db/id source-parent) :block/children reindex-source-parent}
        new-target-parent     {:db/id (:db/id target) :block/children (conj reindex-target-parent new-source-block)}
        tx-data               [retract
                               new-source-parent
                               new-target-parent]]
    tx-data))


(reg-event-fx
  :drop/child
  (fn [_ [_ source source-parent target]]
    {:dispatch [:transact (drop-child source source-parent target)]}))


(defn between
  "http://blog.jenkster.com/2013/11/clojure-less-than-greater-than-tip.html"
  [s t x]
  (if (< s t)
    (and (< s x) (< x t))
    (and (< t x) (< x s))))


(defn drop-same-parent
  [kind source parent target]
  (let [s-order             (:block/order source)
        t-order             (:block/order target)
        target-above?       (< t-order s-order)
        +or-                (if target-above? + -)
        above?              (= kind :above)
        below?              (= kind :below)
        lower-bound         (cond
                              (and above? target-above?) (dec t-order)
                              (and below? target-above?) t-order
                              :else s-order)
        upper-bound         (cond
                              (and above? (not target-above?)) t-order
                              (and below? (not target-above?)) (inc t-order)
                              :else s-order)
        reindex             (d/q '[:find ?ch ?new-order
                                   :keys db/id block/order
                                   :in $ % ?+or- ?parent ?lower-bound ?upper-bound
                                   :where
                                   (between ?parent ?lower-bound ?upper-bound ?ch ?order)
                                   [(?+or- ?order 1) ?new-order]]
                                 @db/dsdb db/rules +or- (:db/id parent) lower-bound upper-bound)
        new-source-order    (cond
                              (and above? target-above?) t-order
                              (and above? (not target-above?)) (dec t-order)
                              (and below? target-above?) (inc t-order)
                              (and below? (not target-above?)) t-order)
        new-source-block    {:db/id (:db/id source) :block/order new-source-order}
        new-parent-children (concat [new-source-block] reindex)
        new-parent          {:db/id (:db/id parent) :block/children new-parent-children}
        tx-data             [new-parent]]
    tx-data))


(reg-event-fx
  :drop/same
  (fn [_ [_ kind source parent target]]
    {:dispatch [:transact (drop-same-parent kind source parent target)]}))


(defn drop-diff-parent
  "- Give source-block target-block's order.
  - inc-after target
  - dec-after source"
  [kind source source-parent target target-parent]
  (let [t-order               (:block/order target)
        new-block             {:db/id (:db/id source) :block/order (if (= kind :above)
                                                                     t-order
                                                                     (inc t-order))}
        reindex-source-parent (dec-after (:db/id source-parent) (:block/order source))
        reindex-target-parent (->> (inc-after (:db/id target-parent) (if (= kind :above)
                                                                       (dec t-order)
                                                                       t-order))
                                   (concat [new-block]))
        retract               [:db/retract (:db/id source-parent) :block/children (:db/id source)]
        new-source-parent     {:db/id (:db/id source-parent) :block/children reindex-source-parent}
        new-target-parent     {:db/id (:db/id target-parent) :block/children reindex-target-parent}]
    [retract
     new-source-parent
     new-target-parent]))


(reg-event-fx
  :drop/diff
  (fn [_ [_ kind source source-parent target target-parent]]
    {:dispatch [:transact (drop-diff-parent kind source source-parent target target-parent)]}))


(defn drop-bullet
  [source-uid target-uid kind]
  (let [source        (db/get-block [:block/uid source-uid])
        target        (db/get-block [:block/uid target-uid])
        source-parent (db/get-parent [:block/uid source-uid])
        target-parent (db/get-parent [:block/uid target-uid])
        same-parent?  (= source-parent target-parent)
        event         (cond
                        (= kind :child) [:drop/child source source-parent target]
                        same-parent? [:drop/same kind source source-parent target]
                        (not same-parent?) [:drop/diff kind source source-parent target target-parent])]
    {:dispatch event}))


(reg-event-fx
  :drop
  (fn [_ [_ source-uid target-uid kind]]
    (drop-bullet source-uid target-uid kind)))


(defn drop-multi-same-parent-all
  [kind source-uids parent target]
  (let [source-blocks       (mapv #(db/get-block [:block/uid %]) source-uids)
        f-source            (first source-blocks)
        l-source            (last source-blocks)
        f-s-order           (:block/order f-source)
        l-s-order           (:block/order l-source)
        t-order             (:block/order target)
        target-above?       (< t-order f-s-order)
        +or-                (if target-above? + -)
        above?              (= kind :above)
        below?              (= kind :below)
        lower-bound         (cond
                              (and above? target-above?) (dec t-order)
                              (and below? target-above?) t-order
                              :else l-s-order)
        upper-bound         (cond
                              (and above? (not target-above?)) t-order
                              (and below? (not target-above?)) (inc t-order)
                              :else f-s-order)
        n                   (count source-uids)
        reindex             (d/q '[:find ?ch ?new-order
                                   :keys db/id block/order
                                   :in $ % ?+or- ?parent ?lower-bound ?upper-bound ?n
                                   :where
                                   (between ?parent ?lower-bound ?upper-bound ?ch ?order)
                                   [(?+or- ?order ?n) ?new-order]]
                                 @db/dsdb db/rules +or- (:db/id parent) lower-bound upper-bound n)
        new-source-blocks   (if target-above?
                              (map-indexed (fn [idx x]
                                             (let [new-order (cond-> (+ idx t-order) below? inc)]
                                               {:db/id       (:db/id x)
                                                :block/order new-order}))
                                           source-blocks)
                              (map-indexed (fn [idx x]
                                             (let [new-order (cond-> (- t-order idx) above? dec)]
                                               {:db/id       (:db/id x)
                                                :block/order new-order}))
                                           (reverse source-blocks)))
        new-parent-children (concat new-source-blocks reindex)
        new-parent          {:db/id (:db/id parent) :block/children new-parent-children}
        tx-data             [new-parent]]
    tx-data))


(defn drop-multi-same-source-parents
  [kind source-uids source-parent target target-parent]
  (let [source-blocks         (mapv #(db/get-block [:block/uid %]) source-uids)
        last-source           (last source-blocks)
        last-s-order          (:block/order last-source)
        t-order               (:block/order target)
        n                     (count source-uids)
        new-source-blocks     (map-indexed (fn [idx x]
                                             (let [new-order (if (= kind :above)
                                                               (+ idx t-order)
                                                               (inc (+ idx t-order)))]
                                               {:db/id (:db/id x) :block/order new-order}))
                                           source-blocks)
        reindex-source-parent (minus-after (:db/id source-parent) last-s-order n)
        bound                 (if (= kind :above) (dec t-order) t-order)
        reindex-target-parent (->> (plus-after (:db/id target-parent) bound n)
                                   (concat new-source-blocks))
        retracts              (map (fn [x] [:db/retract (:db/id source-parent) :block/children [:block/uid x]])
                                   source-uids)
        new-source-parent     {:db/id (:db/id source-parent) :block/children reindex-source-parent}
        new-target-parent     {:db/id (:db/id target-parent) :block/children reindex-target-parent}
        tx-data               (conj retracts new-source-parent new-target-parent)]
    tx-data))


(defn drop-multi-diff-source-parents
  "Only reindex after last target. plus-after"
  [kind source-uids target target-parent]
  (let [filtered-children          (->> (d/q '[:find ?children-uid ?o
                                               :keys block/uid block/order
                                               :in $ % ?target-uid ?not-contains? ?source-uids
                                               :where
                                               (siblings ?target-uid ?children-e)
                                               [?children-e :block/uid ?children-uid]
                                               [(?not-contains? ?source-uids ?children-uid)]
                                               [?children-e :block/order ?o]]
                                             @db/dsdb db/rules (:block/uid target) db/not-contains? (set source-uids))
                                        (sort-by :block/order)
                                        (mapv #(:block/uid %)))
        t-order                    (:block/order target)
        index                      (cond
                                     (= kind :above) t-order
                                     (and (= kind :below) (db/last-child? (:block/uid target))) t-order
                                     (= kind :below) (inc t-order))
        n                          (count filtered-children)
        head                       (subvec filtered-children 0 index)
        tail                       (subvec filtered-children index n)
        new-vec                    (concat head source-uids tail)
        new-source-uids            (map-indexed (fn [idx uid] {:block/uid uid :block/order idx}) new-vec)
        source-parents             (mapv #(db/get-parent [:block/uid %]) source-uids)
        source-blocks              (mapv #(db/get-block [:block/uid %]) source-uids)
        last-s-parent              (last source-parents)
        last-s-order               (:block/order (last source-blocks))
        n                          (count (filter (fn [x] (= (:block/uid x) (:block/uid last-s-parent))) source-parents))
        reindex-last-source-parent (minus-after (:db/id last-s-parent) last-s-order n)
        source-parents             (mapv #(db/get-parent [:block/uid %]) source-uids)
        retracts                   (mapv (fn [uid parent] [:db/retract (:db/id parent) :block/children [:block/uid uid]])
                                         source-uids
                                         source-parents)
        new-target-parent          {:db/id (:db/id target-parent) :block/children new-source-uids}
        ;; need to reindex last-source-parent but requires more index management depending on the level of the target parent
        new-source-parent          {:db/id (:db/id last-s-parent) :block/children reindex-last-source-parent}
        tx-data                    (conj retracts new-target-parent #_new-source-parent)]
    (identity new-source-parent)
    tx-data))


(defn drop-multi-child
  [source-uids target]
  (let [source-blocks         (mapv #(db/get-block [:block/uid %]) source-uids)
        source-parents        (mapv #(db/get-parent [:block/uid %]) source-uids)
        last-source           (last source-blocks)
        last-s-order          (:block/order last-source)
        last-s-parent         (last source-parents)
        new-source-blocks     (map-indexed (fn [idx x] {:block/uid (:block/uid x) :block/order idx})
                                           source-blocks)
        n                     (count (filter (fn [x] (= (:block/uid x) (:block/uid last-s-parent))) source-parents))
        reindex-source-parent (minus-after (:db/id last-s-parent) last-s-order n)
        reindex-target-parent (plus-after (:db/id target) -1 n)
        retracts              (mapv (fn [uid parent] [:db/retract (:db/id parent) :block/children [:block/uid uid]])
                                    source-uids
                                    source-parents)
        new-source-parent     {:db/id (:db/id last-s-parent) :block/children reindex-source-parent}
        new-target-parent     {:db/id (:db/id target) :block/children (concat reindex-target-parent new-source-blocks)}
        tx-data               (conj retracts new-source-parent new-target-parent)]
    tx-data))


(reg-event-fx
  :drop-multi/child
  (fn [_ [_ source-uid target]]
    {:dispatch [:transact (drop-multi-child source-uid target)]}))


(reg-event-fx
  :drop-multi/same-all
  (fn [_ [_ kind source-uids parent target]]
    {:dispatch [:transact (drop-multi-same-parent-all kind source-uids parent target)]}))


(reg-event-fx
  :drop-multi/diff-source
  (fn [_ [_ kind source-uids target target-parent]]
    {:dispatch [:transact (drop-multi-diff-source-parents kind source-uids target target-parent)]}))


(reg-event-fx
  :drop-multi/same-source
  (fn [_ [_ kind source-uids first-source-parent target target-parent]]
    {:dispatch [:transact (drop-multi-same-source-parents kind source-uids first-source-parent target target-parent)]}))


(defn drop-bullet-multi
  "Cases:
  - the same 4 cases from drop-bullet
  - but also if blocks span across multiple parent levels"
  [source-uids target-uid kind]
  (let [same-parent-all?     (db/same-parent? (conj source-uids target-uid))
        same-parent-source?  (db/same-parent? source-uids)
        diff-parents-source? (not same-parent-source?)
        target               (db/get-block [:block/uid target-uid])
        first-source-uid     (first source-uids)
        first-source-parent  (db/get-parent [:block/uid first-source-uid])
        target-parent        (db/get-parent [:block/uid target-uid])
        event                (cond
                               (= kind :child) [:drop-multi/child source-uids target]
                               (and same-parent-all?) [:drop-multi/same-all kind source-uids first-source-parent target]
                               (and diff-parents-source?) [:drop-multi/diff-source kind source-uids target target-parent]
                               (and same-parent-source?) [:drop-multi/same-source kind source-uids first-source-parent target target-parent])]
    {:fx [[:dispatch [:selected/clear-items]]
          [:dispatch event]]}))


(reg-event-fx
  :drop-multi
  (fn [_ [_ uids target-uid kind]]
    (drop-bullet-multi uids target-uid kind)))


(defn text-to-blocks
  [text uid root-order]
  (let [;; Split raw text by line
        lines       (clojure.string/split-lines text)
        ;; Count left offset
        left-counts (->> lines
                         (map #(re-find #"^\s*(-|\*)?" %))
                         (map #(-> % first count)))
        ;; Trim * - and whitespace
        sanitize    (map (fn [x] (clojure.string/replace x #"^\s*(-|\*)?\s*" ""))
                         lines)
        ;; Generate blocks with tempids
        blocks      (map-indexed (fn [idx x]
                                   {:db/id        (dec (* -1 idx))
                                    :block/string x
                                    :block/open   true
                                    :block/uid    (gen-block-uid)}) sanitize)
        ;; Count blocks
        n           (count blocks)
        ;; Assign parents
        parents     (loop [i   1
                           res [(first blocks)]]
                      (if (= n i)
                        res
                        ;; Nested loop: worst-case O(n^2)
                        (recur (inc i)
                               (loop [j (dec i)]
                                 ;; If j is negative, that means the loop has been compared to every previous line,
                                 ;; and there are no previous lines with smaller left-offsets, which means block i
                                 ;; should be a root block.
                                 ;; Otherwise, block i's parent is the first block with a smaller left-offset
                                 (if (neg? j)
                                   (conj res (nth blocks i))
                                   (let [curr-count (nth left-counts i)
                                         prev-count (nth left-counts j nil)]
                                     (if (< prev-count curr-count)
                                       (conj res {:db/id          (:db/id (nth blocks j))
                                                  :block/children (nth blocks i)})
                                       (recur (dec j)))))))))
        ;; assign orders for children. order can be local or based on outer context where paste originated
        ;; if local, look at order within group. if outer, use root-order
        tx-data     (->> (group-by :db/id parents)
                         ;; maps smaller than size 8 are ordered, larger are not https://stackoverflow.com/a/15500064
                         (into (sorted-map-by >))
                         (mapcat (fn [[_tempid blocks]]
                                   (loop [order 0
                                          res   []
                                          data  blocks]
                                     (let [{:block/keys [children] :as block} (first data)]
                                       (cond
                                         (nil? block) res
                                         (nil? children) (let [new-res (conj res {:db/id          [:block/uid uid]
                                                                                  :block/children (assoc block :block/order @root-order)})]
                                                           (swap! root-order inc)
                                                           (recur order
                                                                  new-res
                                                                  (next data)))
                                         :else (recur (inc order)
                                                      (conj res (assoc-in block [:block/children :block/order] order))
                                                      (next data))))))))]
    tx-data))


;; Paste based on conditions of block where paste originated from.
;; - If from an empty block, delete block in place and make that location the root
;; - If at text start of non-empty block, prepend block and focus first new root
;; - If anywhere else beyond text start of an OPEN parent block, prepend children
;; - Otherwise append after current block.

(reg-event-fx
  :paste
  (fn [_ [_ uid text]]
    (let [block         (db/get-block [:block/uid uid])
          {:block/keys [order children open]} block
          {:keys [start value]} (keybindings/destruct-target js/document.activeElement) ; TODO: coeffect
          empty-block?  (and (string/blank? value)
                             (empty? children))
          block-start?  (zero? start)
          parent?       (and children open)
          start-idx     (cond
                          empty-block? order
                          block-start? order
                          parent? 0
                          :else (inc order))
          root-order    (atom start-idx)
          parent        (cond
                          parent? block
                          :else (db/get-parent [:block/uid uid]))
          paste-tx-data (text-to-blocks text (:block/uid parent) root-order)
          ;; the delta between root-order and start-idx is how many root blocks were added
          n             (- @root-order start-idx)
          start-reindex (cond
                          block-start? (dec order)
                          parent? -1
                          :else order)
          amount        (cond
                          empty-block? (dec n)
                          :else n)
          reindex       (plus-after (:db/id parent) start-reindex amount)
          tx-data       (concat reindex
                                paste-tx-data
                                (when empty-block? [[:db/retractEntity [:block/uid uid]]]))]
      {:dispatch-n [[:transact tx-data]
                    (when block-start?
                      (let [block (-> paste-tx-data first :block/children)
                            {:block/keys [uid string]} block
                            n     (count string)]
                        [:editing/uid uid n]))]})))


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


(defn link-unlinked-reference
  "Ignores case. If title is `test`:
  test 1     -> [[test 1]]
  TEST 10    -> [[test 10]]
  [[attest]] -> [[at[[test]]`"
  [string title]
  (let [ignore-case-title (re-pattern (str "(?i)" title))
        new-str           (string/replace string ignore-case-title (str "[[" title "]]"))]
    new-str))


(reg-event-fx
  :unlinked-references/link
  (fn [_ [_ block title]]
    (let [{:block/keys [string uid]} block
          new-str (link-unlinked-reference string title)]
      {:dispatch [:transact [{:db/id [:block/uid uid] :block/string new-str}]]})))


(reg-event-fx
  :unlinked-references/link-all
  (fn [_ [_ unlinked-refs title]]
    (let [new-str-tx-data (->> unlinked-refs
                               (mapcat second unlinked-refs)
                               (map (fn [{:block/keys [string uid]}]
                                      (let [new-str (link-unlinked-reference string title)]
                                        {:db/id [:block/uid uid] :block/string new-str}))))]
      {:dispatch [:transact new-str-tx-data]})))
