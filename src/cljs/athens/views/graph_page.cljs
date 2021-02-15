(ns athens.views.graph-page
  (:require
    [athens.db :as db]
    [athens.style :as styles]
    [datascript.core :as d]
    [re-frame.core :as rf]
    ["react-force-graph" :as rfg]))


(defn build-nodes
  []
  (let [nodes                        (d/q '[:find [?e ...]
                                            :where
                                            [?e :node/title _]]
                                          @db/dsdb)
        nodes-with-edges             (d/q '[:find [?e ...]
                                            :where
                                            [?e :node/title _]
                                            [_ :block/refs ?e]]
                                          @db/dsdb)
        nodes-without-edges          (clojure.set/difference (set nodes) (set nodes-with-edges))
        nodes-with-edges-with-val    (->> (d/q '[:find ?e ?t (count ?r)
                                                 :in $ [?e ...]
                                                 :where
                                                 [?e :node/title ?t]
                                                 [?r :block/refs ?e]]
                                               @db/dsdb nodes-with-edges)
                                          (map (fn [[e t val]]
                                                 {"id"   e
                                                  "name" t
                                                  "val"  val})))
        nodes-without-edges-with-val (->> (d/q '[:find ?e ?t
                                                 :in $ [?e ...]
                                                 :where
                                                 [?e :node/title ?t]]
                                               @db/dsdb nodes-without-edges)
                                          (map (fn [[x t]]
                                                 {"id"   x
                                                  "name" t
                                                  "val"  1})))
        final-nodes                  (concat nodes-with-edges-with-val nodes-without-edges-with-val)]
    final-nodes))


(defn build-links
  []
  (->> (d/q '[:find ?e ?r
              :where
              [?e :node/title ?t]
              [?r :block/refs ?e]]
            @db/dsdb)
       (map (fn [[node-eid ref]]
              {"source"(-> ref
                           db/get-parents-recursively
                           first
                           :db/id)
               "target" node-eid}))))


(defn graph-page
  []
  (fn []
    (let [dark? @(rf/subscribe [:theme/dark])
          nodes (build-nodes)
          links (build-links)
          theme (if dark? styles/THEME-DARK
                          styles/THEME-LIGHT)]
      [:div {:style {:display "flex"}}
       [:> rfg/ForceGraph2D
        {:graphData        {:nodes nodes
                            :links links}
         #_{:nodes [{"id" "foo", "name" "name1", "val" 1}
                    {"id" "bar", "name" "name2", "val" 10}]
            ;;:links []
            :links [{"source" "foo", "target" "bar"}]}
         :nodeAutoColorBy  "group"
         :width            2048
         :height           1100
         :linkColor        "white"
         :nodeCanvasObject (fn [^js node ^js ctx global-scale]
                             (let [label      (.. node -name)
                                   val        (.. node -val)
                                   font-size  8
                                   arc-radius (/ 4 global-scale)
                                   _          (set! (.-font ctx) (str font-size "px Inter"))
                                   text-width 30
                                   x          (.. node -x)
                                   y          (.. node -y)
                                   color      (.. node -color)]
                               (set! (.-filltextAlign ctx) "center")
                               (set! (.-textBaseLine ctx) "middle")
                               (set! (.-fillStyle ctx) (:body-text-color theme))
                               (.fillText ctx label
                                          (- x (/ text-width 2))
                                          (- y (/ 9 global-scale)))
                               (.beginPath ctx)
                               (.arc ctx x y (if (zero? val)
                                               arc-radius
                                               (* arc-radius (js/Math.sqrt (js/Math.sqrt val)))) 0 (* 2 js/Math.PI) false)
                               (set! (.-fillStyle ctx) (:link-color theme))
                               (.fill ctx)))}]])))
