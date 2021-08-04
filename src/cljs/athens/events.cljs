(ns athens.events
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.resolver        :as resolver]
    [athens.db                            :as db :refer [dec-after inc-after minus-after plus-after retract-uid-recursively]]
    [athens.events.remote]
    [athens.patterns                      :as patterns]
    [athens.self-hosted.client            :as client]
    [athens.style                         :as style]
    [athens.util                          :refer [gen-block-uid]]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [clojure.string                       :as string]
    [datascript.core                      :as d]
    [datascript.transit                   :as dt]
    [day8.re-frame.async-flow-fx]
    [day8.re-frame.tracing                :refer-macros [fn-traced]]
    [re-frame.core                        :refer [reg-event-db reg-event-fx inject-cofx subscribe]]))


;; -- re-frame app-db events ---------------------------------------------

(reg-event-fx
  :boot/web
  (fn [_ _]
    {:db         db/rfdb
     :dispatch-n [[:loading/unset]
                  [:local-storage/set-theme]]}))


(reg-event-db
  :init-rfdb
  (fn [_ _]
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
        new-uid                  (gen-block-uid)
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
    (js/console.warn "Called :no-op re-frame event, this shouldn't be happening.")
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
        remote? (assoc :presence/send-editing uid)))))


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
    (update-in db [:selection :items] (fnil conj #{}) uid)))


(reg-event-db
  :selected/remove-item
  (fn [db [_ uid]]
    (update-in db [:selection :items] disj uid)))


(reg-event-db
  :selected/remove-items
  (fn [db [_ uids]]
    (update-in db [:selection :items] #(apply disj %1 %2) uids)))


(reg-event-db
  :selected/add-items
  (fn [db [_ uids]]
    (update-in db [:selection :items] #(apply conj %1 %2) uids)))


(reg-event-db
  :selected/items-order
  (fn [db [_ items-order]]
    (assoc-in db [:selection :order] items-order)))


(reg-event-db
  :selected/clear-items
  (fn [db _]
    (-> db
        (assoc-in [:selection :items] #{})
        (assoc-in [:selection :order] []))))


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


(defn select-down
  [selected-items]
  (let [editing-uid @(subscribe [:editing/uid])
        editing-idx (first (keep-indexed (fn [idx x]
                                           (when (= x editing-uid)
                                             idx))
                                         selected-items))
        [_ f-embed]          (->> selected-items first db/uid-and-embed-id)
        last-item            (last selected-items)
        next-block-uid       (db/next-block-uid last-item true)]
    (cond
      (pos? editing-idx) (subvec selected-items 1)

      ;; shift down started from inside the embed should not go outside embed block
      f-embed            (let [sel-uid (str (-> next-block-uid db/uid-and-embed-id first) "-embed-" f-embed)]
                           (if (js/document.querySelector (str "#editable-uid-" sel-uid))
                             (conj selected-items sel-uid)
                             selected-items))

      next-block-uid     (conj selected-items next-block-uid)
      :else              selected-items)))


;; using a set or a hash map, we would need a secondary editing/uid to maintain the head/tail position
;; this would let us know if the operation is additive or subtractive
(reg-event-db
  :selected/down
  (fn [db [_ selected-items]]
    (assoc-in db [:selection :items] (select-down selected-items))))


(reg-event-fx
  :selected/delete
  (fn [{db :db} [_ selected-uids]]
    (js/console.debug ":selected/delete args" selected-uids)
    (let [local?         (not (client/open?))
          sanitized-uids (map (comp first db/uid-and-embed-id) selected-uids)]
      (js/console.log "selected/delete local?" local?)
      (if local?
        (let [selected-delete-event (common-events/build-selected-delete-event -1
                                                                               sanitized-uids)
              tx                    (resolver/resolve-event-to-tx @db/dsdb selected-delete-event)]
          (js/console.debug  ":selected/delete tx" tx)
          {:fx [[:dispatch-n [[:transact    tx]
                              [:editing/uid nil]]]]
           :db (-> db
                   (assoc-in [:selection :items] #{})
                   (assoc-in [:selection :order] []))})
        {:fx [[:dispatch [:remote/selected-delete {:uids selected-uids}]]]}))))


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


;; Window Size

(reg-event-fx
  :window/set-size
  (fn [_ [_ [x y]]]
    {:local-storage/set! ["ws/window-size" (str x "," y)]}))


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
    (let [new-db    (update db :daily-notes/items (fn [items]
                                                    (into [uid] items)))
          block-uid (gen-block-uid)]
      (if (db/e-by-av :block/uid uid)
        {:db new-db}
        {:db       new-db
         :dispatch [:page/create {:title     title
                                  :page-uid  uid
                                  :block-uid block-uid}]}))))


(reg-event-fx
  :daily-note/next
  (fn [{:keys [db]} [_ {:keys [uid title]}]]
    (let [new-db    (update db :daily-notes/items conj uid)
          block-uid (gen-block-uid)]
      (if (db/e-by-av :block/uid uid)
        {:db new-db}
        {:db       new-db
         :dispatch [:page/create {:title     title
                                  :page-uid  uid
                                  :block-uid block-uid}]}))))


(reg-event-fx
  :daily-note/delete
  (fn [{:keys [db]} [_ uid title]]
    (let [filtered-dn        (filterv #(not= % uid) (:daily-notes/items db)) ; Filter current date from daily note vec
          new-db (assoc db :daily-notes/items filtered-dn)]
      {:fx [[:dispatch [:page/delete uid title]]]
       :db new-db})))


;; -- event-fx and Datascript Transactions -------------------------------

;; Import/Export

(reg-event-fx
  :get-db/init
  (fn [{rfdb :db} _]
    {:db (cond-> db/rfdb
           true (assoc :loading? true)

           (= (js/localStorage.getItem "theme/dark") "true")
           (assoc :theme/dark true))

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
  :page/create
  (fn [_ [_ {:keys [title page-uid block-uid shift?] :or {shift? false} :as args}]]
    (js/console.debug ":page/create args" (pr-str args))
    (let [local? (not (client/open?))]
      (js/console.debug ":page/create local?" local?)
      (if local?
        (let [create-page-event (common-events/build-page-create-event -1
                                                                       page-uid
                                                                       block-uid
                                                                       title)
              tx                (resolver/resolve-event-to-tx @db/dsdb create-page-event)]
          {:fx [[:dispatch-n [[:transact tx]
                              (if shift?
                                [:right-sidebar/open-item page-uid]
                                [:navigate :page {:id page-uid}])
                              [:editing/uid block-uid]]]]})
        {:fx [[:dispatch
               [:remote/page-create page-uid block-uid title shift?]]]}))))


(reg-event-fx
  :page/rename
  (fn [_db [_ {:keys [page-uid old-name new-name callback] :as args}]]
    (let [local? (not (client/open?))]
      (js/console.debug ":page/rename local?" local? ", args:" (pr-str (select-keys args [:page-uid
                                                                                          :old-name
                                                                                          :new-name])))
      (if local?
        (let [page-rename-event (common-events/build-page-rename-event -1
                                                                       page-uid
                                                                       old-name
                                                                       new-name)
              page-rename-tx    (resolver/resolve-event-to-tx @db/dsdb page-rename-event)]
          (js/console.debug ":page/rename txs:" (pr-str page-rename-tx))
          {:fx [[:dispatch [:transact page-rename-tx]]
                [:invoke-callback callback]]})
        {:fx [[:dispatch
               [:remote/page-rename page-uid old-name new-name callback]]]}))))


(reg-event-fx
  :page/merge
  (fn [_db [_ {:keys [page-uid old-name new-name callback] :as args}]]
    (let [local? (not (client/open?))]
      (js/console.debug ":page/merge local?" local? ", args:" (pr-str (select-keys args [:page-uid
                                                                                         :old-name
                                                                                         :new-name])))
      (if local?
        (let [page-merge-event (common-events/build-page-merge-event -1
                                                                     page-uid
                                                                     old-name
                                                                     new-name)
              page-merge-tx    (resolver/resolve-event-to-tx @db/dsdb page-merge-event)]
          (js/console.debug ":page/merge txs:" (pr-str page-merge-tx))
          {:fx [[:dispatch [:transact page-merge-tx]]
                [:invoke-callback callback]]})
        {:fx [[:dispatch
               [:remote/page-merge page-uid old-name new-name callback]]]}))))


(reg-event-fx
  :page/delete
  (fn [_ [_ uid _title]]
    (js/console.debug ":page/delete:" uid)
    (let [local? (not (client/open?))]
      (js/console.debug ":page/delete local?" local?)
      (if local?
        (let [delete-page-event (common-events/build-page-delete-event -1
                                                                       uid)
              tx-data           (resolver/resolve-event-to-tx @db/dsdb delete-page-event)]
          {:fx [[:dispatch [:transact tx-data]]]})
        {:fx [[:dispatch
               [:remote/page-delete uid]]]}))))


(reg-event-fx
  :page/add-shortcut
  (fn [_ [_ uid]]
    (js/console.debug ":page/add-shortcut:" uid)
    (if-let [local? (not (client/open?))]
      (let [add-shortcut-event (common-events/build-page-add-shortcut -1 uid)
            tx-data            (resolver/resolve-event-to-tx @db/dsdb add-shortcut-event)]
        (js/console.debug ":page/add-shortcut: local?" local?)
        {:fx [[:dispatch [:transact tx-data]]]})
      {:fx [[:dispatch [:remote/page-add-shortcut uid]]]})))


(reg-event-fx
  :page/remove-shortcut
  (fn [_ [_ uid]]
    (js/console.debug ":page/remove-shortcut:" uid)
    (if-let [local? (not (client/open?))]
      (let [remove-shortcut-event (common-events/build-page-remove-shortcut -1 uid)
            tx-data               (resolver/resolve-event-to-tx @db/dsdb remove-shortcut-event)]
        (js/console.debug ":page/remove-shortcut:" local?)
        {:fx [[:dispatch [:transact tx-data]]]})
      {:fx [[:dispatch [:remote/page-remove-shortcut uid]]]})))


(reg-event-fx
  :left-sidebar/drop-above
  (fn [_ [_ source-order target-order]]
    (js/console.debug ":left-sidebar/drop-above")
    (if-let [local? (not (client/open?))]
      (let [left-sidebar-drop-above-event (common-events/build-left-sidebar-drop-above -1 source-order target-order)
            tx-data                       (resolver/resolve-event-to-tx @db/dsdb left-sidebar-drop-above-event)]
        (js/console.debug ":left-sidebar/drop-above local?" local?)
        {:fx [[:dispatch [:transact tx-data]]]})
      {:fx [[:dispatch [:remote/left-sidebar-drop-above source-order target-order]]]})))


(reg-event-fx
  :left-sidebar/drop-below
  (fn [_ [_ source-order target-order]]
    (js/console.debug ":left-sidebar/drop-below")
    (if-let [local? (not (client/open?))]
      (let [left-sidebar-drop-below-event (common-events/build-left-sidebar-drop-below -1 source-order target-order)
            tx-data                       (resolver/resolve-event-to-tx @db/dsdb left-sidebar-drop-below-event)]
        (js/console.debug ":left-sidebar/drop-below local?" local?)
        {:fx [[:dispatch [:transact tx-data]]]})
      {:fx [[:dispatch [:remote/left-sidebar-drop-below source-order target-order]]]})))


(reg-event-fx
  :save
  (fn [_ _]
    {:fs/write! nil}))


(reg-event-fx
  :undo
  (fn [_ _]
    (js/console.debug ":undo")
    (let [local? (not (client/open?))]
      (js/console.debug ":undo: local?" local?)
      (if local?
        (let [undo-event (common-events/build-undo-redo-event -1 false)
              tx-data    (resolver/resolve-event-to-tx db/history undo-event)]
          {:fx [[:dispatch [:transact tx-data]]]})
        false))))


(reg-event-fx
  :redo
  (fn [_ _]
    (js/console.debug ":redo")
    (let [local? (not (client/open?))]
      (js/console.debug ":redo: local?" local?)
      (if local?
        (let [redo-event (common-events/build-undo-redo-event -1 true)
              tx-data    (resolver/resolve-event-to-tx db/history redo-event)]
          {:fx [[:dispatch [:transact tx-data]]]})
        false))))


(defn prev-block-uid-without-presence-recursively
  "base case: prev block
  recursive case: keep going until no longer present"
  [uid]
  (let [prev-block-uid (db/prev-block-uid uid)
        has-presence? @(subscribe [:presence/has-presence prev-block-uid])]
    (if has-presence?
      (prev-block-uid-without-presence-recursively prev-block-uid)
      prev-block-uid)))


(reg-event-fx
  :up
  (fn [_ [_ uid]]
    (let [prev-block-uid (prev-block-uid-without-presence-recursively uid)]
      {:dispatch [:editing/uid (or prev-block-uid uid)]})))


(defn next-block-uid-without-presence-recursively
  [uid]
  (let [next-block-uid (db/next-block-uid uid)
        has-presence? @(subscribe [:presence/has-presence next-block-uid])]
    (if has-presence?
      (next-block-uid-without-presence-recursively next-block-uid)
      next-block-uid)))


(reg-event-fx
  :down
  (fn [_ [_ uid]]
    (let [next-block-uid (next-block-uid-without-presence-recursively uid)]
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
        [uid embed-id]  (db/uid-and-embed-id uid)
        block           (db/get-block [:block/uid uid])
        {:block/keys    [children order] :or {children []}} block
        parent          (db/get-parent [:block/uid uid])
        reindex         (dec-after (:db/id parent) (:block/order block))
        prev-block-uid  (db/prev-block-uid uid)
        prev-block      (db/get-block [:block/uid prev-block-uid])
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
      root-embed? nil
      (and (empty? children) (:node/title parent) (zero? order) (clojure.string/blank? value)) (let [tx-data [retract-block new-parent]]
                                                                                                 {:dispatch-n [[:transact tx-data]
                                                                                                               [:editing/uid nil]]})
      (and (not-empty children) (not-empty (:block/children prev-sib))) nil
      (and (not-empty children) (= parent prev-block)) nil
      :else (let [retracts       (mapv (fn [x] [:db/retract (:db/id block) :block/children (:db/id x)]) children)
                  new-prev-block {:db/id          [:block/uid prev-block-uid]
                                  :block/string   (str (:block/string prev-block) value)
                                  :block/children children}
                  tx-data        (conj retracts retract-block new-prev-block new-parent)]
              {:dispatch-later [{:ms 0 :dispatch [:transact tx-data]}
                                {:ms 10 :dispatch [:editing/uid
                                                   (cond-> prev-block-uid
                                                     embed-id (str "-embed-" embed-id))
                                                   (count (:block/string prev-block))]}]}))))


;; todo(abhinav) -- stateless backspace
;; will pick db value of backspace/delete instead of current state
;; which might not be same as blur is not yet called
(reg-event-fx
  :backspace
  (fn [_ [_ uid value]]
    (backspace uid value)))


(defn split-block
  [uid val index new-uid]
  (let [parent     (db/get-parent [:block/uid uid])
        block      (db/get-block [:block/uid uid])
        {:block/keys [order children open] :or {children []}} block
        head       (subs val 0 index)
        tail       (subs val index)
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
    {:dispatch [:transact tx-data]}))


(reg-event-fx
  :split-block-to-children
  (fn [_ [_ {:keys [uid value index new-uid embed-id] :as args}]]
    (js/console.debug ":split-block-to-children" (pr-str args))
    (let [local? (not (client/open?))]
      (js/console.debug ":split-block-to-children local?" local?)
      (if local?
        (let [split-block-to-children-event (common-events/build-split-block-to-children-event -1
                                                                                               uid
                                                                                               value
                                                                                               index
                                                                                               new-uid)
              tx                            (resolver/resolve-event-to-tx @db/dsdb split-block-to-children-event)]
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid (str new-uid (when embed-id
                                                           (str "-embed-" embed-id)))]]]]})
        {:fx [[:dispatch [:remote/split-block-to-children {:uid      uid
                                                           :value    value
                                                           :index    index
                                                           :new-uid  new-uid
                                                           :embed-id embed-id}]]]}))))


(reg-event-fx
  :block/save
  (fn [_ [_ {:keys [uid old-string new-string callback] :as args}]]
    (js/console.debug ":block/save args" (pr-str args))
    (let [local?      (not (client/open?))
          block-eid   (common-db/e-by-av @db/dsdb :block/uid uid)
          do-nothing? (or (not block-eid)
                          ;; TODO Question to Jeff: shold we really ignore save event if entity doesn't exists?
                          ;; Seems like correct thing to do would be to create entity
                          ;; Do you know why?
                          ;; /giphy but why?
                          (= old-string new-string))]
      (js/console.debug ":block/save local?" local?
                        ", do-nothing?" do-nothing?)
      (when-not do-nothing?
        (if local?
          (let [block-save-event (common-events/build-block-save-event -1
                                                                       uid
                                                                       new-string)
                block-save-tx    (resolver/resolve-event-to-tx @db/dsdb block-save-event)]
            {:fx [[:dispatch [:transact block-save-tx]]
                  [:invoke-callback callback]]})
          {:fx [[:dispatch [:remote/block-save {:uid        uid
                                                :new-string new-string
                                                :callback   callback}]]]})))))


(reg-event-fx
  :enter/new-block
  (fn [_ [_ {:keys [block parent new-uid embed-id]}]]
    (js/console.debug ":enter/new-block" (pr-str block) parent new-uid)
    (let [local? (not (client/open?))]
      (js/console.debug ":enter/new-block local?" local?)
      (if local?
        (let [new-block-event (common-events/build-new-block-event -1
                                                                   (:db/id parent)
                                                                   (:block/order block)
                                                                   new-uid)
              tx              (resolver/resolve-event-to-tx @db/dsdb new-block-event)]
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid (str new-uid (when embed-id
                                                           (str "-embed-" embed-id)))]]]]})
        {:fx [[:dispatch [:remote/new-block {:block    block
                                             :parent   parent
                                             :new-uid  new-uid
                                             :embed-id embed-id}]]]}))))


(reg-event-fx
  :enter/add-child
  (fn [_ [_ {:keys [block new-uid embed-id]}]]
    (js/console.debug ":enter/add-child" (pr-str block) new-uid)
    (let [local? (not (client/open?))]
      (js/console.debug ":enter/add-child local?" local?)
      (if local?
        (let [add-child-event (common-events/build-add-child-event -1
                                                                   (:block/uid block)
                                                                   new-uid)
              tx              (resolver/resolve-event-to-tx @db/dsdb add-child-event)]
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid (str new-uid (when embed-id
                                                           (str "-embed-" embed-id)))]]]]})
        {:fx [[:dispatch [:remote/add-child {:parent-uid (:block/uid block)
                                             :new-uid    new-uid
                                             :embed-id   embed-id}]]]}))))


