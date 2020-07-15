(ns athens.views.data-browser
  (:require
    [athens.db :as db]
    [athens.style :refer [color COLORS HSL-COLORS]]
    [clojure.string :as str]
    [datascript.core :as d]
    [garden.color :refer [opacify]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


(defn attr-unique?
  [attr]
  (contains? (get db/schema attr) :db/unique))


(defn attr-many?
  [attr]
  (= (get-in db/schema [attr :db/cardinality])
     :db.cardinality/many))


(defn attr-ref?
  [attr]
  (= (get-in db/schema [attr :db/valueType])
     :db.type/ref))


(defn attr-reverse?
  [attr]
  (when (keyword? attr)
    (str/starts-with? (name attr) "_")))


(defn headings
  [data mode]
  (case mode
    :coll ["idx" "val"]
    :map ["key" "val"]
    :tuples (into ["idx"] (->> data
                               (map count)
                               (apply max)
                               range))
    :maps (into ["idx"] (->> data
                             (mapcat keys)
                             (distinct)))))


(defn coll-rows
  [coll]
  (let [row (fn [[idx value]]
              [{:value idx
                :heading "idx"
                :idx idx}
               {:value value
                :heading "val"
                :idx idx}])]
    (->> coll
         (map-indexed vector)
         (map row))))


(defn reverse-refs-for-attr
  [attr eid]
  (d/q '[:find [?parent ...]
         :in $ ?attr ?eid
         :where [?parent :block/children ?eid]]
       @db/dsdb attr eid))


(defn reverse-attr
  [attr]
  (keyword (str (namespace attr) "/_" (name attr))))


(defn wrap-with-db-id
  [eid]
  {:db/id eid})


(defn reverse-refs
  [eid]
  (let [ref-attrs (->> db/schema
                       keys
                       (filter attr-ref?))]
    (into {}
          (for [attr ref-attrs]
            [(reverse-attr attr)
             (map wrap-with-db-id (reverse-refs-for-attr attr eid))]))))


(defn reverse-rows
  [{:keys [:db/id]}]
  (when id
    (reverse-refs id)))


(defn map-rows
  [m]
  (let [row (fn [[k v]]
              [{:value k
                :heading "key"
                :idx k}
               {:value v
                :attr k
                :heading "val"
                :idx k}])]
    (concat (map row m)
            (map row (reverse-rows m)))))


(defn tuple-rows
  [tuples]
  (let [row (fn [[idx values]]
              (into
                [{:value idx
                  :heading "idx"
                  :idx idx}]
                (map-indexed
                  (fn [heading value]
                    {:value value
                     :heading (str heading)
                     :idx idx})
                  values)))]
    (->> tuples
         (map-indexed vector)
         (map row))))


(defn maps-rows
  [ms]
  (let [hs (headings ms :maps)]
    (for [idx (-> ms count range)]
      (into [{:value idx
              :heading "idx"
              :idx idx}]
            (for [h (rest hs)]
              {:value (get-in ms [idx h])
               :attr h
               :heading (str h)
               :idx idx})))))


(defn get-rows
  [data mode]
  (case mode
    :coll (coll-rows data)
    :map (map-rows data)
    :tuples (tuple-rows data)
    :maps (maps-rows data)))


(defn cell
  [{:keys [value]}]
  (str value))


(def table-style
  {:border-collapse "collapse"
   :font-size "12px"
   :font-family "IBM Plex Sans Condensed"
   :letter-spacing "-0.01em"
   :margin "0.5rem 0 0"
   :min-width "100%"
   ::stylefy/manual [[:td {:border-top (str "1px solid " (color :border-color))
                           :padding "0.125rem"}]
                     [:tbody {:vertical-align "top"}]
                     [:th {:text-align "left" :padding "0.125rem 0.125rem"}]
                     [:tr {:transition "all 0.05s ease"}]
                     [:td:first-child :th:first-child {:padding-left "0.5rem"}]
                     [:td:last-child :th-last-child {:padding-right "0.5rem"}]
                     [:tbody [:tr:hover {:background (opacify (:background-minus-1 HSL-COLORS) 0.15)
                                         :color (:header-text-color COLORS)}]]
                     [:td>ul {:padding "0"
                              :margin "0"
                              :list-style "none"}]
                     [:td [:li {:margin "0 0 0.25rem"
                                :padding-top "0.25rem";
                                :border-top (str "1px solid " (color :border-color))}]]
                     [:td [:li:first-child {:border-top "none" :margin-top "0" :padding-top "0"}]]
                     [:a {:color (:link-color COLORS)}]
                     [:a:hover {:text-decoration "underline"}]]})


(def footer-style
  {:margin "0.5rem 0"
   ::stylefy/manual [[:a {:color (:link-color COLORS)}]]})


(defn table-view
  [data mode limit {:keys [cell-fn]
                    :or {cell-fn cell}}]
  (let [hs (headings data mode)
        rows (get-rows data mode)]
    [:div {:style {:overflow-x "auto"}}
     [:table (use-style table-style)
      [:thead
       [:tr (for [h hs]
              ^{:key (str "heading-" h)}
              [:th (str h)])]]
      [:tbody
       (for [row (if (= mode :map)
                   rows
                   (take limit rows))]
         ^{:key (str "row-" (-> row first :idx))}
         [:tr (for [{:keys [idx heading] :as c} row]
                ^{:key (str idx heading)}
                [:td {:style {:background-color "none"}}
                 (cell-fn c)])])]]]))


(defn coll-of-maps?
  [x]
  (and (coll? x)
       (every? associative? x)
       (not (every? sequential? x))))


(defn tuples?
  [x]
  (and (coll? x)
       (every? sequential? x)))


(defn browser
  [_ & [opts]]
  (let [limit (r/atom 10)
        increase-limit #(swap! limit + 10)]
    (fn [result & _]
      [:div
       [:div (cond

               (coll-of-maps? result)
               (table-view result :maps @limit opts)

               (and (associative? result)
                    (not (sequential? result)))
               (table-view result :map @limit opts)

               (tuples? result)
               (table-view result :tuples @limit opts)

               (coll? result)
               (table-view result :coll @limit opts)

               :else
               (str result))]
       [:div (use-style footer-style)
        (when (and (coll? result)
                   (not (map? result))
                   (< @limit (count result)))
          [:span (str "Showing " @limit " out of " (count result) " rows ")
           [:a {:on-click increase-limit
                :style {:cursor :pointer}}
            "load more"]])]])))
