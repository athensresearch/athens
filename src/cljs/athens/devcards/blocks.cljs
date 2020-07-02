(ns athens.devcards.blocks
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.devcards.dropdown :refer [slash-menu-component context-menu-component]]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :refer [join]]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [goog.functions :refer [debounce]]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [pull]]
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
   :flex-direction "column"})


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


(def block-indicator-style
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

   ::stylefy/manual [[:&.open {}]
                     [:&.closed {}]
                     [:&.closed [(selectors/& (selectors/after)) {:box-shadow (str "0 0 0 2px " (color :body-text-color))
                                                                  :opacity (:opacity-med OPACITIES)}]]
                     [:&.closed [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&.closed [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&:hover:after {:transform "translate(-50%, -50%) scale(1.3)"}]
                     [:&.dragging {:z-index "1000"
                                   :cursor "grabbing"
                                   :color (color :body-text-color)}]
                     [:&.selected {}]]})


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
   :color (color :body-text-color)
   :position "relative"
   :transform-origin "left"
   :z-index "1000"
   :width "100%"
   :animation "drop-area-appear .5s ease"
   ::stylefy/manual [[:&:after {:position "absolute"
                                :content "''"
                                :top "-0.5px"
                                :right "0"
                                :bottom "-0.5px"
                                :left "0"
                                :border-radius "100px"
                                :animation "drop-area-color-pulse 1s ease infinite alternate"
                                :background "currentColor"}]]})


(def block-content-style
  {:position "relative"
   :overflow "visible"
   :z-index "1"
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


;; Helpers

(defn fast-on-change
  [value _uid state]
  (swap! state assoc :atom-string value))


(defn on-change
  [value uid state]
  (dispatch [:transact-event [[:db/add [:block/uid uid] :block/string value]]])
  (fast-on-change value uid state))


(def db-on-change (debounce on-change 500))


(defn toggle
  [id open]
  (dispatch [:transact-event [[:db/add id :block/open (not open)]]]))


(defn on-key-down
  [e uid state]
  (let [key       (.. e -keyCode)
        shift     (.. e -shiftKey)
        val       (.. e -target -value)
        sel-start (.. e -target -selectionStart)]
    (cond
      (and shift (= key KeyCodes.TAB)) (dispatch [:unindent uid])
      (= key KeyCodes.TAB) (dispatch [:indent uid])
      (and shift (= key KeyCodes.ENTER)) (prn "start new line in textarea")
      (= key KeyCodes.ENTER) (dispatch [:enter uid val sel-start])
      (= key KeyCodes.SLASH) (swap! state update :slash? not)
      (and (= key KeyCodes.BACKSPACE) (zero? sel-start)) (dispatch [:backspace uid]))))


;;; Components


;; TODO: more clarity on open? and closed? predicates, why we use `cond` in one case and `if` in another case
(defn block-el
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  [block]
  (let [state (r/atom {:atom-string (:block/string block)
                       :slash? false
                       :context-menu? false})]
    (fn [block]
      (let [{:block/keys [uid string open order children] dbid :db/id} block
            open?       (and (seq children) open)
            closed?     (and (seq children) (not open))
            editing-uid @(subscribe [:editing-uid])
            tooltip-uid @(subscribe [:tooltip-uid])
            {:keys        [x y]
             dragging-uid :uid
             closest-uid  :closest/uid
             closest-kind :closest/kind} @(subscribe [:drag-bullet])
            {:keys [atom-string slash? context-menu?]} @state]

        [:div (use-style block-style
                         {:class    (join " " ["block-container" (when (= dragging-uid uid) "dragging")])
                          :data-uid uid})
         [:div {:style {:display "flex"}}

          ;; Toggle
          (if (seq children)
            [:button (use-style block-disclosure-toggle-style
                                {:class    (cond open? "open" closed? "closed")
                                 :on-click #(toggle [:block/uid uid] open)})
             [:> mui-icons/KeyboardArrowDown {:style {:font-size "16px"}}]]
            [:span (use-style block-disclosure-toggle-style)])

          ;; Bullet
          (if (= dragging-uid uid)
            [:span (merge (use-style block-indicator-style
                                     {:class    (join " " ["bullet" "dragging" (if closed? "closed" "open")])
                                      :data-uid uid})
                          {:style {:transform (str "translate(" x "px, " y "px)")}})]

            [:span (use-style block-indicator-style
                              {:class    (str "bullet " (if closed? "closed" "open"))
                               :data-uid uid
                               :on-click #(navigate-uid uid)
                               :on-context-menu (fn [e]
                                                  (.. e preventDefault)
                                                  (swap! state update :context-menu? not))})])

          ;; Tooltip
          (when (and (= tooltip-uid uid) (not dragging-uid))
            [:div (use-style tooltip-style {:class "tooltip"})
             [:div [:b "db/id"] [:span dbid]]
             [:div [:b "uid"] [:span uid]]
             [:div [:b "order"] [:span order]]])

          ;; Context Menu
          (when context-menu?
            [context-menu-component])

          ;; Actual Contents
          [:div (use-style (merge block-content-style {:user-select (when dragging-uid "none")})
                           {:class    "block-contents"
                            :data-uid uid})
           [autosize/textarea {:value       atom-string
                               :class       (when (= editing-uid uid) "is-editing")
                               :auto-focus  true
                               :on-change   (fn [e]
                                              (let [value (.. e -target -value)]
                                                (fast-on-change value uid state)
                                                (db-on-change value uid state)))
                               :on-key-down (fn [e] (on-key-down e uid state))}]
           [parse-and-render string]

           ;; Drop Indicator
           (when (and (= closest-uid uid) (= closest-kind :child))
             [:span (use-style drop-area-indicator)])]]

         ;; Children
         (when open?
           (for [child children]
             [:div {:style {:margin-left "32px"} :key (:db/id child)}
              [block-el child]]))

         ;; Drop Indicator
         (when (and (= closest-uid uid) (= closest-kind :sibling))
           [:span (use-style drop-area-indicator)])

         (when slash?
           [slash-menu-component])]))))


(defn block-component
  "This query is long because I'm not sure how to recursively find all child blocks with all attributes
  '[* {:block/children [*]}] doesn't work
Also, why does datascript return a reaction of {:db/id nil} when pulling for [:block/uid uid]?
no results for q returns nil
no results for pull eid returns nil
  "
  [ident]
  (let [block (->> @(pull db/dsdb db/block-pull-pattern ident)
                   (db/sort-block))]
    [block-el block]))


;;; Devcards


(defcard-rg Block
  "Pull entity 2347, a block within Athens FAQ, and its children. Doesn't pull parents, unlike `block-page`"
  [block-component 2347])
