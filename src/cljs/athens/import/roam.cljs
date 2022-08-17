(ns athens.import.roam
  (:require
    ["@chakra-ui/react" :refer [VStack Button Box Text Modal ModalOverlay Divider VStack Heading ModalContent ModalHeader ModalFooter ModalBody ModalCloseButton ButtonGroup]]
    ;; [athens.events :as events]
    [athens.common.utils :as common.utils]
    [athens.dates :as dates]
    [athens.db :as db]
    [athens.interceptors :as interceptors]
    [athens.patterns :as patterns]
    [clojure.edn :as edn]
    [datascript.core :as d]
    [re-frame.core :refer [dispatch reg-event-fx]]
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


(defn roam-pages
  [roam-db]
  (d/q '[:find [?pages ...]
         :in $
         :where
         [_ :node/title ?pages]]
       roam-db))


(def roam-node-document-pull-vector
  '[:node/title :block/uid :block/string :block/open :block/order {:block/children ...}])


(defn get-roam-node-document
  [id db]
  (->> (d/pull db roam-node-document-pull-vector id)
       db/sort-block-children))


(defn merge-shared-page
  "If page exists in both databases, but roam-db's page has no children, then do not add the merge block"
  [shared-page roam-db roam-db-filename]
  (let [page-athens              (db/get-node-document shared-page db/dsdb)
        page-roam                (get-roam-node-document shared-page roam-db)
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
            gett
            shared-pages)
       sort))


(defn merge-modal
  [open?]
  (let [close-modal         #(reset! open? false)
        transformed-roam-db (r/atom nil)
        roam-db-filename    (r/atom "")]
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
                "Upload database"]]]])
          (let [roam-pages   (roam-pages @transformed-roam-db)
                shared-pages (get-shared-pages @transformed-roam-db)]
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
                                      (dispatch [:import.roam/edn @transformed-roam-db @roam-db-filename])
                                      (close-modal))}

                "Merge"]]]]))]])))


(reg-event-fx
  :import.roam/edn
  [(interceptors/sentry-span-no-new-tx "upload/roam-edn")]
  (fn [_ [_ transformed-dates-roam-db roam-db-filename]]
    (let [shared-pages   (get-shared-pages transformed-dates-roam-db)
          merge-shared   (mapv (fn [x] (merge-shared-page [:node/title x] transformed-dates-roam-db roam-db-filename))
                               shared-pages)
          merge-unshared (->> (not-shared-pages transformed-dates-roam-db shared-pages)
                              (map (fn [x] (get-roam-node-document [:node/title x] transformed-dates-roam-db))))
          tx-data        (concat merge-shared merge-unshared)]
      ;; TODO: this functionality needs to create a internal representation event instead.
      ;; That will cause it to work in RTC and remove the need to transact directly to the in-memory db.
      {:dispatch [:transact tx-data]})))
