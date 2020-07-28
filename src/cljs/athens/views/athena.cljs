(ns athens.views.athena
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db :refer [search-in-block-content search-exact-node-title search-in-node-title re-case-insensitive]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES ZINDICES]]
    [athens.subs]
    [athens.util :refer [gen-block-uid is-beyond-rect?]]
    [athens.views.buttons :refer [button]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [garden.selectors :as selectors]
    [goog.functions :refer [debounce]]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]])
  (:import
    (goog.events
      KeyCodes)))


;;; Styles


(def container-style
  {:width         "49rem"
   :border-radius "0.25rem"
   :box-shadow    [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :display       "flex"
   :flex-direction "column"
   :background    (color :background-plus-1)
   :position      "fixed"
   :overflow      "hidden"
   :max-height    "60vh"
   :z-index       (:zindex-modal ZINDICES)
   :top           "50%"
   :left          "50%"
   :transform     "translate(-50%, -50%)"
   ;; Styling for the states of the custom search-cancel button, which depend on the input contents
   ::stylefy/manual [[(selectors/+ :input :button) {:opacity 0}]
                     ;; Using ':valid' here as a proxy for "has contents", i.e. "button should appear"
                     [(selectors/+ :input:valid :button) {:opacity 1}]]})


(def athena-input-style
  {:width "100%"
   :border 0
   :font-size      "2.375rem"
   :font-weight    "300"
   :line-height    "1.3"
   :letter-spacing "-0.03em"
   :border-radius  "0.25rem 0.25rem 0 0"
   :background     (color :background-plus-2)
   :color          (color :body-text-color)
   :caret-color    (color :link-color)
   :padding        "1.5rem 4rem 1.5rem 1.5rem"
   :cursor         "text"
   ::stylefy/mode {:focus {:outline "none"}
                   "::placeholder" {:color (color :body-text-color :opacity-low)}
                   "::-webkit-search-cancel-button" {:display "none"}}}) ;; We replace the button elsewhere



(def search-cancel-button-style
  {:background "none"
   :color "inherit"
   :position "absolute"
   :transition "opacity 0.1s ease, background 0.1s ease"
   :cursor "pointer"
   :border 0
   :right "2rem"
   :place-items "center"
   :place-content "center"
   :height "2.5rem"
   :width "2.5rem"
   :border-radius "1000px"
   :display "flex"
   :transform "translate(0%, -50%)"
   :top "50%"
   ::stylefy/manual [[:&:hover :&:focus {:background (color :background-plus-1)}]]})


(def results-list-style
  {:background    (color :background-color)
   :overflow-y "auto"
   :max-height "100%"})


(def results-heading-style
  {:padding "0.25rem 1.125rem"
   :background (color :background-plus-2)
   :display "flex"
   :position "sticky"
   :align-items "center"
   :top "0"
   :justify-content "space-between"
   :box-shadow [["0 1px 0 0 " (color :border-color)]]
   :border-top [["1px solid" (color :border-color)]]})


(def result-style
  {:display "flex"
   :padding "0.75rem 2rem"
   :background (color :background-plus-1)
   :color (color :body-text-color)
   :transition "all .05s ease"
   :border-top [["1px solid " (color :border-color)]]
   ::stylefy/sub-styles {:title {:font-size "1rem"
                                 :margin "0"
                                 :color (color :header-text-color)
                                 :font-weight "500"}
                         :preview {:white-space "wrap"
                                   :word-break "break-word"
                                   :color (color :body-text-color :opacity-med)}
                         :link-leader {:color "transparent"
                                       :margin "auto auto"}}
   ::stylefy/manual [[:b {:font-weight "500"
                          :opacity (:opacity-high OPACITIES)}]
                     [:&.selected :&:hover {:background (color :link-color)
                                            :color "#fff"} ;; Intentionally not a theme value, because we don't have a semantic way to contrast with :link-color 
                      [:.title :.preview :.link-leader :.result-highlight {:color "inherit"}]]]})


(def result-body-style
  {:flex "1 1 100%"
   :display "flex"
   :flex-direction "column"
   :justify-content "center"
   :align-items "flex-start"})


(def result-highlight-style
  {:color (color :body-text-color)
   :font-weight "500"})


(def hint-style
  {:color "inherit"
   :opacity (:opacity-med OPACITIES)
   :font-size "14px"})


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

      (= key KeyCodes.UP)
      (do
        (.. e preventDefault)
        (swap! state update :index #(dec (if (zero? %) (count results) %)))
        (let [cur-index (:index @state)

              ;; Search input box
              input-el (.. e -target)

              ;; Get the result list container which is the last element child
              ;; of the whole athena component

              result-el (.. input-el (closest "div.athena") -lastElementChild)

              ;; Get next element in the result list
              next-el (nth (array-seq (.. result-el -children)) cur-index)]

          ;; Check if next el is beyond the bounds of the result list and scroll if so
          (when (is-beyond-rect? next-el result-el)
            (.. next-el (scrollIntoView (not= cur-index (dec (count results))) {:behavior "auto"})))))

      (= key KeyCodes.DOWN)
      (do
        (.. e preventDefault)
        (swap! state update :index #(if (= % (dec (count results))) 0 (inc %)))
        (let [cur-index (:index @state)
              input-el (.. e -target)
              result-el (.. input-el (closest "div.athena") -lastElementChild)
              next-el (nth (array-seq (.. result-el -children)) cur-index)]
          (when (is-beyond-rect? next-el result-el)
            (.. next-el (scrollIntoView (zero? cur-index) {:behavior "auto"})))))

      :else nil)))


;;; Components


(defn athena-prompt-el
  []
  [button {:on-click #(dispatch [:athena/toggle])
           :primary true
           :style {:font-size "11px"}}
   [:<>
    [:> mui-icons/Search]
    [:span "Find or Create a Page"]]])


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
       [:header {:style {:position "relative"}}
        [:input (use-style athena-input-style
                           {:type        "search"
                            :id          "athena-input"
                            :auto-focus  true
                            :required    true
                            :placeholder "Find or Create Page"
                            :on-change   (fn [e] (search-handler (.. e -target -value)))
                            :on-key-down (fn [e] (key-down-handler e s))})]
        [:button (use-style search-cancel-button-style
                            {:on-click #(set! (.-value (.getElementById js/document "athena-input")))})
         [:> mui-icons/Close]]]
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

                    [:div (use-style result-body-style)
                     [:h4.title (use-sub-style result-style :title)
                      [:b "Create Page: "]
                      query]]
                    [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class mui-icons/Create)]]]

                   [:div (use-style result-style {:key      i
                                                  :on-click (fn []
                                                              (let [selected-page {:node/title   title
                                                                                   :block/uid    uid
                                                                                   :block/string string
                                                                                   :query        query}]
                                                                (dispatch [:athena/toggle])
                                                                (dispatch [:athena/update-recent-items selected-page])
                                                                (navigate-uid uid)))
                                                  :class    (when (= i index) "selected")})
                    [:div (use-style result-body-style)

                     [:h4.title (use-sub-style result-style :title) (highlight-match query title)]
                     (when string
                       [:span.preview (use-sub-style result-style :preview) (highlight-match query string)])]
                    [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class mui-icons/ArrowForward)]]])))]))]])))
