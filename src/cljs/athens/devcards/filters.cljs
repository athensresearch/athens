(ns athens.devcards.filters
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.devcards.buttons :refer [button]]
    [athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    #_[re-frame.core :as re-frame :refer [dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style #_use-sub-style]]))


;;; Styles


(def container-style
  {:flex-basis "30em"
   :display "flex"
   :flex-direction "column"})

;; TODO: move to new Popover component as Title prop
(def title-style
  {:text-align "center"
   :opacity (:opacity-high OPACITIES)})


;; TODO: Replace with styled Input component
(def search-style
  {:width "100%"
   :display "flex"})


(def controls-style
  {:width "100%"
   :display "flex"
   :flex "0 0 auto"
   :font-size "12px"
   :align-items "center"
   :text-align "right"
   :border-bottom (str "1px solid " (color :panel-color))
   :margin "4px 0"
   :padding-bottom "4px"
   :justify-content "space-between"
   :font-weight "500"
   :color (color :body-text-color :opacity-high)
   ::stylefy/manual [[:svg {:font-size "20px"}]]})


(def sort-control-style {:padding "4px 6px"
                         ::stylefy/manual [[:&:hover :&:focus [:& [:+ [:span {:opacity 1}]]]]]})


(def reset-control-style {:margin-left "0.5em"})


(def sort-indicator-style {:margin-right "auto"
                           :transition "all 0.2s ease"
                           :opacity 0
                           :display "flex"
                           :flex-direction "row"
                           :align-items "center"
                           :margin-left "0.5em"})


(def filter-list-style
  {:align-self "stretch"
   :display "flex"
   :flex "1 1 100%"
   :overflow-y "auto"
   :flex-direction "column"})


(def filter-style
  {:width           "100%"
   :display         "flex"
   :justify-content "space-between"
   :padding         "2px 8px"
   :align-items     "center"
   :border-radius   "4px"
   :margin-block-end "1px"
   :user-select     "none"
   :transition      "all 0.1s ease"
   ::stylefy/manual [[:&:hover {:background (color :panel-color :opacity-med)}]
                     [:&:active {:transform "scale(0.99)"}]]})


(def added-style
  {:background-color (color :link-color :opacity-low)
   :color (color :link-color)
   ::stylefy/manual [[:&:hover {:background (color :link-color 0.3)}]
                     [:&:active {:transform "scale(0.99)"}]]})


(def excluded-style
  {:background-color (color :warning-color :opacity-low)
   :color (color :warning-color)
   ::stylefy/manual [[:&:hover {:background (color :warning-color 0.3)}]
                     [:&:active {:transform "scale(0.99)"}]]})


(def count-style
  {:padding "0 1em 0 0"
   :color (color :body-text-color)
   :font-weight "bold"
   :font-size "11px"
   :text-align "right"
   :flex "0 0 3em"})


(def filter-name-style
  {:flex "1 1 100%"
   :color (color :body-text-color)
   :text-align "left"})


(def state-style
  {:font-weight "bold"
   :flex "0 0 auto"
   :font-size "12px"
   :display "flex"
   :align-items "center"
   :letter-spacing "0.1em"
   :text-transform "uppercase"
   :margin-right "0.2em"
   ::stylefy/manual [[:svg {:margin-left "0.2em"
                            :margin-right "0.2em"
                            :font-size "18px"}]]})


;;; Utilities


(def items
  {"Amet"   {:count 6 :state :added}
   "At"     {:count 130 :state :excluded}
   "Diam"   {:count 6}
   "Donec"  {:count 6}
   "Elit"   {:count 30}
   "Elitudomin mesucen defibocutruon"  {:count 1}
   "Erat"   {:count 11}
   "Est"    {:count 2}
   "Eu"     {:count 2}
   "Ipsum"  {:count 2 :state :excluded}
   "Magnis" {:count 10 :state :added}
   "Metus"  {:count 29}
   "Mi"     {:count 7 :state :added}
   "Quam"   {:count 1}
   "Turpis" {:count 97}
   "Vitae"  {:count 1}})


;;; Components


(defn filters-el
  [_uid items]
  (let [s (r/atom {:sort :lex
                   :items items
                   :search ""})]
    (fn [_uid items]
      (let [sort_ (:sort @s)
            filtered-items (reduce-kv
                             (fn [m k v]
                               (if (re-find
                                     (re-pattern (str "(?i)" (:search @s)))
                                     k)
                                 (assoc m k v)
                                 m))
                             {}
                             (:items @s))
            items (if (= sort_ :lex)
                    (into (sorted-map) filtered-items)
                    (into (sorted-map-by (fn [k1 k2]
                                           (compare
                                            [(get-in items [k2 :count]) k1]
                                            [(get-in items [k1 :count]) k2]))) filtered-items))
            num-filters (count (filter
                                (fn [[_k v]] (:state v))
                                items))]

        [:div (use-style container-style)
         [:h5 (use-style title-style) "Filter"]

         ;; Search
         [:input (use-style search-style
                            {:type        "search"
                             :auto-focus  true
                             :placeholder "Add or remove filters"
                             :value (:search @s)
                             :on-change   (fn [e]
                                            (swap! s assoc-in [:search] (.. e -target -value)))})]

         ;; Controls
         [:div (use-style controls-style)
          [button {:label       [:> mui-icons/Sort]
                   :style sort-control-style
                   :on-click-fn (fn [_]
                                  (swap! s assoc :sort (if (= sort_ :lex)
                                                         :count
                                                         :lex)))}]
           [:span (use-style sort-indicator-style) [:<> [:> mui-icons/ArrowDownward] (if (= sort_ :lex) "Title" "Number")]]
           [:span (str num-filters " Filters")]
           [button {:label "Reset"
                    :style reset-control-style
                    :on-click-fn (fn [_]
                                   (swap! s assoc :items
                                          (reduce-kv
                                           (fn [m k v]
                                             (assoc m k (dissoc v :state)))
                                           {}
                                           (:items @s))))}]]


         ;; List
         [:div (use-style filter-list-style)
          (doall
            (for [[k {:keys [count state]}] items
                  :let [added?    (= state :added)
                        excluded? (= state :excluded)]]
              ^{:key k}
              [:div (use-style (merge filter-style
                                      (cond
                                        added? added-style
                                        excluded? excluded-style))
                               {:on-click (fn [_]
                                            (swap! s assoc-in [:items k :state]
                                                   (case state
                                                     nil :added
                                                     :added :excluded
                                                     :excluded nil)))})

               ;; Left
              [:span (use-style count-style) count]
              [:span (use-style filter-name-style) k]

               ;; Right
               (when (or added? excluded?)
                 [:span (use-style state-style) state
                  (if added?
                    [:> mui-icons/Check]
                    [:> mui-icons/Block])])]))]]))))


;;; Devcards


(def devcard-wrapper {:width "300px"})


(defcard-rg Filters
  [:div (use-style devcard-wrapper)
  [filters-el "((some-uid))" items]])
