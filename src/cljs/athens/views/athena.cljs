(ns athens.views.athena
  (:require
   ["@chakra-ui/react" :refer [Modal ModalContent ModalOverlay IconButton Input]]
   ["@material-ui/icons/ArrowForward" :default ArrowForward]
   ["@material-ui/icons/Close" :default Close]
   ["@material-ui/icons/Create" :default Create]
   [athens.common.utils :as utils]
   [athens.db           :as db :refer [search-in-block-content search-exact-node-title search-in-node-title re-case-insensitive]]
   [athens.router       :as router]
   [athens.style        :refer [color OPACITIES]]
   [athens.subs]
   [athens.util         :refer [scroll-into-view]]
   [clojure.string      :as str]
   [goog.dom            :refer [getElement]]
   [goog.events         :as events]
   [re-frame.core       :as rf :refer [subscribe dispatch]]
   [reagent.core        :as r]
   [stylefy.core        :as stylefy :refer [use-style use-sub-style]])
  (:import
   (goog.events
    KeyCodes)))


;; Styles


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
                  (str/split txt query-pattern)))))


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
  (let [key                           (.. e -keyCode)
        shift?                        (.. e -shiftKey)
        {:keys [index query results]} @state
        item                          (get results index)]
    (cond
      (= KeyCodes.ENTER key) (cond
                               ;; if page doesn't exist, create and open
                               (and (zero? index) (nil? item))
                               (let [block-uid (utils/gen-block-uid)]
                                 (dispatch [:athena/toggle])
                                 (js/console.debug "athena key down" (pr-str {:block-uid block-uid
                                                                              :title     query}))
                                 (dispatch [:page/new {:title     query
                                                       :block-uid block-uid
                                                       :shift?    shift?}])
                                 (dispatch [:reporting/page.create {:source :athena
                                                                    :count  1}])
                                 (dispatch [:reporting/navigation {:source :athena
                                                                   :target (str "page/" query)
                                                                   :pane   (if shift?
                                                                             :right-pane
                                                                             :main-pane)}]))
                               ;; if shift: open in right-sidebar
                               shift?
                               (do (dispatch [:athena/toggle])
                                   (dispatch [:right-sidebar/open-page (:node/title item)])
                                   (dispatch [:reporting/navigation {:source :athena
                                                                     :target :page
                                                                     :pane   :right-pane}]))
                               ;; else open in main view
                               :else
                               (let [title (:node/title item)
                                     uid   (:block/uid item)]
                                 (dispatch [:athena/toggle])
                                 (dispatch [:reporting/navigation {:source :athena
                                                                   :target (if title
                                                                             :page
                                                                             :block)
                                                                   :pane   :main-pane}])
                                 (if title
                                   (router/navigate-page title)
                                   (router/navigate-uid uid))
                                 (dispatch [:editing/uid uid])))

      (= key KeyCodes.UP)
      (do
        (.. e preventDefault)
        (swap! state update :index #(dec (if (zero? %) (count results) %)))
        (let [cur-index (:index @state)
              ;; Search input box
              input-el  (.. e -target)
              ;; Get the result list container which is the last element child
              ;; of the whole athena component
              result-el (.. input-el (closest "div.athena") -lastElementChild)
              ;; Get next element in the result list
              next-el   (nth (array-seq (.. result-el -children)) cur-index)]
          ;; Check if next el is beyond the bounds of the result list and scroll if so
          (scroll-into-view next-el result-el (not= cur-index (dec (count results))))))

      (= key KeyCodes.DOWN)
      (do
        (.. e preventDefault)
        (swap! state update :index #(if (= % (dec (count results))) 0 (inc %)))
        (let [cur-index (:index @state)
              input-el  (.. e -target)
              result-el (.. input-el (closest "div.athena") -lastElementChild)
              next-el   (nth (array-seq (.. result-el -children)) cur-index)]
          (scroll-into-view next-el result-el (zero? cur-index))))

      :else nil)))


;; Components


(defn results-el
  [state]
  (let [no-query? (str/blank? (:query @state))
        recent-items @(subscribe [:athena/get-recent])]
    [:<> [:div (use-style results-heading-style)
          [:h5 (if no-query? "Recent" "Results")]
          [:span (use-style hint-style)
           "Press "
           [:kbd "shift + enter"]
           " to open in right sidebar."]]
     (when no-query?
       [:div (use-style results-list-style)
        (doall
         (for [[i x] (map-indexed list recent-items)]
           (when x
             (let [{:keys [query :node/title :block/string]} x]
               [:div (use-style result-style {:key      i
                                              :on-click (fn [e]
                                                          (rf/dispatch [:reporting/navigation {:source :athena
                                                                                               :target :page
                                                                                               :pane   :main-pane}])
                                                          (router/navigate-page title e))})
                [:h4.title (use-sub-style result-style :title) (highlight-match query title)]
                (when string
                  [:span.preview (use-sub-style result-style :preview) (highlight-match query string)])
                [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class ArrowForward)]]]))))])]))


