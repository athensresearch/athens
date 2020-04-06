(ns athens.parser
  (:require [instaparse.core :as insta]
            [reitit.frontend.easy :as rfee]
            [re-frame.core :refer [subscribe]]))

(def parser
  (insta/parser
   "S = c | link | bref (*| hash*)
    <c> = #'\\w+'
    link = <'[['> c <']]'>
    hash = '#' c | '#[[' c ']]'
    bref = <'(('> c <'))'>
   "))

; [[foo]] query [:node/title "foo"] to find :block/uid
; ((bar)) query [:block/uid "bar"] to find :block/string

; does it make sense to do subscriptions here ? kinda complects things, but
; im creating the hiccup anwyays
; not sure how to show transclusions :3
(defn transform
  "Returns a hiccup vector"
  [tree]
  (insta/transform
   {:S (fn [x] [:span x])
    :link (fn [title]
            (let [id (subscribe [:block/uid [:node/title title]])]
              [:span
               [:span {:style {:color "gray"}} "[["]
               [:a {:href (rfee/href :page {:id (:block/uid @id)})} title]
               [:span {:style {:color "gray"}} "]]"]
               ]))
    :bref (fn [id]
            (let [b-string (subscribe [:block/string [:block/uid id]])]
              [:span
               [:a {:href (rfee/href :page {:id id})} (:block/string @b-string)]])
            )}
   tree))


(defn parse [str]
  (let [result (parser str)]
    (when-not (insta/failure? result) (prn "transform" (vec (transform result))))
    (if (insta/failure? result)
      [:span {:style {:color "red"}} str]
      [:span (vec (transform result))])))
