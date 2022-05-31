(ns athens.views.blocks.autocomplete-search
  (:require
    ["/components/Block/Autocomplete" :refer [Autocomplete AutocompleteButton]]
    ["@chakra-ui/react" :refer [Text]]
    [athens.events.inline-search :as inline-search.events]
    [athens.subs.inline-search :as inline-search.subs]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [clojure.string :as string]
    [re-frame.core :as rf]))


(defn inline-item-click
  [state-hooks state uid expansion]
  (let [id     (str "#editable-uid-" uid)
        target (.. js/document (querySelector id))
        type   (rf/subscribe [::inline-search.subs/type])
        f      (case @type
                 :hashtag  textarea-keydown/auto-complete-hashtag
                 :template textarea-keydown/auto-complete-template
                 textarea-keydown/auto-complete-inline)]
    (f uid state-hooks state target expansion)))


(defn inline-search-el
  [_block {:as state-hooks} last-event state]
  (let [inline-search-type (rf/subscribe [::inline-search.subs/type])
        inline-search-index (rf/subscribe [::inline-search.subs/index])]
    (fn [block {:as _state-hooks} _last-event _state]
      (let [{:search/keys [results query]} @state
            is-open (some #(= % @inline-search-type) [:page :block :hashtag :template])]
        [:> Autocomplete {:event @last-event
                          :isOpen is-open
                          :onClose #(rf/dispatch [::inline-search.events/close!])}
         (when is-open
           (if (or (string/blank? query)
                   (empty? results))
             [:> Text {:py "0.4rem"
                       :px "0.8rem"
                       :fontStyle "italics"}
              (str "Search for a " (symbol @inline-search-type))]
             (doall
              (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
                [:> AutocompleteButton {:key (str "inline-search-item" uid)
                                        :isActive (= i @inline-search-index)
                                        :onClick (fn [_] (inline-item-click state-hooks state (:block/uid block) (or title uid)))
                                        :id (str "inline-search-item" uid)}
                 (or title string)]))))]))))
