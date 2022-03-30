(ns athens.views.blocks.autocomplete-search
  (:require
   ["@chakra-ui/react" :refer [Portal Text Menu MenuList MenuItem]]
   [athens.views.blocks.textarea-keydown :as textarea-keydown]
   [clojure.string :as string]))


(defn inline-item-click
  [state uid expansion]
  (let [id     (str "#editable-uid-" uid)
        target (.. js/document (querySelector id))
        f      (case (:search/type @state)
                 :hashtag  textarea-keydown/auto-complete-hashtag
                 :template textarea-keydown/auto-complete-template
                 textarea-keydown/auto-complete-inline)]
    (f state target expansion)))


(defn inline-search-el
  [_block #_ state]
  (fn [block state]
    (let [open? (some #(= % type) [:page :block :hashtag :template])
          {:search/keys [query results index type] caret-position :caret-position} @state
          {:keys [left top]} caret-position]
      (when open?
        [:> Menu {:isOpen open?
                  :autoSelect false
                  :onClose #(swap! state assoc :search/type false)
                  :isLazy true}
         [:> Portal
          [:> MenuList {:position   "absolute"
                        :left (str left "px")
                        :top (str (+ top 24) "px")}
           (if (or (string/blank? query)
                   (empty? results))
             [:> Text (str "Search for a " (symbol type))]
             (doall
              (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
                [:> MenuItem {:key      (str "inline-search-item-" uid)
                              :id       (str "dropdown-item-" i)
                              :class (when (= i index) "isActive")
                                                         ;; if page link, expand to title. otherwise expand to uid for a block ref
                              :onClick (fn [_] (inline-item-click state (:block/uid block) (or title uid)))}
                 (or title string)])))]]]))))
                 
