(ns athens.views.blocks.autocomplete-slash
  (:require
    ["/components/Block/Autocomplete" :refer [Autocomplete AutocompleteButton]]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [reagent.core :as r]))


(defn slash-item-click
  [state block item]
  (let [id        (str "#editable-uid-" (:block/uid block))
        target    (.. js/document (querySelector id))]
    (textarea-keydown/auto-complete-slash state target item)))


(defn slash-menu-el
  [_block]
  (fn [block state]
    (let [{:keys [last-e]} @state
          {:search/keys [index results type]} @state]
      [:> Autocomplete {:event last-e
                        :isOpen (= type :slash)
                        :onClose #(swap! state assoc :search/type false)}
       (when (= type :slash)
         (for [[i [text icon _expansion kbd _pos :as item]] (map-indexed list results)]
           [:> AutocompleteButton {:key     text
                                   :id      (str "dropdown-item-" i)
                                   :command kbd
                                   :isActive (when (= i index) "isActive")
                                   :onClick (fn [_] (slash-item-click state block item))}
            [:<>
             [(r/adapt-react-class icon) {:boxSize 6 :mr 3 :ml 0}]
             text]]))])))
