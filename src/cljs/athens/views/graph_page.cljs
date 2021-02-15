^:cljstyle/ignore
(ns athens.views.graph-page
  (:require
    ["react-force-graph" :as rfg]
    [athens.db :as db]
    [athens.style :as styles]
    [clojure.set :as set]
    [datascript.core :as d]
    [re-frame.core :as rf]))


(defn build-nodes
  []
  (let [all-nodes          (d/q '[:find [?e ...]
                                  :where
                                  [?e :node/title _]]
                                @db/dsdb)
        nodes-with-refs    (d/q '[:find [?e ...]
                                  :where
                                  [?e :node/title _]
                                  [_ :block/refs ?e]]
                                @db/dsdb)
        nodes-without-refs (set/difference (set all-nodes) (set nodes-with-refs))
        nodes-with-refs    (d/q '[:find ?e ?t (count ?r)
                                  :in $ [?e ...]
                                  :where
                                  [?e :node/title ?t]
                                  [?r :block/refs ?e]]
                                @db/dsdb nodes-with-refs)
        nodes-without-refs (d/q '[:find ?e ?t ?c
                                  :in $ [?e ...]
                                  :where
                                  [?e :node/title ?t]
                                  [(get-else $ ?e :always-nil-value 1) ?c]]
                                @db/dsdb nodes-without-refs)
        all-nodes          (map (fn [[e t val]]
                                  {"id"   e
                                   "name" t
                                   "val"  val})
                                (concat nodes-with-refs nodes-without-refs))]
    all-nodes))


(defn build-links
  []
  (->> (d/q '[:find ?e ?r
              :where
              [?e :node/title ?t]
              [?r :block/refs ?e]]
            @db/dsdb)
       (map (fn [[node-eid ref]]
              {"source" (-> ref
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
        ;; example data
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
                                  font-size  (max 10 (-> (js/Math.sqrt val)
                                                         (/ global-scale)
                                                         (* 5)))
                                  text-width (.. ctx (measureText label) -width)
                                  radius     (/ 4 global-scale)]
                              (set! (.-font ctx) (str font-size "px IBM Plex Sans, Sans-Serif"))
                              (set! (.-fillStyle ctx) (:header-text-color theme))
                              (.fillText ctx label
                                         (- x (/ text-width 2))
                                         (- y (/ 7 global-scale)))
                              (.beginPath ctx)
                              (.arc ctx x y
                                    (-> val js/Math.sqrt (* radius))
                                    0
                                    (* 3 js/Math.PI)
                                    false)
                              (set! (.-fillStyle ctx) (:link-color theme))
                              (.fill ctx)))}])))
