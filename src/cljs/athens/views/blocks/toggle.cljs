(ns athens.views.blocks.toggle
  (:require
    ["@material-ui/icons/KeyboardArrowDown" :default KeyboardArrowDown]
    [athens.style :as style]
    [re-frame.core :as rf]
    [stylefy.core :as stylefy]))


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
   :cursor "pointer"
   :padding "0"
   :-webkit-appearance "none"
   :color (style/color :body-text-color :opacity-med)
   ::stylefy/manual [[:&:hover {:color (style/color :link-color)}]
                     [:&:before {:content "''"
                                 :inset "0.25rem -0.125rem"
                                 :z-index -1
                                 :position "absolute"
                                 :transition "opacity 0.1s ease"
                                 :border-radius "0.25rem"
                                 :box-shadow (:4 style/DEPTH-SHADOWS)
                                 :opacity 0
                                 :background (style/color :background-plus-2)}]
                     [:&:hover:before
                      :&:focus-visible:before {:opacity 1}]
                     [:&.closed [:svg {:transform "rotate(-90deg)"}]]
                     [:&:empty {:pointer-events "none"}]]})


(defn toggle
  [id open]
  (rf/dispatch [:transact [[:db/add id :block/open (not open)]]]))


(defn toggle-el
  [{:block/keys [open uid children]} state linked-ref]
  (if (seq children)
    [:button (stylefy/use-style block-disclosure-toggle-style
                                {:class    (if (or (and (true? linked-ref) (:linked-ref/open @state))
                                                   (and (false? linked-ref) open))
                                             "open"
                                             "closed")
                                 :tab-index 0
                                 :on-click (fn [_]
                                             (if (true? linked-ref)
                                               (swap! state update :linked-ref/open not)
                                               (toggle [:block/uid uid] open)))})
     [:> KeyboardArrowDown {:style {:font-size "16px"}}]]
    [:span (stylefy/use-style block-disclosure-toggle-style)]))

