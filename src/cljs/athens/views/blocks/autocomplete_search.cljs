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
  [state-hooks uid expansion]
  (let [id     (str "#editable-uid-" uid)
        target (.. js/document (querySelector id))
        type   (rf/subscribe [::inline-search.subs/type])
        f      (case @type
                 :hashtag  textarea-keydown/auto-complete-hashtag
                 :template textarea-keydown/auto-complete-template
                 textarea-keydown/auto-complete-inline)]
    (f uid state-hooks target expansion)))


(defn inline-search-el
  [_block {:as state-hooks} last-event]
  (let [inline-search-type (rf/subscribe [::inline-search.subs/type])
        inline-search-index (rf/subscribe [::inline-search.subs/index])
        inline-search-results (rf/subscribe [::inline-search.subs/results])
        inline-search-query (rf/subscribe [::inline-search.subs/query])]
    (fn [block {:as _state-hooks} _last-event _state]
      (let [is-open (some #(= % @inline-search-type) [:page :block :hashtag :template])]
        [:> Autocomplete {:event @last-event
                          :isOpen is-open
                          :onClose #(rf/dispatch [::inline-search.events/close!])}
         (when is-open
           (if (or (string/blank? @inline-search-query)
                   (empty? @inline-search-results))
             [:> Text {:py "0.4rem"
                       :px "0.8rem"
                       :fontStyle "italics"}
              (str "Search for a " (symbol @inline-search-type))]
             (doall
               (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list @inline-search-results)]
                 [:> AutocompleteButton {:key (str "inline-search-item" uid)
                                         :isActive (= i @inline-search-index)
                                         :onClick (fn [_] (inline-item-click state-hooks (:block/uid block) (or title uid)))
                                         :id (str "inline-search-item" uid)}
                  (or title string)]))))]))))
