(ns athens.events
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.composite :as composite-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver        :as resolver]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging                :as log]
    [athens.common.utils                  :as common.utils]
    [athens.dates                         :as dates]
    [athens.db                            :as db]
    [athens.electron.db-picker            :as db-picker]
    [athens.events.remote]
    [athens.patterns                      :as patterns]
    [athens.util                          :as util]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [clojure.string                       :as string]
    [datascript.core                      :as d]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [goog.dom :refer [getElement]]
    [re-frame.core :as rf  :refer [reg-event-db reg-event-fx inject-cofx subscribe]]))


;; -- re-frame app-db events ---------------------------------------------

;; TODO: boot/web should be rolled into boot/desktop to have a central boot
;; cycle that works with RTC.
(reg-event-fx
  :boot/web
  [(inject-cofx :local-storage :athens/persist)]
  (fn [{:keys [local-storage]} _]
    {:db         (db/init-app-db local-storage)
     :dispatch-n [[:theme/set]
                  [:loading/unset]]}))


(reg-event-db
  :init-rfdb
  (fn [_ _]
    db/rfdb))


(reg-event-db
  :db/sync
  (fn [db [_]]
    (assoc db :db/synced true)))


(reg-event-db
  :db/not-synced
  (fn [db [_]]
    (assoc db :db/synced false)))


(defn shared-blocks-excl-date-pages
  [roam-db]
  (->> (d/q '[:find [?blocks ...]
              :in $athens $roam
              :where
              [$athens _ :block/uid ?blocks]
              [$roam _ :block/uid ?blocks]
              [$roam ?e :block/uid ?blocks]
              [(missing? $roam ?e :node/title)]]
            @athens.db/dsdb
            roam-db)))


(defn merge-shared-page
  "If page exists in both databases, but roam-db's page has no children, then do not add the merge block"
  [shared-page roam-db roam-db-filename]
  (let [page-athens              (db/get-node-document shared-page)
        page-roam                (db/get-roam-node-document shared-page roam-db)
        athens-child-count       (-> page-athens :block/children count)
        roam-child-count         (-> page-roam :block/children count)
        new-uid                  (common.utils/gen-block-uid)
        today-date-page          (:title (dates/get-day))
        new-children             (conj (:block/children page-athens)
                                       {:block/string   (str "[[Roam Import]] "
                                                             "[[" today-date-page "]] "
                                                             "[[" roam-db-filename "]]")
                                        :block/uid      new-uid
                                        :block/children (:block/children page-roam)
                                        :block/order    athens-child-count
                                        :block/open     true})
        merge-pages              (merge page-roam page-athens)
        final-page-with-children (assoc merge-pages :block/children new-children)]
    (if (zero? roam-child-count)
      merge-pages
      final-page-with-children)))


(defn get-shared-pages
  [roam-db]
  (->> (d/q '[:find [?pages ...]
              :in $athens $roam
              :where
              [$athens _ :node/title ?pages]
              [$roam _ :node/title ?pages]]
            @athens.db/dsdb
            roam-db)
       sort))


(defn pages
  [roam-db]
  (->> (d/q '[:find [?pages ...]
              :in $
              :where
              [_ :node/title ?pages]]
            roam-db)
       sort))


(defn gett
  [s x]
  (not ((set s) x)))


(defn not-shared-pages
  [roam-db shared-pages]
  (->> (d/q '[:find [?pages ...]
              :in $ ?fn ?shared
              :where
              [_ :node/title ?pages]
              [(?fn ?shared ?pages)]]
            roam-db
            athens.events/gett
            shared-pages)
       sort))


(defn update-roam-db-dates
  "Strips the ordinal suffixes of Roam dates from block strings and dates.
  e.g. January 18th, 2021 -> January 18, 2021"
  [db]
  (let [date-pages         (d/q '[:find ?t ?u
                                  :keys node/title block/uid
                                  :in $ ?date
                                  :where
                                  [?e :node/title ?t]
                                  [(?date ?t)]
                                  [?e :block/uid ?u]]
                                db
                                patterns/date-block-string)
        date-block-strings (d/q '[:find ?s ?u
                                  :keys block/string block/uid
                                  :in $ ?date
                                  :where
                                  [?e :block/string ?s]
                                  [(?date ?s)]
                                  [?e :block/uid ?u]]
                                db
                                patterns/date-block-string)
        date-concat        (concat date-pages date-block-strings)
        tx-data            (map (fn [{:keys [block/string node/title block/uid]}]
                                  (cond-> {:db/id [:block/uid uid]}
                                    string (assoc :block/string (patterns/replace-roam-date string))
                                    title (assoc :node/title (patterns/replace-roam-date title))))
                                date-concat)]
    ;; tx-data))
    (d/db-with db tx-data)))


(reg-event-fx
  :upload/roam-edn
  (fn [_ [_ transformed-dates-roam-db roam-db-filename]]
    (let [shared-pages   (get-shared-pages transformed-dates-roam-db)
          merge-shared   (mapv (fn [x] (merge-shared-page [:node/title x] transformed-dates-roam-db roam-db-filename))
                               shared-pages)
          merge-unshared (->> (not-shared-pages transformed-dates-roam-db shared-pages)
                              (map (fn [x] (db/get-roam-node-document [:node/title x] transformed-dates-roam-db))))
          tx-data        (concat merge-shared merge-unshared)]
      ;; TODO: this functionality needs to create a internal representation event instead.
      ;; That will cause it to work in RTC and remove the need to transact directly to the in-memory db.
      {:dispatch [:transact tx-data]})))


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


