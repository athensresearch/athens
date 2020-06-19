(ns athens.devcards.blocks
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.router :refer [navigate-page]]
    [athens.style :refer [base-styles color OPACITIES DEPTH-SHADOWS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [goog.events :as events]
    [komponentit.autosize :as autosize]
    [posh.reagent :refer [transact! posh! pull]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import [goog.events EventType KeyCodes]))



(rf/dispatch [:init-rfdb])


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
                                                                            :block/string "((iWmBJaChO))"
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
   :position "relative"
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
                   [:hover {:color (color :link-color)}]
                   [:before {:content "''"
                             :position "absolute"
                             :top "24px"
                             :bottom "0"
                             :pointer-events "none"
                             :left "22px"
                             :width "1px"
                             :background (color :panel-color)}]]
   ::stylefy/manual [[:&.open {}]
                     [:&.closed {}]
                     [:&.closed [(selectors/& (selectors/after)) {:box-shadow (str "0 0 0 2px " (color :body-text-color))
                                                                  :opacity (:opacity-med OPACITIES)}]]
                     [:&.closed [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&.closed [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&.selected {}]]})


(def tooltip-style
  {:z-index    1 :position "absolute" :left "-200px"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :display    "flex" :flex-direction "column"
   :background-color "white"
   :padding "5px 10px"
   :border-radius "4px"})

;;; Components

(defn toggle
  [dbid open]
  (transact! db/dsdb [{:db/id dbid :block/open (not open)}]))


(declare block-component)

(defn on-key-down [e dbid order]
  (let [key             (.. e -keyCode)
        val             (.. e -target -value)
        selection-start (.. e -target -selectionStart)]
    ;;(prn "KEYDOWN" selection-start (subs val selection-start))
    (cond
      (= key KeyCodes.ENTER)
      (transact! db/dsdb
        ;; FIXME original block doesn't update. textarea and `on-change` prevents update
        [
         ;;{:db/id dbid
         ;; :block/string (subs val 0 selection-start)}
         {;; random-uuid generates length 36 id. Roam uids are 9
          :block/uid       (subs (str (random-uuid)) 27)
          :block/string    (subs val selection-start)
          ;; FIXME makes current block the parent
          :block/_children dbid
          ;; FIXME. order is dependent on parent
          :block/order     (inc order)
          :block/open      true}])

      :else nil)))

;; TODO: more clarity on open? and closed? predicates, why we use `cond` in one case and `if` in another case
(defn block-el
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  [block]
  (fn [block]
    (let [{:block/keys [uid string open order children] dbid :db/id} block
          open?       (and (seq children) open)
          closed?     (and (seq children) (not open))
          editing-uid @(rf/subscribe [:editing-uid])
          tooltip-uid @(rf/subscribe [:tooltip-uid])
          {:keys [x y]
           dragging-uid :uid
           closest-uid :closest/uid
           closest-class :closest/class} @(rf/subscribe [:dragging])]

      [:div (merge (use-style block-style
                     {:class "block-container"
                      :data-uid uid})
              {:style {:border-bottom (when (and (= closest-uid uid)
                                              (= closest-class "block-container")) "5px solid black")}})
       [:div {:style {:display "flex"}}

        ;; toggle
        (if (seq children)
          [:button (use-style block-disclosure-toggle-style {:class (cond open? "open" closed? "closed") :on-click #(toggle dbid open)})
           [:> mui-icons/KeyboardArrowDown {:style {:font-size "16px"}}]]
          [:span (use-style block-disclosure-toggle-style)])

        ;; bullet
        (if (= dragging-uid uid)
          [:span (merge (use-style block-indicator-style
                          {:class    (str "bullet " (if closed? "closed" "open"))
                           :data-uid uid})
                   {:style {:left x :top y}})]

          [:span (use-style block-indicator-style
                   {:class    (str "bullet " (if closed? "closed" "open"))
                    :data-uid uid
                    :on-click #(navigate-page uid)})
           ])

        (if (= tooltip-uid uid)
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
                   [:b "uid: "] [:span uid]
                   ]))])
           ])

        ;;(prn (= tooltip-uid uid) tooltip-uid uid)

        ;; contents — actual text
        [:div {:class    "block-contents"
               :data-uid uid
               :style    {:width         "100%"
                          :user-select   (when dragging-uid "none")
                          :border-bottom (when (and (= closest-uid uid)
                                                 (= closest-class "block-contents")) "5px solid black")}}
         (if (= editing-uid uid)
           [autosize/textarea {:value       string
                               :style       {:width "100%"}
                               :auto-focus  true
                               :on-change   (fn [e]
                                              ;;(prn (.. e -target -value))
                                              (transact! db/dsdb [[:db/add dbid :block/string (.. e -target -value)]]))
                               :on-key-down (fn [e] (on-key-down e dbid order))

                               }]
           [parse-and-render string])]]

       ;; children
       (when open?
         (for [child (:block/children block)]
           [:div {:style {:margin-left "32px"} :key (:db/id child)}
            [block-el child]]))])))


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


