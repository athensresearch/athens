(ns athens.views.blocks
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES]]
    [athens.views.dropdown :refer [slash-menu-component #_menu dropdown]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [goog.dom.selection :refer [setStart getStart setEnd getEnd #_setText getText setCursorPosition #_getEndPoints]]
    [goog.events.KeyCodes :refer [isCharacterKey]]
    [goog.functions :refer [debounce]]
    [komponentit.autosize :as autosize]
    [re-frame.core  :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      KeyCodes)))


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
                                                     :background (color :panel-color)}]]})


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
   :color (color :panel-color)
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

   ::stylefy/manual [[:&.closed-with-children {}]
                     [:&.closed-with-children [(selectors/& (selectors/after)) {:box-shadow (str "0 0 0 2px " (color :body-text-color))
                                                                                :opacity (:opacity-med OPACITIES)}]]
                     [:&.closed-with-children [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&.closed-with-children [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&:hover:after {:transform "translate(-50%, -50%) scale(1.3)"}]
                     [:&.dragging {:z-index "1000"
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
   :z-index "1000"
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
                                 :background (color :panel-color)
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
                                 :box-shadow (str "-4px 0 0 0" (color :panel-color))
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:textarea:focus
                      :.is-editing {:outline "none"
                                    :z-index "10"
                                    :display "block"
                                    :opacity "1"}]
                     [:span [:span
                             :a {:position "relative"
                                 :z-index "2"}]]]})


(stylefy/keyframes "tooltip-appear"
                   [:from
                    {:opacity "0"
                     :transform "scale(0)"}]
                   [:to
                    {:opacity "1"
                     :transform "scale(1)"}])


(def tooltip-style
  {:z-index    2
   :position "absolute"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :flex-direction "column"
   :background-color "white"
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


(defn on-key-down
  "The most important question in all of Athens:

    Vim vs Emacs"
  [e uid state]
  (let [key          (.. e -key)
        key-code     (.. e -keyCode)
        shift        (.. e -shiftKey)
        meta         (.. e -metaKey)
        ctrl         (.. e -ctrlKey)
        alt          (.. e -altKey)
        target       (.. e -target)
        start        (getStart target)
        end          (getEnd target)
        selection    (getText target)
        string       (:atom-string @state)
        query        (:search/query @state)
        block-start? (zero? start)
        block-end?   (= start (count string))
        top-row?     true                                   ;; TODO
        bottom-row?  true                                   ;; TODO
        head         (subs string 0 start)
        tail         (subs string end)]

    (cond

      ;; -- Arrow Keys ---------------------------------------------------------
      (and (= key-code KeyCodes.UP) top-row?) (dispatch [:up uid])
      (and (= key-code KeyCodes.LEFT) block-start?) (dispatch [:left uid])
      (and (= key-code KeyCodes.DOWN) bottom-row?) (dispatch [:down uid])
      (and (= key-code KeyCodes.RIGHT) block-end?) (dispatch [:right uid])

      ;; -- Tab ----------------------------------------------------------------
      (and shift (= key-code KeyCodes.TAB)) (dispatch [:unindent uid])
      (= key-code KeyCodes.TAB) (dispatch [:indent uid])

      ;; -- Enter --------------------------------------------------------------

      ;; shift-enter: add line break
      (and shift (= key-code KeyCodes.ENTER))
      (swap! state assoc :atom-string (str head "\n" tail))

      ;; enter: depends on context
      (= key-code KeyCodes.ENTER) (do (.. e preventDefault)
                                      (dispatch [:enter uid string start state]))

      ;; -- Backspace ----------------------------------------------------------

      ;; if selection, delete entire selection
      (and (not= selection "") (= key-code KeyCodes.BACKSPACE))
      (let [new-tail (subs string end)
            new-str (str head new-tail)]
        (swap! state assoc :atom-string new-str))

      ;; if meta, delete to start of line
      (and meta (= key-code KeyCodes.BACKSPACE)) (swap! state assoc :atom-string tail)

      ;; if at block start, dispatch (requires context)
      (and (= key-code KeyCodes.BACKSPACE) block-start? (= start end)) (dispatch [:backspace uid string])

      ;; if within brackets, delete close bracket as well
      (and (= key-code KeyCodes.BACKSPACE) (= "[]" (subs string (dec start) (inc start))))
      (let [head (subs string 0 (dec start))
            tail (subs string (inc start))
            new-str (str head tail)]
        (js/setTimeout #(setCursorPosition target (dec start)) 10)
        (swap! state assoc :atom-string new-str)
        (swap! state assoc :search/page false))

      ;; default backspace: delete a character
      (= key-code KeyCodes.BACKSPACE) (let [head (subs string 0 (dec start))
                                            new-str (str head tail)]
                                        (when (or (:search/page @state) (:search/block @state))
                                          (swap! state assoc :search/query (subs query 0 (dec (count query)))))
                                        (swap! state assoc :atom-string new-str))

      ;; open slash commands
      (and (= key-code KeyCodes.SLASH)) (swap! state update :slash? not)

      ;; -- Open Bracket -------------------------------------------------------

      ;; if selection, add brackets around selection
      (and (not= "" selection) (= key-code KeyCodes.OPEN_SQUARE_BRACKET))
      (let [surround-selection (str "[" selection "]")
            new-str (str head surround-selection tail)]
        (js/setTimeout (fn []
                         (setStart target (inc start))
                         (setEnd target (inc end)))
                       10)
        (swap! state assoc :atom-string new-str))

      ;; default: auto-create close bracket
      (= key-code KeyCodes.OPEN_SQUARE_BRACKET)
      (let [new-str (str head "[]" tail)
            double-brackets? (= "[[]]" (subs new-str (dec start) (+ start 3)))]
        (js/setTimeout #(setCursorPosition target (inc start)) 10)
        (swap! state assoc :atom-string new-str)
        ;; if second bracket, open search
        (when double-brackets?
          (swap! state assoc :search/page true)))

      ;; TODO: close bracket should not be created if open bracket already exists or user just made a link
      ;;(= key-code KeyCodes.CLOSE_SQUARE_BRACKET)

      ;; -- Parentheses --------------------------------------------------------

      ;; xxx: why doesn't Closure have parens key codes?
      (and shift (= key-code KeyCodes.NINE)) (swap! state update :search/block not)

      ;; -- Hotkeys ------------------------------------------------------------

      (and meta (= key-code KeyCodes.A))
      (do
        (setStart target 0)
        (setEnd target end))

      ;; TODO: undo. conflicts with datascript undo
      (and meta (= key-code KeyCodes.Z)) nil

      ;; TODO: cut
      (and meta (= key-code KeyCodes.X)) nil

      ;; TODO: paste. magical
      (and meta (= key-code KeyCodes.V)) nil

      ;; TODO: bold
      (and meta (= key-code KeyCodes.B)) nil

      ;; TODO: italicize
      (and meta (= key-code KeyCodes.I)) nil

      ;; -- Default: Add new character -----------------------------------------

      (and (not meta) (not ctrl) (not alt) (isCharacterKey key-code))
      (let [new-str (str head key tail)]
        (when (or (:search/page @state) (:search/block @state))
          (swap! state assoc :search/query (str (:search/query @state) key)))
        (swap! state assoc :atom-string new-str)))))

      ;;:else (prn "non-event" key key-code))))


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
  (let [{:keys [_dragging _tooltip]} @state]
    (when (and _tooltip (not _dragging))
    ;;(when false
      [:div (use-style tooltip-style {:class "tooltip"})
       [:div [:b "db/id"] [:span dbid]]
       [:div [:b "uid"] [:span uid]]
       [:div [:b "order"] [:span order]]])))


(defn bullet-el
  [{:block/keys [uid children open]} state]
  [:span (merge (use-style bullet-style
                  {:class     [(when (and (seq children) (not open))
                                 "closed-with-children")]
                   :draggable true
                   :on-mouse-over (fn [_] (swap! state assoc :tooltip true))
                   :on-mouse-out (fn [_] (swap! state assoc :tooltip false))
                   :on-drag-end   (fn [_] (swap! state assoc :dragging false))
                   :on-drag-start (fn [e]
                                    (.. e stopPropagation)
                                    (set! (.. e -dataTransfer -effectAllowed) "move")
                                    (prn "UID" uid)
                                    (.. e -dataTransfer (setData "text/plain" uid))
                                    (swap! state assoc :dragging true))}))])


;; Actual string contents - two elements, one for reading and one for writing
;; seems hacky, but so far no better way to click into the correct position with one conditional element
(defn block-content-el
  [{:block/keys [string uid]} state]
  (let [editing-uid @(subscribe [:editing/uid])]
    [:div (use-style block-content-style
            {:on-drag-enter (fn [e]
                              (.. e stopPropagation)
                              (swap! state assoc :drag-target :child))
             :on-drag-over  (fn [e]
                              (.. e preventDefault)
                              (.. e stopPropagation)
                              false)
             :on-drag-leave (fn [_] (swap! state assoc :drag-target nil))
             :on-drop       (fn [e]
                              (let [source-uid (.. e -dataTransfer (getData "text/plain"))]
                                (.. e preventDefault)
                                (.. e stopPropagation)
                                (swap! state assoc :dragging false)
                                (swap! state assoc :drag-target nil)
                                (when (not= source-uid uid)
                                  (dispatch [:drop-bullet source-uid uid :child]))))})
     [autosize/textarea {:value       (:atom-string @state)
                         :class       (when (= editing-uid uid) "is-editing")
                         :auto-focus  true
                         :id          (str "editable-uid-" uid)
                         :on-change   (fn [_] (db-on-change (:atom-string @state) uid))
                         :on-key-down (fn [e] (on-key-down e uid state))}]
     [parse-and-render string]
     ;; should be (when dragging-global) but this causes react to void the original component, preventing on-drag-end from firing
     (when true
       [:div.drag-n-drop (use-style (merge {:height "1px"}
                                      (when (= (:drag-target @state) :child) {:background-color "red"})))])]))


(defn page-search-el
  [_block state]
  (when (:search/page @state)
    (let [query   (:search/query @state)
          results (when (not (clojure.string/blank? query))
                    (db/search-in-node-title query))]
      (prn query)
      [dropdown {:style {:position "absolute"
                         :top      "100%"
                         :left     "-0.125em"}
                 :content (if (not query)
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
                       :drag-target false})]
    (fn [block]
      (let [{:block/keys [uid string open children]} block
            editing-uid @(subscribe [:editing/uid])
            {dragging :dragging drag-target :drag-target} @state]

        ;; xxx: bad vibes - if not editing-uid, allow ratom to be appended by joining two blocks (deleting at start)
        (when (and (not (= editing-uid uid))
                   (< (count (:atom-string @state)) (count string)))
          (swap! state assoc :atom-string string))

        [:<>

         ;; should be (when dragging-global) but this causes react to void the original component, preventing on-drag-end from firing
         ;; need surface to drag over. probably a better way to do this
         ;; FIXME drop-area-indicator styles no longer work because using a div now and document structure has changed
         (when true
           [:div.drag-n-drop (use-style (merge {:height "1px"}
                                          (when (= drag-target :container) {:background-color "red"})))])

         [:div
          (use-style (merge block-style (when dragging dragging-style))
            ;; TODO: is it possible to make this a show-tree-indicator a mergable -style map like above?
            {:class         (when (and (seq children) open) "show-tree-indicator")
             :on-drag-enter (fn [e]
                              (.. e stopPropagation)
                              (swap! state assoc :drag-target :container))
             :on-drag-over  (fn [e]
                              (.. e preventDefault)
                              (.. e stopPropagation)
                              false)
             :on-drag-leave (fn [_] (swap! state assoc :drag-target nil))
             :on-drop       (fn [e] (let [source-uid (.. e -dataTransfer (getData "text/plain"))]
                                      (.. e preventDefault)
                                      (.. e stopPropagation)
                                      (swap! state assoc :dragging false)
                                      (swap! state assoc :drag-target nil)
                                      (when (not= source-uid uid)
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
          (when (and open (seq children))
            (for [child children]
              [:div {:style {:margin-left "32px"} :key (:db/id child)}
               [block-el child]]))]]))))


(defn block-component
  [ident]
  (let [block (db/get-block-document ident)]
    [block-el block]))
