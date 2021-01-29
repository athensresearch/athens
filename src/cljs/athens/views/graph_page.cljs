(ns athens.views.graph-page
  (:require
    [athens.db :as db]
    [datascript.core :as d]
    ["react-force-graph" :refer [ForceGraph2D]]))
    ;;[reagent.core :as r]
    ;;[reagent.dom :as rdom]
    ;;[stylefy.core :as stylefy :refer [use-style]]))

(defn query-nodes-and-refs
  []
  (d/q '[:find ?e ?t ?r
         :in $
         :where
         [?e :node/title ?t]
         [?r :block/refs ?e]]
       @db/dsdb))

(defn build-nodes
  [nodes-and-refs]
  (->> nodes-and-refs
       (group-by (fn [x]
                   [(first x) (second x)]))
       (map (fn [[[e t] v]]
              {"id"   e
               "name" t
               "val"  (count v)}))))

(defn build-links
  [nodes-and-refs]
  (->> nodes-and-refs
       (map (fn [[source e target-block]]
              {"source" source
               "target" (-> target-block
                            db/get-parents-recursively
                            first
                            :db/id)}))))

;;(let [nodes-and-refs (query-nodes-and-refs)]
;;  (build-nodes nodes-and-refs)
;;  (build-links nodes-and-refs))

(defn graph-page
  []
  (let []
    (fn []
      (let [nodes-and-refs (query-nodes-and-refs)
            nodes          (build-nodes nodes-and-refs)
            links (build-links nodes-and-refs)]
        [:div {:style {:display "flex"}}
         [:> ForceGraph2D
          {:graphData        {:nodes nodes :links links}
                             #_{:nodes [{"id" "foo", "name" "name1", "val" 1}
                                        {"id" "bar", "name" "name2", "val" 10}]
                                :links [{"source" "foo",
                                         "target" "bar"}]}
           :nodeAutoColorBy  "group"
           :width            1000
           :linkColor        "black"
           :nodeCanvasObject (fn [node ctx globalScale]
                               (let [label      (.. node -id)
                                     ;;x          (.. node -x)
                                     ;;y          (.. node -y)
                                     text-width (.. ctx (measureText label) -width)
                                     font-size  (/ 12 globalScale)
                                     [b1 b2 b3 b4] (->> [text-width font-size]
                                                        (map #(+ % (* 0.2 font-size))))]
                                 (set! (.. ctx -font) "12px Sans-Serif")
                                 (set! (.. ctx -fillStyle) "rgba(255, 255, 255, 0.8)")
                                 (.. ctx (fillRect (/ b1 2) (/ b2 2) b3 b4))

                                 (set! (.. ctx -fillStyle) (.. node -color))
                                 (.. ctx (fillText label (.. node -x) (.. node -y)))))}]]))))


