(ns athens.effects
  (:require
    [athens.db :as db]
    [athens.util :as util]
    [athens.walk :as walk]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [cljs.pprint :refer [pprint]]
    [clojure.string :as str]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [goog.dom.selection :refer [setCursorPosition]]
    [posh.reagent :as p :refer [transact!]]
    [re-frame.core :refer [dispatch reg-fx]]
    [stylefy.core :as stylefy]))


;;; Effects

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
  "Filter: new-str doesn't include link, page exists, page has no children, and has no other [[linked refs]].
  Map: retractEntity"
  [old-titles uid new-str]
  (->> old-titles
       (filter (fn [title]
                 (let [node (db/get-block [:node/title title])]
                   (and (not (clojure.string/includes? new-str title))
                        node
                        (empty? (:block/children node))
                        (zero? (db/count-linked-references-excl-uid title uid))))))
       (map (fn [title]
              (when-let [eid (:db/id (db/get-block [:node/title title]))]
                [:db/retractEntity eid])))))


(defn new-refs-to-tx-data
  "Filter: ((ref-uid)) points to an actual block (without a title), and block/ref relationship doesn't exist yet.
  Map: add block/ref relationship."
  [new-block-refs e]
  (->> new-block-refs
       (filter (fn [ref-uid]
                 (let [block (d/q '[:find (pull ?e [*])
                                    :in $ ?uid
                                    :where [?e :block/uid ?uid]]
                                  @db/dsdb
                                  ref-uid)
                       {:keys [node/title db/id]} block
                       refs  (-> e db/get-block-refs set)]
                   (and block
                        (nil? title)
                        (not (contains? refs id))))))
       (map (fn [ref-uid] [:db/add e :block/refs [:block/uid ref-uid]]))))


(defn new-page-refs-to-tx-data
  "Filter: [[node]] points to a real page (has :node/title) and block/ref relationship doesn't exist yet.
  Map: add block/ref relationship."
  [new-page-refs source-eid]
  (->> new-page-refs
       (map #(apply db/e-by-av %))
       (filter (fn [target-eid]
                 (let [block (d/pull @db/dsdb '[*] target-eid)
                       {:keys [db/id]} block
                       refs  (-> source-eid db/get-block-refs set)]
                   (and block
                        (not (contains? refs id))))))
       (map (fn [page-id] [:db/add source-eid :block/refs page-id]))))


(defn old-block-refs-to-tx-data
  "Filter: new-str doesn't include block ref anymore, ((ref-uid)) points to an actual block, and block/ref relationship exists.
  Map: retract relationship."
  [old-block-refs e new-str]
  (->> old-block-refs
       (filter (fn [ref-uid]
                 (let [eid  (db/e-by-av :block/uid ref-uid)
                       refs (-> e db/get-block-refs set)]
                   (and (not (str/includes? new-str (str "((" ref-uid "))")))
                        (contains? refs eid)))))
       (map (fn [ref-uid] [:db/retract e :block/refs [:block/uid ref-uid]]))))


(defn old-page-refs-to-tx-data
  "Filter: [[page]] points to a page and block/ref relationship does exist.
  Map: retract block/ref relationship."
  [old-page-refs source-eid new-str]
  (->> old-page-refs
       (filter (fn [page-id]
                 (let [page (d/pull @db/dsdb '[*] page-id)
                       {:keys [node/title db/id]} page
                       refs  (-> source-eid db/get-block-refs set)]
                   (and (not (str/includes? new-str (str "[[" title "]]")))
                        page
                        title
                        (contains? refs id)))))
       (map (fn [page-id] [:db/retract source-eid :block/refs page-id]))))


(defn parse-for-links
  "When block/string is asserted, parse for links and block refs to add.
  When block/string is retracted, parse for links and block refs to remove.
  Retractions need to look at asserted block/string."
  [with-tx-data]
  (let [assert-titles (->> with-tx-data
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
                           uid            (db/v-by-ea eid :block/uid)
                           assert-string  (nth assertion 2)
                           retract-string (nth retraction 2)
                           assert-data    (walk/walk-string assert-string)
                           retract-data   (walk/walk-string retract-string)
                           new-titles     (new-titles-to-tx-data (:node/titles assert-data) assert-titles)
                           new-page-refs  (new-page-refs-to-tx-data (:page/refs assert-data) eid)
                           new-block-refs (new-refs-to-tx-data (:block/refs assert-data) eid)
                           old-titles     (old-titles-to-tx-data (:node/titles retract-data) uid assert-string)
                           old-block-refs (old-block-refs-to-tx-data (:block/refs retract-data) eid assert-string)
                           old-page-refs  (old-page-refs-to-tx-data (:page/refs retract-data) eid assert-string)
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
                     (and (false? (last assertion)) (nil? retraction))
                     (let [eid            (first retraction)
                           uid            (db/v-by-ea eid :block/uid)
                           assert-string  ""
                           retract-string (nth retraction 2)
                           retract-data   (walk/walk-string retract-string)
                           old-titles     (old-titles-to-tx-data (:node/titles retract-data) uid assert-string)

                           ;; XXX: don't actually need old-block-refs and old-page-refs. They are auto-deleted with `d/with`
                           old-block-refs (old-block-refs-to-tx-data (:block/refs retract-data) eid assert-string)
                           old-page-refs  (old-page-refs-to-tx-data (:page/refs retract-data) eid assert-string)
                           tx-data        (concat []
                                                  old-titles
                                                  old-block-refs
                                                  old-page-refs)]
                       tx-data)))))))


(defn walk-transact
  [tx-data]
  (prn "TX RAW INPUTS")                                     ;; event tx-data
  (pprint tx-data)
  (try
    (let [with-tx-data  (:tx-data (d/with @db/dsdb tx-data))
          more-tx-data  (parse-for-links with-tx-data)
          final-tx-data (vec (concat tx-data more-tx-data))]
      (prn "TX WITH")                                       ;; tx-data normalized by datascript to flat datoms
      (pprint with-tx-data)
      (prn "TX MORE")                                       ;; parsed tx-data, e.g. asserting/retracting pages and references
      (pprint more-tx-data)
      (prn "TX FINAL INPUTS")                               ;; parsing block/string (and node/title) to derive asserted or retracted titles and block refs
      (pprint final-tx-data)
      (let [outputs (:tx-data (transact! db/dsdb final-tx-data))]
        (prn "TX OUTPUTS")
        (pprint outputs)))

    (catch js/Error e
      (js/alert (str e))
      (prn "EXCEPTION" e))))


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
                       (let [html-id (str "#editable-uid-" uid)
                             ;;targets (js/document.querySelectorAll html-id)
                             ;;n       (count (array-seq targets))
                             el      (js/document.querySelector html-id)]
                         #_(cond
                             (zero? n) (prn "No targets")
                             (= 1 n) (prn "One target")
                             (< 1 n) (prn "Several targets"))
                         (when el
                           (.focus el)
                           (when index
                             (setCursorPosition el index)))))
                     100))))


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
