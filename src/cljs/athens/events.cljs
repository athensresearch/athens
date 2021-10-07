(ns athens.events
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver        :as resolver]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging                :as log]
    [athens.common.utils                  :as common.utils]
    [athens.db                            :as db]
    [athens.electron.db-picker            :as db-picker]
    [athens.electron.utils                :as electron.utils]
    [athens.events.remote]
    [athens.patterns                      :as patterns]
    [athens.self-hosted.client            :as client]
    [athens.util                          :as util]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [clojure.string                       :as string]
    [datascript.core                      :as d]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing                :refer-macros [fn-traced]]
    [goog.dom                             :refer [getElement]]
    [re-frame.core                        :refer [reg-event-db reg-event-fx inject-cofx subscribe]]))


;; -- re-frame app-db events ---------------------------------------------

(reg-event-fx
  :boot/web
  [(inject-cofx :local-storage :athens/persist)]
  (fn [{:keys [local-storage]} _]
    {:db         (db/init-app-db local-storage)
     :dispatch-n [[:loading/unset]
                  [:theme/set]]}))


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
        today-date-page          (:title (athens.util/get-day))
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
  :right-sidebar/scroll-top
  (fn []
    {:right-sidebar/scroll-top nil}))


(reg-event-fx
  :editing/uid
  (fn [{:keys [db]} [_ uid index]]
    (let [remote? (client/open?)]
      (cond->
        {:db                    (assoc db :editing/uid uid)
         :editing/focus         [uid index]}
        (and uid remote?) (assoc :presence/send-editing uid)))))


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
          ordered-selection (-> (into [] selected-items)
                                (into [next-block-uid]))]
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
      {:dispatch [:page/create {:title     title
                                :page-uid  uid
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
      {:fx [[:dispatch [:page/delete uid title]]]
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
        (< bottom-delta 1) {:fx [[:dispatch [:daily-note/next (util/get-day (util/uid-to-date (last daily-notes)) 1)]]]}))))


;; -- event-fx and Datascript Transactions -------------------------------

;; Import/Export

(reg-event-fx
  :get-db/init
  (fn [{rfdb :db} _]
    {:db         (assoc db/rfdb :loading? true)
     :async-flow {:first-dispatch [:http/get-db]
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
  :resolve-transact
  (fn [_ [_ event]]
    (let [txs (atomic-resolver/resolve-to-tx @db/dsdb event)]
      (js/console.debug ":resolve-transact resolved" (:event/type event) "to txs:" txs)
      {:fx [[:dispatch [:transact txs]]]})))


(reg-event-fx
  :resolve-transact-forward
  (fn [{:keys [db]} [_ event]]
    (let [selected-db (db-picker/selected-db db)
          forward? (electron.utils/remote-db? selected-db)]
      (js/console.debug ":resolve-transact-forward forward?" forward?)
      {:fx [[:dispatch-n [[:resolve-transact event]
                          ;; TODO: this isn't right, we want to know if we're in RTC
                          ;; and not if the RTC conn is open right now.
                          ;; e.g. can be temporarily offline, but still want to queue up events.
                          (when forward? [:remote/forward-event event])]]]})))


(reg-event-fx
  :page/create
  (fn [{:keys [db]} [_ {:keys [title page-uid block-uid shift?] :or {shift? false} :as args}]]
    (log/debug ":page/create args" (pr-str args))
    (let [event (common-events/build-atomic-event (:remote/last-seen-tx db)
                                                  (atomic-graph-ops/make-page-new-op title
                                                                                     page-uid
                                                                                     block-uid))]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          (cond
                            shift?
                            [:right-sidebar/open-item page-uid]

                            (not (util/is-daily-note page-uid))
                            [:navigate :page {:id page-uid}]

                            (util/is-daily-note page-uid)
                            [:daily-note/add page-uid])

                          [:editing/uid block-uid]]]]})))


(reg-event-fx
  :page/rename
  (fn [{:keys [db]} [_ {:keys [page-uid old-name new-name callback] :as args}]]
    (log/debug ":page/rename args:" (pr-str (select-keys args [:page-uid
                                                               :old-name
                                                               :new-name])))
    (let [event (common-events/build-page-rename-event (:remote/last-seen-tx db)
                                                       page-uid
                                                       old-name
                                                       new-name)]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:invoke-callback callback]]})))


