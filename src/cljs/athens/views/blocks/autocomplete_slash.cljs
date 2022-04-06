(ns athens.views.blocks.autocomplete-slash
  (:require
   ["/components/Block/components/Autocomplete" :refer [Autocomplete]]
   ["@chakra-ui/react" :refer [Portal Menu MenuList MenuItem]]
   [athens.util :as util :refer [get-caret-position]]
   [athens.views.blocks.textarea-keydown :as textarea-keydown]
   [goog.style :refer [getClientPosition]]
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
                        :onClose #(swap! state assoc :search/type false)
                        :onSelect (fn [item] (slash-item-click state block item))
                        :index index
                        :options (clj->js (map-indexed list results))}]
      #_[:> Menu {:isOpen (= type :slash)
                  :onClose #(swap! state assoc :search/type false)
                  :isLazy true}
         [:> Portal
          (when (= type :slash)
            [:> MenuList {:position "absolute"
                          :left (str left "px")
                          :top (str (+ top 24) "px")}
             (doall
              (for [[i [text icon _expansion kbd _pos :as item]] (map-indexed list results)]
                [:> MenuItem {:key     text
                              :isFocusable false
                              :id      (str "dropdown-item-" i)
                              :command kbd
                              :class (when (= i index) "isActive")
                              :onClick (fn [_] (slash-item-click state block item))}
                 [:<>
                  [(r/adapt-react-class icon)]
                  text]]))])]])))
