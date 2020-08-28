(ns athens.views.blocks
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db :refer [count-linked-references-excl-uid]]
    [athens.keybindings :refer [block-key-down auto-complete-slash #_auto-complete-inline]]
    [athens.listeners :refer [multi-block-select-over multi-block-select-up]]
    [athens.parse-renderer :refer [parse-and-render pull-node-from-string]]
    [athens.parser :as parser]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES ZINDICES]]
    [athens.util :refer [now-ts gen-block-uid mouse-offset vertical-center date-string]]
    [athens.views.buttons :refer [button]]
    [athens.views.dropdown :refer [menu-style dropdown-style]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    #_[datascript.transit :as dt]
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
                     [:.block-body {:display "flex"
                                    :border-radius "0.5rem"
                                    :transition "all 0.1s ease"
                                    :position "relative"}
                      [:button.block-edit-toggle {:position "absolute"
                                                  :appearance "none"
                                                  :width "100%"
                                                  :background "none"
                                                  :border 0
                                                  :cursor "text"
                                                  :display "block"
                                                  :z-index 1
                                                  :top 0
                                                  :right 0
                                                  :bottom 0
                                                  :left 0}]
                      [:&:hover {:background (color :background-minus-1)}]]
                     ;; Darken block body when block editing, 
                     [(selectors/> :.is-editing :.block-body) {:background (color :background-minus-1)}]
                     ;; Inset child blocks
                     [:.block-container {:margin-left "2rem"}]]})


(stylefy/class "block-container" block-container-style)


(def block-disclosure-toggle-style
  {:width "1em"
   :height "2em"
   :position "relative"
   :z-index 2
   :flex-shrink "0"
   :display "flex"
   :background "none"
   :border "none"
   :transition "all 0.05s ease"
   :align-items "center"
   :justify-content "center"
   :padding "0"
   :-webkit-appearance "none"
   :color (color :body-text-color 0.4)
   ::stylefy/mode [[:hover {:color (color :link-color)}]
                   [":is(button)" {:cursor "pointer"}]]
   ::stylefy/manual [[:&.closed [:svg {:transform "rotate(-90deg)"}]]
                     [:&:empty {:pointer-events "none"}]]})


(def bullet-style
  {:flex-shrink "0"
   :position "relative"
   :z-index 2
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
   :z-index 2
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
                     ;; May want to refactor specific component styles to somewhere else.
                     ;; Closer to the component perhaps?
                     ;; Code
                     [:code :pre {:font-family "IBM Plex Mono"}]
                     ;; Media Containers
                     ;; Using a CSS hack/convention here to create a responsive container
                     ;; of a specific aspect ratio.
                     ;; TODO: Replace this with the CSS aspect-ratio property once available.
                     [:.media-16-9 {:height 0
                                    :width "calc(100% - 0.25rem)"
                                    :z-index 1
                                    :transform-origin "right center"
                                    :transition "all 0.2s ease"
                                    :padding-bottom (str (* (/ 9 16) 100) "%")
                                    :margin-block "0.25rem"
                                    :margin-inline-end "0.25rem"
                                    :position "relative"}]
                     ;; Media (YouTube embeds, map embeds, etc.)
                     [:iframe {:border 0
                               :box-shadow [["inset 0 0 0 0.125rem" (color :background-minus-1)]]
                               :position "absolute"
                               :height "100%"
                               :width "100%"
                               :cursor "default"
                               :top 0
                               :right 0
                               :left 0
                               :bottom 0
                               :border-radius "0.25rem"}]
                     ;; Images
                     [:img {:border-radius "0.25rem"
                            :max-width "calc(100% - 0.25rem)"}]
                     ;; Checkboxes
                     ;; TODO: Refactor these complicated styles into clip paths or SVGs
                     ;; or something nicer than this
                     [:input [:& (selectors/attr= :type :checkbox) {:appearance "none"
                                                                    :border-radius "0.25rem"
                                                                    :cursor "pointer"
                                                                    :color (color :link-color)
                                                                    :margin-inline-end "0.25rem"
                                                                    :position "relative"
                                                                    :top "0.13em"
                                                                    :width "1rem"
                                                                    :height "1rem"
                                                                    :transition "all 0.05s ease"
                                                                    :transform "scale(1)"
                                                                    :box-shadow "inset 0 0 0 1px"}
                              [:&:after {:content "''"
                                         :position "absolute"
                                         :top "45%" ;; How are the top and left values calculated?
                                         :left "20%" ;;
                                         :width "30%"
                                         :height "60%"
                                         :border-width "0 1.5px 1.5px 0"
                                         :border-style "solid"
                                         :opacity 0
                                         :transform "rotate(45deg) translate(-40%, -50%)"}]
                              [:&:checked {:background (color :link-color)}
                               [:&:after {:opacity 1
                                          :color (color :background-color)}]]
                              [:&:active {:transform "scale(0.9)"}]]]]})


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


