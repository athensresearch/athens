(ns athens.parser
  (:require
    [athens.parse-helper :as parse-helper]
    [instaparse.core :as insta :refer-macros [defparser]]
    [re-frame.core :refer [subscribe]]
    [reitit.frontend.easy :as rfee]))


(declare block-parser transform parse)


;; Instaparse docs: https://github.com/Engelberg/instaparse#readme

(defparser block-parser
  "(* This first rule is the top-level one. *)
   block = ( syntax-in-block / any-char )*
   (* `/` ordered alternation is used to, for example, try to interpret a string beginning with '[[' as a block-link before interpreting it as raw characters. *)
   
   <syntax-in-block> = (block-link | block-ref | hashtag)
   
   block-link = <'[['> any-chars <']]'>
   
   block-ref = <'(('> any-chars <'))'>
   
   hashtag = <'#'> any-chars | <'#'> <'[['> any-chars <']]'>
   
   (* It’s useful to extract this rule because its transform joins the individual characters everywhere it’s used. *)
   (* However, I think in many cases a more specific rule can be used. So we will migrate away from uses of this rule. *)
   any-chars = any-char+
   
   <any-char> = #'\\w|\\W'
   ")


(defn transform
  "Transforms Instaparse output to Hiccup."
  [tree]
  (insta/transform
    {:block      (fn [& raw-contents]
                   ;; use combine-adjacent-strings to collapse individual characters from any-char into one string
                   (let [collapsed-contents (parse-helper/combine-adjacent-strings raw-contents)]
                     (concat [:span {:class "block"}] collapsed-contents)))
     :any-chars  (fn [& chars] (clojure.string/join chars))
     :block-link (fn [title]
                   (let [id (subscribe [:block/uid [:node/title title]])]
                     [:span {:class "block-link"}
                      [:span {:style {:color "gray"}} "[["]
                      [:a {:href  (rfee/href :page {:id (:block/uid @id)})
                           :style {:text-decoration "none" :color "dodgerblue"}} title]
                      [:span {:style {:color "gray"}} "]]"]]))
     :block-ref  (fn [id]
                   (let [string (subscribe [:block/string [:block/uid id]])]
                     [:span {:class "block-ref"
                             :style {:font-size "0.9em" :border-bottom "1px solid gray"}}
                      [:a {:href (rfee/href :page {:id id})} (parse (:block/string @string))]]))
     :hashtag    (fn [tag-name]
                   (let [id (subscribe [:block/uid [:node/title tag-name]])]
                     [:a {:class "hashtag"
                          :style {:color "gray" :text-decoration "none" :font-weight "bold"}
                          :href  (rfee/href :page {:id (:block/uid @id)})}
                      (str "#" tag-name)]))}
    tree))


(defn parse
  "Converts a string of block syntax to Hiccup, with fallback formatting if it can’t be parsed."
  [string]
  (let [result (block-parser string)]
    (if (insta/failure? result)
      [:span
       {:content-editable true
        :title (pr-str (insta/get-failure result))
        :style {:color "red"}}
       string]
      [:span
       {:content-editable true}
       (vec (transform result))])))