(defn search-results-el
  [{:keys [results query index]}]
  [:div (use-style results-list-style)
   (doall
    (for [[i x] (map-indexed list results)
          :let  [block-uid (:block/uid x)
                 parent    (:block/parent x)
                 title     (or (:node/title parent) (:node/title x))
                 uid       (or (:block/uid parent) (:block/uid x))
                 string    (:block/string x)]]
      (if (nil? x)
        ^{:key i}
        [:div (use-style result-style
                         {:on-click (fn [e]
                                      (let [block-uid (utils/gen-block-uid)
                                            shift?    (.-shiftKey e)]
                                        (dispatch [:athena/toggle])
                                        (dispatch [:page/new {:title     query
                                                              :block-uid block-uid}])
                                        (dispatch [:reporting/page.create {:source :athena
                                                                           :count  1}])
                                        (dispatch [:reporting/navigation {:source :athena
                                                                          :target (if parent
                                                                                    (str "block/" block-uid)
                                                                                    (str "page/" title))
                                                                          :pane   (if shift?
                                                                                    :right-pane
                                                                                    :main-pane)}])))
                          :class    (when (= i index) "selected")})

         [:div (use-style result-body-style)
          [:h4.title (use-sub-style result-style :title)
           [:b "Create Page: "]
           query]]
         [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class Create)]]]

        [:div (use-style result-style
                         {:key      i
                          :on-click (fn [e]
                                      (let [selected-page {:node/title   title
                                                           :block/uid    uid
                                                           :block/string string
                                                           :query        query}
                                            shift?        (.-shiftKey e)]
                                        (dispatch [:athena/toggle])
                                        (dispatch [:athena/update-recent-items selected-page])
                                        (dispatch [:reporting/navigation {:source :athena
                                                                          :target (if parent
                                                                                    :block
                                                                                    :page)
                                                                          :pane   (if shift?
                                                                                    :right-pane
                                                                                    :main-pane)}])
                                        (if parent
                                          (router/navigate-uid block-uid)
                                          (router/navigate-page title e))))

                          :class (when (= i index) "selected")})
         [:div (use-style result-body-style)

          [:h4.title (use-sub-style result-style :title) (highlight-match query title)]
          (when string
            [:span.preview (use-sub-style result-style :preview) (highlight-match query string)])]
         [:span.link-leader (use-sub-style result-style :link-leader) [(r/adapt-react-class ArrowForward)]]])))])


(defn athena-component
  []
  (let [athena-open?         (rf/subscribe [:athena/open])
        state                (r/atom {:index   0
                                      :query   nil
                                      :results []})
        search-handler       (create-search-handler state)]
    (fn []
      (when @athena-open?
        [:> Modal {:maxHeight "60vh"
                   :display "flex"
                   :_focus {:outline "none"}
                   :closeOnEsc true
                   :isOpen @athena-open?
                   :onClose #(dispatch [:athena/toggle])}
         [:> ModalOverlay {:_focus {:outline "none"}}]
         [:> ModalContent
          {:width "49rem"
          :_focus {:outline "none"}
           :bg "background.upper"
           :maxWidth "calc(100vw - 4rem)"}
          [:> Input
           {:type "search"
            :width "100%"
            :border 0
            :fontSize "2.375rem"
            :fontWeight "300"
            :lineHeight "1.3"
            :letterSpacing "-0.03em"
            :borderRadius "0.25rem 0.25rem 0 0"
            :color "inherit"
            :height "auto"
            :padding "1.5rem 4rem 1.5rem 1.5rem"
            :cursor "text"
            :id          "athena-input"
            :auto-focus  true
            :required    true
            :_focus {:outline "none"}
            :sx {"::-webkit-search-cancel-button" {:display "none"}}
            :placeholder "Find or Create Page"
            :onChange   (fn [e] (search-handler (.. e -target -value)))
            :onKeyDown (fn [e] (key-down-handler e state))}]
          [:> IconButton {:background "none"
                          :color "inherit"
                          :position "absolute"
                          :transition "opacity 0.1s ease, background 0.1s ease"
                          :cursor "pointer"
                          :border 0
                          :right "2rem"
                          :placeItems "center"
                          :placeContent "center"
                          :height "2.5rem"
                          :width "2.5rem"
                          :borderRadius "1000px"
                          :display "flex"
                          :transform "translate(0%, -50%)"
                          :top "50%"
                          :onClick #(set! (.-value (getElement "athena-input")) nil)}
           [:> Close]]]
         [results-el state]
         [search-results-el @state]]))))
