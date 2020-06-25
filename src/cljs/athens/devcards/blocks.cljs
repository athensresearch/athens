(ns athens.devcards.blocks
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color OPACITIES DEPTH-SHADOWS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [transact! pull]]
    [re-frame.core :as rf]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      KeyCodes)))


(def datoms
  [{:db/id          2381
    :block/uid      "OaSVyM_nr"
    :block/open     true
    :node/title     "Athens FAQ"
    :block/children [{:db/id          2158
                      :block/uid      "BjIm6GeRP"
                      :block/string   "Why open-source?"
                      :block/open     true
                      :block/order    3
                      :block/children [{:db/id        2163
                                        :block/uid    "GNaf3XzpE"
                                        :block/string "The short answer is the security and privacy of your data."
                                        :block/open   true
                                        :block/order  1}
                                       {:db/id          2347
                                        :block/uid      "jbiKpcmIX"
                                        :block/string   "Firstly, I wouldn't be surprised if Roam was eventually open-sourced."
                                        :block/open     true
                                        :block/order    0
                                        :block/children [{:db/id        2176
                                                          :block/uid    "gVINXaN8Y"
                                                          :block/string "Suffice it to say that Roam being open-source is undeniably something that the team has already considered. Why is it not open-source already? You'd have to ask the Roam team, but Roam, a business, is not obligated to open-source anything."
                                                          :block/open   true
                                                          :block/order  2}
                                                         {:db/id          2346
                                                          :block/uid      "ZOxwo0K_7"
                                                          :block/string   "The conclusion of the [[Roam White Paper]] states that Roam's vision is a collective, \"open-source\" intelligence."
                                                          :block/open     true
                                                          :block/order    0
                                                          :block/children [{:db/id        2174
                                                                            :block/uid    "WKWPPSYQa"
                                                                            :block/string "((ZOxwo0K_7))"
                                                                            :block/open   true
                                                                            :block/order  0}]}
                                                         {:db/id        2349
                                                          :block/uid    "VQ-ybRmNh"
                                                          :block/string "In the Roam Slack, I recall Conor saying one eventual goal is to work on a protocol that affords interoperability between open source alternatives. I would share the message but can't find it because of Slack's 10k message limit."
                                                          :block/open   true
                                                          :block/order  1}
                                                         {:db/id        2351
                                                          :block/uid    "PGGS8MFH_"
                                                          :block/string "Ultimately, we don't know when/if Roam will be open-sourced, but it's possible that Athens could accelerate or catalyze this. Regardless, there will always be some who are open-source maximalists and some who want to self-host, because that's probably really the most secure thing you can do (if you know what you're doing)."
                                                          :block/open   true
                                                          :block/order  3}]}]}]}])


(transact! db/dsdb datoms)

;;; Styles


(def block-style
  {:display "flex"
   :line-height "32px"
   :justify-content "flex-start"
   :flex-direction "column"})


(def block-disclosure-toggle-style
  {:width "16px"
   :height "32px"
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
   :width "12px"
   :margin-right "4px"
   :transition "all 0.05s ease"
   :height "32px"
   :color (color :panel-color)
   ::stylefy/mode [[:after {:content "''"
                            :background "currentColor"
                            :transition "all 0.05s ease"
                            :border-radius "100px"
                            :box-shadow "0 0 0 2px transparent"
                            :display "inline-flex"
                            :margin "50% 0 0 50%"
                            :transform "translate(-50%, -50%)"
                            :height "5px"
                            :width "5px"}]
                   [:hover {:color (color :link-color)}]]
                  ;;  [:before {:content "''"
                  ;;            :position "absolute"
                  ;;            :top "24px"
                  ;;            :bottom "0"
                  ;;            :pointer-events "none"
                  ;;            :left "22px"
                  ;;            :width "1px"
                  ;;            :background (color :panel-color)}]

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
                      :.isEditing {:outline "none"
                                   :z-index "10"
                                   :display "block"
                                   :opacity "1"}]
                     [:span [:span
                             :a {:position "relative"
                                 :z-index "2"}]]]})


(def tooltip-style
  {:z-index    1 :position "absolute" :left "-200px"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :display    "flex" :flex-direction "column"
   :background-color "white"
   :padding "5px 10px"
   :border-radius "4px"})


;;; Components


(declare block-component block-el toggle on-key-down)


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