(defn get-client-rect [evt]
  (let [r (.getBoundingClientRect (.-target evt))]
    {:left (.-left r), :top (.-top r)}))

(defn mouse-move-handler [offset uid]
  (fn [evt]
    (let [x (- (.-clientX evt) (:x offset))
          y (- (.-clientY evt) (:y offset))
          closest-sibling (.. (js/document.elementFromPoint (.-clientX evt) (.-clientY evt)) (closest ".block-container"))
          closest-child (.. (js/document.elementFromPoint (.-clientX evt) (.-clientY evt)) (closest ".block-contents"))
          closest (or closest-child closest-sibling)
          closest-uid (when closest (.. closest -dataset -uid))
          closest-class (when closest
                          (if (some #(= "block-container" %) (array-seq (.. closest -classList)))
                            "block-container"
                            "block-contents"))]
      ;;(prn closest-uid closest-class)
      (rf/dispatch [:dragging {:x x :y y
                               :uid uid
                               :closest/uid closest-uid
                               :closest/class closest-class}]))))

(defn mouse-up-handler [on-move]
  (fn me [_evt]
    (let [dragging @(rf/subscribe [:dragging])]
      (prn "mouse up: " dragging)                           ; uncomment to see state of last drag
      (rf/dispatch [:dragging {}])
      (.. (js/document.getSelection) empty)
      (events/unlisten js/window EventType.MOUSEMOVE on-move))))

(defn container []
  ;; when user clicks within a block-contents, pass the uid of the closest ancestor
  (events/listen js/window EventType.MOUSEDOWN
    (fn [e]
      (let [closest (.. e -target (closest ".block-contents"))]
        (when closest
          (rf/dispatch [:editing-uid (.. closest -dataset -uid)])))))

  ;; when a user clicks on a bullet, begin listening to mousemove
  (events/listen js/window EventType.MOUSEDOWN
    (fn [e]
      (let [class-list (-> (.. e -target -classList) array-seq)]
        (when (some #(= "bullet" %) class-list)
          (let [{:keys [left top]} (get-client-rect e)
                offset             {:x (- (.-clientX e) left)
                                    :y (- (.-clientY e) top)}
                uid                (.. e -target -dataset -uid)
                on-move            (mouse-move-handler offset uid)]
            (events/listen js/window EventType.MOUSEMOVE on-move)
            (events/listen js/window EventType.MOUSEUP (mouse-up-handler on-move)))))))

  (events/listen js/window EventType.MOUSEOVER
    (fn [e]
      (let [class-list (array-seq (.. e -target -classList))
            closest (.. e -target (closest ".tooltip"))
            uid (.. e -target -dataset -uid)]
        (cond
          (some #(= "bullet" %) class-list) (rf/dispatch [:tooltip-uid uid])
          closest nil
          :else (rf/dispatch [:tooltip-uid nil])))))

  [block-component 2347])



;;; Devcards


(defcard-rg Import-Styles
  [base-styles])


(defcard-rg Block
  "Pull entity 2347, a block within Athens FAQ, and its children. Doesn't pull parents, unlike `block-page`"
  [container])


(defcard-rg Block-Embed "TODO")


(defcard-rg Transclusion "TODO")
