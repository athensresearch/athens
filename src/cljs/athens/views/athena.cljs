(ns athens.views.athena
  (:require
    ["@material-ui/icons/ArrowForward" :default ArrowForward]
    ["@material-ui/icons/Close" :default Close]
    ["@material-ui/icons/Create" :default Create]
    ["@material-ui/icons/Search" :default Search]
    [athens.db :as db :refer [search-in-block-content search-exact-node-title search-in-node-title re-case-insensitive]]
    [athens.keybindings :refer [mousetrap]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES ZINDICES]]
    [athens.subs]
    [athens.util :refer [gen-block-uid scroll-into-view]]
    [athens.views.buttons :refer [button]]
    [athens.views.utils :as view-utils]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [garden.selectors :as selectors]
    [goog.dom :refer [getElement]]
    [goog.events :as events]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]])
  (:import
    (goog.events
      KeyCodes)))


;; Styles


(def container-style
  {:width         "49rem"
   :max-width "calc(100vw - 1rem)"
   :border-radius "0.25rem"
   :box-shadow    [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :display       "flex"
   :flex-direction "column"
   :background    (color :background-plus-1)
   :position      "fixed"
   :overflow      "hidden"
   :max-height    "60vh"
   :z-index       (:zindex-modal ZINDICES)
   :top           "40%"
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
                   "::-webkit-search-cancel-button" {:display "none"}}}) ; We replace the button elsewhere



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
   :flex-wrap "wrap"
   :gap "0.5rem"
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
                                            :color "#fff"} ; Intentionally not a theme value, because we don't have a semantic way to contrast with :link-color
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


;; Utilities


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
                     :results (vec
                                (concat
                                  [(search-exact-node-title query)]
                                  (search-in-node-title query 20 true)
                                  (search-in-block-content query)))}))))


(defn key-down-handler
  [e state]
  (let [key (.. e -keyCode)
        shift (.. e -shiftKey)
        {:keys [index query results]} @state
        item (get results index)]
    (cond


      :else nil)))


;; Components


(defn athena-prompt-el
  []
  [button {:on-click #(dispatch [:athena/toggle])
           :primary true
           :style {:font-size "11px"}}
   [:<>
    [:> Search]
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
                 [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class ArrowForward)]]]))))])]))


(defn create-page
  [title open-on-sidebar]
  (dispatch
    [:athena/create-page title (gen-block-uid) open-on-sidebar]))


(defn athena-keybindings
  [state children]
  [mousetrap
   {"escape" #(dispatch [:athena/toggle])

    ["enter" "shift+enter"]  (fn [e]
                               (let [shift-pressed (.. e -shiftKey)
                                     {:keys [index query results]} @state
                                     item (get results index)]
                                 (cond
                                   ;; if page doesn't exist, create and open
                                   (and (zero? index) (nil? item))
                                   (create-page query shift-pressed)

                                   ;; if shift: open in right-sidebar
                                   shift-pressed
                                   (do (dispatch [:athena/toggle])
                                       (dispatch [:right-sidebar/open-item (:block/uid item)]))
                                   ;; else open in main view
                                   :else
                                   (do (dispatch [:athena/toggle])
                                       (navigate-uid (:block/uid item))
                                       (dispatch [:editing/uid (:block/uid item)])))))
    "up"     (fn [e]
               (let [{:keys [results]} @state]
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
                   (scroll-into-view next-el result-el (not= cur-index (dec (count results)))))))

    "down"   (fn [e]
               (let [{:keys [results]} @state]
                 (swap! state update :index #(if (= % (dec (count results))) 0 (inc %))
                        (let [cur-index (:index @state)
                              input-el (.. e -target)
                              result-el (.. input-el (closest "div.athena") -lastElementChild)
                              next-el (nth (array-seq (.. result-el -children)) cur-index)]
                          (scroll-into-view next-el result-el (zero? cur-index))))))}

   children])


(defn athena-component
  []
  (let [s (r/atom {:index   0
                   :query   nil
                   :results []})
        search-handler (create-search-handler s)]
    (fn []
      (let [open? @(subscribe [:athena/open])]
        (when open?
          [view-utils/track-outside-click #(dispatch [:athena/toggle])
           [athena-keybindings s
            [:div.athena (use-style container-style)
             [:header {:style {:position "relative"}}
              [:input (use-style athena-input-style
                                 {:type        "search"
                                  :id          "athena-input"
                                  :auto-focus  true
                                  :required    true
                                  :placeholder "Find or Create Page"
                                  :on-change   (fn [e] (search-handler (.. e -target -value)))})]
              [:button (use-style search-cancel-button-style
                                  {:on-click #(set! (.-value (getElement "athena-input")))})
               [:> Close]]]
             [results-el s]
             (let [{:keys [results query index]} @s]
               [:div (use-style results-list-style)
                (doall
                  (for [[i x] (map-indexed list results)
                        :let [parent (:block/parent x)
                              title (or (:node/title parent) (:node/title x))
                              uid (or (:block/uid parent) (:block/uid x))
                              string (:block/string x)]]
                    (if (nil? x)
                      ^{:key i}
                      [:div (use-style result-style {:on-click (fn [_]
                                                                 (let [uid (gen-block-uid)]
                                                                   (dispatch [:athena/toggle])
                                                                   (dispatch [:page/create query uid])
                                                                   (navigate-uid uid)))
                                                     :class    (when (= i index) "selected")})

                       [:div (use-style result-body-style)
                        [:h4.title (use-sub-style result-style :title)
                         [:b "Create Page: "]
                         query]]
                       [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class Create)]]]

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
                       [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class ArrowForward)]]])))])]]])))))
