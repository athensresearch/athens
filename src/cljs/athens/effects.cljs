(ns athens.effects
  (:require
    [athens.config :as config]
    [athens.datsync-utils :as dat-s]
    [athens.db :as db]
    [athens.keybindings :refer [bind-changeable-global-keybindings]]
    [athens.util :as util]
    [athens.walk :as walk]
    [athens.ws-client :as ws]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [cljs.pprint :refer [pprint]]
    [clojure.edn :as edn]
    [clojure.string :as str]
    [dat.sync.client]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [goog.dom.selection :refer [setCursorPosition]]
    [posh.reagent :as p :refer [transact!]]
    [re-frame.core :refer [dispatch reg-fx subscribe]]
    [stylefy.core :as stylefy]))


;; Effects

(defn new-titles-to-tx-data
  "Filter: node/title doesn't exist yet in the db or in the titles being asserted (e.g. when renaming a page and changing it's references).
  Map: new node/title entity."
  [new-titles assert-titles]
  (let [now (util/now-ts)]
    (->> new-titles
         (filter (fn [x]
                   (and (nil? (db/search-exact-node-title x))
                        (not (contains? assert-titles x)))))
         (map (fn [t]
                {:node/title  t
                 :block/uid   (util/gen-block-uid)
                 :create/time now
                 :edit/time   now})))))


