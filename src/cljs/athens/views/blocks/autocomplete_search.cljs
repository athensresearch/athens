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
        type   (rf/subscribe [::inline-search.subs/type uid])
        f      (case @type
                 :hashtag  textarea-keydown/auto-complete-hashtag
                 :template textarea-keydown/auto-complete-template
                 :property textarea-keydown/auto-complete-property
                 textarea-keydown/auto-complete-inline)]
    (f uid state-hooks target expansion)))


(defn inline-search-el
  [block {:as state-hooks} last-event]
  (let [block-uid             (:block/uid block)
        inline-search-type    (rf/subscribe [::inline-search.subs/type block-uid])
        inline-search-index   (rf/subscribe [::inline-search.subs/index block-uid])
        inline-search-results (rf/subscribe [::inline-search.subs/results block-uid])
        inline-search-query   (rf/subscribe [::inline-search.subs/query block-uid])]
    (fn [block {:as _state-hooks} _last-event _state]
      (let [is-open (some #(= % @inline-search-type) [:page :block :hashtag :template :property])]
        [:> Autocomplete {:event   @last-event
                          :isOpen  is-open
                          :onClose #(when is-open
                                      (rf/dispatch [::inline-search.events/close! block-uid]))}
         (when is-open
           (if (or (string/blank? @inline-search-query)
                   (empty? @inline-search-results))
             [:> Text {:py        "0.4rem"
                       :px        "0.8rem"
                       :fontStyle "italics"}
              (str "Search for a " (symbol @inline-search-type))]
             (doall
               (for [[i {:keys [node/title block/string block/uid text]}] (map-indexed list @inline-search-results)]
                 [:> AutocompleteButton {:key      (str "inline-search-item" uid)
                                         :isActive (= i @inline-search-index)
                                         :onClick  (fn [_] (inline-item-click state-hooks (:block/uid block) (or title uid)))
                                         :id       (str "inline-search-item" uid)}
                  (or text title string)]))))]))))