(defn walk-parse-tree-for-links
  [source-str link-fn db-fn]
  (parse/transform {:page-link (fn [& title]
                                 (let [inner-title (apply + title)]
                                   ;; `apply +` can return 0 if `title` is nil or empty string
                                   (when (and (string? inner-title)
                                              (link-fn inner-title))
                                     (let [now (now-ts)
                                           uid (gen-block-uid)]
                                       (db-fn inner-title now uid)))
                                   (str "[[" inner-title "]]")))
                    :hashtag   (fn [& title]
                                 (let [inner-title (apply + title)]
                                   (when (and (string? inner-title)
                                              (link-fn inner-title))
                                     (let [now (now-ts)
                                           uid (gen-block-uid)]
                                       (db-fn inner-title now uid)))
                                   (str "#" inner-title)))} (parser/parse-to-ast source-str)))


(defn on-change
  [oldvalue value uid]
  ;; (prn "ONCHANGE" value)
  ;; TODO: move this to somewhere more comfortable using reframe dispatch
  (dispatch [:transact [{:db/id [:block/uid uid] :block/string value :edit/time (now-ts)}]])
  (walk-parse-tree-for-links
    value
    (fn [inner-title]              (nil? (db/search-exact-node-title inner-title)))
    (fn [inner-title now-time uid]
      (dispatch [:transact [{:node/title  inner-title
                             :block/uid   uid
                             :edit/time   now-time
                             :create/time now-time}]])))
  (walk-parse-tree-for-links
    oldvalue
    (fn [inner-title]
      (let [block (db/search-exact-node-title inner-title)]
        (and (not (nil? block))                                                     ;; makes sure the page link is valid
             (nil? (:block/children (db/get-block-document (:db/id block))))        ;; makes sure the page link has no children
             (zero? (count-linked-references-excl-uid inner-title uid))             ;; makes sure the page link is not present in other pages
             (not (clojure.string/includes? value inner-title)))))  ;; makes sure the page link is deleted in this node as well
    (fn [inner-title _ _]
      (let [uid (:block/uid @(pull-node-from-string inner-title))]
        (when (some? uid) (dispatch [:page/delete uid]))))))


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
                        :on-click (fn [e] (.. e stopPropagation))
                        :on-mouse-leave #(swap! state assoc :tooltip false)})
       [:div [:b "db/id"] [:span dbid]]
       [:div [:b "uid"] [:span uid]]
       [:div [:b "order"] [:span order]]
       [:div [:b "last edit"] [:span (date-string edit-time)]]])))


