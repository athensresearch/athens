(ns athens.views.athena
  (:require
   ["/components/Icons/Icons" :refer [XmarkIcon]]
   ["@chakra-ui/react" :refer [Modal ModalContent ModalOverlay VStack Button IconButton Input HStack Heading Text]]
   [athens.common.utils :as utils]
   [athens.db           :as db :refer [search-in-block-content search-exact-node-title search-in-node-title re-case-insensitive]]
   [athens.router       :as router]
   [athens.subs]
   [athens.util         :refer [scroll-into-view]]
   [clojure.string      :as str]
   [goog.dom            :refer [getElement]]
   [goog.events         :as events]
   [re-frame.core       :as rf :refer [subscribe dispatch]]
   [reagent.core        :as r])
  (:import
    (goog.events
      KeyCodes)))


;; Utilities


(defn highlight-match
  [query txt]
  (let [query-pattern (re-case-insensitive (str "((?<=" query ")|(?=" query "))"))]
    (doall
      (map-indexed (fn [i part]
                     (if (re-find query-pattern part)
                       [:> Text {:class "result-highlight"
                                 :key i} part]
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
              result-el (.. input-el (closest "section.athena-modal") -lastElementChild)
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
              result-el (.. input-el (closest "section.athena-modal") -lastElementChild)
              next-el   (nth (array-seq (.. result-el -children)) cur-index)]
          (scroll-into-view next-el result-el (zero? cur-index))))

      :else nil)))


;; Components

(defn result-el
  [{:keys [title preview prefix icon query on-click active?]}]
  [:> Button {:justifyContent "flex-start"
              :fontWeight "normal"
              :display "flex"
              :height "auto"
              :textAlign "start"
              :flexDirection "row"
              :bg "transparent"
              :px 3
              :py 3
              :isActive active?
              :onClick on-click}
   [:> VStack {:align "stretch"
               :spacing 1
               :overflow "hidden"}
    [:> Heading {:as "h4"
                 :size "sm"} prefix (highlight-match query title)]
    (when preview
      [:> Text {:color "foreground.secondary"
                :textOverflow "ellipsis"
                :overflow "hidden"} (highlight-match query preview)])]
   icon])


(defn results-el
  [state]
  (let [no-query? (str/blank? (:query @state))
        recent-items @(subscribe [:athena/get-recent])]
    [:<> [:> HStack {:fontSize "sm"
                     :px 6
                     :py 2
                     :color "foreground.secondary"
                     :borderTop "1px solid"
                     :borderColor "separator.divider"
                     :justifyContent "space-between"}
          [:> Heading {:size "xs"}
           (if no-query? "Recent" "Results")]
          [:> Text
           "Press "
           [:kbd "shift + enter"]
           " to open in right sidebar."]]
     (when no-query?
       [:> VStack {:align "stretch"
                   :spacing 1
                   :borderTopWidth "1px"
                   :borderTopStyle "solid"
                   :borderColor "separator.divider"
                   :pt 4
                   :mb 4
                   :px 4
                   :overflowY "overlay"
                   :_empty {:display "none"}}
        (doall
          (for [[i x] (map-indexed list recent-items)]
            (when x
              (let [{:keys [query :node/title :block/string]} x]
                [result-el {:key      i
                            :title title
                            :query query
                            :preview string
                            :on-click (fn [e]
                                        (rf/dispatch [:reporting/navigation {:source :athena
                                                                             :target :page
                                                                             :pane   :main-pane}])
                                        (router/navigate-page title e))}]))))])]))


(defn search-results-el
  [{:keys [results query index]}]
  [:> VStack {:align "stretch"
              :borderTopWidth "1px"
              :borderTopStyle "solid"
              :borderColor "separator.divider"
              :spacing 1
              :pt 4
              :mb 4
              :px 4
              :overflowY "overlay"
              :_empty {:display "none"}}
   (doall
     (for [[i x] (map-indexed list results)
           :let  [block-uid (:block/uid x)
                  parent    (:block/parent x)
                  title     (or (:node/title parent) (:node/title x))
                  uid       (or (:block/uid parent) (:block/uid x))
                  string    (:block/string x)]]
       (if (nil? x)
         ^{:key i}
         [result-el {:key      i
                     :title    query
                     :prefix   "Create page: "
                     :preview  nil
                     :query    query
                     :active?  (= i index)
                     :on-click (fn [e]
                                 (rf/dispatch [:athena/toggle])
                                 (rf/dispatch [:right-sidebar/open-page (:node/title x) e])
                                 (rf/dispatch [:reporting/navigation {:source :athena
                                                                      :target :page
                                                                      :pane   :right-pane}]))}]
         [result-el {:key i
                     :title title
                     :query query
                     :preview string
                     :active? (= i index)
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
                                     (router/navigate-page title e))))}])))])


(defn athena-component
  []
  (let [athena-open?         (rf/subscribe [:athena/open])
        state                (r/atom {:index   0
                                      :query   nil
                                      :results []})
        search-handler       (create-search-handler state)]
    (fn []
      [:> Modal {:maxHeight "60vh"
                 :display "flex"
                 :scrollBehavior "inside"
                 :outline "none"
                 :closeOnEsc true
                 :isOpen @athena-open?
                 :onClose #(dispatch [:athena/toggle])}
       [:> ModalOverlay]
       [:> ModalContent {:width "49rem"
                         :class "athena-modal"
                         :overflow "hidden"
                         :backdropFilter "blur(20px)"
                         :bg "background.vibrancy"
                         :maxWidth "calc(100vw - 4rem)"}
        [:> Input
         {:type "search"
          :width "100%"
          :border 0
          :fontSize "2.375rem"
          :fontWeight "300"
          :lineHeight "1.3"
          :letterSpacing "-0.03em"
          :color "inherit"
          :background "none"
          :borderRadius 0
          :height "auto"
          :padding "1.5rem 4rem 1.5rem 1.5rem"
          :cursor "text"
          :id "athena-input"
          :auto-focus true
          :required true
          :_focus {:outline "none"}
          :sx {"::placeholder" {:color "foreground.secondary"}
               "::-webkit-search-cancel-button" {:display "none"}}
          :placeholder "Find or Create Page"
          :on-change   (fn [e] (search-handler (.. e -target -value)))
          :on-key-down (fn [e] (key-down-handler e state))}]
        (when (:query @state)
          [:> IconButton {:background "none"
                          :color "foreground.secondary"
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
                          :top "2rem"
                          :onClick #(set! (.-value (getElement "athena-input")) nil)}
           [:> XmarkIcon {:boxSize 6}]])
        [results-el state]
        [search-results-el @state]]])))