(reg-event-fx
  :enter/split-block
  (fn [_ [_ {:keys [uid value index new-uid embed-id] :as args}]]
    (js/console.debug ":enter/split-block" (pr-str args))
    (let [local? (not (client/open?))]
      (js/console.debug ":enter/split-block local?" local?)
      (if local?
        (let [split-block-event (common-events/build-split-block-event -1
                                                                       uid
                                                                       value
                                                                       index
                                                                       new-uid)
              tx                (resolver/resolve-event-to-tx @db/dsdb split-block-event)]
          (js/console.debug ":enter/split-block tx:" (pr-str tx))
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid (str new-uid (when embed-id
                                                           (str "-embed-" embed-id)))]]]]})

        {:fx [[:dispatch [:remote/split-block args]]]}))))


(reg-event-fx
  :enter/bump-up
  (fn [_ [_ {:keys [uid new-uid embed-id] :as args}]]
    (js/console.debug ":enter/bump-up args" (pr-str args))
    (let [local? (not (client/open?))]
      (js/console.debug ":enter/bump-up local?" local?)
      (if local?
        (let [bump-up-event (common-events/build-bump-up-event -1
                                                               uid
                                                               new-uid)
              tx            (resolver/resolve-event-to-tx @db/dsdb bump-up-event)]
          (js/console.debug ":enter/bump-up tx:" (pr-str tx))
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid (str new-uid (when embed-id
                                                           (str "-embed-" embed-id)))]]]]})
        {:fx [[:dispatch [:remote/bump-up args]]]}))))


