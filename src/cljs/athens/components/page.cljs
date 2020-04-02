(ns athens.components.page
  (:require [re-frame.core :refer [subscribe]]
            [reitit.frontend.easy :as rfee]
            )
  )

;    [:span.attribute (clojure.string/replace-first string #"(\w+)::" "$1:")]
(defn replace-attr [string]
  (let [[_match attr rst] (re-find #"^(\w+)::(.*$)" string)
        node (subscribe [:block/uid [:node/title attr]])]
    [:p>span
     [:a.ref-attr {:href (rfee/href :page {:block-uid (:block/uid @node)})} (str attr ":" )]
     rst]))

; how to do this recursively?
; and must include rest of line :)
(defn replace-link [string]
  (let [[match link rst] (re-find #"\[\[(\w+)\]\]" string)
        node (subscribe [:block/uid [:node/title link]])]
    [:p>span
     [:a.ref-attr {:href (rfee/href :page {:block-uid (:block/uid @node)})} match]]))

(defn replace-tag [])


(defn render-block-string
  "attr:: at beginning of blockstring into a link and bold
   [[]] recursively link and
   (()) recursively link, underline transclude
   # and #[[]] into link and gray
"
  [string]
  (fn []
    (cond
      (re-find #"^(\w+)::(.*$)" string) [replace-attr string]
      (re-find #"\[\[\w+\]\]" string) [replace-link string]
      :else [:p string]
      )
;    [:p [replace-attr string]]
;    [replace-link string]
    ))

;; (defn render-node [node-eid]
;;   (let [node @(subscribe [:node node-eid])
;;         blocks @(subscribe [:blocks node-eid])]
;;     [:div
;;      [:h1 (:node/title node)]
;;      [:h4 "Attributes"]
;;      [:ul (map (fn [[k v]] [:li {:key (str k ":" v)} (str k ": " v)]) node)]
;;      [:h4 "Blocks"]
;;      [:div (for [b (:block/children blocks)]
;;              (render-block b))
;;       ]]))

(defn render-blocks [block-uid]
  (fn [block-uid]
    (let [block (subscribe [:block/children [:block/uid block-uid]])]
      [:div
       (for [ch (:block/children @block)]
         (let [{:block/keys [string uid children]} ch]
           ^{:key uid}
           [:div
            [render-block-string string]
            [:div {:style {:margin-left 20}}
             [render-blocks uid]]]))])))


(defn main []
  (let [current-route (subscribe [:current-route])]
    (fn []
      (let [node (subscribe [:node [:block/uid (-> @current-route :path-params :block-uid)]])]
        [:div
         [:h1 "Page Panel"]
         [:h2 (:node/title @node)]
         [render-blocks (-> @current-route :path-params :block-uid)]
         ]))))
