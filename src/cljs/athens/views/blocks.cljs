(ns athens.views.blocks
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.keybindings :refer [block-key-down]]
    [athens.listeners :refer [multi-block-select-over multi-block-select-up]]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.parser :as parser]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES ZINDICES]]
    [athens.util :refer [now-ts gen-block-uid mouse-offset vertical-center]]
    [athens.views.all-pages :refer [date-string]]
    [athens.views.buttons :refer [button]]
    [athens.views.dropdown :refer [slash-menu-component menu-style dropdown]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [goog.dom.classlist :refer [contains]]
    [goog.events :as events]
    [goog.functions :refer [debounce]]
    [instaparse.core :as parse]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      EventType)))

;;; Styles
;;; 
;;; Blocks use Em units in many places rather than Rem units because
;;; blocks need to scale with their container: sidebar blocks are
;;; smaller than main content blocks, for instance.


(def block-container-style
  {:display "flex"
   :line-height "2em"
   :position "relative"
   :border-radius "0.125rem"
   :justify-content "flex-start"
   :flex-direction "column"
   ::stylefy/manual [[:&.show-tree-indicator:before {:content "''"
                                                     :position "absolute"
                                                     :width "1px"
                                                     :left "calc(1.25em + 1px)"
                                                     :top "2em"
                                                     :bottom "0"
                                                     :transform "translateX(50%)"
                                                     :background (color :border-color)}]
                     [:&:after {:content "''"
                                :z-index -1
                                :position "absolute"
                                :top "0.75px"
                                :right 0
                                :bottom "0.75px"
                                :left 0
                                :opacity 0
                                :pointer-events "none"
                                :border-radius "0.25rem"
                                :transition "opacity 0.075s ease"
                                :background (color :link-color :opacity-lower)
                                :box-shadow [["0 0.25rem 0.5rem -0.25rem" (color :background-color :opacity-med)]]}]
                     [:&.is-selected:after {:opacity 1}]
                     [:.block-container {:margin-left "2rem"}]]})


(stylefy/class "block-container" block-container-style)


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
                            :box-shadow "0 0 0 0.125rem transparent"
                            :display "inline-flex"
                            :margin "50% 0 0 50%"
                            :transform "translate(-50%, -50%)"
                            :height "0.3125em"
                            :width "0.3125em"}]
                   [:hover {:color (color :link-color)}]]

   ::stylefy/manual [[:&.closed-with-children [(selectors/& (selectors/after)) {:box-shadow (str "0 0 0 0.125rem " (color :body-text-color))
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
   :color (color :link-color :opacity-high)
   :position "relative"
   :transform-origin "left"
   :z-index 3
   :width "100%"
   :opacity 0
   ::stylefy/manual [[:&:after {:position "absolute"
                                :content "''"
                                :top "-0.5px"
                                :right "0"
                                :bottom "-0.5px"
                                :left "2em"
                                :border-radius "100px"
                                :background "currentColor"}]]})


(def block-content-style
  {:position "relative"
   :overflow "visible"
   :flex-grow "1"
   :word-break "break-word"
   ::stylefy/manual [[:textarea {:display "none"}]
                     [:&:hover [:textarea [(selectors/& (selectors/not :.is-editing)) {:display "block"
                                                                                       :z-index 1}]]]
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
                                 :border-radius "0.25rem"
                                 :transition "opacity 0.15s ease"
                                 :box-shadow (str "-0.25rem 0 0 0" (color :background-minus-1))
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:.is-editing {:outline "none"
                                    :z-index 3
                                    :display "block"
                                    :opacity "1"}]
                     [:span [:span
                             :a {:position "relative"
                                 :z-index 2}]]
                     ;; May want to refactor specific component styles to somewhere else
                     ;; iframes (YouTube embeds, map embeds, etc.)
                     [:iframe {:border 0
                               :border-radius "0.25rem"}]
                     ;; Images
                     [:img {:border-radius "0.25rem"}]
                     ;; Checkboxes
                     ]})


(stylefy/class "block-content" block-content-style)


(stylefy/keyframes "tooltip-appear"
                   [:from
                    {:opacity "0"
                     :transform "scale(0)"}]
                   [:to
                    {:opacity "1"
                     :transform "scale(1)"}])


(def tooltip-style
  {:z-index (:zindex-dropdown ZINDICES)
   :position "absolute"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :flex-direction "column"
   :background-color (color :background-plus-1)
   :padding "0.5rem 0.75rem"
   :border-radius "0.25rem"
   :line-height "1.75rem"
   :left "0.5rem"
   :top "2rem"
   :transform-origin "0.5rem 1.5rem"
   :min-width "9rem"
   :animation "tooltip-appear .2s ease"
   :transition "background .1s ease"
   :display "table"
   :color (color :body-text-color :opacity-high)
   :border-spacing "0.25rem"
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
                                :top "-0.75rem"
                                :bottom "-1rem"
                                :border-radius "inherit"
                                :left "-1rem"
                                :right "-1rem"
                                :z-index -1
                                :display "block"}]]})