(reg-event-fx
  :enter/open-block-add-child
  (fn [_ [_ {:keys [block new-uid embed-id]}]]
    (js/console.debug ":enter/open-block-add-child" (pr-str block) new-uid)
    (let [local? (not (client/open?))]
      (js/console.debug ":enter/open-block-add-child local?" local?)
      (if local?
        (let [block-uid                  (:block/uid block)
              open-block-add-child-event (common-events/build-open-block-add-child-event -1
                                                                                         block-uid
                                                                                         new-uid)
              tx                         (resolver/resolve-event-to-tx @db/dsdb open-block-add-child-event)]
          (js/console.debug ":enter/open-block-add-child tx:" (pr-str tx))
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid (str new-uid (when embed-id
                                                           (str "-embed-" embed-id)))]]]]})
        {:fx [[:dispatch [:remote/open-block-add-chilid {:parent-uid (:block/uid block)
                                                         :new-uid    new-uid
                                                         :embed-id   embed-id}]]]}))))


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
        parent                (db/get-parent [:block/uid uid])
        is-parent-root-embed? (= (some-> d-key-down :target
                                         (.. (closest ".block-embed"))
                                         (. -firstChild)
                                         (.getAttribute "data-uid"))
                                 (str (:block/uid parent) "-embed-" embed-id))
        root-block?           (boolean (:node/title parent))
        context-root-uid      (get-in rfdb [:current-route :path-params :id])
        new-uid               (gen-block-uid)
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
                                [:enter/split-block {:uid      uid
                                                     :value    value
                                                     :index    start
                                                     :new-uid  new-uid
                                                     :embed-id embed-id}]

                                (empty? value)
                                [:unindent {:uid              uid
                                            :d-key-down       d-key-down
                                            :context-root-uid context-root-uid
                                            :embed-id         embed-id}]

                                (and (zero? start) value)
                                [:enter/bump-up {:uid      uid
                                                 :new-uid  new-uid
                                                 :embed-id embed-id}])]
    (js/console.debug "[Enter] ->" (pr-str event))
    {:fx [[:dispatch event]]}))


