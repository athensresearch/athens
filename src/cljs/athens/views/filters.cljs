(ns athens.views.filters
  (:require
    ["@material-ui/icons/ArrowDownward" :default ArrowDownward]
    ["@material-ui/icons/Block" :default Block]
    ["@material-ui/icons/Check" :default Check]
    ["@material-ui/icons/FilterList" :default FilterList]
    ["@material-ui/icons/Sort" :default Sort]
    [athens.style :refer [color OPACITIES]]
    [athens.views.buttons :refer [button]]
    [athens.views.textinput :refer [textinput]]
    [cljsjs.react]
    [cljsjs.react.dom]
    #_[re-frame.core :as re-frame :refer [dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style #_use-sub-style]]))


;;; Styles


(def container-style
  {:flex-basis "30em"
   :display "flex"
   :overflow "auto"
   :flex-direction "column"})


(def search-style
  {:align-self "stretch"
   :display "flex"})


(def controls-style
  {:width "100%"
   :display "flex"
   :flex "0 0 auto"
   :font-size "12px"
   :align-items "center"
   :text-align "right"
   :border-bottom (str "1px solid " (color :background-minus-1))
   :margin "0.25rem 0 0"
   :padding-bottom "0.25rem"
   :justify-content "space-between"
   :font-weight "500"
   :color (color :body-text-color :opacity-high)
   ::stylefy/manual [[:svg {:font-size "20px"}]]})


(def sort-control-style {:padding "0.25rem 0.375rem"
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
   :padding "0.25rem 0 0"
   :flex-direction "column"})


(def filter-style
  {:width           "100%"
   :display         "flex"
   :justify-content "space-between"
   :padding         "0.125rem 0.5rem"
   :align-items     "center"
   :border-radius   "0.25rem"
   :margin-block-end "1px"
   :user-select     "none"
   :transition      "all 0.1s ease"
   ::stylefy/manual [[:&:hover {:background (color :background-minus-1 :opacity-med)}]
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
   :color (color :body-text-currentColor)
   :font-weight "bold"
   :font-size "11px"
   :text-align "right"
   :flex "0 0 3em"})


(def filter-name-style
  {:flex "1 1 100%"
   :color (color :body-text-currentColor)
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


(def no-items-message-style
  {:text-align "center"
   :opacity (:opacity-med OPACITIES)
   :margin "0"})


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

         ;; Search
         [textinput (use-style search-style
                               {:type        "search"
                                :autoFocus  true
                                :placeholder "Type to find filters"
                                :icon [:> FilterList]
                                :value (:search @s)
                                :on-change   (fn [e]
                                               (swap! s assoc-in [:search] (.. e -target -value)))})]

         ;; Controls
         [:div (use-style controls-style)
          [button {:style sort-control-style
                   :on-click (fn [_]
                               (swap! s assoc :sort (if (= sort_ :lex)
                                                      :count
                                                      :lex)))}
           [:> Sort]]
          [:span (use-style sort-indicator-style) [:<> [:> ArrowDownward] (if (= sort_ :lex) "Title" "Number")]]
          [:span (str num-filters " Active")]
          [button {:style reset-control-style
                   :on-click (fn [_]
                               (swap! s assoc :items
                                      (reduce-kv
                                       (fn [m k v]
                                         (assoc m k (dissoc v :state)))
                                       {}
                                       (:items @s))))}
           "Reset"]]
         

         ;; List
         [:div (use-style filter-list-style)
          (if (> (count items) 0)
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
                     [:> Check]
                     [:> Block])])]))
            [:p (use-style no-items-message-style) "No filters found"])]]))))
