;; (ns athens.views.blocks.autocomplete-slash
;;   (:require
;;     ["@chakra-ui/react" :refer [Menu MenuList MenuItem Portal]]
;;     [athens.views.blocks.textarea-keydown :as textarea-keydown]
;;     [goog.events :as events]
;;     [reagent.core :as r]))


;; (defn slash-item-click
;;   [state block item]
;;   (let [id        (str "#editable-uid-" (:block/uid block))
;;         target    (.. js/document (querySelector id))]
;;     (textarea-keydown/auto-complete-slash state target item)))


;; (defn slash-menu-el
;;   [_block state]
;;   (let [ref (atom nil)
;;         clear-search (swap! state assoc :search/type false)
;;         handle-click-outside (fn [e]
;;                                (let [{:search/keys [type]} @state]
;;                                  (when (and (= type :slash)
;;                                             (not (.. @ref (contains (.. e -target)))))
;;                                    clear-search)))]
;;     (r/create-class
;;       {:display-name           "slash-menu"
;;        :component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
;;        :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
;;        :reagent-render         (fn [block state]
;;                                  (let [{:search/keys [index results type] caret-position :caret-position} @state
;;                                        {:keys [left top]} caret-position]
;;                                    (js/console.log caret-position)
;;                                    (when (= type :slash)
;;                                      (js/console.log "opened")
;;                                      [:> Portal
;;                                       [:> Box {:position "absolute"
;;                                                :ref #(reset! ref %)
;;                                                :left (+ left 24)
;;                                                :top (+ top 24)}
;;                                               ;; don't blur textarea when clicking to auto-complete
;;                                        :on-mouse-down (fn [e] (.. e preventDefault))
;;                                        [:> Menu {:isOpen (= type :slash)
;;                                                  :isLazy true
;;                                                  :onClose clear-search}
;;                                         [:> MenuList
;;                                          (doall
;;                                           (for [[i [text icon _expansion kbd _pos :as item]] (map-indexed list results)]
;;                                             [:> MenuItem {:key      text
;;                                                           :id       (str "dropdown-item-" i)
;;                                                           :isActive (= i index)
;;                                                           :command  kbd
;;                                                           :onClick (fn [_] (slash-item-click state block item))}
;;                                              [:<> [(r/adapt-react-class icon)] [:span text]]]))]]]])))})))


(ns athens.views.blocks.autocomplete-slash
  (:require
    ["@chakra-ui/react" :refer [Portal Menu MenuList MenuItem]]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [goog.events :as events]
    [reagent.core :as r]))


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
                                  [:> Portal
                                   [:> Menu {:isOpen (= type :slash)
                                             :ref #(reset! ref %)
                                             ;; don't blur textarea when clicking to auto-complete
                                             :on-mouse-down (fn [e] (.. e preventDefault))}
                                    [:> MenuList {:position "absolute"
                                                  :left (+ left 0)
                                                  :top (+ top 24)}
                                     (doall
                                      (for [[i [text icon _expansion kbd _pos :as item]] (map-indexed list results)]
                                        [:> MenuItem {:key      text
                                                      :id       (str "dropdown-item-" i)
                                                      :isActive (= i index)
                                                      :command  kbd
                                                      :onClick (fn [_] (slash-item-click state block item))}
                                         [:<>
                                          [(r/adapt-react-class icon)]
                                          [:span text]]]))]]]))})))