(reg-event-fx
  :enter
  (fn [{rfdb :db} [_ uid d-event]]
    (enter rfdb uid d-event)))


(reg-event-fx
  :indent
  (fn [_ [_ {:keys [uid d-key-down] :as args}]]
    "- `block-zero`: The first block in a page
     - `value`     : The current string inside the block being indented. Otherwise, if user changes block string and indents,
                     the local string  is reset to original value, since it has not been unfocused yet (which is currently the
                     transaction that updates the string). "
    (js/console.debug ":indent" args)
    (let [local?                    (not (client/open?))
          block                     (common-db/get-block @db/dsdb [:block/uid uid])
          block-zero?               (zero? (:block/order block))
          {:keys [value start end]} d-key-down]
      (js/console.debug ":indent local?" local?
                        ", block-zero?" block-zero?)
      (when-not block-zero?
        (if local?
          (let [indent-event (common-events/build-indent-event -1
                                                               uid
                                                               value)
                tx           (resolver/resolve-event-to-tx @db/dsdb indent-event)]
            (js/console.debug ":indent tx:" (pr-str tx))
            {:fx [[:dispatch            [:transact tx]]
                  [:set-cursor-position [uid start end]]]})
          {:fx [[:dispatch [:remote/indent (merge (select-keys args [:uid])
                                                  {:start start
                                                   :end   end
                                                   :value value})]]]})))))


