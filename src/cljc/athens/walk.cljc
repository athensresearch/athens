(ns athens.walk
  (:require
    [athens.db :as db]
    [athens.parser :as parser]
    [athens.util :as util]
    [clojure.string :as str]
    [datascript.core :as d]
    [instaparse.core :as parse]))


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
                 (let [block (d/pull @db/dsdb '[*] [:block/uid ref-uid])
                       {:keys [node/title db/id]} block
                       refs  (-> e db/get-block-refs set)]
                   (and block
                        (nil? title)
                        (not (contains? refs id))))))
       (map (fn [ref-uid] [:db/add e :block/refs [:block/uid ref-uid]]))))


(defn old-refs-to-tx-data
  "Filter: new-str doesn't include block ref anymore, ((ref-uid)) points to an actual block, and block/ref relationship exists.
  Map: retract relationship."
  [old-block-refs e new-str]
  (->> old-block-refs
       (filter (fn [ref-uid]
                 (when-not (str/includes? new-str (str "((" ref-uid "))"))
                   (let [eid  (db/e-by-av :block/uid ref-uid)
                         refs (-> e db/get-block-refs set)]
                     (contains? refs eid)))))
       (map (fn [ref-uid] [:db/retract e :block/refs [:block/uid ref-uid]]))))


(defn parse-for-links
  "When block/string is asserted, parse for links and block refs to add.
  When block/string is retracted, parse for links and block refs to remove.
  Retractions need to look at asserted block/string.

  TODO: when user edits title, parse for new pages."
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
                           assert-data    (walk-string assert-string)
                           retract-data   (walk-string retract-string)
                           new-titles     (new-titles-to-tx-data (:node/titles assert-data) assert-titles)
                           new-block-refs (new-refs-to-tx-data (:block/refs assert-data) eid)
                           old-titles     (old-titles-to-tx-data (:node/titles retract-data) uid assert-string)
                           old-block-refs (old-refs-to-tx-data (:block/refs retract-data) eid assert-string)
                           tx-data        (concat []
                                                  new-titles
                                                  new-block-refs
                                                  old-titles
                                                  old-block-refs)]
                       tx-data)

                     ;; [assertion]
                     (and (true? (last assertion)) (nil? retraction))
                     (let [eid            (first assertion)
                           assert-string  (nth assertion 2)
                           assert-data    (walk-string assert-string)
                           new-titles     (new-titles-to-tx-data (:node/titles assert-data) assert-titles)
                           new-block-refs (new-refs-to-tx-data (:block/refs assert-data) eid)
                           tx-data        (concat []
                                                  new-titles
                                                  new-block-refs)]
                       tx-data)

                     ;; [retraction]
                     (and (false? (last assertion)) (nil? retraction))
                     (let [eid            (first retraction)
                           uid            (db/v-by-ea eid :block/uid)
                           assert-string  ""
                           retract-string (nth retraction 2)
                           retract-data   (walk-string retract-string)
                           old-titles     (old-titles-to-tx-data (:node/titles retract-data) uid assert-string)
                           old-block-refs (old-refs-to-tx-data (:block/refs retract-data) eid assert-string)
                           tx-data        (concat []
                                                  old-titles
                                                  old-block-refs)]
                       tx-data)))))))