(reg-event-fx
  :page/merge
  (fn [{:keys [db]} [_ {:keys [page-uid old-name new-name callback] :as args}]]
    (log/debug ":page/merge args:" (pr-str (select-keys args [:page-uid
                                                              :old-name
                                                              :new-name])))
    (let [event (common-events/build-page-merge-event (:remote/last-seen-tx db)
                                                      page-uid
                                                      old-name
                                                      new-name)]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:invoke-callback callback]]})))


(reg-event-fx
  :page/delete
  (fn [{:keys [db]} [_ uid _title]]
    (log/debug ":page/delete:" uid)
    (let [event (common-events/build-page-delete-event (:remote/last-seen-tx db)
                                                       uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :page/add-shortcut
  (fn [{:keys [db]} [_ uid]]
    (js/console.debug ":page/add-shortcut:" uid)
    (let [event (common-events/build-page-add-shortcut (:remote/last-seen-tx db) uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :page/remove-shortcut
  (fn [{:keys [db]} [_ uid]]
    (log/debug ":page/remove-shortcut:" uid)
    (let [event (common-events/build-page-remove-shortcut (:remote/last-seen-tx db) uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :left-sidebar/drop-above
  (fn [{:keys [db]} [_ source-order target-order]]
    (log/debug ":left-sidebar/drop-above" ", source-order:" source-order ", target-order:" target-order)
    (let [event (common-events/build-left-sidebar-drop-above (:remote/last-seen-tx db) source-order target-order)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :left-sidebar/drop-below
  (fn [{:keys [db]} [_ source-order target-order]]
    (log/debug ":left-sidebar/drop-below" ", source-order:" source-order ", target-order:" target-order)
    (let [event (common-events/build-left-sidebar-drop-below (:remote/last-seen-tx db) source-order target-order)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :save
  (fn [_ _]
    {:fs/write! nil}))


(reg-event-fx
  :undo
  (fn [{:keys [db]} _]
    (log/debug ":undo")
    (let [local? (not (client/open?))]
      (log/debug ":undo: local?" local?)
      (if local?
        (let [undo-event (common-events/build-undo-redo-event (:remote/last-seen-tx db) false)
              tx-data    (resolver/resolve-event-to-tx db/history undo-event)]
          {:fx [[:dispatch [:transact tx-data]]]})
        {:fx [[:dispatch [:alert/js "Undo not supported in Lan-Party, yet."]]]}))))


(reg-event-fx
  :redo
  (fn [{:keys [db]} _]
    (log/debug ":redo")
    (let [local? (not (client/open?))]
      (log/debug ":redo: local?" local?)
      (if local?
        (let [redo-event (common-events/build-undo-redo-event (:remote/last-seen-tx db) true)
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
  (fn [{:keys [db]} [_ uid]]
    (log/debug ":backspace/delete-only-child:" (pr-str uid))
    (let [event (common-events/build-delete-only-child-event (:remote/last-seen-tx db) uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:dispatch [:editing/uid nil]]]})))


(reg-event-fx
  :backspace/delete-merge-block
  (fn [{:keys [db]} [_ {:keys [uid value prev-block-uid embed-id prev-block] :as args}]]
    (log/debug ":backspace/delete-merge-block args:" (pr-str args))
    (let [event (common-events/build-delete-merge-block-event (:remote/last-seen-tx db) uid value)]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:dispatch [:editing/uid
                        (cond-> prev-block-uid
                          embed-id (str "-embed-" embed-id))
                        (count (:block/string prev-block))]]]})))


(reg-event-fx
  :split-block-to-children
  (fn [{:keys [db]} [_ {:keys [uid value index new-uid embed-id] :as args}]]
    (log/debug ":split-block-to-children" (pr-str args))
    (let [event (common-events/build-split-block-to-children-event (:remote/last-seen-tx db)
                                                                   uid
                                                                   value
                                                                   index
                                                                   new-uid)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


;; Atomic events start ==========

(reg-event-fx
  :enter/new-block
  (fn [{:keys [db]} [_ {:keys [block parent new-uid embed-id]}]]
    (log/debug ":enter/new-block" (pr-str block) (pr-str parent) (pr-str new-uid))
    (let [op    (atomic-graph-ops/make-block-new-op (:block/uid parent)
                                                    new-uid
                                                    (inc (:block/order block)))
          event (common-events/build-atomic-event (:remote/last-seen-tx db) op)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


(reg-event-fx
  :block/save
  (fn [{:keys [db]} [_ {:keys [uid old-string new-string callback] :as args}]]
    (log/debug ":block/save args" (pr-str args))
    (let [local?      (not (client/open?))
          block-eid   (common-db/e-by-av @db/dsdb :block/uid uid)
          do-nothing? (or (not block-eid)
                          (= old-string new-string))
          op          (graph-ops/build-block-save-op @db/dsdb uid old-string new-string)
          event       (common-events/build-atomic-event (:remote/last-seen-tx db) op)]
      (js/console.debug ":block/save local?" local?
                        ", do-nothing?" do-nothing?)
      (when-not do-nothing?
        {:fx [[:dispatch [:resolve-transact-forward event]]
              [:invoke-callback callback]]}))))


(reg-event-fx
  :page/new
  (fn [{:keys [db]} [_ {:keys [title page-uid block-uid] :as args}]]
    (log/debug ":page/new args" (pr-str args))
    (let [op    (atomic-graph-ops/make-page-new-op title
                                                   page-uid
                                                   block-uid)
          event (common-events/build-atomic-event (:remote/last-seen-tx db) op)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid block-uid]]]]})))


;; Atomic events end ==========


(reg-event-fx
  :enter/add-child
  (fn [{:keys [db]} [_ {:keys [block new-uid embed-id add-time?]
                        :or {add-time? false}
                        :as args}]]
    (log/debug ":enter/add-child args:" (pr-str args))
    (let [event (common-events/build-add-child-event (:remote/last-seen-tx db)
                                                     (:block/uid block)
                                                     new-uid
                                                     add-time?)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


(reg-event-fx
  :enter/split-block
  (fn [{:keys [db]} [_ {:keys [parent-uid uid new-uid new-order old-string value index embed-id] :as args}]]
    (log/debug ":enter/split-block" (pr-str args))
    (let [op    (graph-ops/build-block-split-op @db/dsdb
                                                {:parent-uid      parent-uid
                                                 :old-block-uid   uid
                                                 :new-block-uid   new-uid
                                                 :new-block-order new-order
                                                 :old-string      old-string
                                                 :new-string      value
                                                 :index           index})
          event (common-events/build-atomic-event (:remote/last-seen-tx db) op)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


(reg-event-fx
  :enter/bump-up
  (fn [{:keys [db]} [_ {:keys [uid new-uid embed-id] :as args}]]
    (log/debug ":enter/bump-up args" (pr-str args))
    (let [event (common-events/build-bump-up-event (:remote/last-seen-tx db)
                                                   uid
                                                   new-uid)]
      {:fx [[:dispatch-n [[:resolve-transact-forward event]
                          [:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]]})))


(reg-event-fx
  :enter/open-block-add-child
  (fn [{:keys [db]} [_ {:keys [block new-uid embed-id]}]]
    (log/debug ":enter/open-block-add-child" (pr-str block) (pr-str new-uid))
    (let [block-uid                  (:block/uid block)
          event (common-events/build-open-block-add-child-event (:remote/last-seen-tx db)
                                                                block-uid
                                                                new-uid)]
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
        {:block/keys [string order]
         :as         block}   (db/get-block [:block/uid uid])
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
                                [:split-block-to-children {:uid     uid
                                                           :value   value
                                                           :index   start
                                                           :new-uid new-uid}]

                                (and (empty? value) embed-id (not is-parent-root-embed?))
                                [:unindent {:uid              uid
                                            :d-key-down       d-key-down
                                            :context-root-uid context-root-uid
                                            :embed-id         embed-id}]

                                (and (empty? value) embed-id is-parent-root-embed?)
                                [:enter/new-block {:block    block
                                                   :parent   parent
                                                   :new-uid  new-uid
                                                   :embed-id embed-id}]

                                (not (zero? start))
                                [:enter/split-block {:uid        uid
                                                     :old-string string
                                                     :parent-uid parent-uid
                                                     :value      value
                                                     :index      start
                                                     :new-uid    new-uid
                                                     :new-order  (inc order)
                                                     :embed-id   embed-id}]

                                (empty? value)
                                [:unindent {:uid              uid
                                            :d-key-down       d-key-down
                                            :context-root-uid context-root-uid
                                            :embed-id         embed-id}]

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


(reg-event-fx
  :indent
  (fn [{:keys [db]} [_ {:keys [uid d-key-down] :as args}]]
    ;; - `block-zero`: The first block in a page
    ;; - `value`     : The current string inside the block being indented. Otherwise, if user changes block string and indents,
    ;;                 the local string  is reset to original value, since it has not been unfocused yet (which is currently the
    ;;                 transaction that updates the string).
    (let [local?                    (not (client/open?))
          block                     (common-db/get-block @db/dsdb [:block/uid uid])
          block-zero?               (zero? (:block/order block))
          {:keys [value start end]} d-key-down]
      (log/debug ":indent local?" local?
                 ", args:" (pr-str args)
                 ", block-zero?" block-zero?)
      (when-not block-zero?
        (let [event (common-events/build-indent-event (:remote/last-seen-tx db)
                                                      uid
                                                      value)]
          {:fx [[:dispatch            [:resolve-transact-forward event]]
                [:set-cursor-position [uid start end]]]})))))


(reg-event-fx
  :indent/multi
  (fn [{:keys [db]} [_ {:keys [uids]}]]
    (log/debug ":indent/multi" (pr-str uids))
    (let [sanitized-selected-uids  (mapv (comp first common-db/uid-and-embed-id) uids)
          dsdb                     @db/dsdb
          same-parent?             (common-db/same-parent? dsdb sanitized-selected-uids)
          first-block-order        (:block/order (common-db/get-block dsdb [:block/uid (first sanitized-selected-uids)]))
          block-zero?              (zero? first-block-order)]
      (log/debug ":indent/multi same-parent?" same-parent?
                 ", not block-zero?" (not  block-zero?))
      (when (and same-parent? (not block-zero?))
        (let [event  (common-events/build-indent-multi-event (:remote/last-seen-tx db)
                                                             sanitized-selected-uids)]
          {:fx [[:dispatch [:resolve-transact-forward event]]]})))))


(reg-event-fx
  :unindent
  (fn [{:keys [db]} [_ {:keys [uid d-key-down context-root-uid embed-id] :as args}]]
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
          {:keys [value start end]} d-key-down]
      (log/debug ":unindent do-nothing?" do-nothing?)
      (when-not do-nothing?
        (let [event (common-events/build-unindent-event (:remote/last-seen-tx db)
                                                        uid
                                                        value)]
          {:fx [[:dispatch-n [[:resolve-transact-forward event]
                              [:editing/uid (str uid (when embed-id
                                                       (str "-embed-" embed-id)))]]]
                [:set-cursor-position [uid start end]]]})))))


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
        (let [event (common-events/build-unindent-multi-event (:remote/last-seen-tx db)
                                                              sanitized-selected-uids)]
          {:fx [[:dispatch [:resolve-transact-forward event]]]})))))


(reg-event-fx
  :drop/child
  (fn [{:keys [db]} [_ {:keys [source-uid target-uid] :as args}]]
    (log/debug ":drop/child args" (pr-str args))
    (let [event (common-events/build-drop-child-event (:remote/last-seen-tx db)
                                                      source-uid
                                                      target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-multi/child
  (fn [{:keys [db]} [_ {:keys [source-uids target-uid] :as args}]]
    (log/debug ":drop-multi/child args" (pr-str args))
    (let [event (common-events/build-drop-multi-child-event (:remote/last-seen-tx db)
                                                            source-uids
                                                            target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-link/child
  (fn [{:keys [db]} [_ {:keys [source-uid target-uid] :as args}]]
    (log/debug ":drop-link/child args" (pr-str args))
    (let [event (common-events/build-drop-link-child-event (:remote/last-seen-tx db)
                                                           source-uid
                                                           target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop/diff-parent
  (fn [{:keys [db]} [_ {:keys [drag-target source-uid target-uid] :as args}]]
    (log/debug ":drop/diff-parent args" args)
    (let [event (common-events/build-drop-diff-parent-event (:remote/last-seen-tx db)
                                                            drag-target
                                                            source-uid
                                                            target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-link/diff-parent
  (fn [{:keys [db]} [_ {:keys [drag-target source-uid target-uid] :as args}]]
    (log/debug ":drop-link/diff-parent args" args)
    (let [event (common-events/build-drop-link-diff-parent-event (:remote/last-seen-tx db)
                                                                 drag-target
                                                                 source-uid
                                                                 target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop/same
  (fn [{:keys [db]} [_ {:keys [drag-target source-uid target-uid] :as args}]]
    (log/debug ":drop/same args" args)
    (let [event (common-events/build-drop-same-event (:remote/last-seen-tx db)
                                                     drag-target
                                                     source-uid
                                                     target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-multi/same-source
  (fn [{:keys [db]} [_ {:keys [drag-target source-uids target-uid] :as args}]]
    ;; When the selected blocks have same parent and are DnD under some other block this event is fired.
    (log/debug ":drop-multi/same-source args" args)
    (let [event (common-events/build-drop-multi-same-source-event (:remote/last-seen-tx db)
                                                                  drag-target
                                                                  source-uids
                                                                  target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-multi/same-all
  (fn [{:keys [db]} [_ {:keys [drag-target source-uids target-uid] :as args}]]
    ;; When the selected blocks have same parent and are DnD under the same parent this event is fired.
    ;; This also applies if on selects multiple Zero level blocks and change the order among other Zero level blocks.
    (log/debug ":drop-multi/same-all args" args)
    (let [event (common-events/build-drop-multi-same-all-event (:remote/last-seen-tx db)
                                                               drag-target
                                                               source-uids
                                                               target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-link/same-parent
  (fn [{:keys [db]} [_ {:keys [drag-target source-uid target-uid] :as args}]]
    (log/debug ":drop-link/same-parent args" args)
    (let [event (common-events/build-drop-link-same-parent-event (:remote/last-seen-tx db)
                                                                 drag-target
                                                                 source-uid
                                                                 target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-multi/diff-source-same-parents
  (fn [{:keys [db]} [_ {:keys [drag-target source-uids target-uid] :as args}]]
    (log/debug ":drop-multi/diff-source-same-parents args" args)
    (let [event (common-events/build-drop-multi-diff-source-same-parents-event (:remote/last-seen-tx db)
                                                                               drag-target
                                                                               source-uids
                                                                               target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :drop-multi/diff-source-diff-parents
  (fn [{:keys [db]} [_ {:keys [drag-target source-uids target-uid] :as args}]]
    (log/debug ":drop-multi/diff-source-diff-parents args" args)
    (let [event (common-events/build-drop-multi-diff-source-diff-parents-event (:remote/last-seen-tx db)
                                                                               drag-target
                                                                               source-uids
                                                                               target-uid)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :paste-internal
  (fn [_ [_ uid internal-representation]]
    (println "internal representation is " internal-representation)
    (let [[uid]  (db/uid-and-embed-id uid)
          paste-internal-event (common-events/build-paste-internal-event -1
                                                                         uid
                                                                         internal-representation)]
      {:fx [[:dispatch [:resolve-transact-forward paste-internal-event]]]})))


(reg-event-fx
  :paste
  (fn [{:keys [db]} [_ uid text :as args]]
    (log/debug ":paste args" args)
    (let [local?          (not (client/open?))
          [uid embed-id]  (db/uid-and-embed-id uid)
          {:keys [start
                  value]} (textarea-keydown/destruct-target js/document.activeElement)
          block-start?    (zero? start)]
      (log/debug ":paste local?" local?
                 ", args:" (pr-str args))
      (if local?
        (let [paste-event (common-events/build-paste-event (:remote/last-seen-tx db)
                                                           uid
                                                           text
                                                           start
                                                           value)
              tx          (resolver/resolve-event-to-tx @db/dsdb paste-event)]
          (log/debug ":paste tx" tx)
          {:fx [[:dispatch [:transact tx]]
                (when block-start?
                  (let [block                  (-> tx first :block/children)
                        {:block/keys [uid
                                      string]} block
                        n                      (count string)]
                    [:editing/uid
                     (cond-> uid
                       embed-id (str "-embed-" embed-id))
                     n]))]})
        {:fx [[:dispatch
               [:alert/js "Sorry, Paste event isn't ported to remote setup, yet."]
               #_[:remote/paste {:uid   uid
                                 :text  text
                                 :start start
                                 :value value}]]]}))))


(reg-event-fx
  :paste-verbatim
  (fn [{:keys [db]} [_ uid text]]
    ;; NOTE: use of `value` is questionable, it's the DOM so it's what users sees,
    ;; but what users sees should taken from DB. How would `value` behave with multiple editors?
    (let [{:keys [start value]} (textarea-keydown/destruct-target js/document.activeElement)
          event                 (common-events/build-paste-verbatim-event (:remote/last-seen-tx db) uid text start value)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :unlinked-references/link
  (fn [{:keys [db]} [_ {:block/keys [string uid]} title]]
    (log/debug ":unlinked-references/link:" uid)
    (let [event (common-events/build-unlinked-references-link (:remote/last-seen-tx db) uid string title)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :unlinked-references/link-all
  (fn [{:keys [db]} [_ unlinked-refs title]]
    (log/debug ":unlinked-references/link:" title)
    (let [event (common-events/build-unlinked-references-link-all (:remote/last-seen-tx db) unlinked-refs title)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(reg-event-fx
  :block/open
  (fn [{:keys [db]} [_ {:keys [block-uid open?] :as args}]]
    (log/debug ":block/open args" args)
    (let [event (common-events/build-block-open-event (:remote/last-seen-tx db)
                                                      block-uid
                                                      open?)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))

