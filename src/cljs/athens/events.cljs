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
    (update db :selected/items (fnil conj #{}) uid)))


(reg-event-db
  :selected/remove-item
  (fn [db [_ uid]]
    (update db :selected/items disj uid)))


(reg-event-db
  :selected/remove-items
  (fn [db [_ uids]]
    (update db :selected/items #(apply disj %1 %2) uids)))


(reg-event-db
  :selected/add-items
  (fn [db [_ uids]]
    (update db :selected/items #(apply conj %1 %2) uids)))


(reg-event-db
  :selected/clear-items
  (fn [db _]
    (assoc db :selected/items #{})))


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
    (assoc db :selected/items (select-up selected-items))))


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
    (assoc db :selected/items (select-down selected-items))))


(defn delete-selected
  "We know that we only need to dec indices after the last block. The former blocks are necessarily going to remove all
  tail children, meaning we only need to be concerned with the last N blocks that are selected, adjacent siblings, to
  determine the minus-after value."
  [selected-items]
  (let [last-item (-> selected-items last db/uid-and-embed-id first)
        selected-sibs-of-last (->> selected-items
                                   (mapv (comp first db/uid-and-embed-id))
                                   (d/q '[:find ?sib-uid ?o
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
                                        @db/dsdb last-item)
                                   (sort-by second))
        [uid order] (last selected-sibs-of-last)
        parent (db/get-parent [:block/uid uid])
        n (count selected-sibs-of-last)]
    (minus-after (:db/id parent) order n)))


(reg-event-fx
  :selected/delete
  (fn [{:keys [db]} [_ selected-items]]
    (let [sanitize-selected (map (comp first db/uid-and-embed-id) selected-items)
          retract-vecs      (mapcat #(retract-uid-recursively %) sanitize-selected)
          reindex-last-selected-parent (delete-selected sanitize-selected)
          tx-data           (concat retract-vecs reindex-last-selected-parent)]
      {:fx [[:dispatch [:transact tx-data]]
            [:dispatch [:editing/uid nil]]]
       :db (assoc db :selected/items [])})))


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
  (fn [_ [_ title uid]]
    (js/console.debug ":page/create" title uid)
    (let [local? (not (client/open?))]
      (js/console.debug ":page/create local?" local?)
      (if local?
        (let [create-page-event (common-events/build-page-create-event -1
                                                                       uid
                                                                       title)
              tx                (resolver/resolve-event-to-tx @db/dsdb create-page-event)
              child-uid         (-> tx
                                    first ; page
                                    :block/children
                                    first ; 1st child
                                    :block/uid)]
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid child-uid]]]]})
        {:fx [[:dispatch
               [:remote/page-create uid title]]]}))))


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


;; resetting ds-conn re-computes all subs and is the cause
;;    for huge delays when undo/redo is pressed
;; here is another alternative strategy for undo/redo
;; Core ideas are inspired from here https://tonsky.me/blog/datascript-internals/
;;    1. db-before + tx-data = db-after
;;    2. DataScript DB contains only currently relevant datoms.
;; 1 is the math that is happening here when you undo/redo but in a much more performant way
;; 2 asserts that even if you are reapplying same txn over and over only relevant ones will be present
;;    - and overall size of transit file does not increase
;; Note: only relevant txns(ones that user deliberately made, not undo/redo ones) go into history
;; Note: Once session is lost(App is closed) edit history is also lost
;; Also cmd + z -> cmd + z -> edit something --- future(cmd + shift + z) doesn't work
;;    - as there is no logical way to assert the future when past has changed hence history is reset
;;    - very similar to intelli-j edit model or any editor's undo/redo mechanism for that matter
(defn inverse-tx
  ([] (inverse-tx false))
  ([redo?]
   (let [[tx-m-id datoms] (cond->> @db/history
                            redo? reverse
                            true (some (fn [[tx bool datoms]]
                                         (and ((if redo? not (complement not)) bool) [tx datoms]))))]
     (reset! db/history (->> @db/history
                             (map (fn [[tx-id bool datoms]]
                                    (if (= tx-id tx-m-id)
                                      [tx-id redo? datoms]
                                      [tx-id bool datoms])))
                             doall))
     (cond->> datoms
       (not redo?) reverse
       true (map (fn [datom]
                   (let [[id attr val _txn sig?] (vec datom)]
                     [(cond
                        (and sig? (not redo?)) :db/retract
                        (and (not sig?) (not redo?)) :db/add
                        (and sig? redo?) :db/add
                        (and (not sig?) redo?) :db/retract)
                      id attr val])))
       ;; Caveat -- we need a way to signal history watcher if this txn is relevant
       ;;     - send a dummy datom, this will get added to user's data
       ;;     - we can easily filter it out while writing to fs but it will have a perf penalty
       ;;     - Unless we are exporting transit to a common format, this can stay(only one datom -- point 2 mentioned above)
       ;;           - although a filter while exporting is more strategic -- once in a while op, compared to fs write(very frequent)
       true (concat [[:db/add "new" :from-undo-redo true]])))))


(reg-event-fx
  :undo
  (fn [_ _]
    {:dispatch [:transact (inverse-tx)]}))


(reg-event-fx
  :redo
  (fn [_ _]
    {:dispatch [:transact (inverse-tx true)]}))


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
                                                                   (:db/id block)
                                                                   new-uid)
              tx              (resolver/resolve-event-to-tx @db/dsdb add-child-event)]
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid (str new-uid (when embed-id
                                                           (str "-embed-" embed-id)))]]]]})
        {:fx [[:dispatch [:remote/add-child {:block-eid (:remote/db-id block)
                                             :new-uid   new-uid
                                             :embed-id  embed-id}]]]}))))


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
        (let [block-eid                  (:db/id block)
              open-block-add-child-event (common-events/build-open-block-add-child-event -1
                                                                                         block-eid
                                                                                         new-uid)
              tx                         (resolver/resolve-event-to-tx @db/dsdb open-block-add-child-event)]
          (js/console.debug ":enter/open-block-add-child tx:" (pr-str tx))
          {:fx [[:dispatch-n [[:transact tx]
                              [:editing/uid (str new-uid (when embed-id
                                                           (str "-embed-" embed-id)))]]]]})
        {:fx [[:dispatch [:remote/open-block-add-chilid {:block-eid (:remote/db-id block)
                                                         :new-uid   new-uid
                                                         :embed-id  embed-id}]]]}))))


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
    (indent-multi (mapv (comp first db/uid-and-embed-id) uids))))