;; TODO: more clarity on open? and closed? predicates, why we use `cond` in one case and `if` in another case
(defn block-el
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  [{:block/keys [uid string open order children]}]
  (let [open?       (and (seq children) open)
        closed?     (and (seq children) (not open))
        editing-uid @(rf/subscribe [:editing-uid])
        tooltip-uid @(rf/subscribe [:tooltip-uid])
        {:keys        [x y]
         dragging-uid :uid
         closest-uid  :closest/uid
         closest-kind :closest/kind} @(rf/subscribe [:drag-bullet])]

    [:div (merge (use-style block-style
                            {:class    "block-container"
                             :data-uid uid}))
     [:div {:style {:display "flex"}}

      ;; Toggle
      (if (seq children)
        [:button (use-style block-disclosure-toggle-style
                            {:class (cond open? "open" closed? "closed")
                             :on-click #(toggle [:block/uid uid] open)})
         [:> mui-icons/KeyboardArrowDown {:style {:font-size "16px"}}]]
        [:span (use-style block-disclosure-toggle-style)])

      ;; Bullet
      (if (= dragging-uid uid)
        [:span (merge (use-style block-indicator-style
                                 {:class    (clojure.string/join " " ["bullet" "dragging" (if closed? "closed" "open")])
                                  :data-uid uid})
                      {:style {:transform (str "translate(" x "px, " y "px)")}})]

        [:span (use-style block-indicator-style
                          {:class    (str "bullet " (if closed? "closed" "open"))
                           :data-uid uid
                           :on-click #(navigate-uid uid)})])

      ;; Tooltip
      (when (and (= tooltip-uid uid)
                 (not dragging-uid))
        [:div (use-style tooltip-style {:class "tooltip"})
         [:span [:b "uid: "] uid]
         [:span [:b "order: "] order]
         (when children
           [:<>
            [:span [:b "children: "]]
            (for [ch children]
              (let [{:block/keys [uid order]} ch]
                [:span {:style {:margin-left "20px"} :key uid}
                 [:b "order: "] [:span order]
                 [:span " | "]
                 [:b "uid: "] [:span uid]]))])])

      ;; Actual Contents
      [:div (use-style (merge block-content-style {:width       "100%"
                                                   :user-select (when dragging-uid "none")})
                       {:class    "block-contents"
                        :data-uid uid})
         [autosize/textarea {:value       string
                             :class       (when (= editing-uid uid) "isEditing")
                             :auto-focus  true
                             :on-change   (fn [e]
                                            ;;(prn (.. e -target -value)))
                                            (transact! db/dsdb [[:db/add [:block/uid uid] :block/string (.. e -target -value)]]))
                             :on-key-down (fn [e] (on-key-down e [:block/uid uid] order))}]
         [parse-and-render string]

       ;; Drop Indicator
       (when (and (= closest-uid uid)
                  (= closest-kind :child))
         [:span (use-style drop-area-indicator)])]]

     ;; Children
     (when open?
       (for [child children]
         [:div {:style {:margin-left "32px"} :key (:db/id child)}
          [block-el child]]))

     (when (and (= closest-uid uid) (= closest-kind :sibling))
       [:span (use-style drop-area-indicator)])]))

;; Helpers

(defn toggle
  [ident open]
  (transact! db/dsdb [{:db/id ident :block/open (not open)}]))


(defn on-key-down
  [e ident order]
  (let [key             (.. e -keyCode)
        val             (.. e -target -value)
        selection-start (.. e -target -selectionStart)]
    (prn "KEYDOWN" selection-start (subs val selection-start) key ident order KeyCodes.ENTER)
    (cond
      ;;(= key KeyCodes.ENTER)
      ;;(transact! db/dsdb
      ;;  ;; FIXME original block doesn't update. textarea and `on-change` prevents update
      ;;           [;;{:db/id ident
      ;;   ;; :block/string (subs val 0 selection-start)}
      ;;            {;; random-uuid generates length 36 id. Roam uids are 9
      ;;             :block/uid       (subs (str (random-uuid)) 27)
      ;;             :block/string    (subs val selection-start)
      ;;    ;; FIXME makes current block the parent
      ;;             :block/_children ident
      ;;    ;; FIXME. order is dependent on parent
      ;;             :block/order     (inc order)
      ;;             :block/open      true}])

      :else nil)))

;;; Devcards


(defcard-rg Block
  "Pull entity 2347, a block within Athens FAQ, and its children. Doesn't pull parents, unlike `block-page`"
  [block-component 2347])
