(ns athens.views.blocks.autocomplete-search
  (:require
   ["@chakra-ui/react" :refer [Portal Button VStack Text]]
   [athens.views.blocks.textarea-keydown :as textarea-keydown]
   [clojure.string :as string]
   [goog.events :as events]
   [reagent.core :as r]))


(defn inline-item-click
  [state uid expansion]
  (let [id     (str "#editable-uid-" uid)
        target (.. js/document (querySelector id))
        f      (case (:search/type @state)
                 :hashtag  textarea-keydown/auto-complete-hashtag
                 :template textarea-keydown/auto-complete-template
                 textarea-keydown/auto-complete-inline)]
    (f state target expansion)))


(defn inline-search-el
  [_block state]
  (let [ref                  (atom nil)
        handle-click-outside (fn [e]
                               (let [{:search/keys [type]} @state]
                                 (when (and (#{:page :block :hashtag :template} type)
                                            (not (.. @ref (contains (.. e -target)))))
                                   (swap! state assoc :search/type false))))]
    (r/create-class
     {:display-name           "inline-search"
      :component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
      :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
      :reagent-render         (fn [block state]
                                (let [{:search/keys [query results index type] caret-position :caret-position} @state
                                      {:keys [left top]} caret-position]
                                  (when (some #(= % type) [:page :block :hashtag :template])
                                    [:> Portal
                                     [:> VStack {:ref #(reset! ref %)
                                                 ;; don't blur textarea when clicking to auto-complete
                                                 :on-mouse-down (fn [e] (.. e preventDefault))
                                                 :position   "absolute"
                                                 :overflow "auto"
                                                 :p 1
                                                 :align "stretch"
                                                 :justify "stretch"
                                                 :width "max-content"
                                                 :bg "background.upper"
                                                 :maxHeight "20rem"
                                                 :top (str (+ 24 top) "px")
                                                 :left (str (+ 24 left) "px")}
                                      (if (or (string/blank? query)
                                              (empty? results))
                                        [:> Text (str "Search for a " (symbol type))]
                                        (doall
                                         (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
                                           [:> Button {:key      (str "inline-search-item" uid)
                                                       :id       (str "dropdown-item-" i)
                                                       :width "100%"
                                                       :isActive   (= index i)
                                                         ;; if page link, expand to title. otherwise expand to uid for a block ref
                                                       :onClick (fn [_] (inline-item-click state (:block/uid block) (or title uid)))}
                                            (or title string)])))]])))})))

