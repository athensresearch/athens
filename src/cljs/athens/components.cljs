(ns athens.components
  (:require
    ["@material-ui/icons/Edit" :default Edit]
    [athens.db :as db]
    [athens.parse-renderer :refer [component]]
    [athens.style :refer [color]]
    [athens.util :refer [now-ts recursively-modify-block-for-embed]]
    [athens.views.blocks.core :as blocks]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


(defn todo-on-click
  [uid from-str to-str]
  (let [current-block-content (:block/string (db/get-block [:block/uid uid]))]
    (dispatch [:transact [{:block/uid    uid
                           :block/string (clojure.string/replace
                                           current-block-content
                                           from-str
                                           to-str)
                           :edit/time    (now-ts)}]])))


(defn span-click-stop
  "Stop clicks from propagating to textarea and thus preventing edit mode
   TODO() - might be a good idea to keep an edit icon at top right
     for every component."
  [children]
  [:span {:on-click (fn [e]
                      (.. e stopPropagation))}
   children])


(defmethod component :todo
  [_content uid]
  [span-click-stop
   [:input {:type      "checkbox"
            :checked   false
            :on-change #(todo-on-click uid #"\{\{\[\[TODO\]\]\}\}" "{{[[DONE]]}}")}]])


(defmethod component :done
  [_content uid]
  [span-click-stop
   [:input {:type      "checkbox"
            :checked   true
            :on-change #(todo-on-click uid #"\{\{\[\[DONE\]\]\}\}" "{{[[TODO]]}}")}]])


(defmethod component :youtube
  [content _uid]
  [span-click-stop
   [:div.media-16-9
    [:iframe {:src   (str "https://www.youtube.com/embed/" (get (re-find #".*v=([a-zA-Z0-9_\-]+)" content) 1))
              :allow "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"}]]])


(defmethod component :iframe
  [content _uid]
  [span-click-stop
   [:div.media-16-9
    [:iframe {:src (re-find #"http.*" content)}]]])


(defmethod component :self
  [content _uid]
  [span-click-stop
   [:button {:style {:color       "red"
                     :font-family "IBM Plex Mono"}}
    content]])


(def block-embed-adjustments
  {:background (color :background-minus-2 :opacity-med)
   :position   "relative"
   ::stylefy/manual [[:>.block-container {:margin-left "0"
                                          ::stylefy/manual [[:textarea {:background "transparent"}]]}]
                     [:>svg              {:position   "absolute"
                                          :right      "5px"
                                          :top        "5px"
                                          :font-size  "1rem"
                                          :z-index    "5"
                                          :cursor     "pointer"}]]})


(defmethod component :block-embed
  [content uid]
  ;; bindings are eval only once in with-let
  ;; which is needed to keep embed integrity else it will update on
  ;; each re-render. Similar to ref-comp
  (let [block-uid (last (re-find #"\(\((.+)\)\)" content))]
    ;; todo -- not reactive. some cases where delete then ctrl-z doesn't work
    (if (db/e-by-av :block/uid block-uid)
      (r/with-let [embed-id (random-uuid)]
                  [:div.block-embed (use-style block-embed-adjustments)
                   (let [block (db/get-block-document [:block/uid block-uid])]
                     [:<>
                      [blocks/block-el
                       (recursively-modify-block-for-embed block embed-id)
                       {:linked-ref false}
                       {:block-embed? true}]
                      (when-not @(subscribe [:editing/is-editing uid])
                        [:> Edit
                         {:on-click (fn [e]
                                      (.. e stopPropagation)
                                      (dispatch [:editing/uid uid]))}])])])
      ;; roam actually hides the brackets around [[embed]]
      [:span "{{" (str/replace content block-uid "invalid") "}}"])))


