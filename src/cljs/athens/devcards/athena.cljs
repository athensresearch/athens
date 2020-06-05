(ns athens.devcards.athena
  (:require
    [athens.devcards.db :refer [new-conn posh-conn! load-real-db-button]]
    [athens.events]
    [athens.lib.dom.attributes :refer [with-attributes with-styles]]
    [athens.router :refer [navigate-page]]
    [athens.style :refer [style-guide-css +flex-space-between +depth-64]]
    [athens.subs]
    [cljsjs.react]
    [cljsjs.react.dom]
    [datascript.core :as d]
    [devcards.core :refer-macros [defcard-rg]]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]))


(defcard-rg Import-Styles
  [style-guide-css])


(defcard-rg Instantiate-app-db
  "Using re-frame, even though DevCards </3 re-frame. Not using re-frame elsewhere for subs, but will probably
  need refactoring or real isolation later.

  - https://github.com/athensresearch/athens/issues/126
  - https://github.com/bhauman/devcards/issues/105
  - https://github.com/bhauman/devcards/pull/131/
  ")


(dispatch [:init-rfdb])


(defcard-rg Instantiate-dsdb)
(defonce conn (new-conn))
(posh-conn! conn)


(defn handler
  []
  (let [n (inc (:max-eid @conn))
        n-child (inc n)]
    (d/transact! conn [{:node/title     (str "Test Page " n)
                        :block/uid      (str "uid-" n)
                        :block/children [{:block/string (str "Test Block" n-child) :block/uid (str "uid-" n-child)}]}])))


(defcard-rg Create-Page
  "Press button and then search \"test\" "
  [:button.primary {:on-click handler} "Create Test Pages and Blocks"])


(defcard-rg Load-Real-DB
  [load-real-db-button conn])


(defn athena-prompt
  []
  [:button.primary (with-attributes (with-styles {:padding 0})
                     {:on-click #(dispatch [:toggle-athena])})
   [:div (with-styles {:display "inline-block" :padding "6px 0 6px 8px"})
    "🔍"]
   [:div (with-styles {:display "inline-block" :font-weight "normal" :padding "6px 16px" :color "#322F38"})
    "Find or Create a Page"]])


(defn re-case-insensitive
  "More options here https://clojuredocs.org/clojure.core/re-pattern"
  [query]
  (re-pattern (str "(?i)" query)))


(defn search-in-block-title
  [db query]
  (d/q '[:find [(pull ?node [:db/id :node/title :block/uid]) ...]
         :in $ ?query-pattern
         :where
         [?node :node/title ?txt]
         [(re-find ?query-pattern ?txt)]]
       db
       (re-case-insensitive query)))


(defn get-parent-node
  [block]
  (loop [b block]
    (if (:node/title b)
      (assoc block :block/parent b)
      (recur (first (:block/_children b))))))


(defn search-in-block-content
  [db query]
  (->>
    (d/q '[:find [(pull ?block [:db/id :block/uid :block/string :node/title {:block/_children ...}]) ...]
           :in $ ?query-pattern
           :where
           [?block :block/string ?txt]
           [(re-find ?query-pattern ?txt)]]
         db
         (re-case-insensitive query))
    (map get-parent-node)
    (map #(dissoc % :block/_children))))


(defn highlight-match
  [query txt]
  (let [query-pattern (re-case-insensitive (str "((?<=" query ")|(?=" query "))"))]
    (map-indexed (fn [i part]
                   (if (re-find query-pattern part)
                     [:span {:key i :style {:background-color "#F9A132" :font-size "inherit" :line-height "inherit"}} part]
                     part))
                 (clojure.string/split txt query-pattern))))


(def +query
  (with-styles +depth-64
    {:background-color "white"
     :position "absolute"
     :z-index  99
     :top      "100%"
     :left     0
     :right    0
     :overflow-y "auto"
     :max-height "500px"}))


(def +athena-input
  (with-styles {:width "100%"
                :border 0
                :font-size      "38px"
                :font-weight    "300"
                :line-height    "49px"
                :letter-spacing "-0.03em"
                :color          "#433F38"
                :padding "25px 0 25px 35px"
                :cursor "text"}))


(defn recent
  []
  [:div (with-styles +flex-space-between {:padding "0px 18px 0px 32px" :background-color "white" :border-top "1px solid rgba(67, 63, 56, .5)"})
   [:h5 "Recent"]
   [:div
    [:span "Press "]
    [:span (with-styles {:text-transform "uppercase" :font-family "IBM Plex Sans Condensed" :font-size "12px" :font-weight 600
                         :border "1px solid rgba(67, 63, 56, 0.25)" :border-radius "4px"
                         :padding "0 4px"})
     "shift + enter"]
    [:span " to open in right sidebar."]]])


(def +container
  (with-styles +depth-64
    {:width         "784px"
     :border-radius "4px"
     :display       "inline-block"
     :position      "fixed"
     :top           "30%"
     :left          "50%"
     :transform     "translate(-50%, -50%)"
     :z-index       2}))


(defn athena
  [conn]
  (let [*cache (r/atom {})
        *match (r/atom nil)
        db (d/db conn)
        athena? (subscribe [:athena])
        handler (fn [e]
                  (let [query (.. e -target -value)]
                    (let [result (when-not (clojure.string/blank? query)
                                   (or (get @*cache query)
                                       (let [result (cond-> {:pages (search-in-block-title db query)}
                                                      (count query) (assoc :blocks (search-in-block-content db query)))]
                                         (swap! *cache assoc query result)
                                         result)))]
                      (reset! *match [query result]))))]
    (when @athena?
      [:div +container
       [:div {:style {:box-shadow "inset 0px -1px 0px rgba(0, 0, 0, 0.1)"}}
        [:input (with-attributes +athena-input
                  {:type        "search"
                   :placeholder "Find or Create Page",
                   :on-change   handler})]]
       [recent]
       [(fn []
          (let [[query {:keys [pages blocks] :as result}] @*match]
            (when result
              [:div (with-styles +query)
               (for [[i x] (map-indexed list (take 40 (concat (take 20 pages) blocks)))]
                 (let [parent (:block/parent x)
                       page-title (or (:node/title parent) (:node/title x))
                       block-uid (or (:block/uid parent) (:block/uid x))
                       block-string (:block/string x)]
                   [:div (with-attributes {:class "athena-result" :key i :on-click #(navigate-page block-uid)})
                    [:div
                     [:h4 (highlight-match query page-title)]
                     (when block-string
                       [:span (highlight-match query block-string)])]
                    [:h4 (with-styles {:margin-left "auto"}) "➡️"]]))])))]])))


(defcard-rg Athena-Prompt
  "Must press again to close. Doesn't go away if you click outside."
  [:<>
   [athena-prompt]
   [athena conn]])
