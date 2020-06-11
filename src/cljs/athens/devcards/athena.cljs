(ns athens.devcards.athena
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.devcards.buttons :refer [button-primary]]
    [athens.devcards.db :refer [new-conn posh-conn! load-real-db-button]]
    [athens.events]
    [athens.router :refer [navigate-page]]
    [athens.style :refer [base-styles DEPTH-SHADOWS COLORS HSL-COLORS OPACITIES]]
    [athens.subs]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [datascript.core :as d]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.color :refer [opacify]]
    [garden.selectors :as selectors]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


(defcard-rg Import-Styles
  [base-styles])


(defcard-rg Instantiate-app-db
  "Using re-frame, even though DevCards </3 re-frame. Not using re-frame elsewhere for subs, but will probably
  need refactoring or real isolation later.

  - https://github.com/athensresearch/athens/issues/126
  - https://github.com/bhauman/devcards/issues/105
  - https://github.com/bhauman/devcards/pull/131/
  ")


(dispatch [:init-rfdb])

(defcard-rg Instantiate-Dsdb)
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
  [button-primary {:on-click-fn handler
                   :label "Create Test Pages and Blocks"}])


(defcard-rg Load-Real-DB
  [load-real-db-button conn])


;; STYLES



(def container-style
  {:width         "784px"
   :border-radius "4px"
   :box-shadow    [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (opacify (:body-text-color HSL-COLORS) (first OPACITIES))]]
   :display       "flex"
   :flex-direction "column"
   :background    (:app-bg-color HSL-COLORS)
   :position      "fixed"
   :overflow      "hidden"
   :max-height    "60vh"
   :top           "50%"
   :left          "50%"
   :transform     "translate(-50%, -50%)"
   :z-index       2})


(def athena-input-style
  {:width "100%"
   :border 0
   :font-size      "38px"
   :font-weight    "300"
   :line-height    "49px"
   :letter-spacing "-0.03em"
   :border-radius "4px 4px 0 0"
   :color          "#433F38"
   :caret-color    (:link-color COLORS)
   :padding "24px"
   :cursor "text"
   ::stylefy/mode {:focus {:outline "none"}
                   "::placeholder" {:opacity (nth OPACITIES 2)}}})


(def results-list-style
  {:background    (:app-bg-color HSL-COLORS)
   :overflow-y "auto"
   :max-height "100%"})


(def results-heading-style
  {:padding "4px 18px"
   :background (:app-bg-color HSL-COLORS)
   :display "flex"
   :position "sticky"
   :top "0"
   :justify-content "space-between"
   :box-shadow [["0 1px 0 0 " (opacify (:body-text-color HSL-COLORS) 0.12)]]
   :border-top [["1px solid" (opacify (:body-text-color HSL-COLORS) 0.12)]]})


(def result-style
  {:display "grid"
   :grid-template "\"title icon\" \"preview icon\""
   :grid-gap "0 12px"
   :grid-template-columns "1fr auto"
   :padding "8px 32px"
   :background (opacify (:body-text-color HSL-COLORS) 0.02)
   :transition "all .05s ease"
   :border-top [["1px solid " (opacify (:body-text-color HSL-COLORS) 0.12)]]
   ::stylefy/sub-styles {:title {:grid-area "title"
                                 :font-size "16px"
                                 :margin "0"
                                 :color (:header-text-color COLORS)
                                 :font-weight "500"}
                         :preview {:grid-area "preview"
                                   :white-space "wrap"
                                   :word-break "break-word"
                                   :overflow "hidden"
                                   :text-overflow "ellipsis"
                                   :display "-webkit-box"
                                   :-webkit-line-clamp "1"
                                   :-webkit-box-orient "vertical"
                                   :color (opacify (:body-text-color COLORS) (nth OPACITIES 3))}
                         :link-leader {:grid-area "icon"
                                       :color "transparent"
                                       :margin "auto auto"}}
   ::stylefy/mode {:hover {:background (:link-color HSL-COLORS)
                           :color (:app-bg-color COLORS)}}
   ::stylefy/manual [[:&:hover [:.title :.preview :.link-leader {:color "inherit !important"}]]]})


(def result-highlight-style
  {:color "inherit"
   :font-weight "500"})


(def hint-style
  {:color "inherit"
   :opacity (nth OPACITIES 3)
   :font-size "14px"
   ::stylefy/manual [[:kbd {:text-transform "uppercase"
                            :font-family "inherit"
                            :font-size "12px"
                            :font-weight 600
                            :border "1px solid rgba(67, 63, 56, 0.25)"
                            :border-radius "4px"
                            :padding "0 4px"}]]})


;; COMPONENTS

(defn athena-prompt
  []
  [button-primary {:on-click-fn #(dispatch [:toggle-athena])
                   :label [:<>
                           [:> mui-icons/Search]
                           [:span "Find or Create a Page"]]
                   :style {:font-size "11px"}}])


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
                     [:span (use-style result-highlight-style {:key i}) part]
                     part))
                 (clojure.string/split txt query-pattern))))


(defn recent
  []
  [:div (use-style results-heading-style)
   [:h5 "Recent"]
   [:span (use-style hint-style)
    "Press "
    [:kbd "shift + enter"]
    " to open in right sidebar."]])


(defn athena
  [conn]
  (let [*cache (r/atom {})
        *match (r/atom nil)
        db (d/db conn)
        athena? (subscribe [:athena])
        handler (fn [e]
                  (let [query (.. e -target -value)]
                    (if (clojure.string/blank? query)
                      (reset! *match [query nil])
                      (let [result (or (get @*cache query)
                                       (cond-> {:pages (search-in-block-title db query)}
                                         (count query) (assoc :blocks (search-in-block-content db query))))]
                        (swap! *cache assoc query result)
                        (reset! *match [query result])))))]
    (when @athena?
      [:div (use-style container-style)
       [:input (use-style athena-input-style
                          {:type        "search"
                           :auto-focus  true
                           :placeholder "Find or Create Page"
                           :on-change   handler})]
       [recent]
       [(fn []
          (let [[query {:keys [pages blocks] :as result}] @*match]
            (when result
              [:div (use-style results-list-style)
               (for [[i x] (map-indexed list (take 40 (concat (take 20 pages) blocks)))]
                 (let [parent (:block/parent x)
                       page-title (or (:node/title parent) (:node/title x))
                       block-uid (or (:block/uid parent) (:block/uid x))
                       block-string (:block/string x)]
                   [:div (use-style result-style {:key i :on-click #(navigate-page block-uid)})
                    [:h4.title (use-sub-style result-style :title) (highlight-match query page-title)]
                    (when block-string
                      [:span.preview (use-sub-style result-style :preview) (highlight-match query block-string)])
                    [:span.link-leader (use-sub-style result-style :link-leader) "->"]]))])))]])))


(defcard-rg Athena-Prompt
  "Must press again to close. Doesn't go away if you click outside."
  [:<>
   [athena-prompt]
   [athena conn]])
