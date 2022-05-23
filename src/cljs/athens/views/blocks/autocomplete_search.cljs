(ns athens.views.blocks.autocomplete-search
  (:require
    ["/components/Block/Autocomplete" :refer [Autocomplete AutocompleteButton]]
    ["@chakra-ui/react" :refer [Text]]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [clojure.string :as string]))


(defn inline-item-click
  [state-hooks state uid expansion]
  (let [id     (str "#editable-uid-" uid)
        target (.. js/document (querySelector id))
        f      (case (:search/type @state)
                 :hashtag  textarea-keydown/auto-complete-hashtag
                 :template textarea-keydown/auto-complete-template
                 textarea-keydown/auto-complete-inline)]
    (f uid state-hooks state target expansion)))


(defn inline-search-el
  [_block {:as state-hooks} state]
  (fn [block {:as _state-hooks} _state]
    (let [{:keys [last-e]} @state
          {:search/keys [index results type query]} @state
          is-open (some #(= % type) [:page :block :hashtag :template])]
      [:> Autocomplete {:event last-e
                        :isOpen is-open
                        :onClose #(swap! state assoc :search/type false)}
       (when is-open
         (if (or (string/blank? query)
                 (empty? results))
           [:> Text {:py "0.4rem"
                     :px "0.8rem"
                     :fontStyle "italics"}
            (str "Search for a " (symbol type))]
           (doall
             (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
               [:> AutocompleteButton {:key (str "inline-search-item" uid)
                                       :isActive (= i index)
                                       :onClick (fn [_] (inline-item-click state-hooks state (:block/uid block) (or title uid)))
                                       :id (str "inline-search-item" uid)}
                (or title string)]))))])))
