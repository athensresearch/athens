(ns athens.common-events.graph.ops
  "Building (including contextual resolution) Graph Ops like a boss."
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.graph.atomic    :as atomic]
    [athens.common-events.graph.composite :as composite]
    [athens.common.utils                  :as utils]
    [clojure.set                          :as set]))


(defn build-page-new-op
  "Creates `:page/new` & `:block/new` ops.
  If page already exists, just creates atomic `:block/new`.
  If page doesn't exist, generates composite of atomic `:page/new` & `:block/new`."
  [db page-title page-uid block-uid]
  (if (common-db/e-by-av db :block/uid page-uid)
    (atomic/make-block-new-op page-uid block-uid 0)
    (composite/make-consequence-op {:op/type :page/new}
                                   [(atomic/make-page-new-op page-title page-uid)
                                    (atomic/make-block-new-op page-uid block-uid 0)])))


(defn build-block-save-op
  "Creates `:block/save` op, taking into account context.
  So it might be a composite or atomic event, depending if new page link is present and if pages exist."
  [db block-uid old-string new-string]
  (let [links-in-old    (utils/find-page-links old-string)
        links-in-new    (utils/find-page-links new-string)
        link-diff       (set/difference links-in-new links-in-old)
        new-page-titles (remove #(seq (common-db/get-page-uid-by-title db %))
                                link-diff)
        atomic-pages    (when-not (empty? new-page-titles)
                          (into []
                                (for [title new-page-titles]
                                  (build-page-new-op db
                                                     title
                                                     (utils/gen-block-uid)
                                                     (utils/gen-block-uid)))))
        atomic-save     (atomic/make-block-save-op block-uid old-string new-string)
        block-save-op   (if (empty? atomic-pages)
                          atomic-save
                          (composite/make-consequence-op {:op/type :block/save}
                                                         (conj atomic-pages
                                                               atomic-save)))]
    block-save-op))


(defn build-block-split-op
  "Creates `:block/split` composite op, taking into account context."
  [db {:keys [parent-uid old-block-uid new-block-uid new-block-order
              old-string new-string index]}]
  (let [save-block-op     (build-block-save-op db
                                               old-block-uid
                                               old-string
                                               (subs new-string 0 index))
        new-block-op      (atomic/make-block-new-op parent-uid new-block-uid new-block-order)
        new-block-save-op (build-block-save-op db
                                               new-block-uid
                                               ""
                                               (subs new-string index))
        split-block-op    (composite/make-consequence-op {:op/type :block/split}
                                                         [save-block-op
                                                          new-block-op
                                                          new-block-save-op])]
    split-block-op))
