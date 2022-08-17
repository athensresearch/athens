(ns athens.views.blocks.context-menu
  (:require
    [athens.db :as db]
    [athens.listeners :as listeners]
    [athens.subs.selection :as select-subs]
    [athens.util :refer [toast]]
    [clojure.string :as string]
    [re-frame.core :as rf]))


(defn handle-copy-refs
  [_ uid]
  (let [selected-items @(rf/subscribe [::select-subs/items])
        ;; use this when using datascript-transit
        ;; uids (map (fn [x] [:block/uid x]) selected-items)
        ;; blocks (d/pull-many @db/dsdb '[*] ids)
        data           (if (empty? selected-items)
                         (str "((" uid "))")
                         (->> (map (fn [uid] (str "((" uid "))\n")) selected-items)
                              (string/join "")))]
    (.. js/navigator -clipboard (writeText data))
    (toast (clj->js {:title (if (> (count selected-items) 1)
                              "Copied refs to clipboard"
                              "Copied ref to clipboard")}))))


(defn handle-copy-unformatted
  "If copying only a single block, dissoc children to not copy subtree."
  [^js uid]
  (let [uids @(rf/subscribe [::select-subs/items])]
    (if (empty? uids)
      (let [block (dissoc (db/get-block [:block/uid uid]) :block/children)
            data  (listeners/blocks-to-clipboard-data 0 block true)]
        (.. js/navigator -clipboard (writeText data)))
      (let [data (->> (map #(db/get-block [:block/uid %]) uids)
                      (map #(listeners/blocks-to-clipboard-data 0 % true))
                      (apply str))]
        (.. js/navigator -clipboard (writeText data)))))
  (toast (clj->js {:title "Copied content to clipboard" :status "success"})))


(defn handle-click-comment
  [e uid]
  (rf/dispatch [:comment/show-editor uid])
  (.. e preventDefault))


