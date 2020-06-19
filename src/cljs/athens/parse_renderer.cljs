(ns athens.parse-renderer
  (:require
    [athens.db :as db]
    [athens.parser :as parser]
    [athens.router :refer [navigate-page]]
    [instaparse.core :as insta]
    [posh.reagent :refer [pull #_q]]))


(declare parse-and-render)


;; Instaparse transforming docs: https://github.com/Engelberg/instaparse#transforming-the-tree
(defn transform
  "Transforms Instaparse output to Hiccup."
  [tree]
  (insta/transform
    {:block     (fn [& contents]
                  (concat [:span {:class "block"}] contents))
     :page-link (fn [title]
                  (let [node (pull db/dsdb '[*] [:node/title title])]
                    [:span {:class "page-link"}
                     [:span {:style {:color "gray"}} "[["]
                     [:span {:on-click #(navigate-page (:block/uid @node))
                             :style    {:text-decoration "none" :color "dodgerblue"}} title]
                     [:span {:style {:color "gray"}} "]]"]]))
     :block-ref (fn [uid]
                  (let [block (pull db/dsdb '[*] [:block/uid uid])]
                    [:span {:class "block-ref"
                            :style {:font-size "0.9em" :border-bottom "1px solid gray"}}
                     [:span {:on-click #(navigate-page uid)} (parse-and-render (:block/string @block))]]))
     :hashtag   (fn [tag-name]
                  (let [node (pull db/dsdb '[*] [:node/title tag-name])]
                    [:span {:class    "hashtag"
                            :style    {:color "gray" :text-decoration "none" :font-weight "bold"}
                            :on-click #(navigate-page (:block/uid @node))}
                     (str "#" tag-name)]))
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
