(ns athens.electron.images
  (:require
    [athens.common.utils :as common.utils]
    [athens.electron.utils :as electron.utils]
    [re-frame.core :as rf]))


;; Image Paste
(defn save-image
  ([item extension]
   (save-image "" "" item extension))
  ([head tail item extension]
   (let [{:keys [images-dir name]}          @(rf/subscribe [:db-picker/selected-db])
         _                (prn head tail images-dir name item extension)
         file             (.getAsFile item)
         img-filename     (.resolve (electron.utils/path) images-dir (str "img-" name "-" (common.utils/gen-block-uid) "." extension))
         reader           (js/FileReader.)
         new-str          (str head "![](" "file://" img-filename ")" tail)
         cb               (fn [e]
                            (let [img-data (as->
                                             (.. e -target -result) x
                                             (clojure.string/replace-first x #"data:image/(jpeg|gif|png);base64," "")
                                             (js/Buffer. x "base64"))]
                              (when-not (.existsSync (electron.utils/fs) images-dir)
                                (.mkdirSync (electron.utils/fs) images-dir))
                              (.writeFileSync (electron.utils/fs) img-filename img-data)))]
     (set! (.. reader -onload) cb)
     (.readAsDataURL reader file)
     new-str)))


(defn dnd-image
  [target-uid drag-target item extension]
  (let [new-str               (save-image item extension)]
    ;; delay because you want to create block *after* the file has been saved to filesystem
    ;; otherwise, <img> is created too fast, and no image is rendered
    ;; TODO: this functionality needs to create an event instead and upload the file to work with RTC.
    (js/setTimeout #(rf/dispatch [:graph/add-internal-representation
                                  [{:block/string new-str}]
                                  {:block/uid target-uid :relation drag-target}]) 50)))