(defn inline-search-el
  [state]
  (let [{:search/keys [query results index type]} @state]
    [:div (merge (use-style dropdown-style)
                 {:style {:position   "absolute"
                          :top        "100%"
                          :max-height "20rem"
                          :left       "1.75em"}})
     (if (clojure.string/blank? query)
       [:div (str "Search for a " (symbol type))]
       (doall
         [:div#dropdown-menu (use-style menu-style)
          (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
            [button {:key      (str "inline-search-item" uid)
                     :id       (str "dropdown-item-" i)
                     :active   (= index i)
                     ;; TODO: pass relevant textarea values to auto-complete-inline
                     ;;#(auto-complete-inline state % (or title string))}
                     :on-click #(prn "TODO")}
             (or title string)])]))]))


(defn slash-menu-el
  [state]
  (let [{:search/keys [index results]} @state]
    [:div (merge (use-style dropdown-style) {:style {:position "absolute" :top "100%" :left "-0.125em"}})
     [:div#dropdown-menu (merge (use-style menu-style) {:style {:max-height "8em"}})
      (for [[i [text icon _expansion kbd]] (map-indexed list results)]
        [button {:key      text
                 :id       (str "dropdown-item-" i)
                 :active   (= i index)
                 ;; TODO: do not unfocus textarea
                 :on-click #(auto-complete-slash i state)}
         [:<> [(r/adapt-react-class icon)] [:span text] (when kbd [:kbd kbd])]])]]))


(defn paste
  "Clipboard data can only be accessed if user triggers JavaScript paste event.
  Uses previous keydown event to determine if shift was held, since the paste event has no knowledge of shift key.
  Cases:
  - User pastes and last keydown has shift -> default
  - User pastes and clipboard data doesn't have new lines -> default
  - User pastes without shift and clipboard data has new line characters -> PREVENT default and convert to outliner blocks"
  [e uid state]
  (let [data (.. e -clipboardData (getData "text"))
        line-breaks (re-find #"\r?\n" data)
        no-shift (-> @state :last-keydown :shift not)]
    (when (and line-breaks no-shift)
      (.. e preventDefault)
      (dispatch [:paste uid data]))))


(defn block-on-change
  [e _uid state]
  (let [{:keys [string/generated]} @state]
    (if generated
      (swap! state assoc :string/local generated :string/generated nil)
      (swap! state assoc :string/local (.. e -target -value)))))


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
       [autosize/textarea {:value         (:string/local @state)
                           :class         [(when is-editing "is-editing") "textarea"]
                           :auto-focus    true
                           :id            (str "editable-uid-" uid)
                           ;; use a combination of on-change and on-key-down. imperfect, but good enough until we rewrite keybindings
                           :on-change     (fn [e] (block-on-change e uid state))
                           :on-paste      (fn [e] (paste e uid state))
                           :on-key-down   (fn [e] (block-key-down e uid state))
                           ;; TODO: allow user to select multiple times while holding shift
                           ;; FIXME: always unselects on mouse up
                           :on-mouse-down (fn [_]
                                            (events/listen js/window EventType.MOUSEOVER multi-block-select-over)
                                            (events/listen js/window EventType.MOUSEUP multi-block-select-up))
                           :on-click      (fn [e]
                                            (let [source-uid @(subscribe [:editing/uid])]
                                              ;; if shift key is held when user clicks across multiple blocks, select the blocks
                                              (when (and source-uid uid (not= source-uid uid) (.. e -shiftKey))
                                                (let [target (.. e -target)
                                                      page (or (.. target (closest ".node-page")) (.. target (closest ".block-page")))
                                                      target-block (.. target (closest ".block-container"))
                                                      blocks (vec (array-seq (.. page (querySelectorAll ".block-container"))))
                                                      [start end] (-> (keep-indexed (fn [i el]
                                                                                      (when (or (= el target-block)
                                                                                                (= source-uid (.. el -dataset -uid)))
                                                                                        i))
                                                                                    blocks))]
                                                  (when (and start end)
                                                    (let [selected-blocks (subvec blocks start (inc end))
                                                          selected-uids (mapv #(.. % -dataset -uid) selected-blocks)]
                                                      (dispatch [:editing/uid nil])
                                                      (dispatch [:selected/add-items selected-uids])))))))}]
       [parse-and-render string uid]
       [:div (use-style (merge drop-area-indicator (when (= :child (:drag-target @state)) {:opacity 1})))]])))


(defn bullet-el
  [_ _]
  (fn [block state]
    (let [{:block/keys [uid children open]} block
          {:context-menu/keys [show x y]} @state]

      [:<>
       (when show
         [:div (merge (use-style dropdown-style)
                      {:style {:position "fixed"
                               :x        (str x "px")
                               :y        (str y "px")}})
          [:div (use-style menu-style)
           ;; TODO: create listener that lets user exit context menu if click outside
           [button {:on-click (fn [_]
                                (let [selected-items @(subscribe [:selected/items])
                                      ;; use this when using datascript-transit
                                      ;uids (map (fn [x] [:block/uid x]) selected-items)
                                      ;blocks (d/pull-many @db/dsdb '[*] ids)
                                      data (cond
                                             (= show :one) (str "((" uid "))")
                                             (= show :many) (->> (map (fn [uid] (str "((" uid "))\n")) selected-items)
                                                                 (str/join "")))]
                                  (.. js/navigator -clipboard (writeText data))
                                  (swap! state assoc :context-menu/show false)))}
                                  ; TODO: unable to copy with roam/data as data type. leaving this scrap here until return to this problem
                                  ;(= show :many) (dt/write-transit-str
                                  ;                 {:db-id       nil ;; roam has a value for this
                                  ;                  :type        :copy ;; or :cut
                                  ;                  :copied-data block-refs}))]
                                  ;(let [blob (js/Blob. [dt-data] (clj->js {"type" "roam/data"}))
                                  ;      item (js/ClipboardItem. (clj->js {"roam/data" blob}))]
                                  ;  (.then (.. js/navigator -clipboard (write [item]))
                                  ;         #(js/console.log "suc" %)
                                  ;         #(js/console.log "fail" %)))))}



            (cond
              (= show :one) "Copy block ref"
              (= show :many) "Copy block refs")]]])
       [:span (use-style bullet-style
                         {:class           [(when (and (seq children) (not open))
                                              "closed-with-children")]
                          :on-mouse-over   #(swap! state assoc :tooltip true)
                          :on-mouse-out    (fn [e]
                                             (let [related (.. e -relatedTarget)]
                                               (when-not (and related (contains related "tooltip"))
                                                 (swap! state assoc :tooltip false))))
                          :on-click        (fn [e] (navigate-uid uid e))
                          :draggable       true
                          :on-context-menu (fn [e]
                                             (.. e preventDefault)
                                             (let [selected-blocks @(subscribe [:selected/items])
                                                   rect (.. e -target getBoundingClientRect)
                                                   new-context-menu-state (merge {:context-menu/x    (.. rect -left)
                                                                                  :context-menu/y    (.. rect -bottom)
                                                                                  :context-menu/show (if (empty? selected-blocks)
                                                                                                       :one
                                                                                                       :many)})]
                                               (if (empty? selected-blocks)
                                                 (swap! state merge new-context-menu-state)
                                                 (swap! state merge new-context-menu-state))))
                          :on-drag-start   (fn [e]
                                             (set! (.. e -dataTransfer -effectAllowed) "move")
                                             (.. e -dataTransfer (setData "text/plain" uid))
                                             (swap! state assoc :dragging true))
                          :on-drag-end     (fn [_]
                                             ;; FIXME: not always called
                                             ;         (prn "DRAG END BULLET")
                                             (swap! state assoc :dragging false))})]])))


;;TODO: more clarity on open? and closed? predicates, why we use `cond` in one case and `if` in another case)
(defn block-el
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  [block]
  (let [state (r/atom {:string/local      (:block/string block)
                       :string/generated  nil
                       :string/previous   (:block/string block) ;; this is for detecting what's deleted to process page deletion
                       :search/type       nil ;; one of #{:page :block :slash}
                       :search/results    nil
                       :search/query      nil
                       :search/index      nil
                       :dragging          false
                       :drag-target       nil
                       :edit/time         (:edit/time block)
                       :last-keydown      nil
                       :context-menu/x    nil
                       :context-menu/y    nil
                       :context-menu/show false})]

    ;; If generated string is updated, automatically update local string
    ;; Necessary because modifying generated string itself won't trigger the on-change event of the textarea
    ;; local string must be modified to trigger new value of generated string
    (add-watch state :generated-string-listener
               (fn [_context _atom old new]
                 (when (and (not= (:string/generated old) (:string/generated new))
                            (not (nil? (:string/generated new))))
                   (swap! state assoc :string/local (:string/generated new)))))

    (add-watch state :local-string-listener
               (fn [_context _atom old new]
                 (let [{:block/keys [uid]} block]
                   (when (not= (:string/local old) (:string/local new))
                     (db-on-change (:string/previous old) (:string/local new) uid)))))

    (fn [block]
      (let [{:block/keys [uid string open children] edit-time :edit/time} block
            {:search/keys [type] :keys [dragging drag-target] state-edit-time :edit/time} @state
            is-editing @(subscribe [:editing/is-editing uid])
            is-selected @(subscribe [:selected/is-selected uid])]

        ;;(prn uid is-selected)

        ;; if block is updated in datascript, update local block state
        (when (< state-edit-time edit-time)
          (let [new-state {:edit/time edit-time :string/local string :string/previous string}]
            (swap! state merge new-state)))

        [:div
         {:class         ["block-container"
                          (when dragging "dragging")
                          (when is-editing "is-editing")
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

         [:div.block-body
          [:button.block-edit-toggle
           {:on-click (fn [e]
                        (when (false? (.. e -shiftKey))
                          (dispatch [:editing/uid uid])))}]

          [toggle-el block]
          [bullet-el block state]
          [tooltip-el block state]
          [block-content-el block state is-editing]]

         (cond
           (or (= type :page) (= type :block)) [inline-search-el state]
           (= type :slash) [slash-menu-el state])

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