(def dragging-style
  {:opacity "0.25"})


(stylefy/class "dragging" dragging-style)

;; Helpers

(defn on-change
  [value uid]
  ;; (prn "ONCHANGE" value)
  (dispatch [:transact [{:db/id [:block/uid uid] :block/string value :edit/time (now-ts)}]])
  ;; automatically add non-existent pages
  ;; TODO: delete pages that are no longer connected to anything else
  (parse/transform {:page-link (fn [& title]
                                 (let [inner-title (apply + title)]
                                   (when (nil? (db/search-exact-node-title inner-title))
                                     (let [now (now-ts)
                                           uid (gen-block-uid)]
                                       (dispatch [:transact [{:node/title     inner-title
                                                              :block/uid      uid
                                                              :edit/time      now
                                                              :create/time    now}]])))
                                   (str "[[" inner-title "]]")))} (parser/parse-to-ast value)))


(def db-on-change (debounce on-change 1000))


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
  [{:block/keys [uid order] dbid :db/id edit-time :edit/time} state]
  (let [{:keys [dragging tooltip]} @state]
    (when (and tooltip (not dragging))
      [:div (use-style tooltip-style
                       {:class          "tooltip"
                        :on-mouse-leave #(swap! state assoc :tooltip false)})
       [:div [:b "db/id"] [:span dbid]]
       [:div [:b "uid"] [:span uid]]
       [:div [:b "order"] [:span order]]
       [:div [:b "last edit"] [:span (date-string edit-time)]]])))


;; flipped around

(defn page-search-el
  [_block state]
  (let [{:search/keys [page block query results index]} @state]
    (when (or block page)
      [dropdown {:style   {:position "absolute"
                           :top      "100%"
                           :max-height "20rem"
                           :left     "1.75em"}
                 :content (if (clojure.string/blank? query)
                            [:div "Start Typing!"]
                            (doall
                              [:div (use-style menu-style {:id "dropdown-menu"})
                               (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
                                 ^{:key (str "inline-search-item" uid)}
                                 [button
                                  {:on-click #(prn "expand")
                                   :active (when (= index i) true)
                                   :id (str "result-" i)}
                                  (or title string)])]))}])))


;; Actual string contents - two elements, one for reading and one for writing
;; seems hacky, but so far no better way to click into the correct position with one conditional element
(defn block-content-el
  [_ _ _]
  (fn [block state is-editing]
    (let [{:block/keys [string uid]} block]
      [:div {:class "block-content"
             :on-click (fn [e]
                         (when (false? (.. e -shiftKey))
                           (dispatch [:editing/uid uid])))}
       [autosize/textarea {:value         (:atom-string @state)
                           :class         [(when is-editing "is-editing") "textarea"]
                           :auto-focus    true
                           :id            (str "editable-uid-" uid)
                           ;; never actually use on-change. rather, use :string-listener to update datascript. necessary to make react happy
                           :on-change     (fn [_])
                           :on-key-down   (fn [e] (block-key-down e uid state))
                           :on-mouse-down (fn [e]
                                            (if (.. e -shiftKey)
                                              (let [target          (.. e -target)
                                                    ;; TODO: implement for block-page
                                                    node-page       (.. target (closest ".node-page"))
                                                    source-uid      @(subscribe [:editing/uid])
                                                    target-block    (.. target (closest ".block-container"))
                                                    blocks          (vec (array-seq (.. node-page (querySelectorAll ".block-container"))))
                                                    [start end] (-> (keep-indexed (fn [i el]
                                                                                    (when (or (= el target-block)
                                                                                              (= source-uid (.. el -dataset -uid)))
                                                                                      i))
                                                                                  blocks)
                                                                    sort)
                                                    selected-blocks (subvec blocks start (inc end))
                                                    selected-uids   (mapv #(.. % -dataset -uid) selected-blocks)]
                                                (dispatch [:selected/add-items selected-uids]))
                                              (do
                                                (events/listen js/window EventType.MOUSEOVER multi-block-select-over)
                                                (events/listen js/window EventType.MOUSEUP multi-block-select-up))))}]

;;(dispatch [:selected/add-item uid]))}]
       [parse-and-render string uid]
       [:div (use-style (merge drop-area-indicator (when (= :child (:drag-target @state)) {:opacity 1})))]])))


