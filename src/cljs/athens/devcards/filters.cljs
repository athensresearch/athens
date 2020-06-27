(ns athens.devcards.filters
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.devcards.buttons :refer [button-primary]]
    #_[athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :refer [join]]
    [devcards.core :refer [defcard-rg]]
    #_[re-frame.core :as re-frame :refer [dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style #_use-sub-style]]))


;;; Styles


(def container-style
  {:width "400px"
   :display "flex"
   :flex-direction "column"})


(def search-style
  {:width "100%"
   :display "flex"})


(def controls-style
  {:width "100%"
   :display "flex"
   :justify-content "space-between"})


(def filter-list-style
  {:width "100%"
   :display "flex"
   :flex-direction "column"})


(def filter-style
  {:width           "100%"
   :display         "flex"
   :justify-content "space-between"
   :height "30px"})


(def added-style
  {:background-color "lightblue"})


(def excluded-style
  {:background-color "salmon"})


(def count-style
  {:padding "5px"
   :width "30px"})


(def filter-name-style
  {:padding-left "10px"})


;;; Utilities


(def items
  {"Amet"   {:count 6 :state :added}
   "At"     {:count 30 :state :excluded}
   "Diam"   {:count 6}
   "Donec"  {:count 6}
   "Elit"   {:count 30}
   "Elitu"  {:count 1}
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
         [:h5 "Filter"]

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
          [button-primary {:label       [:> mui-icons/Sort]
                           :on-click-fn (fn [_]
                                          (swap! s assoc :sort (if (= sort_ :lex)
                                                                 :count
                                                                 :lex)))}]
          [:div
           [:span (str num-filters " Filters Active")]
           [button-primary {:label       "Reset"
                            :on-click-fn (fn [_]
                                           (swap! s assoc :items
                                                  (reduce-kv
                                                    (fn [m k v]
                                                      (assoc m k (dissoc v :state)))
                                                    {}
                                                    (:items @s))))}]]]


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
               [:div
                [:span (use-style count-style) count]
                [:span (use-style filter-name-style) k]]

               ;; Right
               (when (or added? excluded?)
                 [:span {:style {:display "flex"}} state
                  (if added?
                    [:> mui-icons/Check]
                    [:> mui-icons/Block])])]))]]))))


;;; Devcards


(defcard-rg Filters
  [filters-el "((some-uid))" items])
