(ns athens.parse-renderer
  (:require
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
                                             :z-index "-1"
                                             :opacity "0"
                                             :border-radius "4px"
                                             :transition "all 0.05s ease"
                                             :background (color :link-color 0.1)}]
                                  [:&:hover:after {:opacity "1"}]]})


(def hashtag {::stylefy/mode [[:hover {:text-decoration "underline"}]]
              ::stylefy/manual [[:.formatting {:opacity (:opacity-low OPACITIES)}]]})


(def image {:border-radius "2px"})


(def url-link {:cursor "pointer"
               :text-decoration "none"
               :color (color :link-color)
               ::stylefy/mode [[:hover {:text-decoration "underline"}]]})


(def block-ref {:font-size "0.9em"
                :transition "background 0.05s ease"
                :border-bottom [["1px" "solid" (color :highlight-color)]]
                ::stylefy/mode [[:hover {:background-color (color :highlight-color :opacity-lower)
                                         :cursor "alias"}]]})


;;; Components


;; Instaparse transforming docs: https://github.com/Engelberg/instaparse#transforming-the-tree
(defn transform
  "Transforms Instaparse output to Hiccup."
  [tree]
  (insta/transform
    {:block     (fn [& contents]
                  (concat [:span {:class "block" :style {:white-space "pre-line"}}] contents))
     :page-link (fn [title]
                  (let [node (pull db/dsdb '[*] [:node/title title])]
                    [:span (use-style page-link {:class "page-link"})
                     [:span {:class "formatting"} "[["]
                     [:span {:on-click (fn [e] (navigate-uid (:block/uid @node) e))} title]
                     [:span {:class "formatting"} "]]"]]))
     :block-ref (fn [uid]
                  (let [block (pull db/dsdb '[*] [:block/uid uid])]
                    [:span (use-style block-ref {:class "block-ref"})
                     [:span {:class "contents" :on-click #(navigate-uid uid)} (parse-and-render (:block/string @block))]]))
     :hashtag   (fn [tag-name]
                  (let [node (pull db/dsdb '[*] [:node/title tag-name])]
                    [:span (use-style hashtag) {:class    "hashtag"
                                                :on-click #(navigate-uid (:block/uid @node))}
                     [:span {:class "formatting"} "#"]
                     [:span {:class "contents"} tag-name]]))
     :url-image (fn [{url :url alt :alt}]
                  [:img (use-style image {:class "url-image"
                                          :alt   alt
                                          :src   url})])
     :url-link  (fn [{url :url} text]
                  [:a (use-style url-link {:class "url-link"
                                           :href  url})
                   text])
     :bold      (fn [text]
                  [:strong {:class "contents bold"} text])}
    tree))


(defn parse-and-render
  "Converts a string of block syntax to Hiccup, with fallback formatting if it can’t be parsed."
  [string]
  (let [result (parser/parse-to-ast string)]
    (if (insta/failure? result)
      [:span
       {:title (pr-str (insta/get-failure result))
        :style {:color "red"}}
       string]
      [vec (transform result)])))
