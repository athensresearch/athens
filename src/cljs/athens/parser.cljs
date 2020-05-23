(ns athens.parser
  (:require
    [instaparse.core :as insta]
    [re-frame.core :refer [subscribe]]
    [reitit.frontend.easy :as rfee]))


(declare transform parse)


(def parser
  (insta/parser
    "S = c | link | bref | hash
    <c> = #'(\\w|\\s)+'
    link = <'[['> c <']]'>
    hash = <'#'> c | <'#'> <'[['> c <']]'>
    bref = <'(('> c <'))'>
   "))


(defn transform
  "Transforms instaparse output to hiccup."
  [tree]
  (insta/transform
    {:S    (fn [x] [:span x])
     :link (fn [title]
             (let [id (subscribe [:block/uid [:node/title title]])]
               [:span
                [:span {:style {:color "gray"}} "[["]
                [:a {:href  (rfee/href :page {:id (:block/uid @id)})
                     :style {:text-decoration "none" :color "dodgerblue"}} title]
                [:span {:style {:color "gray"}} "]]"]]))
     :hash (fn [title]
             (let [id (subscribe [:block/uid [:node/title title]])]
               [:a {:style {:color "gray" :text-decoration "none" :font-weight "bold"}
                    :href  (rfee/href :page {:id (:block/uid @id)})}
                (str "#" title)]))
     :bref (fn [id]
             (let [string (subscribe [:block/string [:block/uid id]])]
               [:span {:style {:font-size "0.9em" :border-bottom "1px solid gray"}}
                [:a {:href (rfee/href :page {:id id})} (parse (:block/string @string))]]))}
    tree))


(defn parse
  [string]
  (let [result (parser string)]
    (if (insta/failure? result)
      [:span
       {:content-editable true
        :title (pr-str (insta/get-failure result))
        :style {:color "red"}}
       string]
      [:span
       {:content-editable true}
       (vec (transform result))])))
