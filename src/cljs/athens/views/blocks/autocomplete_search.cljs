(ns athens.views.blocks.autocomplete-search
  (:require
    ["@chakra-ui/react" :refer [Portal Popover PopoverTrigger PopoverBody Button PopoverContent Text Box]]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [clojure.string :as string]))


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
  [_block]
  (fn [block state]
    (let [{:search/keys [index results type query] caret-position :caret-position} @state
          can-open (some #(= % type) [:page :block :hashtag :template])
          {:keys [left top]} caret-position]
      [:> Popover {:isOpen can-open
                   :placement "bottom-start"
                   :isLazy true
                   :returnFocusOnClose false
                   :closeOnBlur true
                   :closeOnEsc true
                   :onClose #(swap! state assoc :search/type false)
                   :autoFocus false
                   :onMouseDown (fn [e] (.. e preventDefault))}
       [:> PopoverTrigger
        [:> Box {:position "fixed"
                 :overflow "auto"
                 :width "0"
                 :height "0"
                 :top (str (+ 24 top) "px")
                 :left (str (+ 24 left) "px")}]]
       [:> Portal
        [:> PopoverContent
         [:> PopoverBody {:p 0
                          :overflow "hidden"
                          :borderRadius "inherit"}
          (when can-open
            (if (or (string/blank? query)
                    (empty? results))
              [:> Text {:py "0.4rem"
                        :px "0.8rem"
                        :fontStyle "italics"}
               (str "Search for a " (symbol type))]
              (doall
                (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
                  [:> Button {:key (str "inline-search-item" uid)
                              :id (str "dropdown-item-" i)
                              :borderRadius "0"
                              :justifyContent "flex-start"
                              :width "100%"
                              :_first {:borderTopRadius "inherit"}
                              :_last {:borderBottomRadius "inherit"}
                              :isActive (= index i)
                              ;; if page link, expand to title. otherwise expand to uid for a block ref
                              :onClick (fn [_] (inline-item-click state (:block/uid block) (or title uid)))}
                   (or title string)]))))]]]])))

