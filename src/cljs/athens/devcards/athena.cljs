(ns athens.devcards.athena
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.devcards.buttons :refer [button-primary]]
    [athens.devcards.db :refer [load-real-db-button]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES]]
    [athens.subs]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [datascript.core :as d]
    [devcards.core :refer-macros [defcard-rg]]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]])
  (:import
    (goog.events
      KeyCodes)))


;;; Styles


(def container-style
  {:width         "784px"
   :border-radius "4px"
   :box-shadow    [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :display       "flex"
   :flex-direction "column"
   :background    (color :app-bg-color)
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
   :caret-color    (color :link-color)
   :padding "24px"
   :cursor "text"
   ::stylefy/mode {:focus {:outline "none"}
                   "::placeholder" {:color (color :body-text-color :opacity-low)}}})


(def results-list-style
  {:background    (color :app-bg-color)
   :overflow-y "auto"
   :max-height "100%"})


(def results-heading-style
  {:padding "4px 18px"
   :background (color :app-bg-color)
   :display "flex"
   :position "sticky"
   :top "0"
   :justify-content "space-between"
   :box-shadow [["0 1px 0 0 " (color :body-text-color :opacity-lower)]]
   :border-top [["1px solid" (color :body-text-color :opacity-lower)]]})


(def result-style
  {:display "grid"
   :grid-template "\"title icon\" \"preview icon\""
   :grid-gap "0 12px"
   :grid-template-columns "1fr auto"
   :padding "8px 32px"
   :background (color :body-text-color 0.02)
   :transition "all .05s ease"
   :border-top [["1px solid " (color :body-text-color :opacity-lower)]]
   ::stylefy/sub-styles {:title {:grid-area "title"
                                 :font-size "16px"
                                 :margin "0"
                                 :color (color :header-text-color)
                                 :font-weight "500"}
                         :preview {:grid-area "preview"
                                   :white-space "wrap"
                                   :word-break "break-word"
                                  ;;  :overflow "hidden"
                                  ;;  :text-overflow "ellipsis"
                                  ;;  :display "-webkit-box"
                                  ;;  :-webkit-line-clamp "2"
                                  ;;  :-webkit-box-orient "vertical"
                                   :color (color :body-text-color :opacity-med)}
                         :link-leader {:grid-area "icon"
                                       :color "transparent"
                                       :margin "auto auto"}}
   ::stylefy/mode {:hover {:background (color :link-color)
                           :color (color :app-bg-color)}}
   ::stylefy/manual [[:&:hover [:.title :.preview :.link-leader :.result-highlight {:color "inherit"}]]]})


(def result-highlight-style
  {:color "#000"
   :font-weight "500"})


(def hint-style
  {:color "inherit"
   :opacity (:opacity-med OPACITIES)
   :font-size "14px"
   ::stylefy/manual [[:kbd {:text-transform "uppercase"
                            :font-family "inherit"
                            :font-size "12px"
                            :font-weight 600
                            :border "1px solid rgba(67, 63, 56, 0.25)"
                            :border-radius "4px"
                            :padding "0 4px"}]]})


;;; Utilities


(defn re-case-insensitive
  "More options here https://clojuredocs.org/clojure.core/re-pattern"
  [query]
  (re-pattern (str "(?i)" query)))


(defn search-in-block-title
  [query]
  (d/q '[:find [(pull ?node [:db/id :node/title :block/uid]) ...]
         :in $ ?query-pattern
         :where
         [?node :node/title ?txt]
         [(re-find ?query-pattern ?txt)]]
       @db/dsdb
       (re-case-insensitive query)))


(defn get-parent-node
  [block]
  (loop [b block]
    (if (:node/title b)
      (assoc block :block/parent b)
      (recur (first (:block/_children b))))))


