(ns athens.parse-renderer
  (:require
    [athens.components :as components]
    [athens.db :as db]
    [athens.parser :as parser]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color OPACITIES]]
    [clojure.string :as str]
    [instaparse.core :as insta]
    [posh.reagent :refer [pull #_q]]
    [stylefy.core :as stylefy :refer [use-style]]))


(declare parse-and-render)


;;; Styles

(def page-link {:cursor "pointer"
                :text-decoration "none"
                :color (color :link-color)
                :position "relative"
                ::stylefy/manual [[:.formatting {:color (color :body-text-color)
                                                 :opacity (:opacity-low OPACITIES)}]
                                  [:&:after {:content "''"
                                             :display "inline-block"
                                             :position "absolute"
                                             :top "-1px"
                                             :right "-0.2em"
                                             :left "-0.2em"
                                             :bottom "-1px"
                                             :z-index -1
                                             :opacity "0"
                                             :border-radius "0.25rem"
                                             :transition "all 0.05s ease"
                                             :background (color :link-color 0.1)}]
                                  [:&:hover:after {:opacity "1"}]
                                  [:&:hover {:z-index 1}]]})


(def hashtag {::stylefy/mode [[:hover {:text-decoration "underline" :cursor "pointer"}]]
              ::stylefy/manual [[:.formatting {:opacity (:opacity-low OPACITIES)}]]})


(def image {:border-radius "0.125rem"})


(def url-link {:cursor "pointer"
               :text-decoration "none"
               :color (color :link-color)
               ::stylefy/mode [[:hover {:text-decoration "underline"}]]})


(def block-ref {:font-size "0.9em"
                :transition "background 0.05s ease"
                :border-bottom [["1px" "solid" (color :highlight-color)]]
                ::stylefy/mode [[:hover {:background-color (color :highlight-color :opacity-lower)
                                         :cursor "alias"}]]})


(defn parse-title
  "Title coll is a sequence of plain strings or hiccup elements. If string, return string, otherwise parse the hiccup
  for its plain-text representation."
  [title-coll]
  (->> (map (fn [el]
              (if (string? el)
                el
                (str "[[" (clojure.string/join (get-in el [3 2])) "]]"))) title-coll)
       (str/join "")))



;;; Helper functions for recursive link rendering
(defn pull-node-from-string
  "Gets a block's node from the display string name (or partially parsed string tree)"
  [title-coll]
  (let [title (parse-title title-coll)]
    (pull db/dsdb '[*] [:node/title title])))


(defn render-page-link
  "Renders a page link given the title of the page."
  [title]
  (let [node (pull-node-from-string title)]
    [:span (use-style page-link {:class "page-link"})
     [:span {:class "formatting"} "[["]
     (into [:span {:on-click (fn [e]
                               (.. e stopPropagation) ;; prevent bubbling up click handler for nested links
                               (navigate-uid (:block/uid @node) e))}]
           title)
     [:span {:class "formatting"} "]]"]]))


;;; Components


;; Instaparse transforming docs: https://github.com/Engelberg/instaparse#transforming-the-tree
(defn transform
  "Transforms Instaparse output to Hiccup."
  [tree uid]
  (insta/transform
    {:block         (fn [& contents]
                      (concat [:span {:class "block" :style {:white-space "pre-line"}}] contents))
     ;; for more information regarding how custom components are parsed, see `doc/components.md`
     :component     (fn [& contents]
                      (components/render-component (first contents) uid))
     :page-link     (fn [& title-coll] (render-page-link title-coll))
     :hashtag       (fn [& title-coll]
                      (let [node (pull-node-from-string title-coll)]
                        [:span (use-style hashtag {:class    "hashtag"
                                                   :on-click #(navigate-uid (:block/uid @node))})
                         [:span {:class "formatting"} "#"]
                         [:span {:class "contents"} title-coll]]))
     :block-ref     (fn [ref-uid]
                      (let [block (pull db/dsdb '[*] [:block/uid ref-uid])]
                        (if @block
                          [:span (use-style block-ref {:class "block-ref"})
                           [:span {:class "contents" :on-click #(navigate-uid ref-uid %)}
                            (if (= uid ref-uid)
                              [parse-and-render "{{SELF}}"]
                              [parse-and-render (:block/string @block) ref-uid])]]
                          (str "((" ref-uid "))"))))
     :url-image     (fn [{url :url alt :alt}]
                      [:img (use-style image {:class "url-image"
                                              :alt   alt
                                              :src   url})])
     :url-link      (fn [{url :url} text]
                      [:a (use-style url-link {:class "url-link"
                                               :href  url
                                               :target "_blank"})
                       text])
     :bold          (fn [text]
                      [:strong {:class "contents bold"} text])
     :pre-formatted (fn [text]
                      [:code text])}
   tree))


(defn parse-and-render
  "Converts a string of block syntax to Hiccup, with fallback formatting if it can’t be parsed."
  [string uid]
  (let [result (parser/parse-to-ast string)]
    (if (insta/failure? result)
      [:span
       {:title (pr-str (insta/get-failure result))
        :style {:color "red"}}
       string]
      [vec (transform result uid)])))
