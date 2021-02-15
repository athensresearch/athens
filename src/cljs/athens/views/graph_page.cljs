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

      [:> rfg/ForceGraph2D
       {:graphData        {:nodes nodes
                           :links links}
        #_{:nodes [{"id" "foo", "name" "name1", "val" 1}
                   {"id" "bar", "name" "name2", "val" 10}]
           :links [{"source" "foo", "target" "bar"}]}
        :width            (* 0.95 (.-innerWidth js/window))
        :height           (* 0.95 (.-innerHeight js/window))
        :linkColor        (fn [] (:border-color theme))
        :nodeCanvasObject (fn [^js node ^js ctx global-scale]
                            (let [label      (.. node -name)
                                  val        (.. node -val)
                                  x          (.. node -x)
                                  y          (.. node -y)
                                  font-size  8
                                  text-width 30
                                  radius     (/ 4 global-scale)]
                              (set! (.-font ctx) (str font-size "px IBM Plex Sans, Sans-Serif"))
                              (set! (.-filltextAlign ctx) "center")
                              (set! (.-textBaseLine ctx) "middle")
                              (set! (.-fillStyle ctx) (:header-text-color theme))
                              (.fillText ctx label
                                         (- x (/ text-width 2))
                                         (- y (/ 9 global-scale)))
                              (.beginPath ctx)
                              (.arc ctx x y
                                    (-> val js/Math.sqrt (* radius))
                                    0
                                    (* 3 js/Math.PI)
                                    false)
                              (set! (.-fillStyle ctx) (:link-color theme))
                              (.fill ctx)))}])))
