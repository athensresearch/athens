(ns athens.parse-renderer
  (:require
    [athens.components :as components]
    [athens.db :as db]
    [athens.parser :as parser]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color OPACITIES]]
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


(def hashtag {::stylefy/mode [[:hover {:text-decoration "underline"}]]
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

;;; Helper functions for recursive link rendering
(defn pull-node-from-string
  "Gets a block's node from the display string name (or partially parsed string tree)"
  [string]
  (pull db/dsdb '[*] [:node/title (str "" (apply + (map (fn [el]
                                                          (if (string? el)
                                                            el
                                                            (str "[[" (clojure.string/join (get-in el [3 2])) "]]"))) string)))]))


(defn render-page-link
  "Renders a page link given the title of the page."
  [title]
  ;; This method feels a bit hacky: it extracts the DOM tree of its children components and re-wrap the content in double parentheses. Should we do something about it?
  ;; TODO: touch from inner content should navigate to the inner (children) page, but in this implementation doesn't work
  (let [node (pull-node-from-string title)]
    [:span (use-style page-link {:class "page-link"})
     [:span {:class "formatting"} "[["]
     [:span {:on-click (fn [e] (.. e stopPropagation) (navigate-uid (:block/uid @node) e))} (concat title)]
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
     :page-link     (fn [& title] (render-page-link title))
     :block-ref     (fn [uid]
                      (let [block (pull db/dsdb '[*] [:block/uid uid])]
                        [:span (use-style block-ref {:class "block-ref"})
                         [:span {:class "contents" :on-click #(navigate-uid uid)} (parse-and-render (:block/string @block) uid)]]))
     :hashtag       (fn [& tag-name]
                      (let [parsed-name (concat tag-name)
                            node        (pull db/dsdb '[*] [:node/title parsed-name])]
                        [:span (use-style hashtag {:class    "hashtag"
                                                   :on-click #(navigate-uid (:block/uid @node))})
                         [:span {:class "formatting"} "#"]
                         [:span {:class "contents"} parsed-name]]))
     :url-image     (fn [{url :url alt :alt}]
                      [:img (use-style image {:class "url-image"
                                              :alt   alt
                                              :src   url})])
     :url-link      (fn [{url :url} text]
                      [:a (use-style url-link {:class "url-link"
                                               :href  url})
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
