(ns athens.parse-renderer
  (:require
    [athens.db :as db]
    [athens.style :refer [color OPACITIES]]
    [athens.parser :as parser]
    [athens.router :refer [navigate-uid]]
    [instaparse.core :as insta]
    [posh.reagent :refer [pull #_q]]
    [stylefy.core :as stylefy :refer [use-style]]))


(declare parse-and-render)


;;; Styles


(def block {})

(def page-link {:cursor "pointer"
                :text-decoration "none"
                :color (color :link-color)
                ::stylefy/manual [[:.formatting {:display "none"}
                                   :&:hover {:background-color (color :panel-background)} ]]})

(def hashtag {::stylefy/manual [[:.formatting {:opacity (:opacity-low OPACITIES)}]]})


;;; Components


;; Instaparse transforming docs: https://github.com/Engelberg/instaparse#transforming-the-tree
(defn transform
  "Transforms Instaparse output to Hiccup."
  [tree]
  (insta/transform
    {:block     (fn [& contents]
                  (concat [:span (use-style block {:class "block"})] contents))
     :page-link (fn [title]
                  (let [node (pull db/dsdb '[*] [:node/title title])]
                    [:span (use-style page-link {:class "page-link"})
                     [:span {:class "formatting"} "[["]
                     [:span {:on-click #(navigate-uid (:block/uid @node))} title]
                     [:span {:class "formatting"} "]]"]]))
     :block-ref (fn [uid]
                  (let [block (pull db/dsdb '[*] [:block/uid uid])]
                    [:span {:class "block-ref"
                            :style {:font-size "0.9em" :border-bottom "1px solid gray"}}
                     [:span {:on-click #(navigate-uid uid)} (parse-and-render (:block/string @block))]]))
     :hashtag   (fn [tag-name]
                  (let [node (pull db/dsdb '[*] [:node/title tag-name])]
                    [:span (use-style hashtag) {:class    "hashtag"
                                                :on-click #(navigate-uid (:block/uid @node))}
                     [:span {:class "formatting"} "#"]
                     tag-name]))
     :url-image (fn [{url :url alt :alt}]
                  [:img {:class "url-image"
                         :alt   alt
                         :src   url}])
     :url-link  (fn [{url :url} text]
                  [:a {:class "url-link"
                       :href  url}
                   text])
     :bold      (fn [text]
                  [:strong {:class "bold"} text])}
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
