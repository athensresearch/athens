(ns athens.views.blocks.autocomplete-slash
  (:require
    ["@chakra-ui/react" :refer [Portal Menu MenuList MenuItem]]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [reagent.core :as r]))


(defn slash-item-click
  [state block item]
  (let [id        (str "#editable-uid-" (:block/uid block))
        target    (.. js/document (querySelector id))]
    (textarea-keydown/auto-complete-slash state target item)))


(defn slash-menu-el
  [_block state]
  (fn [block state]
    (let [{:search/keys [index results type] caret-position :caret-position} @state
          {:keys [left top]} caret-position]
      [:> Menu {:isOpen (= type :slash)
                :onClose #(swap! state assoc :search/type false)
                :isLazy true}
       [:> Portal
        [:> MenuList {:position "absolute"
                      :left (str left "px")
                      :top (str (+ top 24) "px")}
         (doall
          (for [[i [text icon _expansion kbd _pos :as item]] (map-indexed list results)]
            [:> MenuItem {:key     text
                          :id      (str "dropdown-item-" i)
                          :command kbd
                          :class (when (= i index) "isActive")
                          :onClick (fn [_] (slash-item-click state block item))}
             [:<>
              [(r/adapt-react-class icon)]
              text]]))]]])))