(reg-event-fx
  :indent/multi
  (fn [_ [_ {:keys [uids]}]]
    (js/console.debug ":indent/multi" uids)
    (let [local?                   (not (client/open?))
          sanitized-selected-uids  (mapv (comp first common-db/uid-and-embed-id) uids)
          dsdb                     @db/dsdb
          same-parent?             (common-db/same-parent? dsdb sanitized-selected-uids)
          first-block-order        (:block/order (common-db/get-block dsdb [:block/uid (first sanitized-selected-uids)]))
          block-zero?              (zero? first-block-order)]
      (js/console.debug ":indent/multi local?"       local?
                        ", same-parent?"       same-parent?
                        ", not block-zero?"    (not  block-zero?))
      (when (and same-parent? (not block-zero?))
        (if local?
          (let [indent-multi-event  (common-events/build-indent-multi-event -1
                                                                            sanitized-selected-uids)
                tx                  (resolver/resolve-event-to-tx dsdb indent-multi-event)]
            (js/console.debug ":indent/multi tx" (pr-str tx))
            {:fx [[:dispatch [:transact tx]]]})
          {:fx [[:dispatch [:remote/indent-multi {:uids sanitized-selected-uids}]]]})))))


(reg-event-fx
  :unindent
  (fn [{_rfdb :db} [_ {:keys [uid d-key-down context-root-uid embed-id] :as args}]]
    (js/console.debug ":unindent args" (pr-str args))
    (let [local?                    (not (client/open?))
          parent                    (common-db/get-parent @db/dsdb
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
      (js/console.debug ":unindent local?" local?
                        ", do-nothing?" do-nothing?)
      (when-not do-nothing?
        (if local?
          (let [unindent-event (common-events/build-unindent-event -1
                                                                   uid
                                                                   value)
                tx             (resolver/resolve-event-to-tx @db/dsdb unindent-event)]
            (js/console.debug ":unindent tx:" (pr-str tx))
            {:fx [[:dispatch-n [[:transact tx]
                                [:editing/uid (str uid (when embed-id
                                                         (str "-embed-" embed-id)))]]]
                  [:set-cursor-position [uid start end]]]})
          {:fx [[:dispatch [:remote/unindent (merge (select-keys args [:uid :embed-id])
                                                    {:start start
                                                     :end   end
                                                     :value value})]]]})))))


(reg-event-fx
  :unindent/multi
  (fn [{rfdb :db} [_ {:keys [uids]}]]
    (js/console.debug ":unindent/multi" uids)
    (let [local?                      (not (client/open?))
          [f-uid f-embed-id]          (common-db/uid-and-embed-id (first uids))
          sanitized-selected-uids     (map (comp
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
          context-root-uid            (get-in rfdb [:current-route :path-params :id])
          do-nothing?                 (or parent-title
                                          (not same-parent?)
                                          (and same-parent? is-parent-root-embed?)
                                          (= parent-uid context-root-uid))]
      (js/console.debug ":unindent/multi local?" local?
                        ", do-nothing?"          do-nothing?)
      (when-not do-nothing?
        (if local?
          (let [unindent-multi-event  (common-events/build-unindent-multi-event -1
                                                                                sanitized-selected-uids)

                tx                  (resolver/resolve-event-to-tx @db/dsdb unindent-multi-event)]
            (js/console.debug ":unindent/multi tx" (pr-str tx))
            {:fx [[:dispatch [:transact tx]]]})
          {:fx [[:dispatch [:remote/unindent-multi {:uids sanitized-selected-uids}]]]})))))


(reg-event-fx
  :drop/child
  (fn [_ [_ {:keys [source-uid target-uid] :as args}]]
    (js/console.debug ":drop/child args" (pr-str args))
    (let [local? (not (client/open?))]
      (js/console.debug ":drop/child local?" local?)
      (if local?
        (let [drop-child-event (common-events/build-drop-child-event -1
                                                                     source-uid
                                                                     target-uid)
              tx               (resolver/resolve-event-to-tx @db/dsdb drop-child-event)]
          (js/console.debug ":drop/child tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-child args]]]}))))


(reg-event-fx
  :drop-multi/child
  (fn [_ [_ {:keys [source-uids target-uid] :as args}]]
    (js/console.debug ":drop-multi/child args" (pr-str args))
    (let [local? (not (client/open?))]
      (js/console.debug ":drop-multi/child local?" local?)
      (if local?
        (let [drop-multi-child-event (common-events/build-drop-multi-child-event -1
                                                                                 source-uids
                                                                                 target-uid)
              tx                     (resolver/resolve-event-to-tx @db/dsdb drop-multi-child-event)]
          (js/console.debug ":drop-multi/child tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-multi-child args]]]}))))


(reg-event-fx
  :drop-link/child
  (fn [_ [_ {:keys [source-uid target-uid] :as args}]]
    (js/console.debug ":drop-link/child args" (pr-str args))
    (let [local? (not (client/open?))]
      (js/console.debug ":drop-link/child local?" local?)
      (if local?
        (let [drop-link-child-event (common-events/build-drop-link-child-event -1
                                                                               source-uid
                                                                               target-uid)
              tx                    (resolver/resolve-event-to-tx @db/dsdb drop-link-child-event)]
          (js/console.debug ":drop-link/child tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-link-child args]]]}))))


(reg-event-fx
  :drop/diff-parent
  (fn [_ [_ {:keys [drag-target source-uid target-uid] :as args}]]
    (js/console.debug ":drop/diff-parent args" args)
    (let [local? (not (client/open?))]
      (js/console.debug ":drop/diff-parent local?" local?)
      (if local?
        (let [drop-diff-parent-event (common-events/build-drop-diff-parent-event -1
                                                                                 drag-target
                                                                                 source-uid
                                                                                 target-uid)
              tx                     (resolver/resolve-event-to-tx @db/dsdb drop-diff-parent-event)]
          (js/console.debug ":drop/diff-parent tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-diff-parent args]]]}))))


(reg-event-fx
  :drop-link/diff-parent
  (fn [_ [_ {:keys [drag-target source-uid target-uid] :as args}]]
    (js/console.debug ":drop-link/diff-parent args" args)
    (let [local? (not (client/open?))]
      (js/console.debug ":drop-link/diff-parent local?" local?)
      (if local?
        (let [drop-link-diff-parent-event (common-events/build-drop-link-diff-parent-event -1
                                                                                           drag-target
                                                                                           source-uid
                                                                                           target-uid)
              tx                          (resolver/resolve-event-to-tx @db/dsdb drop-link-diff-parent-event)]
          (js/console.debug ":drop-link/diff-parent tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-link-diff-parent args]]]}))))


(reg-event-fx
  :drop/same
  (fn [_ [_ {:keys [drag-target source-uid target-uid] :as args}]]
    (js/console.debug ":drop/same args" args)
    (let [local? (not (client/open?))]
      (js/console.debug ":drop/same local?" local?)
      (if local?
        (let [drop-same-event   (common-events/build-drop-same-event -1
                                                                     drag-target
                                                                     source-uid
                                                                     target-uid)
              tx                (resolver/resolve-event-to-tx @db/dsdb drop-same-event)]
          (js/console.debug ":drop/same tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-same args]]]}))))


(reg-event-fx
  :drop-multi/same-source
  (fn [_ [_ {:keys [drag-target source-uids target-uid] :as args}]]
    ;; When the selected blocks have same parent and are DnD under some other block this event is fired.
    (js/console.debug ":drop-multi/same-source args" args)
    (let [local? (not (client/open?))]
      (js/console.debug ":drop-multi/same-source local?" local?)
      (if local?
        (let [drop-multi-same-source-event   (common-events/build-drop-multi-same-source-event -1
                                                                                               drag-target
                                                                                               source-uids
                                                                                               target-uid)
              tx                (resolver/resolve-event-to-tx @db/dsdb drop-multi-same-source-event)]
          (js/console.debug ":drop-multi/same-source tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-multi-same-source args]]]}))))


(reg-event-fx
  :drop-multi/same-all
  (fn [_ [_ {:keys [drag-target source-uids target-uid] :as args}]]
    ;; When the selected blocks have same parent and are DnD under the same parent this event is fired.
    ;; This also applies if on selects multiple Zero level blocks and change the order among other Zero level blocks.
    (js/console.debug ":drop-multi/same-all args" args)
    (let [local? (not (client/open?))]
      (js/console.debug ":drop-multi/same-all local?" local?)
      (if local?
        (let [drop-multi-same-all-event   (common-events/build-drop-multi-same-all-event -1
                                                                                         drag-target
                                                                                         source-uids
                                                                                         target-uid)
              tx                (resolver/resolve-event-to-tx @db/dsdb drop-multi-same-all-event)]
          (js/console.debug ":drop-multi/same-all tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-multi-same-all args]]]}))))


(reg-event-fx
  :drop-link/same-parent
  (fn [_ [_ {:keys [drag-target source-uid target-uid] :as args}]]
    (js/console.debug ":drop-link/same-parent args" args)
    (let [local? (not (client/open?))]
      (js/console.debug ":drop-link/same-parent local?" local?)
      (if local?
        (let [drop-link-same-parent-event   (common-events/build-drop-link-same-parent-event -1
                                                                                             drag-target
                                                                                             source-uid
                                                                                             target-uid)
              tx                (resolver/resolve-event-to-tx @db/dsdb drop-link-same-parent-event)]
          (js/console.debug ":drop-link/same-parent tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-link-same args]]]}))))



(reg-event-fx
  :drop-multi/diff-source-same-parents
  (fn [_ [_ {:keys [drag-target source-uids target-uid] :as args}]]
    (js/console.debug ":drop-multi/diff-source-same-parents args" args)
    (let [local? (not (client/open?))]
      (js/console.log "local?" local?)
      (if local?
        (let [drop-multi-diff-source-same-parents-event (common-events/build-drop-multi-diff-source-same-parents-event -1
                                                                                                                       drag-target
                                                                                                                       source-uids
                                                                                                                       target-uid)
              tx                                        (resolver/resolve-event-to-tx @db/dsdb drop-multi-diff-source-same-parents-event)]
         (js/console.log ":drop-multi-diff-source-same-parents tx" tx)
         {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-multi-diff-source-same-parents args]]]}))))


(reg-event-fx
  :drop-multi/diff-source-diff-parents
  (fn [_ [_ {:keys [drag-target source-uids target-uid] :as args}]]
    (js/console.debug ":drop-multi/diff-source-diff-parents args" args)
    (let [local? (not (client/open?))]
      (js/console.log "local?" local?)
      (if local?
        (let [drop-multi-diff-source-diff-parents-event (common-events/build-drop-multi-diff-source-diff-parents-event -1
                                                                                                                       drag-target
                                                                                                                       source-uids
                                                                                                                       target-uid)
              tx                                        (resolver/resolve-event-to-tx @db/dsdb drop-multi-diff-source-diff-parents-event)]
          (js/console.log ":drop-multi-diff-source-diff-parents tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/drop-multi-diff-source-diff-parents args]]]}))))




(defn text-to-blocks
  [text uid root-order]
  (let [;; Split raw text by line
        lines       (->> (clojure.string/split-lines text)
                         (filter (comp not clojure.string/blank?)))
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
                                    :block/uid    (gen-block-uid)})
                                 sanitize)
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
    (let [[uid embed-id]  (db/uid-and-embed-id uid)
          block         (db/get-block [:block/uid uid])
          {:block/keys  [order children open]} block
          {:keys [start value]} (textarea-keydown/destruct-target js/document.activeElement) ; TODO: coeffect
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
                        [:editing/uid (cond-> uid
                                        embed-id (str "-embed-" embed-id)) n]))]})))


(reg-event-fx
  :paste-verbatim
  (fn [{_db :db} [_ uid text]]
    ;; NOTE: use of `value` is questionable, it's the DOM so it's what users sees,
    ;; but what users sees should taken from DB. How would `value` behave with multiple editors?
    (let [{:keys [start value]} (textarea-keydown/destruct-target js/document.activeElement)
          ;; TODO: this is a wrong check, it should check if we're by configuration in Lan Party
          local?                (not (client/open?))]
      (if local?
        {:fx [[:dispatch [:transact (resolver/resolve-event-to-tx
                                      db/dsdb
                                      (common-events/build-paste-verbatim-event -1 uid text start value))]]]}

        {:fx [[:dispatch [:remote/paste-verbatim uid text start value]]]}))))


(reg-event-fx
  :unlinked-references/link
  (fn [_ [_ {:block/keys [string uid]} title]]
    (js/console.debug ":unlinked-references/link:" uid)
    (let [local? (not (client/open?))]
      (js/console.debug ":unlinked-references/link: local?" local?)
      (if local?
        (let [unlinked-references-link-event (common-events/build-unlinked-references-link -1 uid string title)
              tx-data                        (resolver/resolve-event-to-tx @db/dsdb unlinked-references-link-event)]
          {:fx [[:dispatch [:transact tx-data]]]})
        {:fx [[:dispatch [:remote/unlinked-references-link string uid title]]]}))))


(reg-event-fx
  :unlinked-references/link-all
  (fn [_ [_ unlinked-refs title]]
    (js/console.debug ":unlinked-references/link:" title)
    (let [local? (not (client/open?))]
      (js/console.debug ":unlinked-references/link: local?" local?)
      (if local?
        (let [unlinked-references-link-all-event (common-events/build-unlinked-references-link-all -1 unlinked-refs title)
              tx-data                            (resolver/resolve-event-to-tx @db/dsdb unlinked-references-link-all-event)]
          {:fx [[:dispatch [:transact tx-data]]]})
        {:fx [[:dispatch [:remote/unlinked-references-link-all unlinked-refs title]]]}))))


(reg-event-fx
  :block/open
  (fn [_ [_ {:keys [block-uid open?] :as args}]]
    (js/console.debug ":block/open args" args)
    (let [local? (not (client/open?))]
      (js/console.debug ":block/open local?" local?)
      (if local?
        (let [block-open-event   (common-events/build-block-open-event -1
                                                                       block-uid
                                                                       open?)
              tx                (resolver/resolve-event-to-tx @db/dsdb block-open-event)]
          (js/console.debug ":block/open tx" tx)
          {:fx [[:dispatch [:transact tx]]]})
        {:fx [[:dispatch [:remote/block-open args]]]}))))