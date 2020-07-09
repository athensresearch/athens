(ns athens.views.athena
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db :refer [search-in-block-content search-exact-node-title search-in-node-title re-case-insensitive]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES ZINDICES]]
    [athens.subs]
    [athens.util :refer [gen-block-uid]]
    [athens.views.buttons :refer [button-primary]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [goog.functions :refer [debounce]]
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
   :z-index       (:zindex-modal ZINDICES)
   :top           "50%"
   :left          "50%"
   :transform     "translate(-50%, -50%)"})


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
   ::stylefy/manual [[:&.selected {:background (color :link-color)
                                   :color (color :app-bg-color)}
                      [:.title :.preview :.link-leader :.result-highlight {:color "inherit"}]]
                     [:&:hover [:.title :.preview :.link-leader :.result-highlight {:color "inherit"}]]]})


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


(defn highlight-match
  [query txt]
  (let [query-pattern (re-case-insensitive (str "((?<=" query ")|(?=" query "))"))]
    (doall
      (map-indexed (fn [i part]
                     (if (re-find query-pattern part)
                       [:span.result-highlight (use-style result-highlight-style {:key i}) part]
                       part))
                   (clojure.string/split txt query-pattern)))))


(defn create-search-handler
  [state]
  (fn [query]
    (if (str/blank? query)
      (reset! state {:index   0
                     :query   nil
                     :results []})
      (reset! state {:index   0
                     :query   query
                     :results (->> (concat [(search-exact-node-title query)]
                                           (take 20 (search-in-node-title query))
                                           (take 20 (search-in-block-content query)))
                                   vec)}))))


(defn key-down-handler
  [e state]
  (let [key (.. e -keyCode)
        shift (.. e -shiftKey)
        {:keys [index query results]} @state
        item (get results index)]

    (cond
      ;; FIXME: why does this only work in Devcards?
      (= key KeyCodes.ESC)
      (dispatch [:athena/toggle])

      (and shift (= KeyCodes.ENTER key) (zero? index) (nil? item))
      (let [uid (gen-block-uid)]
        (dispatch [:athena/toggle])
        (dispatch [:right-sidebar/open-item uid]))

      (and shift (= key KeyCodes.ENTER))
      (do
        (dispatch [:athena/toggle])
        (dispatch [:right-sidebar/open-item (:block/uid item)]))

      (and (= KeyCodes.ENTER key) (zero? index) (nil? item))
      (let [uid (gen-block-uid)]
        (dispatch [:athena/toggle])
        (dispatch [:page/create query uid])
        (navigate-uid uid))

      (= key KeyCodes.ENTER)
      (do (dispatch [:athena/toggle])
          (navigate-uid (or (:block/uid (:block/parent item)) (:block/uid item))))

      ;; TODO: change scroll as user reaches top or bottom
      ;; TODO: what happens when user goes to -1? or past end of list?
      (= key KeyCodes.UP)
      (swap! state update :index dec)

      (= key KeyCodes.DOWN)
      (swap! state update :index inc)

      :else nil)))


;;; Components


(defn athena-prompt-el
  []
  [button-primary {:on-click-fn #(dispatch [:athena/toggle])
                   :label [:<>
                           [:> mui-icons/Search]
                           [:span "Find or Create a Page"]]
                   :style {:font-size "11px"}}])


(defn results-el
  [state]
  (let [query? (str/blank? (:query @state))
        recent-items @(subscribe [:athena/get-recent])]
    [:<> [:div (use-style results-heading-style)
          [:h5 (if query? "Recent" "Results")]
          [:span (use-style hint-style)
           "Press "
           [:kbd "shift + enter"]
           " to open in right sidebar."]]
     (when query?
       [:div (use-style results-list-style)
        (doall
          (for [[i x] (map-indexed list recent-items)]
            (when x
              (let [{:keys [query :node/title :block/uid :block/string]} x]
                [:div (use-style result-style {:key      i
                                               :on-click #(navigate-uid uid)})
                 [:h4.title (use-sub-style result-style :title) (highlight-match query title)]
                 (when string
                   [:span.preview (use-sub-style result-style :preview) (highlight-match query string)])
                 [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class mui-icons/ArrowForward)]]]))))])]))


(defn athena-component
  []
  (let [open? @(subscribe [:athena/open])
        s (r/atom {:index 0
                   :query nil
                   :results []})
        search-handler (debounce (create-search-handler s) 500)]
    (when open?
      [:div.athena (use-style container-style)
       [:input (use-style athena-input-style
                          {:type        "search"
                           :auto-focus  true
                           :placeholder "Find or Create Page"
                           :on-change   (fn [e] (search-handler (.. e -target -value)))
                           :on-key-down (fn [e] (key-down-handler e s))})]
       [results-el s]
       [(fn []
          (let [{:keys [results query index]} @s]
            [:div (use-style results-list-style)
             (doall
               (for [[i x] (map-indexed list results)
                     :let [parent (:block/parent x)
                           title  (or (:node/title parent) (:node/title x))
                           uid    (or (:block/uid parent) (:block/uid x))
                           string (:block/string x)]]
                 (if (nil? x)
                   ^{:key i}
                   [:div (use-style result-style {:on-click (fn [_]
                                                              (let [uid (gen-block-uid)]
                                                                (dispatch [:athena/toggle])
                                                                (dispatch [:page/create query uid])
                                                                (navigate-uid uid)))
                                                  :class (when (= i index) "selected")})
                    [:h4.title (use-sub-style result-style :title)
                     [:b "Create Page: "]
                     query]
                    [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class mui-icons/Create)]]]
                   [:div (use-style result-style {:key      i
                                                  :on-click (fn []
                                                              (let [selected-page {:node/title   title
                                                                                   :block/uid    uid
                                                                                   :block/string string
                                                                                   :query        query}]
                                                                (dispatch [:athena/update-recent-items selected-page])
                                                                (navigate-uid uid)))
                                                  :class    (when (= i index) "selected")})
                    [:h4.title (use-sub-style result-style :title) (highlight-match query title)]
                    (when string
                      [:span.preview (use-sub-style result-style :preview) (highlight-match query string)])
                    [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class mui-icons/ArrowForward)]]])))]))]])))
