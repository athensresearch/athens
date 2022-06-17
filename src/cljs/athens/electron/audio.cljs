(ns athens.electron.audio
  (:require
    [athens.common.utils :as common.utils]
    [athens.db :as db]
    [athens.electron.utils :as electron.utils]
    [re-frame.core :as rf]))

(defn save-audio
  ([item extension]
   (save-audio "" "" item extension))
  ([head tail item extension]
   ;; TODO: change images-dir to audios-dir, for some reason returns nil even if I added the path and key in the utils.clj local-db.
   (let [{:keys [name] audios-dir :images-dir} @(rf/subscribe [:db-picker/selected-db])
         _                (prn head tail audios-dir name item extension)
         file             (.getAsFile item)
         audio-filename   (.resolve (electron.utils/path)
                                    audios-dir
                                    (str "audio-" name "-" (common.utils/gen-block-uid) "." extension))
         reader           (js/FileReader.)
         new-str          (str head "^[](" "file://" audio-filename ")" tail)
         cb               (fn [e]
                            (let [audio-data (as->
                                               (.. e -target -result) x
                                               (clojure.string/replace-first x #"data:audio/(wav|mp3);base64," "")
                                               (js/Buffer. x "base64"))]
                              (when-not (.existsSync (electron.utils/fs) audios-dir)
                                (.mkdirSync (electron.utils/fs) audios-dir))
                              (.writeFileSync (electron.utils/fs) audio-filename audio-data)))]
     (set! (.. reader -onload) cb)
     (.readAsDataURL reader file)
     new-str)))

(defn dnd-audio
  [target-uid drag-target item extension]
  (let [new-str               (save-audio item extension)
        {:block/keys [order]} (db/get-block [:block/uid target-uid])
        parent                (db/get-parent [:block/uid target-uid])
        block                 (db/get-block [:block/uid target-uid])
        new-block             {:block/uid (common.utils/gen-block-uid) :block/order 0 :block/string new-str :block/open true}
        tx-data               (if (= drag-target :first)
                                (let [reindex          (db/inc-after (:db/id block) -1)
                                      new-children     (conj reindex new-block)
                                      new-target-block {:db/id [:block/uid target-uid] :block/children new-children}]
                                  new-target-block)
                                (let [index        (case drag-target
                                                     :before (dec order)
                                                     :after  order)
                                      reindex      (db/inc-after (:db/id parent) index)
                                      new-children (conj reindex new-block)
                                      new-parent   {:db/id (:db/id parent) :block/children new-children}]
                                  new-parent))]
    ;; delay because you want to create block *after* the file has been saved to filesystem
    ;; otherwise, <img> is created too fast, and no image is rendered
    ;; TODO: this functionality needs to create an event instead and upload the file to work with RTC.
    (js/setTimeout #(rf/dispatch [:transact [tx-data]]) 50)))