(defn bullet-el
  [_ _]
  (fn [{:block/keys [uid children open]} state]
    [:span (use-style bullet-style
                      {:class         [(when (and (seq children) (not open))
                                         "closed-with-children")]
                       :on-mouse-over #(swap! state assoc :tooltip true)
                       :on-mouse-out  (fn [e]
                                        (let [related (.. e -relatedTarget)]
                                          (when-not (and related (contains related "tooltip"))
                                            (swap! state assoc :tooltip false))))
                       :draggable     true
                       :on-drag-start (fn [e]
                                        (set! (.. e -dataTransfer -effectAllowed) "move")
                                        (.. e -dataTransfer (setData "text/plain" uid))
                               ;;(dispatch [:dragging/uid uid])
                                        (swap! state assoc :dragging true))
                       :on-drag-end   (fn [_]
                               ;; FIXME: not always called
                                        (prn "DRAG END BULLET")
                                        (swap! state assoc :dragging false))})]))


;;TODO: more clarity on open? and closed? predicates, why we use `cond` in one case and `if` in another case)
(defn block-el
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  [block]
  (let [state (r/atom {:atom-string (:block/string block)
                       :slash? false
                       :search/page false
                       :search/query nil
                       :search/block false
                       :search/index 0
                       :dragging false
                       :drag-target nil
                       :edit/time (:edit/time block)})]
    (add-watch state :string-listener
               (fn [_context _atom old new]
                 (let [{:keys [atom-string]} new]
                   (when (not= (:atom-string old) atom-string)
                     (db-on-change atom-string (:block/uid block))))))

    (fn [block]
      (let [{:block/keys [uid string open children] edit-time :edit/time} block
            {dragging :dragging drag-target :drag-target state-edit-time :edit/time} @state
            is-editing @(subscribe [:editing/is-editing uid])
            is-selected @(subscribe [:selected/is-selected uid])]

        ;;(prn uid is-selected)

        ;; if block is updated in datascript, update local block state
        (when (< state-edit-time edit-time)
          (let [new-state {:edit/time edit-time :atom-string string}]
            (swap! state merge new-state)))

        [:div
         {:class         ["block-container"
                          (when dragging "dragging")
                          (when is-selected "is-selected")
                          ;; TODO: is it possible to make this show-tree-indicator a mergable -style map like above?
                          (when (and (seq children) open) "show-tree-indicator")]
          :data-uid      uid
          :on-drag-over  (fn [e]
                           (.. e preventDefault)
                           (.. e stopPropagation)
                           ;; if last block-container (i.e. no siblings), allow drop below
                           ;; if block or ancestor has css dragging class, do not show drop indicator
                           (let [offset            (mouse-offset e)
                                 middle-y          (vertical-center (.. e -target))
                                 closest-container (.. e -target (closest ".block-container"))
                                 next-sibling      (.. closest-container -nextElementSibling)
                                 last-child?       (nil? next-sibling)
                                 dragging-ancestor (.. e -target (closest ".dragging"))
                                 not-dragging?     (nil? dragging-ancestor)
                                 target            (when not-dragging?
                                                     (cond
                                                       ;; if above midpoint, show drop indicator above block
                                                       (< (:y offset) middle-y) :above
                                                       ;; if no children and over 50 pixels from the left, show child drop indicator
                                                       (and (empty? children) (< 50 (:x offset))) :child
                                                       ;; if below midpoint and last child, show drop indicator below
                                                       (and last-child? (< middle-y (:y offset))) :below))]
                             (swap! state assoc :drag-target target)))
          :on-drag-enter (fn [_])
          :on-drag-leave (fn [_]
                           (swap! state assoc :drag-target nil))
          :on-drop       (fn [e]
                           (.. e stopPropagation)
                           (let [source-uid (.. e -dataTransfer (getData "text/plain"))]
                             (cond
                               (nil? drag-target) nil
                               (= source-uid uid) nil)
                             (dispatch [:drop-bullet source-uid uid drag-target])
                             (swap! state assoc :drag-target nil)))}
         [:div (use-style (merge drop-area-indicator (when (= drag-target :above) {:opacity "1"})))]

         [:div {:style {:display "flex"}}
          [toggle-el block]
          [bullet-el block state]
          [tooltip-el block state]
          [block-content-el block state is-editing]]

         (when (:slash? @state)
           [slash-menu-component {:style {:position "absolute" :top "100%" :left "-0.125em"}}])
         [page-search-el block state]

         ;; Children
         (when (and open (seq children))
           (for [child children]
             [:div {:key (:db/id child)}
              [block-el child]]))

         [:div (use-style (merge drop-area-indicator (when (= drag-target :below) {:opacity "1"})))]]))))


(defn block-component
  [ident]
  (let [block (db/get-block-document ident)]
    [block-el block]))
