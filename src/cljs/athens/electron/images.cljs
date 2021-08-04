(ns athens.electron.images
  (:require
    [athens.db :as db]
    [re-frame.core :as rf]))


(def electron (js/require "electron"))
(def path (js/require "path"))
(def fs (js/require "fs"))
(def stream (js/require "stream"))

(def remote (.. electron -remote))
(def dialog (.. remote -dialog))
(def IMAGES-DIR-NAME "images")


;; Image Paste
(defn save-image
  ([item extension]
   (save-image "" "" item extension))
  ([head tail item extension]
   (let [{:keys [images-dir name]}          @(rf/subscribe [:db-picker/selected-db])
         _                (prn head tail images-dir name item extension)
         file             (.getAsFile item)
         img-filename     (.resolve path images-dir (str "img-" name "-" (athens.util/gen-block-uid) "." extension))
         reader           (js/FileReader.)
         new-str          (str head "![](" "file://" img-filename ")" tail)
         cb               (fn [e]
                            (let [img-data (as->
                                             (.. e -target -result) x
                                             (clojure.string/replace-first x #"data:image/(jpeg|gif|png);base64," "")
                                             (js/Buffer. x "base64"))]
                              (when-not (.existsSync fs images-dir)
                                (.mkdirSync fs images-dir))
                              (.writeFileSync fs img-filename img-data)))]
     (set! (.. reader -onload) cb)
     (.readAsDataURL reader file)
     new-str)))


(defn dnd-image
  [target-uid drag-target item extension]
  (let [new-str   (save-image item extension)
        {:block/keys [order]} (db/get-block [:block/uid target-uid])
        parent    (db/get-parent [:block/uid target-uid])
        block     (db/get-block [:block/uid target-uid])
        new-block {:block/uid (athens.util/gen-block-uid) :block/order 0 :block/string new-str :block/open true}
        tx-data   (if (= drag-target :child)
                    (let [reindex          (db/inc-after (:db/id block) -1)
                          new-children     (conj reindex new-block)
                          new-target-block {:db/id [:block/uid target-uid] :block/children new-children}]
                      new-target-block)
                    (let [index        (case drag-target
                                         :above (dec order)
                                         :below order)
                          reindex      (db/inc-after (:db/id parent) index)
                          new-children (conj reindex new-block)
                          new-parent   {:db/id (:db/id parent) :block/children new-children}]
                      new-parent))]
    ;; delay because you want to create block *after* the file has been saved to filesystem
    ;; otherwise, <img> is created too fast, and no image is rendered
    (js/setTimeout #(rf/dispatch [:transact [tx-data]]) 50)))
