(ns athens.views.blocks.autocomplete-slash
  (:require
    ["/components/Block/Autocomplete" :refer [Autocomplete AutocompleteButton]]
    [athens.events.inline-search :as inline-search.events]
    [athens.subs.inline-search :as inline-search.subs]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [reagent.ratom :as ratom]))


(defn slash-item-click
  [block item]
  (let [block-uid (:block/uid block)
        id        (str "#editable-uid-" block-uid)
        target    (.. js/document (querySelector id))]
    (textarea-keydown/auto-complete-slash block-uid target item)))


(defn slash-menu-el
  [block last-event]
  (let [block-uid             (:block/uid block)
        inline-search-type    (rf/subscribe [::inline-search.subs/type block-uid])
        inline-search-index   (rf/subscribe [::inline-search.subs/index block-uid])
        inline-search-results (rf/subscribe [::inline-search.subs/results block-uid])
        open?                 (ratom/reaction (= @inline-search-type :slash))]
    (fn [block _last-event _state]
      [:> Autocomplete {:event   @last-event
                        :isOpen  @open?
                        :onClose #(when @open?
                                    (rf/dispatch [::inline-search.events/close! block-uid]))}
       (when @open?
         (doall
           (for [[i [text icon _expansion kbd _pos :as item]] (map-indexed list @inline-search-results)]
             [:> AutocompleteButton {:key      text
                                     :id       (str "dropdown-item-" i)
                                     :command  kbd
                                     :isActive (when (= i @inline-search-index) "isActive")
                                     :onClick  (fn [_] (slash-item-click block item))}
              [:<>
               [(r/adapt-react-class icon) {:boxSize 6 :mr 3 :ml 0}]
               text]])))])))
