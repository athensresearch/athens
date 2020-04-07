(ns athens.parser
  (:require [instaparse.core :as insta]
            [reitit.frontend.easy :as rfee]
            [re-frame.core :refer [subscribe]]))

(def parser
  (insta/parser
   "S = c | link | bref | hash
    <c> = #'(\\w|\\s)+'
    link = <'[['> c <']]'>
    hash = <'#'> c | <'#'> <'[['> c <']]'>
    bref = <'(('> c <'))'>
   "))

(defn transform
  "Returns a hiccup vector"
  [tree]
  (insta/transform
   {:S (fn [x] [:span x])
    :link (fn [title]
            (let [id (subscribe [:block/uid [:node/title title]])]
              [:span
               [:span {:style {:color "gray"}} "[["]
               [:a {:href (rfee/href :page {:id (:block/uid @id)})
                    :style {:text-decoration "none" :color "dodgerblue"}} title]
               [:span {:style {:color "gray"}} "]]"]
               ]))
    :hash (fn [title]
            (let [id (subscribe [:block/uid [:node/title title]])]
              [:a {:style {:color "gray" :text-decoration "none" :font-weight "bold"}
                   :href (rfee/href :page {:id (:block/uid @id)})}
               (str "#"  title)]))
    :bref (fn [id]
            (let [string (subscribe [:block/string [:block/uid id]])]
              [:span
               [:a {:href (rfee/href :page {:id id})} (:block/string @string)]]))}
   tree))


(defn parse [str]
  (let [result (parser str)]
    (if (insta/failure? result)
      [:span {:style {:color "red"}} str]
      [:span (vec (transform result))])))