(reg-event-fx
  :unindent
  (fn [{rfdb :db} [_ {:keys [uid d-key-down context-root-uid embed-id] :as args}]]
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


(defn unindent-multi
  "Do not do anything if root block child or if blocks are not siblings.
  Otherwise, retract and assert new parent for each block, and reindex parent and grandparent."
  [uids context-root-uid]
  (let [[f-uid f-embed-id]    (-> uids first db/uid-and-embed-id)
        parent                (db/get-parent [:block/uid f-uid])
        same-parent?          (db/same-parent? uids)
        ;; when all selected items are from same embed block
        ;; check if immediate parent is root-embed
        is-parent-root-embed? (when same-parent?
                                (some-> "#editable-uid-"
                                        (str f-uid "-embed-" f-embed-id)
                                        js/document.querySelector
                                        (.. (closest ".block-embed"))
                                        (. -firstChild)
                                        (.getAttribute "data-uid")
                                        (= (str (:block/uid parent) "-embed-" f-embed-id))))]
    (cond
      (:node/title parent)                     nil
      (= (:block/uid parent) context-root-uid) nil
      (not same-parent?)                       nil
      ;; if all selected are from same embed block with root embed
      ;; as parent un-indent should do nothing -- blocks will disappear from embed and
      ;; have to manually navigate to block to see the un-indented blocks
      (and same-parent? is-parent-root-embed?) nil
      :else (let [grandpa         (db/get-parent (:db/id parent))
                  sanitized-uids  (map (comp
                                         first
                                         db/uid-and-embed-id) uids)
                  blocks          (map #(db/get-block [:block/uid %]) sanitized-uids)
                  o-parent        (:block/order parent)
                  n-blocks        (count blocks)
                  last-block      (last blocks)
                  reindex-parent  (minus-after (:db/id parent) (:block/order last-block) n-blocks)
                  new-parent      {:db/id (:db/id parent) :block/children reindex-parent}
                  new-blocks      (map-indexed (fn [idx uid] {:block/uid uid :block/order (+ idx (inc o-parent))})
                                               sanitized-uids)
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


(defn drop-link-child
  "Create a new block with the reference to the source block, as a child"
  [source target]
  (let [new-uid               (gen-block-uid)
        new-string            (str "((" (source :block/uid) "))")
        new-source-block      {:block/uid new-uid :block/string new-string :block/order 0 :block/open true}
        reindex-target-parent (inc-after (:db/id target) -1)
        new-target-parent     {:db/id (:db/id target) :block/children (conj reindex-target-parent new-source-block)}
        tx-data               [new-source-block
                               new-target-parent]]
    tx-data))


(reg-event-fx
  :drop-link/child
  (fn [_ [_ source target]]
    {:dispatch [:transact (drop-link-child source target)]}))


(defn drop-link-same-parent
  "Create a new block with the reference to the source block, under the same parent as the source"
  [kind source parent target]
  (let [new-uid             (gen-block-uid)
        new-string          (str "((" (source :block/uid) "))")
        s-order             (:block/order source)
        t-order             (:block/order target)
        target-above?       (< t-order s-order)
        +or-                (if target-above? + -)
        above?                (= kind :above)
        below?                (= kind :below)
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
        new-source-block      {:block/uid new-uid :block/string new-string :block/order new-source-order}
        new-parent-children (concat [new-source-block] reindex)
        new-parent          {:db/id (:db/id parent) :block/children new-parent-children}
        tx-data             [new-parent]]
    tx-data))


(reg-event-fx
  :drop-link/same
  (fn [_ [_ kind source parent target]]
    {:dispatch [:transact (drop-link-same-parent kind source parent target)]}))


(defn drop-link-diff-parent
  "Add a link to the source block and reorder the target"
  [kind source target target-parent]
  (let [new-uid             (gen-block-uid)
        new-string          (str "((" (source :block/uid) "))")
        t-order               (:block/order target)
        new-block             {:block/uid new-uid :block/string new-string :block/order (if (= kind :above)
                                                                                          t-order
                                                                                          (inc t-order))}
        reindex-target-parent (->> (inc-after (:db/id target-parent) (if (= kind :above)
                                                                       (dec t-order)
                                                                       t-order))
                                   (concat [new-block]))
        new-target-parent     {:db/id (:db/id target-parent) :block/children reindex-target-parent}]
    [new-target-parent]))


(reg-event-fx
  :drop-link/diff
  (fn [_ [_ kind source target target-parent]]
    {:dispatch [:transact (drop-link-diff-parent kind source target target-parent)]}))


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
  [source-uid target-uid kind effect-allowed]
  (let [source        (db/get-block [:block/uid source-uid])
        target        (db/get-block [:block/uid target-uid])
        source-parent (db/get-parent [:block/uid source-uid])
        target-parent (db/get-parent [:block/uid target-uid])
        same-parent?  (= source-parent target-parent)
        event         (cond
                        (and (= effect-allowed "move") (= kind :child)) [:drop/child source source-parent target]
                        (and (= effect-allowed "move") same-parent?) [:drop/same kind source source-parent target]
                        (and (= effect-allowed "move") (not same-parent?)) [:drop/diff kind source source-parent target target-parent]
                        (and (= effect-allowed "link") (= kind :child)) [:drop-link/child source target]
                        (and (= effect-allowed "link") same-parent?) [:drop-link/same kind source source-parent target]
                        (and (= effect-allowed "link") (not same-parent?)) [:drop-link/diff kind source target target-parent])]
    {:dispatch event}))


(reg-event-fx
  :drop
  (fn [_ [_ source-uid target-uid kind effect-allowed]]
    (drop-bullet source-uid target-uid kind effect-allowed)))


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
  (let [source-uids          (map (comp first db/uid-and-embed-id) source-uids)
        target-uid           (first (db/uid-and-embed-id target-uid))
        same-parent-all?     (db/same-parent? (conj source-uids target-uid))
        same-parent-source?  (db/same-parent? source-uids)
        diff-parents-source? (not same-parent-source?)
        target               (db/get-block [:block/uid target-uid])
        first-source-uid     (first source-uids)
        first-source-parent  (db/get-parent [:block/uid first-source-uid])
        target-parent        (db/get-parent [:block/uid target-uid])
        event                (cond
                               (= kind :child) [:drop-multi/child source-uids target]
                               same-parent-all? [:drop-multi/same-all kind source-uids first-source-parent target]
                               diff-parents-source? [:drop-multi/diff-source kind source-uids target target-parent]
                               same-parent-source? [:drop-multi/same-source kind source-uids first-source-parent target target-parent])]
    {:fx [[:dispatch [:selected/clear-items]]
          [:dispatch event]]}))


(reg-event-fx
  :drop-multi
  (fn [_ [_ uids target-uid kind]]
    (drop-bullet-multi uids target-uid kind)))


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
  (fn [_ [_ {:block/keys [string uid] :as block} title]]
    (js/console.debug ":unlinked-references/link:" uid)
    (if-let [local? (not (client/open?))]
      (let [unlinked-references-link-event (common-events/build-unlinked-references-link -1 uid string title)
            tx-data                        (resolver/resolve-event-to-tx @db/dsdb unlinked-references-link-event)]
        (js/console.debug ":unlinked-references/link: local?" local?)
        {:fx [[:dispatch [:transact tx-data]]]})
      {:fx [[:dispatch [:remote/unlinked-references-link block title]]]})))


(reg-event-fx
  :unlinked-references/link-all
  (fn [_ [_ unlinked-refs title]]
    (js/console.debug ":unlinked-references/link:" title)
    (if-let [local? (not (client/open?))]
      (let [unlinked-references-link-all-event (common-events/build-unlinked-references-link-all -1 unlinked-refs title)
            tx-data                            (resolver/resolve-event-to-tx @db/dsdb unlinked-references-link-all-event)]
        (js/console.debug ":unlinked-references/link: local?" local?)
        {:fx [[:dispatch [:transact tx-data]]]})
      {:fx [[:dispatch [:remote/unlinked-references-link-all unlinked-refs title]]]})))
