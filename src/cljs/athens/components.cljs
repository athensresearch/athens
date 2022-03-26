(ns athens.components
  (:require
   ["@chakra-ui/react" :refer [Checkbox Box Button]]
   ["@material-ui/icons/Edit" :default Edit]
   [athens.db :as db]
   [athens.parse-renderer :refer [component]]
   [athens.reactive :as reactive]
   [athens.util :refer [recursively-modify-block-for-embed]]
   [athens.views.blocks.core :as blocks]
   [clojure.string :as str]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]))


(defn todo-on-click
  [uid from-str to-str]
  (let [current-block-content (:block/string (db/get-block [:block/uid uid]))
        new-block-content     (clojure.string/replace current-block-content
                                                      from-str
                                                      to-str)]
    (dispatch [:block/save {:uid       uid
                            :string    new-block-content
                            :add-time? true}])))


(defn span-click-stop
  "Stop clicks from propagating to textarea and thus preventing edit mode
   TODO() - might be a good idea to keep an edit icon at top right
     for every component."
  [children]
  [:span {:style {:display "contents"}
          :on-click (fn [e] (.. e stopPropagation))}
   children])


(defmethod component :todo
  [_content uid]
  [span-click-stop
   [:> Checkbox {:isChecked false
                 :verticalAlign "middle"
                 :transform "translateY(-2px)"
                 :onChange #(todo-on-click uid #"\{\{\[\[TODO\]\]\}\}" "{{[[DONE]]}}")}]])


(defmethod component :done
  [_content uid]
  [span-click-stop
   [:> Checkbox {:isChecked   true
                 :verticalAlign "middle"
                 :transform "translateY(-2px)"
                 :onChange #(todo-on-click uid #"\{\{\[\[DONE\]\]\}\}" "{{[[TODO]]}}")}]])


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
   [:> Button {:variant "link"
               :color "red"}
    content]])


(defmethod component :block-embed
  [content uid]
  ;; bindings are eval only once in with-let
  ;; which is needed to keep embed integrity else it will update on
  ;; each re-render. Similar to ref-comp
  (let [block-uid (last (re-find #"\(\((.+)\)\)" content))]
    ;; todo -- not reactive. some cases where delete then ctrl-z doesn't work
    (if (db/e-by-av :block/uid block-uid)
      (r/with-let [embed-id (random-uuid)]
                  [:> Box {:class "block-embed"
                           :bg "background.basement"
                           :position "relative"
                           :sx {"> .block-container" {:ml 0
                                                      :pr "1.3rem"
                                                      "textarea" {:background "transparent"}}
                                "> svg" {:position "absolute"
                                         :right "5px"
                                         :top "5px"
                                         :fontSize "1rem"
                                         :zIndex "5"
                                         :cursor "pointer"}}}
                   (let [block (reactive/get-reactive-block-document [:block/uid block-uid])]
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
      [:span "{{" content "}}"])))


