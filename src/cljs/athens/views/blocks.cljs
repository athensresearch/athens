(ns athens.views.blocks
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.keybindings :refer [block-key-down]]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES]]
    [athens.views.dropdown :refer [slash-menu-component #_menu dropdown]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [goog.dom :refer [getAncestorByClass]]
    [goog.dom.classlist :refer [contains]]
    [goog.functions :refer [debounce]]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def block-style
  {:display "flex"
   :line-height "2em"
   :position "relative"
   :justify-content "flex-start"
   :flex-direction "column"
   ::stylefy/manual [[:&.show-tree-indicator:before {:content "''"
                                                     :position "absolute"
                                                     :width "1px"
                                                     :left "calc(1.25em + 1px)"
                                                     :top "2em"
                                                     :bottom "0"
                                                     :transform "translateX(50%)"
                                                     :background (color :border-color)}]]})


(def block-disclosure-toggle-style
  {:width "1em"
   :height "2em"
   :flex-shrink "0"
   :display "flex"
   :background "none"
   :border "none"
   :border-radius "100px"
   :transition "all 0.05s ease"
   :align-items "center"
   :justify-content "center"
   :padding "0"
   :-webkit-appearance "none"
   :color (color :body-text-color 0.4)
   ::stylefy/mode [[:hover {:color (color :link-color)}]
                   [":is(button)" {:cursor "pointer"}]]
   ::stylefy/manual [[:&.closed [:svg {:transform "rotate(-90deg)"}]]]})


(def bullet-style
  {:flex-shrink "0"
   :cursor "pointer"
   :width "0.75em"
   :margin-right "0.25em"
   :transition "all 0.05s ease"
   :height "2em"
   :color (color :body-text-color :opacity-low)
   ::stylefy/mode [[:after {:content "''"
                            :background "currentColor"
                            :transition "all 0.05s ease"
                            :border-radius "100px"
                            :box-shadow "0 0 0 2px transparent"
                            :display "inline-flex"
                            :margin "50% 0 0 50%"
                            :transform "translate(-50%, -50%)"
                            :height "0.3125em"
                            :width "0.3125em"}]
                   [:hover {:color (color :link-color)}]]

   ::stylefy/manual [[:&.closed-with-children [(selectors/& (selectors/after)) {:box-shadow (str "0 0 0 2px " (color :body-text-color))
                                                                                :opacity (:opacity-med OPACITIES)}]]
                     [:&.closed-with-children [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&:hover:after {:transform "translate(-50%, -50%) scale(1.3)"}]
                     [:&.dragging {:z-index 1
                                   :cursor "grabbing"
                                   :color (color :body-text-color)}]]})


(stylefy/keyframes "drop-area-appear"
                   [:from
                    {:opacity "0"}]
                   [:to
                    {:opacity "1"}])


(stylefy/keyframes "drop-area-color-pulse"
                   [:from
                    {:opacity (:opacity-lower OPACITIES)}]
                   [:to
                    {:opacity (:opacity-med OPACITIES)}])


(def drop-area-indicator
  {:display "block"
   :height "1px"
   :margin-bottom "-1px"
   :color (color :body-text-color :opacity-low)
   :position "relative"
   :transform-origin "left"
   :z-index 3
   :width "100%"
   ;;:animation "drop-area-appear .5s ease"
   ::stylefy/manual [[:&:after {:position "absolute"
                                :content "''"
                                :top "-0.5px"
                                :right "0"
                                :bottom "-0.5px"
                                :left "0"
                                :border-radius "100px"
                                ;;:animation "drop-area-color-pulse 1s ease infinite alternate"
                                :background "currentColor"}]]})


(def block-content-style
  {:position "relative"
   :overflow "visible"
   :flex-grow "1"
   :word-break "break-word"
   ::stylefy/manual [[:textarea {:display "none"}]
                     [:&:hover [:textarea {:display "block"
                                           :z-index 1}]]
                     [:textarea {:-webkit-appearance "none"
                                 :cursor "text"
                                 :resize "none"
                                 :transform "translate3d(0,0,0)"
                                 :color "inherit"
                                 :padding "0"
                                 :background (color :background-minus-1)
                                 :position "absolute"
                                 :top "0"
                                 :left "0"
                                 :right "0"
                                 :width "100%"
                                 :min-height "100%"
                                 :caret-color (color :link-color)
                                 :margin "0"
                                 :font-size "inherit"
                                 :line-height "inherit"
                                 :border-radius "4px"
                                 :transition "opacity 0.15s ease"
                                 :box-shadow (str "-4px 0 0 0" (color :background-minus-1))
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:textarea:focus
                      :.is-editing {:outline "none"
                                    :z-index 3
                                    :display "block"
                                    :opacity "1"}]
                     [:span [:span
                             :a {:position "relative"
                                 :z-index 2}]]]})


(stylefy/keyframes "tooltip-appear"
                   [:from
                    {:opacity "0"
                     :transform "scale(0)"}]
                   [:to
                    {:opacity "1"
                     :transform "scale(1)"}])


(def tooltip-style
  {:z-index    4
   :position "absolute"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :flex-direction "column"
   :background-color (color :background-plus-1)
   :padding "8px 12px"
   :border-radius "4px"
   :line-height "24px"
   :left "8px"
   :top "32px"
   :transform-origin "8px 24px"
   :min-width "150px"
   :animation "tooltip-appear .2s ease"
   :transition "background .1s ease"
   :display "table"
   :color (color :body-text-color :opacity-high)
   :border-spacing "4px"
   ::stylefy/manual [[:div {:display "table-row"}]
                     [:b {:display "table-cell"
                          :user-select "none"
                          :text-align "right"
                          :text-transform "uppercase"
                          :font-size "12px"
                          :letter-spacing "0.1em"
                          :opacity (:opacity-med OPACITIES)}]
                     [:span {:display "table-cell"
                             :user-select "all"}
                      [:&:hover {:color (color :header-text-color)}]]
                     [:&:after {:content "''"
                                :position "absolute"
                                :top "-12px"
                                :bottom "-16px"
                                :border-radius "inherit"
                                :left "-16px"
                                :right "-16px"
                                :z-index -1
                                :display "block"}]]})


(def dragging-style
  {:background-color "lightblue"})


;; Helpers

(defn on-change
  [value uid]
  (dispatch [:transact [[:db/add [:block/uid uid] :block/string value]]]))


(def db-on-change (debounce on-change 500))


(defn toggle
  [id open]
  (dispatch [:transact [[:db/add id :block/open (not open)]]]))


;;; Components

(defn toggle-el
  [{:block/keys [open uid children]}]
  (if (seq children)
    [:button (use-style block-disclosure-toggle-style
                        {:class    (if open "open" "closed")
                         :on-click #(toggle [:block/uid uid] open)})
     [:> mui-icons/KeyboardArrowDown {:style {:font-size "16px"}}]]
    [:span (use-style block-disclosure-toggle-style)]))


;; FIXME: fix flicker from on-mouse-enter on-mouse-leave
(defn tooltip-el
  [{:block/keys [uid order] dbid :db/id} state]
  (let [{:keys [dragging tooltip]} @state]
    (when (and tooltip (not dragging))
      [:div (use-style tooltip-style
                       {:class          "tooltip"
                        :on-mouse-leave #(swap! state assoc :tooltip false)})
       [:div [:b "db/id"] [:span dbid]]
       [:div [:b "uid"] [:span uid]]
       [:div [:b "order"] [:span order]]])))


(defn bullet-el
  [{:block/keys [uid children open]} state]
  [:span (merge (use-style bullet-style
                           {:class         [(when (and (seq children) (not open))
                                              "closed-with-children")]
                            :draggable     true
                            :on-mouse-over #(swap! state assoc :tooltip true)
                            :on-mouse-out  (fn [e]
                                             (when-not (contains (.. e -relatedTarget) "tooltip")
                                               (swap! state assoc :tooltip false)))
                            :on-drag-end   (fn [_] (swap! state assoc :dragging false))
                            :on-drag-start (fn [e]
                                             (.. e stopPropagation)
                                             (set! (.. e -dataTransfer -effectAllowed) "move")
                                    ;;(prn "UID" uid)
                                             (.. e -dataTransfer (setData "text/plain" uid))
                                             (swap! state assoc :dragging true))}))])


;; Actual string contents - two elements, one for reading and one for writing
;; seems hacky, but so far no better way to click into the correct position with one conditional element
(defn block-content-el
  [{:block/keys [string uid children]} state]
  (let [editing-uid @(subscribe [:editing/uid])]

    (when (and (not (= editing-uid uid))
               (< (count (:atom-string @state)) (count string)))
      (swap! state assoc :atom-string string))

    [:div (use-style block-content-style
                     {:class         "block-content"
                      :on-drag-enter (fn [e]
                                       (.. e stopPropagation)
                                       (swap! state assoc :drag-target :child))
                      :on-drag-over  (fn [e]
                                       (.. e preventDefault)
                                       (.. e stopPropagation)
                                       false)
                      :on-drag-leave (fn [e]
                                       (.. e stopPropagation)
                                       (let [related-container (getAncestorByClass (.. e -relatedTarget) "block-container")
                                             source-container  (getAncestorByClass (.. e -target) "block-container")]
                                         (cond
                                           (= related-container source-container) nil
                                           :else (swap! state assoc :drag-target nil))))
                      :on-drop       (fn [e]
                                       (let [source-uid      (.. e -dataTransfer (getData "text/plain"))
                                             parent-dragging (getAncestorByClass (.. e -target) "dragging")]
                                         (.. e preventDefault)
                                         (.. e stopPropagation)
                                         (swap! state assoc :dragging false)
                                         (swap! state assoc :drag-target nil)
                                         (when (and (nil? parent-dragging) (not= source-uid uid))
                                           (dispatch [:drop-bullet source-uid uid :child]))))})

     [autosize/textarea {:value       (:atom-string @state)
                         :class       [(when (= editing-uid uid) "is-editing") "textarea"]
                         :auto-focus  true
                         :id          (str "editable-uid-" uid)
                         :on-change   (fn [_]
                                        (when (not= string (:atom-string @state))
                                          (db-on-change (:atom-string @state) uid)))
                         :on-key-down (fn [e] (block-key-down e uid state))}]
     [parse-and-render string]
     ;; don't show drop indicator when dragging to its children
     (when (and (empty? children) (not (:dragging @state)))
       [:div.drag-n-drop (use-style (merge {:height "2px"}
                                           (when (= (:drag-target @state) :child) {:background-color "red"})))])]))

;; flipped around

(defn page-search-el
  [_block state]
  (when (:search/page @state)
    (let [query   (:search/query @state)
          results (when (not (clojure.string/blank? query))
                    (db/search-in-node-title query))]
      [dropdown {:style   {:position "absolute"
                           :top      "100%"
                           :left     "-0.125em"}
                 :content (if (or (not query) (clojure.string/blank? query))
                            [:div "Start Typing!"]
                            (for [{:keys [node/title block/uid]} results]
                              ^{:key uid}
                              [:div {:on-click #(navigate-uid uid)} title]))}])))


;;TODO: more clarity on open? and closed? predicates, why we use `cond` in one case and `if` in another case)
(defn block-el
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  [block]
  (let [state (r/atom {:atom-string (:block/string block)
                       :slash? false
                       :search/page false
                       :search/query nil
                       :search/block false
                       :dragging false
                       :drag-target nil})]
    (fn [block]
      (let [{:block/keys [uid #_string open children order]} block
            {dragging :dragging drag-target :drag-target} @state
            parent (db/get-parent [:block/uid uid])
            last-child? (= order (dec (count (:block/children parent))))]

        ;; xxx: bad vibes - if not editing-uid, allow ratom to be appended by joining two blocks (deleting at start)

        ;;(prn "target" uid drag-target)

        [:<>

         ;; should be (when dragging-global) but this causes react to void the original component, preventing on-drag-end from firing
         ;; need surface to drag over. probably a better way to do this
         ;; FIXME drop-area-indicator styles no longer work because using a div now and document structure has changed
         (when true
           [:div.drag-n-drop (use-style (merge {:height "2px"}
                                               (when (= drag-target :container) {:background-color "blue"})))])

         [:div.block-container
          (use-style (merge block-style (when dragging dragging-style))
            ;; TODO: is it possible to make this show-tree-indicator a mergable -style map like above?
                     {:class         [(when dragging "dragging")
                                      (when (and (seq children) open) "show-tree-indicator")]
                      :on-drag-enter (fn [e]
                                       (.. e stopPropagation)
                                       (swap! state assoc :drag-target :container))
                      :on-drag-over  (fn [e]
                                       (.. e preventDefault)
                                       (.. e stopPropagation)
                                       false)
                      :on-drag-leave (fn [e]
                                       (let [related-container (getAncestorByClass (.. e -relatedTarget) "block-container")
                                             source-container  (getAncestorByClass (.. e -target) "block-container")]
                                         (when-not (= related-container source-container)
                                           (swap! state assoc :drag-target nil))))
                      :on-drop       (fn [e]
                                       (let [source-uid      (.. e -dataTransfer (getData "text/plain"))
                                             parent-dragging (getAncestorByClass (.. e -target) "dragging")]
                                         (.. e preventDefault)
                                         (.. e stopPropagation)
                                         (swap! state assoc :dragging false)
                                         (swap! state assoc :drag-target nil)
                                         (when (and (nil? parent-dragging) (not= source-uid uid))
                                           (dispatch [:drop-bullet source-uid uid :sibling]))))})

          [:div {:style {:display "flex"}}
           [toggle-el block]
           [bullet-el block state]
           [tooltip-el block state]
           [block-content-el block state]]

          (when (:slash? @state)
            [slash-menu-component {:style {:position "absolute" :top "100%" :left "-0.125em"}}])
          [page-search-el block state]

          ;; Children
          ;; if last element and no children, allow drop
          (when (and open (seq children))
            (for [child children]
              [:div {:style {:margin-left "32px"} :key (:db/id child)}
               [block-el child]]))]

         (when last-child?
           [:div.drag-n-drop (use-style (merge {:height "2px"}
                                               (when (= drag-target :container) {:background-color "green"})))])]))))


(defn block-component
  [ident]
  (let [block (db/get-block-document ident)]
    [block-el block]))