(defn search-in-block-content
  [query]
  (->>
    (d/q '[:find [(pull ?block [:db/id :block/uid :block/string :node/title {:block/_children ...}]) ...]
           :in $ ?query-pattern
           :where
           [?block :block/string ?txt]
           [(re-find ?query-pattern ?txt)]]
         @db/dsdb
         (re-case-insensitive query))
    (map get-parent-node)
    (map #(dissoc % :block/_children))))


(defn highlight-match
  [query txt]
  (let [query-pattern (re-case-insensitive (str "((?<=" query ")|(?=" query "))"))]
    (doall
      (map-indexed (fn [i part]
                     (if (re-find query-pattern part)
                       [:span.result-highlight (use-style result-highlight-style {:key i}) part]
                       part))
                   (clojure.string/split txt query-pattern)))))


(defn search-handler
  [*cache *match]
  (fn [e]
    (let [query (.. e -target -value)]
      (if (clojure.string/blank? query)
        (reset! *match [query nil])
        (let [result (or (get @*cache query)
                         (cond-> {:pages (search-in-block-title query)}
                           (count query) (assoc :blocks (search-in-block-content query))))]
          (swap! *cache assoc query result)
          (reset! *match [query result]))))))


(defn key-down-handler
  [e]
  (let [key (.. e -keyCode)]
    (cond
      ;; exit athena
      (= key KeyCodes.ESC) (dispatch [:toggle-athena])

      ;; TODO: navigate to page
      (= key KeyCodes.ENTER) nil

      ;; TODO: move selection up or down
      (= key KeyCodes.UP) nil
      (= key KeyCodes.DOWN) nil

      :else nil)))


;;; Components


(defn athena-prompt-el
  []
  [button-primary {:on-click-fn #(dispatch [:toggle-athena])
                   :label [:<>
                           [:> mui-icons/Search]
                           [:span "Find or Create a Page"]]
                   :style {:font-size "11px"}}])


(defn recent-el
  []
  [:div (use-style results-heading-style)
   [:h5 "Recent"]
   [:span (use-style hint-style)
    "Press "
    [:kbd "shift + enter"]
    " to open in right sidebar."]])


(defn athena-el
  [athena? *match change-handler]
  (when athena?
    [:div.athena (use-style container-style)
     [:input (use-style athena-input-style
                        {:type        "search"
                         :auto-focus  true
                         :placeholder "Find or Create Page"
                         :on-change   change-handler
                         :on-key-down key-down-handler})]
     [recent-el]
     [(fn []
        (let [[query {:keys [pages blocks] :as result}] @*match]
          (when result
            [:div (use-style results-list-style)
             (doall
               (for [[i x] (map-indexed list (take 40 (concat (take 20 pages) blocks)))]
                 (let [parent       (:block/parent x)
                       page-title   (or (:node/title parent) (:node/title x))
                       block-uid    (or (:block/uid parent) (:block/uid x))
                       block-string (:block/string x)]
                   [:div (use-style result-style {:key i :on-click #(navigate-uid block-uid)})
                    [:h4.title (use-sub-style result-style :title) (highlight-match query page-title)]
                    (when block-string
                      [:span.preview (use-sub-style result-style :preview) (highlight-match query block-string)])
                    [:span.link-leader (use-sub-style result-style :link-leader) [:> mui-icons/ArrowForward]]])))])))]]))


(defn athena-component
  []
  (let [athena? @(subscribe [:athena])
        *cache  (r/atom {})
        *match  (r/atom nil)
        handler (search-handler *cache *match)]
    [athena-el athena? *match handler]))


;;; Devcards


(defcard-rg Create-Page
  "Press button and then search \"test\" "
  [button-primary {:on-click-fn (fn []
                                  (let [n       (inc (:max-eid @db/dsdb))
                                        n-child (inc n)]
                                    (d/transact! db/dsdb [{:node/title     (str "Test Page " n)
                                                           :block/uid      (str "uid-" n)
                                                           :block/children [{:block/string (str "Test Block" n-child) :block/uid (str "uid-" n-child)}]}])))
                   :label       "Create Test Pages and Blocks"}])


(defcard-rg Load-Real-DB
  [load-real-db-button])


(defcard-rg Athena-Prompt
  [:<>
   [athena-prompt-el]
   [athena-component]])