(defn old-titles-to-tx-data
  "Purpose is to remove orphan pages. However, if entire entity is retracted, orphan pages are still created.

  Filter: new-str doesn't include link, page exists, page has no children, and has no other [[linked refs]].
  Map: retractEntity"
  [old-titles new-str with-db]
  (->> old-titles
       (filter (fn [title]
                 (let [node (db/pull-nil with-db '[*] [:node/title title])]
                   (and (not (clojure.string/includes? new-str title))
                        node
                        (empty? (:block/children node))
                        (= 1 (db/linked-refs-count title))))))
       (map (fn [title]
              (when-let [eid (:db/id (db/pull-nil with-db '[*] [:node/title title]))]
                [:db/retractEntity eid])))))


(defn new-refs-to-tx-data
  "Filter: ((ref-uid)) points to a valid block (no :node/title).
  Map: add block/ref relationship."
  [new-block-refs e]
  (->> new-block-refs
       (filter (fn [ref-uid]
                 (let [block (d/q '[:find (pull ?e [*]) .
                                    :in $ ?uid
                                    :where [?e :block/uid ?uid]]
                                  @db/dsdb ref-uid)
                       {:keys [node/title]} block]
                   (and block (nil? title)))))
       (map (fn [ref-uid] [:db/add e :block/refs [:block/uid ref-uid]]))))


(defn new-page-refs-to-tx-data
  "Filter: No filter.
  Map: add block/ref relationship."
  [new-page-refs source-eid]
  (->> new-page-refs
       (map (fn [page-id] [:db/add source-eid :block/refs page-id]))))


(defn old-block-refs-to-tx-data
  "Filter: new-str doesn't include block ref anymore, ((ref-uid)) points to an actual block, and block/ref relationship exists.
  Map: retract relationship."
  [old-block-refs e new-str]
  (->> old-block-refs
       (filter (fn [ref-uid]
                 (let [eid (db/e-by-av :block/uid ref-uid)]
                   (and eid
                        (not (str/includes? new-str (str "((" ref-uid "))")))))))
       (map (fn [ref-uid] [:db/retract e :block/refs [:block/uid ref-uid]]))))


(defn old-page-refs-to-tx-data
  "Filter: [[page]] points to a page and block/ref relationship does exist.
  Map: retract block/ref relationship.

  Edge Cases:
  1. Merging two pages (renaming a page to a title that already exists).
  - This attempt to update all the Linked References strings
  - Querying with-db rather than the current-db to check that entity retraction already takes care of block/ref retraction.

  2. Deleting an orphan page, i.e. deleting a [[link]] when the [[link]] has no children and no other linked references
  - In this case, we can't use with-db, because the orphan page retraction happens in old-titles-to-tx-data.
  - Pass `old-titles` and check that the block/ref being deleted is not there to avoid double retraction.
  - Don't use :db.fn/retractAttribute because :db.cardinality/many"
  [old-page-refs source-eid new-str with-db old-titles]
  (->> old-page-refs
       (filter (fn [page-id]
                 (let [page (db/pull-nil with-db '[*] page-id)
                       old-pages-eids (set (map second old-titles))
                       {:keys [node/title]} page]
                   (and (not (str/includes? new-str (str "[[" title "]]")))
                        page
                        title
                        (not (get old-pages-eids (:db/id page)))))))
       (map (fn [page-id]
              (when-let [page (db/pull-nil with-db '[*] page-id)]
                [:db/retract source-eid :block/refs [:block/uid (:block/uid page)]])))))


(defn parse-for-links
  "When block/string is asserted, parse for links and block refs to add.
  When block/string is retracted, parse for links and block refs to remove.
  Retractions need to look at asserted block/string. Use empty string if only retract."
  [with-tx]
  (let [with-tx-data  (:tx-data with-tx)
        with-db       (:db-after with-tx)
        assert-titles (->> with-tx-data
                           (filter #(and (= (second %) :node/title)
                                         (true? (last %))))
                           (map #(nth % 2))
                           set)]
    (->> with-tx-data
         (filter #(= (second %) :block/string))
         ;; group-by entity
         (group-by first)
         ;; map sort-by so [true false] gives us [assertion retraction], [assertion], or [retraction]
         (mapv (fn [[_eid datoms]]
                 (sort-by #(-> % last not) datoms)))
         (mapcat (fn [[assertion retraction]]
                   (cond
                     ;; [assertion retraction]
                     (and (true? (last assertion)) (false? (last retraction)))
                     (let [eid            (first assertion)
                           assert-string  (nth assertion 2)
                           retract-string (nth retraction 2)
                           assert-data    (walk/walk-string assert-string)
                           retract-data   (walk/walk-string retract-string)
                           new-block-refs (new-refs-to-tx-data (:block/refs assert-data) eid)
                           old-titles     (old-titles-to-tx-data (:node/titles retract-data) assert-string with-db)
                           new-titles     (new-titles-to-tx-data (:node/titles assert-data) assert-titles)
                           new-page-refs  (new-page-refs-to-tx-data (:page/refs assert-data) eid)
                           old-block-refs (old-block-refs-to-tx-data (:block/refs retract-data) eid assert-string)
                           old-page-refs  (old-page-refs-to-tx-data (:page/refs retract-data) eid assert-string with-db old-titles)
                           tx-data        (concat []
                                                  new-titles
                                                  new-block-refs
                                                  new-page-refs
                                                  old-titles
                                                  old-block-refs
                                                  old-page-refs)]
                       tx-data)

                     ;; [assertion]
                     (and (true? (last assertion)) (nil? retraction))
                     (let [eid            (first assertion)
                           assert-string  (nth assertion 2)
                           assert-data    (walk/walk-string assert-string)
                           new-titles     (new-titles-to-tx-data (:node/titles assert-data) assert-titles)
                           new-page-refs  (new-page-refs-to-tx-data (:page/refs assert-data) eid)
                           new-block-refs (new-refs-to-tx-data (:block/refs assert-data) eid)
                           tx-data        (concat []
                                                  new-titles
                                                  new-block-refs
                                                  new-page-refs)]
                       tx-data)

                     ;; [retraction]
                     ;; :block/string itself is rarely retracted directly.
                     (and (false? (last assertion)) (nil? retraction))
                     (let [eid            (first retraction)
                           assert-string  ""
                           retract-string (nth retraction 2)
                           retract-data   (walk/walk-string retract-string)
                           old-titles     (old-titles-to-tx-data (:node/titles retract-data) assert-string with-db)
                           old-block-refs (old-block-refs-to-tx-data (:block/refs retract-data) eid assert-string)
                           old-page-refs  (old-page-refs-to-tx-data (:page/refs retract-data) eid assert-string with-db old-titles)
                           tx-data        (concat []
                                                  old-titles
                                                  old-block-refs
                                                  old-page-refs)]
                       tx-data)))))))


(defn ph-link-created!
  "Only creates `link-created` events for now.
  TODO: link-deleted events"
  [outputs]
  (doall (->> outputs
              (filter (fn [[_e a _v _t t-or-f]]
                        (and (= a :block/refs)
                             t-or-f)))
              (map (fn [[e _a v _t _t-or-f]]
                     (let [num-refs (-> (d/pull @db/dsdb '[:block/_refs] v)
                                        :block/_refs
                                        count)
                           block-or-page (if (:node/title (d/pull @db/dsdb '[:node/title :block/string] e))
                                           :page
                                           :block)]
                       {:refs num-refs
                        :attr block-or-page})))
              (map (fn [x]
                     (.. js/posthog (capture "link-created", (clj->js x))))))))


(defn dev-pprint
  [data]
  (when config/debug? (pprint data)))


(defn walk-transact
  [tx-data]
  (let [socket-status     (subscribe [:socket-status])
        remote-graph-conf (subscribe [:db/remote-graph-conf])]
    (if (= @socket-status :closed)
      (dispatch [:show-snack-msg
                 {:msg "Graph is now read only"}])
      (do (dev-pprint "TX RAW INPUTS")                             ; event tx-data
          (dev-pprint tx-data)
          (try
            (let [with-tx (d/with @db/dsdb tx-data)]
              (dev-pprint "TX WITH")                               ; tx-data normalized by datascript to flat datoms
              (dev-pprint (:tx-data with-tx))
              (let [more-tx-data  (parse-for-links with-tx)
                    final-tx-data (vec (concat tx-data more-tx-data))]
                (dev-pprint "TX MORE")                             ; parsed tx-data, e.g. asserting/retracting pages and references
                (dev-pprint more-tx-data)
                (dev-pprint "TX FINAL INPUTS")                     ; parsing block/string (and node/title) to derive asserted or retracted titles and block refs
                (dev-pprint final-tx-data)
                (let [{:keys [db-before tx-data]} (transact! db/dsdb final-tx-data)]

                  ;; check remote data against previous db
                  (when (and (:default? @remote-graph-conf)
                             (= @socket-status :running))
                    ((:send-fn ws/channel-socket)
                     [:dat.sync.client/tx
                      [ws/cur-random
                       (dat-s/remote-tx
                         db-before
                         (mapv (fn [[e a v _t sig?]]
                                 [(if sig? :db/add :db/retract) e a v])
                               tx-data))]]))

                  (ph-link-created! tx-data)
                  (dev-pprint "TX OUTPUTS")
                  (dev-pprint tx-data))))

            (catch js/Error e
              (js/alert (str e))
              (js/console.log "EXCEPTION" e)))))))


(reg-fx
  :transact!
  (fn [tx-data]
    (walk-transact tx-data)))


(reg-fx
  :reset-conn!
  (fn [new-db]
    (d/reset-conn! db/dsdb new-db)))


(reg-fx
  :local-storage/set!
  (fn [[key value]]
    (js/localStorage.setItem key value)))


(reg-fx
  :local-storage/assoc!
  (fn [[storage-key key value]]
    (let [old-value (or (edn/read-string (js/localStorage.getItem storage-key)) {})
          new-value (assoc old-value key value)]
      (js/localStorage.setItem storage-key new-value))))


(reg-fx
  :local-storage/dissoc!
  (fn [[storage-key key]]
    (let [old-value (or (edn/read-string (js/localStorage.getItem storage-key)) {})
          new-value (dissoc old-value key)]
      (js/localStorage.setItem storage-key new-value))))


(reg-fx
  :local-storage/set-db!
  (fn [db]
    (js/localStorage.setItem "datascript/DB" (dt/write-transit-str db))))


(reg-fx
  :http
  (fn [{:keys [url method opts on-success on-failure]}]
    (go
      (let [http-fn (case method
                      :post http/post :get http/get
                      :put http/put :delete http/delete)
            res     (<! (http-fn url opts))
            {:keys [success body] :as all} res]
        (if success
          (dispatch (conj on-success body))
          (dispatch (conj on-failure all)))))))


(reg-fx
  :timeout
  (let [timers (atom {})]
    (fn [{:keys [action id event wait]}]
      (case action
        :start (swap! timers assoc id (js/setTimeout #(dispatch event) wait))
        :clear (do (js/clearTimeout (get @timers id))
                   (swap! timers dissoc id))))))


;; Using DOM, focus the target block.
;; There can actually be multiple elements with the same #editable-uid-UID HTML id
;; The same unique datascript block can be rendered multiple times: node-page, right sidebar, linked/unlinked references
;; In this case, find the all the potential HTML blocks with that uid. The one that shares the same closest ancestor as the
;; activeElement (where the text caret is before the new focus happens), is the container of the block to focus on.

;; If an index is passed, set cursor to that index.

;; TODO: some issues
;; - auto-focus on textarea
;; - searching for common-ancestor on inside of setTimeout vs outside
;;   - element sometimes hasn't been created yet (enter), sometimes has been just destroyed (backspace)
;; - uid sometimes nil

(reg-fx
  :editing/focus
  (fn [[uid index]]
    (if (nil? uid)
      (when-let [active-el (.-activeElement js/document)]
        (.blur active-el))
      (js/setTimeout (fn []
                       (let [[uid embed-id]  (db/uid-and-embed-id uid)
                             html-id         (str "editable-uid-" uid)
                             ;; targets (js/document.querySelectorAll html-id)
                             ;; n       (count (array-seq targets))
                             el              (js/document.querySelector
                                               (if embed-id
                                                 (or
                                                   ;; find exact embed block
                                                   (str "textarea[id='" html-id "-embed-" embed-id "']")
                                                   ;; find embedded that starts with current html id (embed id changed due to re-render)
                                                   (str "textarea[id^='" html-id "-embed-']"))
                                                 ;; take default
                                                 (str "#" html-id)))]
                         #_(cond
                             (zero? n) (prn "No targets")
                             (= 1 n) (prn "One target")
                             (< 1 n) (prn "Several targets"))
                         (when el
                           (.focus el)
                           (when index
                             (setCursorPosition el index)))))
                     100))))


;; todo(abhinav)
;; think of this + up/down + editing/focus for common up down press
;; and cursor goes to apt position rather than last visited point in the block(current)
;; inspirations - intelli-j's up/down
(reg-fx
  :set-cursor-position
  (fn [[uid start end]]
    (js/setTimeout (fn []
                     (when-let [target (js/document.querySelector (str "#editable-uid-" uid))]
                       (.focus target)
                       (set! (.-selectionStart target) start)
                       (set! (.-selectionEnd target) end)))
                   100)))


(reg-fx
  :stylefy/tag
  (fn [[tag properties]]
    (stylefy/tag tag properties)))


(reg-fx
  :alert/js!
  (fn [message]
    (js/alert message)))


(reg-fx
  :right-sidebar/scroll-top
  (fn []
    (let [right-sidebar (js/document.querySelector ".right-sidebar-content")]
      (when right-sidebar
        (set! (.. right-sidebar -scrollTop) 0)))))


(def unbind-global-keybindings (atom nil))


(reg-fx
  :keybindings/bind!
  (fn []
    (when (not (nil? @unbind-global-keybindings))
      (@unbind-global-keybindings))
    (reset! unbind-global-keybindings
            (bind-changeable-global-keybindings))))

