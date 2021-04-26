(ns athens.views.blocks.autocomplete-search
  (:require
    [athens.style :as style]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [athens.views.buttons :as buttons]
    [athens.views.dropdown :as dropdown]
    [clojure.string :as string]
    [goog.events :as events]
    [reagent.core :as r]
    [stylefy.core :as stylefy]))


(defn inline-item-click
  [state uid expansion]
  (let [id     (str "#editable-uid-" uid)
        target (.. js/document (querySelector id))]
    (case (:search/type @state)
      :hashtag (textarea-keydown/auto-complete-hashtag state target expansion)
      (textarea-keydown/auto-complete-inline state target expansion))))


(defn inline-search-el
  [_block state]
  (let [ref                  (atom nil)
        handle-click-outside (fn [e]
                               (let [{:search/keys [type]} @state]
                                 (when (and (or (= type :page) (= type :block) (= type :hashtag))
                                            (not (.. @ref (contains (.. e -target)))))
                                   (swap! state assoc :search/type false))))]
    (r/create-class
      {:display-name           "inline-search"
       :component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [block state]
                                 (let [{:search/keys [query results index type] caret-position :caret-position} @state
                                       {:keys [left top]} caret-position]
                                   (when (some #(= % type) [:page :block :hashtag])
                                     [:div (merge (stylefy/use-style dropdown/dropdown-style
                                                                     {:ref           #(reset! ref %)
                                                                      ;; don't blur textarea when clicking to auto-complete
                                                                      :on-mouse-down (fn [e] (.. e preventDefault))})
                                                  {:style {:position   "absolute"
                                                           :max-height "20rem"
                                                           :z-index    (:zindex-popover style/ZINDICES)
                                                           :top        (+ 24 top)
                                                           :left       (+ 24 left)}})
                                      [:div#dropdown-menu (stylefy/use-style dropdown/menu-style)
                                       (if (or (string/blank? query)
                                               (empty? results))
                                         ;; Just using button for styling
                                         [buttons/button (stylefy/use-style {:opacity (style/OPACITIES :opacity-low)}) (str "Search for a " (symbol type))]
                                         (doall
                                           (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
                                             [buttons/button {:key      (str "inline-search-item" uid)
                                                              :id       (str "dropdown-item-" i)
                                                              :active   (= index i)
                                                              ;; if page link, expand to title. otherwise expand to uid for a block ref
                                                              :on-click (fn [_] (inline-item-click state (:block/uid block) (or title uid)))
                                                              :style    {:text-align "left"}}
                                              (or title string)])))]])))})))

