(ns athens.views.pages.block-page
  (:require
    ["@material-ui/icons/Link" :default Link]
    [athens.parse-renderer :as parse-renderer]
    [athens.reactive :as reactive]
    [athens.router :as router]
    [athens.style :refer [color]]
    [athens.views.blocks.core :as blocks]
    [athens.views.breadcrumbs :refer [breadcrumbs-list breadcrumb]]
    [athens.views.pages.node-page :as node-page]
    [garden.selectors :as selectors]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;; Styles


(def title-style
  {:position        "relative"
   :overflow        "visible"
   :flex-grow       "1"
   :margin          "0.1em 0"
   :letter-spacing  "-0.03em"
   :word-break      "break-word"
   :line-height     "1.4em"
   ::stylefy/manual [[:textarea {:-webkit-appearance "none"
                                 :cursor             "text"
                                 :resize             "none"
                                 :transform          "translate3d(0,0,0)"
                                 :color              "inherit"
                                 :font-weight        "inherit"
                                 :padding            "0"
                                 :letter-spacing     "inherit"
                                 :width              "100%"
                                 :min-height         "100%"
                                 :caret-color        (color :link-color)
                                 :background         "transparent"
                                 :margin             "0"
                                 :font-size          "inherit"
                                 :line-height        "inherit"
                                 :border-radius      "0.25rem"
                                 :transition         "opacity 0.15s ease"
                                 :border             "0"
                                 :font-family        "inherit"
                                 :visibility         "hidden"
                                 :position           "absolute"}]
                     [:textarea ["::-webkit-scrollbar" {:display "none"}]]
                     [:textarea:focus
                      :.is-editing {:outline    "none"
                                    :visibility "visible"
                                    :position   "relative"}]
                     [(selectors/+ :.is-editing :span) {:visibility "hidden"
                                                        :position   "absolute"}]]})


;; Helpers


(defn persist-textarea-string
  "A helper fn that takes `state` containing textarea changes and when user has made a text change dispatches `transact-string`.
   Used in `block-page-el` function to log when there is a diff and `on-blur`"
  [state block-uid]
  (dispatch [:block/save {:uid       block-uid
                          :string    (:string/local state)
                          :add-time? true}]))


;; Components

(defn block-page-change
  [e _uid state]
  (let [value (.. e -target -value)]
    (swap! state assoc :string/local value)))


(defn breadcrumb-handle-click
  "If block is in main, navigate to page. If in right sidebar, replace right sidebar item."
  [e uid breadcrumb-uid]
  (let [right-sidebar? (.. e -target (closest ".right-sidebar"))]
    (if right-sidebar?
      (dispatch [:right-sidebar/navigate-item uid breadcrumb-uid])
      (router/navigate-uid breadcrumb-uid e))))


(defn linked-refs-el
  [id]
  (let [linked-refs (reactive/get-reactive-linked-references id)]
    (when (seq linked-refs)
      [:div (use-style node-page/references-style {:key "Linked References"})
       [:section
        [:h4 (use-style node-page/references-heading-style)
         [(r/adapt-react-class Link)]
         [:span "Linked References"]]
        ;; Hide button until feature is implemented
        ;; [:> Button {:disabled true} [(r/adapt-react-class FilterList)]]]
        [:div (use-style node-page/references-list-style)
         (doall
           (for [[group-title group] linked-refs]
             [:div (use-style node-page/references-group-style {:key (str "group-" group-title)})
              [:h4 (use-style node-page/references-group-title-style)
               [:a {:on-click #(router/navigate-page (parse-renderer/parse-title group-title))}
                group-title]]
              (doall
                (for [block group]
                  [:div (use-style node-page/references-group-block-style {:key (str "ref-" (:block/uid block))})
                   [node-page/ref-comp block]]))]))]]])))


(defn parents-el
  [uid id]
  (let [parents (reactive/get-reactive-parents-recursively id)]
    [:span {:style {:color "gray"}}
     [breadcrumbs-list {:style {:font-size "1.2rem"}}
      (doall
        (for [{:keys [node/title block/string] breadcrumb-uid :block/uid} parents]
          ^{:key breadcrumb-uid}
          [breadcrumb {:key (str "breadcrumb-" breadcrumb-uid)
                       :on-click #(breadcrumb-handle-click % uid breadcrumb-uid)}
           [:span {:style {:pointer-events "none"}}
            [parse-renderer/parse-and-render (or title string)]]]))]]))


(defn block-page-el
  [_ _ _ _]
  (let [state (r/atom {:string/local    nil
                       :string/previous nil})]
    (fn [block]
      (let [{:block/keys [string children uid comment] :db/keys [id]} block]
        (when (not= string (:string/previous @state))
          (swap! state assoc :string/previous string :string/local string))

        [:div.block-page (use-style node-page/page-style {:data-uid uid})
         ;; Parent Context
         [parents-el uid id]

         ;; Header
         [:h1 (merge
                (use-style title-style {:data-uid uid :class "block-header"})
                {:on-click (fn [e]
                             (.. e preventDefault)
                             (if (.. e -shiftKey)
                               (router/navigate-uid uid e)
                               (dispatch [:editing/uid uid])))})
          [autosize/textarea
           {:id          (str "editable-uid-" uid)
            :value       (:string/local @state)
            :class       (when @(subscribe [:editing/is-editing uid]) "is-editing")
            :auto-focus  true
            :on-blur     (fn [_] (persist-textarea-string @state uid))
            :on-key-down (fn [e] (node-page/handle-key-down e uid state nil))
            :on-change   (fn [e] (block-page-change e uid state))}]
          (if (clojure.string/blank? (:string/local @state))
            [:wbr]
            [:span [parse-renderer/parse-and-render (:string/local @state) uid]])]

         (when (pos? (count (:block/comment block)))
           [athens.views.comments.inline/inline-comments (:block/comment block) uid false])
         ;; Children
         [:div (for [child children]
                 (let [{:keys [db/id]} child]
                   ^{:key id} [blocks/block-el child]))]

         ;; Refs
         [linked-refs-el id]]))))


(defn page
  [ident]
  (let [block (reactive/get-reactive-block-document ident)]
    [block-page-el block]))