;; no ops -- does not do anything
;; useful in situations where there is no dispatch value
(reg-event-fx
  :no-op
  (fn [_ _]
    (log/warn "Called :no-op re-frame event, this shouldn't be happening.")
    {}))


;; TODO: dec all indices > closed item
(reg-event-db
  :right-sidebar/close-item
  (fn [db [_ uid]]
    (let [{:right-sidebar/keys [items]} db]
      (cond-> (update db :right-sidebar/items dissoc uid)
        (= 1 (count items)) (assoc :right-sidebar/open false)))))


(reg-event-db
  :right-sidebar/navigate-item
  (fn [db [_ uid breadcrumb-uid]]
    (let [block      (d/pull @db/dsdb '[:node/title :block/string] [:block/uid breadcrumb-uid])
          item-index (get-in db [:right-sidebar/items uid :index])
          new-item   (merge block {:open true :index item-index})]
      (-> db
          (update-in [:right-sidebar/items] dissoc uid)
          (update-in [:right-sidebar/items] assoc breadcrumb-uid new-item)))))


;; TODO: change right sidebar items from map to datascript
(reg-event-fx
  :right-sidebar/open-item
  (fn [{:keys [db]} [_ uid is-graph?]]
    (let [block     (d/pull @db/dsdb '[:node/title :block/string] [:block/uid uid])
          new-item  (merge block {:open true :index -1 :is-graph? is-graph?})
          ;; Avoid a memory leak by forgetting the comparison function
          ;; that is stored in the sorted map
          ;; `(assoc (:right-sidebar/items db) uid new-item)`
          new-items (into {}
                          (assoc (:right-sidebar/items db) uid new-item))
          inc-items (reduce-kv (fn [m k v] (assoc m k (update v :index inc)))
                               {}
                               new-items)
          sorted-items (into (sorted-map-by (fn [k1 k2]
                                              (compare
                                                [(get-in inc-items [k1 :index]) k2]
                                                [(get-in inc-items [k2 :index]) k1]))) inc-items)]
      {:db         (assoc db :right-sidebar/items sorted-items)
       :dispatch-n [(when (not (:right-sidebar/open db)) [:right-sidebar/toggle])
                    [:right-sidebar/scroll-top]]})))


(reg-event-fx
  :right-sidebar/open-page
  (fn [{:keys [db]} [_ page-title is-graph?]]
    (let [{:keys [:block/uid]
           :as   block} (d/pull @db/dsdb '[:block/uid :node/title :block/string] [:node/title page-title])
          new-item      (merge block {:open true :index -1 :is-graph? is-graph?})
          ;; Avoid a memory leak by forgetting the comparison function
          ;; that is stored in the sorted map
          ;; `(assoc (:right-sidebar/items db) uid new-item)`
          new-items     (into {}
                              (assoc (:right-sidebar/items db) uid new-item))
          inc-items     (reduce-kv (fn [m k v] (assoc m k (update v :index inc)))
                                   {}
                                   new-items)
          sorted-items  (into (sorted-map-by (fn [k1 k2]
                                               (compare
                                                 [(get-in inc-items [k1 :index]) k2]
                                                 [(get-in inc-items [k2 :index]) k1]))) inc-items)]
      {:db         (assoc db :right-sidebar/items sorted-items)
       :dispatch-n [(when (not (:right-sidebar/open db)) [:right-sidebar/toggle])
                    [:right-sidebar/scroll-top]]})))


(reg-event-fx
  :right-sidebar/scroll-top
  (fn []
    {:right-sidebar/scroll-top nil}))


(reg-event-fx
  :editing/uid
  (fn [{:keys [db]} [_ uid index]]
    (let [remote? (db-picker/remote-db? db)]
      {:db            (assoc db :editing/uid uid)
       :editing/focus [uid index]
       :dispatch-n    [(when (and uid remote?)
                         [:presence/send-update {:block-uid uid}])]})))


(reg-event-fx
  :editing/target
  (fn [{:keys [db]} [_ target]]
    (let [uid (-> (.. target -id)
                  (string/split "editable-uid-")
                  second)]
      {:db (assoc db :editing/uid uid)})))


(reg-event-fx
  :editing/first-child
  (fn [_ [_ uid]]
    (when-let [first-block-uid (db/get-first-child-uid uid @db/dsdb)]
      {:dispatch [:editing/uid first-block-uid]})))


(defn select-up
  [selected-items]
  (let [first-item       (first selected-items)
        [_ o-embed]      (db/uid-and-embed-id first-item)
        prev-block-uid   (db/prev-block-uid first-item)
        prev-block-o-uid (-> prev-block-uid db/uid-and-embed-id first)
        prev-block       (db/get-block [:block/uid prev-block-o-uid])
        parent           (db/get-parent [:block/uid (-> first-item db/uid-and-embed-id first)])
        editing-uid      @(subscribe [:editing/uid])
        editing-idx      (first (keep-indexed (fn [idx x]
                                                (when (= x editing-uid)
                                                  idx))
                                              selected-items))
        n                (count selected-items)
        new-items        (cond
                           ;; if prev-block is root node TODO: (OR context root), don't do anything
                           (and (zero? editing-idx) (> n 1)) (pop selected-items)
                           (:node/title prev-block) selected-items
                           ;; if prev block is parent, replace editing/uid and first item w parent; remove children
                           (= (:block/uid parent) prev-block-o-uid) (let [parent-children (-> (map #(:block/uid %) (:block/children parent))
                                                                                              set)
                                                                          to-keep         (->> selected-items
                                                                                               (map #(-> % db/uid-and-embed-id first))
                                                                                               (filter (fn [x] (not (contains? parent-children x)))))
                                                                          new-vec         (into [prev-block-uid] to-keep)]
                                                                      new-vec)

                           ;; shift up started from inside the embed should not go outside embed block
                           o-embed (let [selected-uid (str prev-block-o-uid "-embed-" o-embed)
                                         html-el      (js/document.querySelector (str "#editable-uid-" prev-block-o-uid "-embed-" o-embed))]
                                     (if html-el
                                       (into [selected-uid] selected-items)
                                       selected-items))

                           :else (into [prev-block-uid] selected-items))]
    new-items))


(reg-event-db
  :selected/up
  (fn [db [_ selected-items]]
    (assoc-in db [:selection :items] (select-up selected-items))))


;; using a set or a hash map, we would need a secondary editing/uid to maintain the head/tail position
;; this would let us know if the operation is additive or subtractive
(reg-event-db
  :selected/down
  (fn [db [_ selected-items]]
    (let [last-item         (last selected-items)
          next-block-uid    (db/next-block-uid last-item true)
          ordered-selection (cond-> (into [] selected-items)
                              next-block-uid (into [next-block-uid]))]
      (log/debug ":selected/down, new-selection:" (pr-str ordered-selection))
      (assoc-in db [:selection :items] ordered-selection))))


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


(reg-event-fx
  :confirm/js
  (fn [_ [_ message true-cb false-cb]]
    {:confirm/js! [message true-cb false-cb]}))


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


;; Connection status

(reg-event-db
  :conn-status
  (fn [db [_ status]]
    (assoc db :connection-status status)))


;; Daily Notes

(reg-event-db
  :daily-note/reset
  (fn [db [_ uid]]
    (assoc db :daily-notes/items uid)))


(reg-event-db
  :daily-note/add
  (fn [db [_ uid]]
    (update db :daily-notes/items (comp rseq sort distinct conj) uid)))


(reg-event-fx
  :daily-note/ensure-day
  (fn [_ [_ {:keys [uid title]}]]
    (when-not (db/e-by-av :block/uid uid)
      {:dispatch [:page/new {:title     title
                             :block-uid (common.utils/gen-block-uid)}]})))


(reg-event-fx
  :daily-note/prev
  (fn [{:keys [db]} [_ {:keys [uid] :as day}]]
    (let [new-db (update db :daily-notes/items (fn [items]
                                                 (into [uid] items)))]
      {:db       new-db
       :dispatch [:daily-note/ensure-day day]})))


(reg-event-fx
  :daily-note/next
  (fn [_ [_ {:keys [uid] :as day}]]
    {:dispatch-n [[:daily-note/ensure-day day]
                  [:daily-note/add uid]]}))


(reg-event-fx
  :daily-note/delete
  (fn [{:keys [db]} [_ uid title]]
    (let [filtered-dn        (filterv #(not= % uid) (:daily-notes/items db)) ; Filter current date from daily note vec
          new-db (assoc db :daily-notes/items filtered-dn)]
      {:fx [[:dispatch [:page/delete title]]]
       :db new-db})))


(reg-event-fx
  :daily-note/scroll
  (fn [_ [_]]
    (let [daily-notes @(subscribe [:daily-notes/items])
          el          (getElement "daily-notes")
          offset-top  (.. el -offsetTop)
          rect        (.. el getBoundingClientRect)
          from-bottom (.. rect -bottom)
          from-top    (.. rect -top)
          doc-height  (.. js/document -documentElement -scrollHeight)
          top-delta   (- offset-top from-top)
          bottom-delta (- from-bottom doc-height)]
      ;; Don't allow user to scroll up for now.
      (cond
        (< top-delta 1) nil #_(dispatch [:daily-note/prev (get-day (uid-to-date (first daily-notes)) -1)])
        (< bottom-delta 1) {:fx [[:dispatch [:daily-note/next (dates/get-day (dates/uid-to-date (last daily-notes)) 1)]]]}))))


;; -- event-fx and Datascript Transactions -------------------------------

;; Import/Export


(reg-event-fx
  :http-success/get-db
  (fn [_ [_ json-str]]
    (let [datoms (db/str-to-db-tx json-str)
          new-db (d/db-with (d/empty-db common-db/schema) datoms)]
      {:dispatch [:reset-conn new-db]})))


(reg-event-fx
  :theme/set
  (fn [{:keys [db]} _]
    (util/switch-body-classes (if (-> db :athens/persist :theme/dark)
                                ["is-theme-light" "is-theme-dark"]
                                ["is-theme-dark" "is-theme-light"]))
    {}))


(reg-event-fx
  :theme/toggle
  (fn [{:keys [db]} _]
    {:db       (update-in db [:athens/persist :theme/dark] not)
     :dispatch [:theme/set]}))


;; Datascript



;; TODO: remove this event and also :transact! when the following are converted to events:
;; - athens.electron.images/dnd-image (needs file upload)
;; - :upload/roam-edn (needs internal representation)
;; - athens.self-hosted.client/db-dump-handler (needs internal representation)
;; - :undo / :redo
;; No other reframe events should be calling this event.
(reg-event-fx
  :transact
  (fn [_ [_ tx-data]]
    (let [synced?   @(subscribe [:db/synced])
          electron? (athens.util/electron?)]
      (if (and synced? electron?)
        {:fx [[:transact! tx-data]
              [:dispatch [:db/not-synced]]
              [:dispatch [:save]]]}
        {:fx [[:transact! tx-data]]}))))


(reg-event-fx
  :reset-conn
  (fn [_ [_ db]]
    {:reset-conn! db}))


(reg-event-fx
  :electron-sync
  (fn [_ _]
    (let [synced?   @(subscribe [:db/synced])
          electron? (athens.util/electron?)]
      (merge {}
             (when (and synced? electron?)
               {:fx [[:dispatch [:db/not-synced]]
                     [:dispatch [:save]]]})))))


;; Resolves events when the event-fx is resolved, instead of taking a resolution.
;; This is useful for when you want to apply events one after the other, using
;; the resulting db to resolve to next one.
(reg-event-fx
  :resolve-transact
  (fn [_ [_ event]]
    (log/debug "events/resolve-transact, event:" (pr-str event))
    (atomic-resolver/resolve-transact! db/dsdb event)
    {:fx [[:dispatch [:electron-sync]]]}))


;; Same as above, but also forwards the event.
;; Anything that uses atomic-resolver/resolve-transact! must duplicate the code
;; to ensure db/dsdb is synchronized and cannot just reuse the existing :resolve* events.
(reg-event-fx
  :resolve-transact-forward
  (fn [{:keys [db]} [_ event]]
    (let [forward? (db-picker/remote-db? db)]
      (log/debug ":resolve-transact-forward event:" (pr-str event) "forward?" (pr-str forward?))
      (atomic-resolver/resolve-transact! db/dsdb event)
      {:fx [[:dispatch-n [[:electron-sync]
                          (when forward? [:remote/forward-event event])]]]})))


(reg-event-fx
  :page/delete
  (fn [_ [_ title]]
    (log/debug ":page/delete:" title)
    (let [event (common-events/build-atomic-event (atomic-graph-ops/make-page-remove-op title))]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :left-sidebar/add-shortcut
  (fn [_ [_ name]]
    (log/debug ":page/add-shortcut:" name)
    (let [add-shortcut-op (atomic-graph-ops/make-shortcut-new-op name)
          event           (common-events/build-atomic-event add-shortcut-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :left-sidebar/remove-shortcut
  (fn [_ [_ name]]
    (log/debug ":page/remove-shortcut:" name)
    (let [remove-shortcut-op (atomic-graph-ops/make-shortcut-remove-op name)
          event              (common-events/build-atomic-event remove-shortcut-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :left-sidebar/drop-above
  (fn [_ [_ source-order target-order]]
    (log/debug ":left-sidebar/drop-above" ", source-order:" source-order ", target-order:" target-order)
    (let [event (common-events/build-left-sidebar-drop-above source-order target-order)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :left-sidebar/drop-below
  (fn [_ [_ source-order target-order]]
    (log/debug ":left-sidebar/drop-below" ", source-order:" source-order ", target-order:" target-order)
    (let [event (common-events/build-left-sidebar-drop-below source-order target-order)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :save
  (fn [_ _]
    {:fs/write! nil}))


(reg-event-fx
  :undo
  (fn [{:keys [db]} _]
    (log/debug ":undo")
    (let [local? (not (db-picker/remote-db? db))]
      (log/debug ":undo: local?" local?)
      (if local?
        (let [undo-event (common-events/build-undo-redo-event  false)
              tx-data    (resolver/resolve-event-to-tx db/history undo-event)]
          {:fx [[:dispatch [:transact tx-data]]]})
        {:fx [[:dispatch [:alert/js "Undo not supported in Lan-Party, yet."]]]}))))


(reg-event-fx
  :redo
  (fn [{:keys [db]} _]
    (log/debug ":redo")
    (let [local? (not (db-picker/remote-db? db))]
      (log/debug ":redo: local?" local?)
      (if local?
        (let [redo-event (common-events/build-undo-redo-event  true)
              tx-data    (resolver/resolve-event-to-tx db/history redo-event)]
          {:fx [[:dispatch [:transact tx-data]]]})
        {:fx [[:dispatch [:alert/js "Redo not supported in Lan-Party, yet."]]]}))))


(reg-event-fx
  :up
  (fn [_ [_ uid]]
    (let [prev-block-uid (db/prev-block-uid uid)]
      {:dispatch [:editing/uid (or prev-block-uid uid)]})))


(reg-event-fx
  :down
  (fn [_ [_ uid]]
    (let [next-block-uid (db/next-block-uid uid)]
      {:dispatch [:editing/uid (or next-block-uid uid)]})))


(defn backspace
  "If root and 0th child, 1) if value, no-op, 2) if blank value, delete only block.
  No-op if parent is missing.
  No-op if parent is prev-block and block has children.
  No-op if prev-sibling-block has children.
  Otherwise delete block and join with previous block
  If prev-block has children"
  [uid value]
  (let [root-embed?     (= (some-> (str "#editable-uid-" uid)
                                   js/document.querySelector
                                   (.. (closest ".block-embed"))
                                   (. -firstChild)
                                   (.getAttribute "data-uid"))
                           uid)
        db              @db/dsdb
        [uid embed-id]  (common-db/uid-and-embed-id uid)
        block           (common-db/get-block db [:block/uid uid])
        {:block/keys [children order] :or {children []}} block
        parent          (common-db/get-parent db [:block/uid uid])
        prev-block-uid  (common-db/prev-block-uid db uid)
        prev-block      (common-db/get-block db [:block/uid prev-block-uid])
        prev-sib-order  (dec (:block/order block))
        prev-sib        (some->> (common-db/prev-sib db uid prev-sib-order)
                                 (common-db/get-block db))
        event           (cond
                          (or (not parent)
                              root-embed?
                              (and (not-empty children) (not-empty (:block/children prev-sib)))
                              (and (not-empty children) (= parent prev-block)))
                          nil

                          (and (empty? children) (:node/title parent) (zero? order) (clojure.string/blank? value))
                          [:backspace/delete-only-child uid]

                          :else
                          [:backspace/delete-merge-block {:uid uid
                                                          :value value
                                                          :prev-block-uid prev-block-uid
                                                          :embed-id embed-id
                                                          :prev-block prev-block}])]
    (log/debug "[Backspace] args:" (pr-str {:uid uid
                                            :value value})
               ", event:" (pr-str event))
    (when event
      {:fx [[:dispatch event]]})))


;; todo(abhinav) -- stateless backspace
;; will pick db value of backspace/delete instead of current state
;; which might not be same as blur is not yet called
(reg-event-fx
  :backspace
  (fn [_ [_ uid value]]
    (backspace uid value)))


(reg-event-fx
  :backspace/delete-only-child
  (fn [_ [_ uid]]
    (log/debug ":backspace/delete-only-child:" (pr-str uid))
    (let [op    (graph-ops/build-block-remove-op @db/dsdb uid)
          event (common-events/build-atomic-event op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:dispatch [:editing/uid nil]]]})))


;; Atomic events start ==========

(reg-event-fx
  :enter/new-block
  (fn [_ [_ {:keys [block parent new-uid embed-id]}]]
    (log/debug ":enter/new-block" (pr-str block) (pr-str parent) (pr-str new-uid))
    (let [op    (atomic-graph-ops/make-block-new-op new-uid {:ref-uid (:block/uid block)
                                                             :relation :after})
          event (common-events/build-atomic-event op)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


(reg-event-fx
  :block/save
  (fn [{:keys [db]} [_ {:keys [uid old-string string callback] :as args}]]
    (log/debug ":block/save args" (pr-str args))
    (let [local?      (not (db-picker/remote-db? db))
          block-eid   (common-db/e-by-av @db/dsdb :block/uid uid)
          do-nothing? (or (not block-eid)
                          (= old-string string))
          op          (graph-ops/build-block-save-op @db/dsdb uid string)
          event       (common-events/build-atomic-event op)]
      (log/debug ":block/save local?" local?
                 ", do-nothing?" do-nothing?)
      (when-not do-nothing?
        {:fx [[:dispatch [:resolve-transact-forward event]]
              [:invoke-callback callback]]}))))


(reg-event-fx
  :page/new
  (fn [_ [_ {:keys [title block-uid shift?] :or {shift? false} :as args}]]
    (log/debug ":page/new args" (pr-str args))
    (let [event (common-events/build-atomic-event (graph-ops/build-page-new-op @db/dsdb
                                                                               title
                                                                               block-uid))]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:page/new-followup title shift?]
                          [:editing/uid block-uid]]]]})))


(reg-event-fx
  :page/rename
  (fn [_ [_ {:keys [old-name new-name callback] :as args}]]
    (log/debug ":page/rename args:" (pr-str (select-keys args [:old-name :new-name])))
    (let [event (common-events/build-atomic-event (atomic-graph-ops/make-page-rename-op old-name new-name))]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:invoke-callback callback]]})))


(reg-event-fx
  :page/merge
  (fn [_ [_ {:keys [from-name to-name callback] :as args}]]
    (log/debug ":page/merge args:" (pr-str (select-keys args [:from-name :to-name])))
    (let [event (common-events/build-atomic-event (atomic-graph-ops/make-page-merge-op from-name to-name))]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:invoke-callback callback]]})))


(reg-event-fx
  :page/new-followup
  (fn [_ [_ title shift?]]
    (log/debug ":page/new-followup title" title "shift?" shift?)
    (let [page-uid (common-db/get-page-uid @db/dsdb title)]
      {:fx [[:dispatch-n [(cond
                            shift?
                            [:right-sidebar/open-item page-uid]

                            (not (dates/is-daily-note page-uid))
                            [:navigate :page {:id page-uid}]

                            (dates/is-daily-note page-uid)
                            [:daily-note/add page-uid])]]]})))


(reg-event-fx
  :backspace/delete-merge-block
  (fn [_ [_ {:keys [uid value prev-block-uid embed-id prev-block] :as args}]]
    (log/debug ":backspace/delete-merge-block args:" (pr-str args))
    (let [op    (graph-ops/build-block-remove-merge-op @db/dsdb
                                                       uid
                                                       prev-block-uid
                                                       value)
          event (common-events/build-atomic-event  op)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid
                           (cond-> prev-block-uid
                             embed-id (str "-embed-" embed-id))
                           (count (:block/string prev-block))]]]]})))


;; Atomic events end ==========


(reg-event-fx
  :enter/add-child
  (fn [_ [_ {:keys [block new-uid embed-id] :as args}]]
    (log/debug ":enter/add-child args:" (pr-str args))
    (let [position (common-db/compat-position @db/dsdb {:ref-uid  (:block/uid block)
                                                        :relation :first})
          event    (common-events/build-atomic-event (atomic-graph-ops/make-block-new-op new-uid position))]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


(reg-event-fx
  :enter/split-block
  (fn [_ [_ {:keys [uid new-uid value index embed-id relation] :as args}]]
    (log/debug ":enter/split-block" (pr-str args))
    (let [op    (graph-ops/build-block-split-op @db/dsdb
                                                {:old-block-uid uid
                                                 :new-block-uid new-uid
                                                 :string        value
                                                 :index         index
                                                 :relation      relation})
          event (common-events/build-atomic-event op)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


(reg-event-fx
  :enter/bump-up
  (fn [_ [_ {:keys [uid new-uid embed-id] :as args}]]
    (log/debug ":enter/bump-up args" (pr-str args))
    (let [position (common-db/compat-position @db/dsdb {:ref-uid  uid
                                                        :relation :before})
          event    (common-events/build-atomic-event (atomic-graph-ops/make-block-new-op new-uid position))]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


(reg-event-fx
  :enter/open-block-add-child
  (fn [_ [_ {:keys [block new-uid embed-id]}]]
    ;; Triggered when there is a closed embeded block with no content in the top level block
    ;; and then one presses enter in the embeded block.
    (log/debug ":enter/open-block-add-child" (pr-str block) (pr-str new-uid))
    (let [block-uid                  (:block/uid block)
          block-open-op           (atomic-graph-ops/make-block-open-op block-uid
                                                                       true)
          position                (common-db/compat-position @db/dsdb {:ref-uid  (:block/uid block)
                                                                       :relation :first})
          add-child-op            (atomic-graph-ops/make-block-new-op new-uid position)
          open-block-add-child-op (composite-ops/make-consequence-op {:op/type :open-block-add-child}
                                                                     [block-open-op
                                                                      add-child-op])
          event                   (common-events/build-atomic-event open-block-add-child-op)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


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
  (let [root-embed?           (= (some-> d-key-down :target
                                         (.. (closest ".block-embed"))
                                         (. -firstChild)
                                         (.getAttribute "data-uid"))
                                 uid)
        [uid embed-id]        (db/uid-and-embed-id uid)
        block                 (db/get-block [:block/uid uid])
        {parent-uid :block/uid
         :as        parent}   (db/get-parent [:block/uid uid])
        is-parent-root-embed? (= (some-> d-key-down :target
                                         (.. (closest ".block-embed"))
                                         (. -firstChild)
                                         (.getAttribute "data-uid"))
                                 (str parent-uid "-embed-" embed-id))
        root-block?           (boolean (:node/title parent))
        context-root-uid      (get-in rfdb [:current-route :path-params :id])
        new-uid               (common.utils/gen-block-uid)

        {:keys [value start]} d-key-down
        event                 (cond
                                (and (:block/open block)
                                     (not-empty (:block/children block))
                                     (= start (count value)))
                                [:enter/add-child {:block    block
                                                   :new-uid  new-uid
                                                   :embed-id embed-id}]

                                (and embed-id root-embed?
                                     (= start (count value)))
                                [:enter/open-block-add-child {:block    block
                                                              :new-uid  new-uid
                                                              :embed-id embed-id}]

                                (and (not (:block/open block))
                                     (not-empty (:block/children block))
                                     (= start (count value)))
                                [:enter/new-block {:block    block
                                                   :parent   parent
                                                   :new-uid  new-uid
                                                   :embed-id embed-id}]

                                (and (empty? value)
                                     (or (= context-root-uid (:block/uid parent))
                                         root-block?))
                                [:enter/new-block {:block    block
                                                   :parent   parent
                                                   :new-uid  new-uid
                                                   :embed-id embed-id}]

                                (and (:block/open block)
                                     embed-id root-embed?
                                     (not= start (count value)))
                                [:enter/split-block {:uid        uid
                                                     :value      value
                                                     :index      start
                                                     :new-uid    new-uid
                                                     :embed-id   embed-id
                                                     :relation   :first}]

                                (and (empty? value) embed-id (not is-parent-root-embed?))
                                [:unindent {:uid              uid
                                            :d-key-down       d-key-down
                                            :context-root-uid context-root-uid
                                            :embed-id         embed-id
                                            :local-string     ""}]

                                (and (empty? value) embed-id is-parent-root-embed?)
                                [:enter/new-block {:block    block
                                                   :parent   parent
                                                   :new-uid  new-uid
                                                   :embed-id embed-id}]

                                (not (zero? start))
                                [:enter/split-block {:uid        uid
                                                     :value      value
                                                     :index      start
                                                     :new-uid    new-uid
                                                     :embed-id   embed-id
                                                     :relation   :after}]

                                (empty? value)
                                [:unindent {:uid              uid
                                            :d-key-down       d-key-down
                                            :context-root-uid context-root-uid
                                            :embed-id         embed-id
                                            :local-string     ""}]

                                (and (zero? start) value)
                                [:enter/bump-up {:uid      uid
                                                 :new-uid  new-uid
                                                 :embed-id embed-id}])]
    (log/debug "[Enter] ->" (pr-str event))
    (assert parent-uid (str "[Enter] no parent for block-uid: " uid))
    {:fx [[:dispatch event]]}))


(reg-event-fx
  :enter
  (fn [{rfdb :db} [_ uid d-event]]
    (enter rfdb uid d-event)))


(defn get-prev-block-uid-and-target-rel
  [uid]
  (let [prev-block-uid            (:block/uid (common-db/nth-sibling @db/dsdb uid -1))
        prev-block-children?      (if prev-block-uid
                                    (seq (:block/children (common-db/get-block @db/dsdb [:block/uid prev-block-uid])))
                                    nil)
        target-rel                (if prev-block-children?
                                    :last
                                    :first)]
    [prev-block-uid target-rel]))


(defn block-save-block-move-composite-op
  [source-uid ref-uid relation string]
  (let [block-save-op             (graph-ops/build-block-save-op @db/dsdb source-uid string)
        location                  (common-db/compat-position @db/dsdb {:ref-uid ref-uid
                                                                       :relation relation})
        block-move-op             (atomic-graph-ops/make-block-move-op source-uid
                                                                       location)
        block-save-block-move-op  (composite-ops/make-consequence-op {:op/type :block-save-block-move}
                                                                     [block-save-op
                                                                      block-move-op])]
    block-save-block-move-op))


(reg-event-fx
  :indent
  (fn [{:keys [_db]} [_ {:keys [uid d-key-down local-string] :as args}]]
    ;; - `block-zero`: The first block in a page
    ;; - `value`     : The current string inside the block being indented. Otherwise, if user changes block string and indents,
    ;;                 the local string  is reset to original value, since it has not been unfocused yet (which is currently the
    ;;                 transaction that updates the string).
    (let [block                        (common-db/get-block @db/dsdb [:block/uid uid])
          block-zero?                  (zero? (:block/order block))
          [prev-block-uid target-rel]  (get-prev-block-uid-and-target-rel uid)
          {:keys [start end]}          d-key-down
          block-save-block-move-op     (block-save-block-move-composite-op uid
                                                                           prev-block-uid
                                                                           target-rel
                                                                           local-string)
          event                        (common-events/build-atomic-event block-save-block-move-op)]

      (log/debug "null-sib-uid" (and block-zero?
                                     prev-block-uid)
                 ", args:" (pr-str args)
                 ", block-zero?" block-zero?)
      (when (and prev-block-uid
                 (not block-zero?))
        {:fx [[:dispatch            [:resolve-transact-forward event]]
              [:set-cursor-position [uid start end]]]}))))


(reg-event-fx
  :indent/multi
  (fn [_ [_ {:keys [uids]}]]
    (log/debug ":indent/multi" (pr-str uids))
    (let [sanitized-selected-uids  (mapv (comp first common-db/uid-and-embed-id) uids)
          f-uid                    (first sanitized-selected-uids)
          dsdb                     @db/dsdb
          [prev-block-uid
           target-rel]             (get-prev-block-uid-and-target-rel f-uid)
          same-parent?             (common-db/same-parent? dsdb sanitized-selected-uids)
          first-block-order        (:block/order (common-db/get-block dsdb [:block/uid f-uid]))
          block-zero?              (zero? first-block-order)]
      (log/debug ":indent/multi same-parent?" same-parent?
                 ", not block-zero?" (not  block-zero?))
      (when (and same-parent? (not block-zero?))
        {:fx [[:dispatch [:drop-multi/sibling {:source-uids sanitized-selected-uids
                                               :target-uid  prev-block-uid
                                               :drag-target target-rel}]]]}))))


(reg-event-fx
  :unindent
  (fn [{:keys [_db]} [_ {:keys [uid d-key-down context-root-uid embed-id local-string] :as args}]]
    (log/debug ":unindent args" (pr-str args))
    (let [parent                    (common-db/get-parent @db/dsdb
                                                          (common-db/e-by-av @db/dsdb :block/uid uid))
          is-parent-root-embed?     (= (some-> d-key-down
                                               :target
                                               (.. (closest ".block-embed"))
                                               (. -firstChild)
                                               (.getAttribute "data-uid"))
                                       (str (:block/uid parent) "-embed-" embed-id))
          do-nothing?               (or is-parent-root-embed?
                                        (:node/title parent)
                                        (= context-root-uid (:block/uid parent)))
          {:keys [start end]}       d-key-down
          block-save-block-move-op  (block-save-block-move-composite-op uid
                                                                        (:block/uid parent)
                                                                        :after
                                                                        local-string)
          event                     (common-events/build-atomic-event block-save-block-move-op)]

      (log/debug ":unindent do-nothing?" do-nothing?)
      (when-not do-nothing?
        {:fx [[:dispatch-n [[:resolve-transact-forward event]
                            [:editing/uid (str uid (when embed-id
                                                     (str "-embed-" embed-id)))]]]
              [:set-cursor-position [uid start end]]]}))))


(reg-event-fx
  :unindent/multi
  (fn [{:keys [db]} [_ {:keys [uids]}]]
    (log/debug ":unindent/multi" uids)
    (let [[f-uid f-embed-id]          (common-db/uid-and-embed-id (first uids))
          sanitized-selected-uids     (mapv (comp
                                              first
                                              common-db/uid-and-embed-id) uids)
          {parent-title :node/title
           parent-uid   :block/uid}   (common-db/get-parent @db/dsdb [:block/uid f-uid])
          same-parent?                (common-db/same-parent? @db/dsdb sanitized-selected-uids)
          is-parent-root-embed?       (when same-parent?
                                        (some-> "#editable-uid-"
                                                (str f-uid "-embed-" f-embed-id)
                                                js/document.querySelector
                                                (.. (closest ".block-embed"))
                                                (. -firstChild)
                                                (.getAttribute "data-uid")
                                                (= (str parent-uid "-embed-" f-embed-id))))
          context-root-uid            (get-in db [:current-route :path-params :id])
          do-nothing?                 (or parent-title
                                          (not same-parent?)
                                          (and same-parent? is-parent-root-embed?)
                                          (= parent-uid context-root-uid))]
      (log/debug ":unindent/multi do-nothing?" do-nothing?)
      (when-not do-nothing?
        {:fx [[:dispatch [:drop-multi/sibling {:source-uids  sanitized-selected-uids
                                               :target-uid   parent-uid
                                               :drag-target  :after}]]]}))))


(reg-event-fx
  :block/move
  (fn [_ [_ {:keys [source-uid target-uid target-rel] :as args}]]
    (log/debug ":block/move args" (pr-str args))
    (let [atomic-event (common-events/build-atomic-event
                         (atomic-graph-ops/make-block-move-op source-uid
                                                              {:ref-uid target-uid
                                                               :relation target-rel}))]
      {:fx [[:dispatch [:resolve-transact-forward atomic-event]]]})))


(reg-event-fx
  :block/link
  (fn [_ [_ {:keys [source-uid target-uid target-rel] :as args}]]
    (log/debug ":block/link args" (pr-str args))
    (let [block-uid    (common.utils/gen-block-uid)
          atomic-event (common-events/build-atomic-event
                         (composite-ops/make-consequence-op {:op/type :block/link}
                                                            [(atomic-graph-ops/make-block-new-op block-uid
                                                                                                 {:ref-uid target-uid
                                                                                                  :relation target-rel})
                                                             (atomic-graph-ops/make-block-save-op block-uid
                                                                                                  (str "((" source-uid "))"))]))]
      {:fx [[:dispatch [:resolve-transact-forward atomic-event]]]})))


(defn- block-move-chain
  [target-uid source-uids first-rel]
  (composite-ops/make-consequence-op {:op/type :block/move-chain}
                                     (concat [(atomic-graph-ops/make-block-move-op (first source-uids)
                                                                                   {:ref-uid target-uid
                                                                                    :relation first-rel})]
                                             (doall
                                               (for [[one two] (partition 2 1 source-uids)]
                                                 (atomic-graph-ops/make-block-move-op two
                                                                                      {:ref-uid one
                                                                                       :relation :after}))))))


(reg-event-fx
  :drop-multi/child
  (fn [_ [_ {:keys [source-uids target-uid] :as args}]]
    (log/debug ":drop-multi/child args" (pr-str args))
    (let [atomic-op (block-move-chain target-uid source-uids :first)
          event     (common-events/build-atomic-event atomic-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-multi/sibling
  (fn [_ [_ {:keys [source-uids target-uid drag-target] :as args}]]
    ;; When the selected blocks have same parent and are DnD under the same parent this event is fired.
    ;; This also applies if on selects multiple Zero level blocks and change the order among other Zero level blocks.
    (log/debug ":drop-multi/sibling args" (pr-str args))
    (let [rel-position drag-target
          atomic-op    (block-move-chain target-uid source-uids rel-position)
          event        (common-events/build-atomic-event atomic-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :paste-internal
  (fn [_ [_ uid internal-representation]]
    (println "internal representation is " internal-representation)
    (let [[uid]  (db/uid-and-embed-id uid)
          op     (bfs/build-paste-op @db/dsdb
                                     uid
                                     internal-representation)
          event  (common-events/build-atomic-event op)]
      (log/debug "paste internal event is" (pr-str event))
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :paste-verbatim
  (fn [_ [_ uid text]]
    ;; NOTE: use of `value` is questionable, it's the DOM so it's what users sees,
    ;; but what users sees should taken from DB. How would `value` behave with multiple editors?
    (let [{:keys [start value]} (textarea-keydown/destruct-target js/document.activeElement)
          block-empty? (string/blank? value)
          block-start? (zero? start)
          new-string   (cond
                         block-empty?       text
                         (and (not block-empty?)
                              block-start?) (str text value)
                         :else              (str (subs value 0 start)
                                                 text
                                                 (subs value start)))
          op          (graph-ops/build-block-save-op @db/dsdb uid new-string)
          event       (common-events/build-atomic-event op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :unlinked-references/link
  (fn [_ [_ {:block/keys [string uid]} title]]
    (log/debug ":unlinked-references/link:" uid)
    (let [ignore-case-title  (re-pattern (str "(?i)" title))
          new-str            (string/replace string ignore-case-title (str "[[" title "]]"))
          op                 (graph-ops/build-block-save-op @db/dsdb
                                                            uid
                                                            new-str)
          event              (common-events/build-atomic-event op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :unlinked-references/link-all
  (fn [_ [_ unlinked-refs title]]
    (log/debug ":unlinked-references/link:" title)
    (let [block-save-ops      (mapv
                                (fn [{:block/keys [string uid]}]
                                  (let [ignore-case-title (re-pattern (str "(?i)" title))
                                        new-str           (string/replace string ignore-case-title (str "[[" title "]]"))]
                                    (graph-ops/build-block-save-op @db/dsdb
                                                                   uid
                                                                   new-str)))
                                unlinked-refs)
          link-all-op         (composite-ops/make-consequence-op {:op/type :block/unlinked-refs-link-all}
                                                                 block-save-ops)
          event              (common-events/build-atomic-event link-all-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(rf/reg-event-fx
  :block/open
  (fn [_ [_ {:keys [block-uid open?] :as args}]]
    (log/debug ":block/open args" args)
    (let [event (common-events/build-atomic-event
                  (atomic-graph-ops/make-block-open-op block-uid open?))]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))

