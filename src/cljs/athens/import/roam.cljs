(ns athens.import.roam
  (:require
    ["@chakra-ui/react" :refer [VStack Button Box Text Modal ModalOverlay Divider VStack Heading ModalContent ModalHeader ModalFooter ModalBody ModalCloseButton ButtonGroup]]
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.composite :as composite-ops]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.dates :as dates]
    [athens.db :as db]
    [athens.patterns :as patterns]
    [clojure.data :as data]
    [clojure.edn :as edn]
    [clojure.walk :as walk]
    [datascript.core :as d]
    [re-frame.core :refer [dispatch]]
    [reagent.core :as r]))


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


(defn file-cb
  [e transformed-db roam-db-filename]
  (let [fr   (js/FileReader.)
        file (.. e -target -files (item 0))]
    (set! (.-onload fr)
          (fn [e]
            (let [edn-data                  (.. e -target -result)
                  filename                  (.-name file)
                  db                        (edn/read-string {:readers datascript.core/data-readers} edn-data)
                  transformed-dates-roam-db (update-roam-db-dates db)]
              (reset! roam-db-filename filename)
              (reset! transformed-db transformed-dates-roam-db))))
    (.readAsText fr file)))


(def roam-node-document-pull-vector
  '[:node/title :block/uid :block/string :block/open :block/order {:block/children ...}])


(defn get-roam-node-document
  [db eid]
  (->> (d/pull db roam-node-document-pull-vector eid)
       db/sort-block-children))


(defn get-roam-internal-representation
  "Like common-db/get-internal representation but for roam dbs."
  [db eid]
  (when (d/entity db eid)
    (let [rename-ks          {:block/open :block/open?
                              :node/title :page/title}
          remove-ks          [:db/id :block/order]
          remove-ks-on-match [[:block/open? :block/open?]
                              [:block/uid   :page/title]]]
      (->> (get-roam-node-document db eid)
           (walk/postwalk-replace rename-ks)
           (walk/prewalk (fn [node]
                           (if (map? node)
                             (as-> node n
                                   (apply dissoc n remove-ks)
                                   (reduce common-db/dissoc-on-match n remove-ks-on-match))
                             node)))))))


(defn get-page-titles
  [db]
  (->> (d/datoms db :avet :node/title)
       (map #(nth % 2))
       set))


(defn shared-and-non-shared-pages
  [athens-db roam-db]
  (let [athens-pages (get-page-titles athens-db)
        roam-pages (get-page-titles roam-db)
        [non-shared _ shared] (data/diff roam-pages athens-pages)]
    [shared non-shared]))


(defn page->ops
  [athens-db roam-db roam-db-filename title]
  (let [default-position {:page/title title :relation :last}
        internal-repr    (get-roam-internal-representation roam-db [:node/title title])
        internal-repr    (cond-> internal-repr

                           ;; Page exists and has blocks in both athens-db and roam-db.
                           ;; Wrap IR in a new block that mentions the import.
                           (and (:block/children internal-repr)
                                (graph-ops/get-path athens-db [:node/title title] [::graph-ops/last]))
                           (-> (select-keys [:block/children])
                               (merge {:block/string (str "[[Roam Import]] "
                                                          "[[" (:title (dates/get-day)) "]] "
                                                          "[[" roam-db-filename "]]")})))]
    (bfs/internal-representation->atomic-ops @db/dsdb [internal-repr] default-position)))


;; 90% of max, enough for some wiggle roam with the consequence op.
(def max-payload-size (* 0.9 common-events/max-event-size-in-bytes))


(defn dispatch-payload
  [payload]
  (dispatch [:resolve-transact-forward (->> payload
                                            (composite-ops/make-consequence-op {:op/type :import/roam})
                                            common-events/build-atomic-event)]))


(defn process-import
  "Import roam pages as events under the common-events/max-event-size-in-bytes limit."
  [athens-db roam-db roam-db-filename _progress]
  (loop [[op & ops]   (->> (get-page-titles roam-db)
                           (mapcat (partial page->ops athens-db roam-db roam-db-filename)))
         payload      []
         payload-size 0]
    (let [op-size              (-> op common-events/serialize count)
          payload-size-with-op (+ payload-size op-size)]

      (cond
        ;; There's no more operations to process.
        ;; Send the last payload, if any.
        (nil? op)
        (when (seq payload)
          (dispatch-payload payload))

        ;; This single op is too big, likely a >1mb string.
        ;; We don't really support this, so ignore the op.
        ;; TODO: maybe report that we ignored it?
        (> op-size max-payload-size)
        (recur ops payload payload-size)

        ;; This op can't fit in the current payload.
        ;; Dispatch current payload, and add op to next payload.
        (> payload-size-with-op max-payload-size)
        (do
          (dispatch-payload payload)
          (recur ops [op] op-size))

        ;; This op still fits on the payload, add it and
        ;; go look at the next one.
        :else
        (recur ops (conj payload op) payload-size-with-op)))))


(defn merge-modal
  [open?]
  (let [close-modal         #(reset! open? false)
        transformed-roam-db (r/atom nil)
        roam-db-filename    (r/atom "")
        progress            (r/atom 0) ; TODO
        ]
    (fn []
      [:> Modal {:isOpen @open?
                 :onClose close-modal
                 :closeOnOverlayClick false
                 :size "lg"}
       [:> ModalOverlay]
       [:> ModalContent
        [:> ModalHeader
         "Merge from Roam"]
        [:> ModalCloseButton]
        (if (nil? @transformed-roam-db)
          (let [inputRef (atom nil)]
            [:> ModalBody
             [:input {:ref #(reset! inputRef %)
                      :style {:display "none"}
                      :type "file"
                      :accept ".edn"
                      :on-change #(file-cb % transformed-roam-db roam-db-filename)}]
             [:> Heading {:size "md" :as "h2"} "How to merge from Roam"]
             [:> Box {:position "relative"
                      :padding-bottom "56.25%"
                      :margin         "1rem 0 0"
                      :borderRadius  "8px"
                      :overflow "hidden"
                      :flex "1 1 100%"
                      :width          "100%"}
              [:iframe {:src                   "https://www.loom.com/embed/787ed48da52c4149b031efb8e17c0939?hide_owner=true&hide_share=true&hide_title=true&hideEmbedTopBar=true"
                        :frameBorder           "0"
                        :webkitallowfullscreen "true"
                        :mozallowfullscreen    "true"
                        :allowFullScreen       true
                        :style                 {:position "absolute"
                                                :top      0
                                                :left     0
                                                :width    "100%"
                                                :height   "100%"}}]]
             [:> ModalFooter
              [:> ButtonGroup
               [:> Button
                {:onClick #(.click @inputRef)}
                "Upload workspace"]]]])
          (let [athens-db @athens.db/dsdb
                roam-db @transformed-roam-db
                [shared-pages roam-pages] (shared-and-non-shared-pages athens-db roam-db)]
            [:> ModalBody
             [:> Text {:size "md"} (str "Your Roam DB had " (count roam-pages)) " pages. " (count shared-pages) " of these pages were also found in your Athens DB. Press Merge to continue merging your DB."]
             [:> Divider {:my 4}]
             [:> Heading {:size "md" :as "h3"} "Shared Pages"]
             [:> VStack {:as "ol"
                         :align "stretch"
                         :maxHeight "400px"
                         :overflowY "auto"}
              (for [x shared-pages]
                ^{:key x}
                [:li [:> Text (str "[[" x "]]")]])]
             [:> ModalFooter
              [:> ButtonGroup
               [:> Button {:onClick (fn []
                                      (process-import athens-db roam-db @roam-db-filename progress)
                                      (close-modal))}

                "Merge"]]]]))]])))

