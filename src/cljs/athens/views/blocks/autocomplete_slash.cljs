(ns athens.views.blocks.autocomplete-slash
  (:require
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [athens.views.buttons :as buttons]
    [athens.views.dropdown :as dropdown]
    [goog.events :as events]
    [reagent.core :as r]
    [stylefy.core :as stylefy]))


(defn slash-item-click
  [state block item]
  (let [id        (str "#editable-uid-" (:block/uid block))
        target    (.. js/document (querySelector id))]
    (textarea-keydown/auto-complete-slash state target item)))


(defn slash-menu-el
  [_block state]
  (let [ref (atom nil)
        handle-click-outside (fn [e]
                               (let [{:search/keys [type]} @state]
                                 (when (and (= type :slash)
                                            (not (.. @ref (contains (.. e -target)))))
                                   (swap! state assoc :search/type false))))]
    (r/create-class
      {:display-name           "slash-menu"
       :component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [block state]
                                 (let [{:search/keys [index results type] caret-position :caret-position} @state
                                       {:keys [left top]} caret-position]
                                   (when (= type :slash)
                                     [:div (merge (stylefy/use-style dropdown/dropdown-style
                                                                     {:ref           #(reset! ref %)
                                                                      ;; don't blur textarea when clicking to auto-complete
                                                                      :on-mouse-down (fn [e] (.. e preventDefault))})
                                                  {:style {:position "absolute" :left (+ left 24) :top (+ top 24)}})
                                      [:div#dropdown-menu (merge (stylefy/use-style dropdown/menu-style) {:style {:max-height "8em"}})
                                       (doall
                                         (for [[i [text icon _expansion kbd _pos :as item]] (map-indexed list results)]
                                           [buttons/button {:key      text
                                                            :id       (str "dropdown-item-" i)
                                                            :active   (= i index)
                                                            :on-click (fn [_] (slash-item-click state block item))}
                                            [:<> [(r/adapt-react-class icon)] [:span text] (when kbd [:kbd kbd])]]))]])))})))


