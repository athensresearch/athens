(ns athens.parse-renderer
  (:require
    [athens.parser :as parser]
    [instaparse.core :as insta]
    [re-frame.core :refer [subscribe]]
    [reitit.frontend.easy :as rfee]))


(declare parse-and-render)


;; Instaparse transforming docs: https://github.com/Engelberg/instaparse#transforming-the-tree

(defn transform
  "Transforms Instaparse output to Hiccup."
  [tree]
  (insta/transform
    {:block     (fn [& contents]
                  (concat [:span {:class "block"}] contents))
     :page-link (fn [title]
                  (let [id (subscribe [:block/uid [:node/title title]])]
                    [:span {:class "page-link"}
                     [:span {:style {:color "gray"}} "[["]
                     [:a {:href  (rfee/href :page {:id (:block/uid @id)})
                          :style {:text-decoration "none" :color "dodgerblue"}} title]
                     [:span {:style {:color "gray"}} "]]"]]))
     :block-ref (fn [id]
                  (let [string (subscribe [:block/string [:block/uid id]])]
                    [:span {:class "block-ref"
                            :style {:font-size "0.9em" :border-bottom "1px solid gray"}}
                     [:a {:href (rfee/href :page {:id id})} (parse-and-render (:block/string @string))]]))
     :hashtag   (fn [tag-name]
                  (let [id (subscribe [:block/uid [:node/title tag-name]])]
                    [:a {:class "hashtag"
                         :style {:color "gray" :text-decoration "none" :font-weight "bold"}
                         :href  (rfee/href :page {:id (:block/uid @id)})}
                     (str "#" tag-name)]))
     :url-image  (fn [{url :url alt :alt}]
                   [:img {:class "url-image"
                          :alt alt
                          :src url}])
     :url-link  (fn [{url :url} text]
                  [:a {:class "url-link"
                       :href url}
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
      [:span
       (vec (transform result))])))
