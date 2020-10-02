(ns athens.effects
  (:require
    [athens.db :as db]
    [athens.parse-renderer :refer [pull-node-from-string]]
    [athens.parser :as parser]
    [athens.util :refer [now-ts gen-block-uid]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [cljs.pprint :refer [pprint]]
    [clojure.string :as str]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [goog.dom :refer [getElement]]
    [goog.dom.selection :refer [setCursorPosition]]
    [instaparse.core :as parse]
    [posh.reagent :refer [transact!]]
    [re-frame.core :refer [dispatch reg-fx]]))


;;; Effects

;; Algorithm:
;; - look at string (old or new)
;; - parse for database values: links, block refs, attributes (not yet supported), etc.
;; - filter based on remove or add conditions
;; - map to datoms
(defn walk-string
  "Walk previous and new strings to delete or add links, block references, etc. to datascript."
  [string]
  (let [data (atom {})]
    (parse/transform
      {:page-link (fn [& title]
                    (let [inner-title (str/join "" title)]
                      (swap! data update :node/titles #(conj % inner-title))
                      (str "[[" inner-title "]]")))
       :hashtag   (fn [& title]
                    (let [inner-title (str/join "" title)]
                      (swap! data update :node/titles #(conj % inner-title))
                      (str "#" inner-title)))
       :block-ref (fn [uid] (swap! data update :block/refs #(conj % uid)))}
      (parser/parse-to-ast string))
    @data))


(defn new-titles-to-tx-data
  [new-titles]
  (let [now (now-ts)]
    (->> new-titles
         (filter (fn [x] (nil? (db/search-exact-node-title x))))
         (map (fn [t]
                {:node/title  t
                 :block/uid   (gen-block-uid)
                 :create/time now
                 :edit/time   now})))))


(defn old-titles-to-tx-data
  [old-titles uid new-str]
  (->> old-titles
       (filter (fn [t]
                 (let [block (db/search-exact-node-title t)]
                   ;; makes sure the page link is deleted in this node as well
                   (and (not (clojure.string/includes? new-str t))
                        ;; makes sure the page link is deleted in this node as well
                        (not (nil? block))
                        ;; makes sure the page link has no children
                        (nil? (:block/children (db/get-block-document (:db/id block))))
                        ;; makes sure the page link is not present in other pages
                        (zero? (db/count-linked-references-excl-uid t uid))))))
       (mapcat (fn [t]
                 (let [uid (:block/uid @(pull-node-from-string t))]
                   (when (some? uid)
                     (db/retract-uid-recursively uid)))))))


(defn new-refs-to-tx-data
  [new-block-refs uid]
  (->> new-block-refs
       (filter (fn [ref-uid]
                 ;; check that ((ref-uid)) points to an actual entity
                 ;; find refs of uid
                 ;; if ((ref-uid)) is not yet a reference, then map datoms
                 (let [eid (db/e-by-av :block/uid ref-uid)
                       refs (-> (db/get-block-refs uid) set)]
                   (nil? (refs eid)))))
       (map (fn [ref-uid] [:db/add [:block/uid uid] :block/refs [:block/uid ref-uid]]))))


(defn old-refs-to-tx-data
  [old-block-refs uid new-str]
  (->> old-block-refs
       (filter (fn [ref-uid]
                 ;; check that ((ref-uid)) points to an actual entity
                 ;; find refs of uid
                 ;; if ((ref-uid)) is no longer in the current string and IS a valid reference, retract
                 (when (not (str/includes? new-str (str "((" ref-uid "))")))
                   (let [eid  (db/e-by-av :block/uid ref-uid)
                         refs (-> (db/get-block-refs uid) set)]
                     (refs eid)))))
       (map (fn [ref-uid] [:db/retract [:block/uid uid] :block/refs [:block/uid ref-uid]]))))


;; or node/title
;; when block/string is asserted, parse for links and block refs to add
;; when block/string is retracted, parse for links and block refs to remove
;; retractions need to look at asserted block/string too. if includes?, obvious filter

(defn parse-for-links
  "Compare previous string with current string.
    - If links were added, transact pages.
    - If links were removed and page is an orphan, retract page.
    - If block refs were added, transact block/ref.
    - If block refs were removed, retract block/ref."
  [with-tx-data]
  (->> with-tx-data
       (filter #(= (second %) :block/string))
       ;; group-by entity
       (group-by first)
       ;; map sort-by so [true false] gives us [assertion retraction]
       (mapv (fn [[_eid datoms]]
               (sort-by #(-> % last not) datoms)))
       (mapcat (fn [[assertion retraction]]
                 (let [eid            (first assertion)
                       retract-string (nth retraction 2)
                       assert-string  (nth assertion 2)
                       uid            (db/v-by-ea eid :block/uid)
                       retract-data   (walk-string retract-string)
                       assert-data    (walk-string assert-string)
                       new-titles     (new-titles-to-tx-data (:node/titles assert-data))
                       old-titles     (old-titles-to-tx-data (:node/titles retract-data) uid assert-string)
                       new-block-refs (new-refs-to-tx-data (:block/refs assert-data) uid)
                       old-block-refs (old-refs-to-tx-data (:block/refs retract-data) uid assert-string)
                       tx-data        (concat []
                                              new-titles
                                              old-titles
                                              new-block-refs
                                              old-block-refs)]
                   tx-data)))))

;;(def a (atom nil))

(reg-fx
  :transact!
  (fn [tx-data]
    (prn "TX INPUTS")
    (pprint tx-data)
    (let [with-tx-data  (:tx-data (d/with @db/dsdb tx-data))
          more-tx-data  (parse-for-links with-tx-data)
          final-tx-data (vec (concat tx-data more-tx-data))]
      ;;(reset! a with-tx-data)
      (prn "TX OUTPUTS")
      (let [outputs (:tx-data (transact! db/dsdb final-tx-data))]
        (pprint outputs)))))


(reg-fx
  :reset-conn!
  (fn [new-db]
    (d/reset-conn! db/dsdb new-db)))


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
;; If an index is passed, set cursor that index.
(reg-fx
  :editing/focus
  (fn [[uid index]]
    (js/setTimeout (fn []
                     (let [id (str "editable-uid-" uid)
                           el (getElement id)]
                       (when el
                         (.focus el)
                         (when index
                           (setCursorPosition el index)))))
                   300)))

