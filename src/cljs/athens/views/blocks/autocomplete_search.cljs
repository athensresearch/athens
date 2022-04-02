(ns athens.views.blocks.autocomplete-search
  (:require
   ["@chakra-ui/react" :refer [Portal Menu MenuItem MenuList Text Box]]
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
  [_block]
  (fn [block state]
    (let [{:search/keys [index results type query] caret-position :caret-position} @state
          {:keys [left top]} caret-position]
      (println type)
      (println (when type (symbol type)))
      (when (some #(= % type) [:page :block :hashtag :template])
        [:> Menu {:isOpen true
                  :autoSelect false}
         [:> Portal
          [:> MenuList {;; don't blur textarea when clicking to auto-complete
                        :on-mouse-down (fn [e] (.. e preventDefault))
                        :as Box
                        :position "absolute"
                        :overflow "auto"
                        :p 1
                        :align "stretch"
                        :justify "stretch"
                        :width "max-content"
                        :bg "background.upper"
                        :maxHeight "20rem"
                        :top (str (+ 24 top) "px")
                        :left (str (+ 24 left) "px")}
           (if (or (string/blank? query)
                   (empty? results))
             [:> Text (str "Search for a " (symbol type))]
             (doall
              (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
                [:> MenuItem {:key      (str "inline-search-item" uid)
                              :id       (str "dropdown-item-" i)
                              :width "100%"
                              :isActive   (= index i)
                                                         ;; if page link, expand to title. otherwise expand to uid for a block ref
                              :onClick (fn [_] (inline-item-click state (:block/uid block) (or title uid)))}
                 (or title string)])))]]]))))

