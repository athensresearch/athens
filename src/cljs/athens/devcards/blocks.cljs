(ns athens.devcards.blocks
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.router :refer [navigate-page]]
    [athens.style :refer [base-styles color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [datascript.core :as d]
    [devcards.core :refer-macros [defcard defcard-rg]]
    [garden.selectors :as selectors]
    [posh.reagent :refer [transact! posh! pull]]
    [stylefy.core :as stylefy :refer [use-style]]))

;; DATA

(defcard Instantiate-Dsdb)


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


(defonce conn (d/create-conn db/schema))
(posh! conn)
(transact! conn datoms)


;; CSS ;;

(defcard-rg Import-Styles
  [base-styles])


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


;; HELPERS ;;
(defn toggle
  [dbid open?]
  (transact! conn [{:db/id dbid :block/open (not open?)}]))


(declare block-component)


;; COMPONENTS ;;
(defn block-el
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  [block]
  (let [{:block/keys [uid string open children] dbid :db/id} block
        open?   (and (seq children) open)
        closed? (and (seq children) (not open))]
    [:div (use-style block-style)
     [:div {:style {:display "flex"}}
      (if (seq children)
        [:button (use-style block-disclosure-toggle-style {:class (cond open? "open" closed? "closed") :on-click #(toggle dbid open)})
         [:> mui-icons/KeyboardArrowDown {:style {:font-size "16px"}}]]
        [:span (use-style block-disclosure-toggle-style)])
      [:a (use-style block-indicator-style {:class (if closed? "closed" "open") :on-click #(navigate-page uid)})]
      [:span string]
      ;; TODO parse-and-render will break because it uses rfee/href
      ;;[:span (parse-and-render string)]
      ]
     (when open?
       (for [child (:block/children block)]
         [:div {:style {:margin-left "32px"} :key (:db/id child)}
          [block-el child]]))]))


(defn block-component
  "This query is long because I'm not sure how to recursively find all child blocks with all attributes
  '[* {:block/children [*]}] doesn't work
Also, why does datascript return a reaction of {:db/id nil} when pulling for [:block/uid uid]?
no results for q returns nil
no results for pull eid returns nil
  "
  [conn ident]
  (let [block (->> @(pull conn db/block-pull-pattern ident)
                   (db/sort-block))]
    [block-el block]))


(defcard-rg Block
  "Pull entity 2347, a block within Athens FAQ, and its children. Doesn't pull parents, unlike `block-page`"
  [block-component conn 2347])


(defcard-rg Block-Embed
  "TODO")


(defcard-rg Transclusion
  "TODO")
