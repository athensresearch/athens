^:cljstyle/ignore
(ns ^{:doc
      "
      Graph and controls are designed to work with local and global graph
      global graphs vs local graphs -- local graphs have an explicit root node
      and customizations are based on that where as global doesn't have an explicit root

      Relies on material ui comps for user inputs.

      Conf strategy:
      A default is set when ns is evaled in user's runtime which saves it
      to localStorage. During init load this default or local storage conf
      is loaded into re-frame db(reactive purposes)
      Every edit saves this new conf to db as well as localStorage and all
      future graphs that are opened will be based on that.
      "}
  athens.views.pages.graph
  (:require
    ["@material-ui/core/ExpansionPanel" :as ExpansionPanel]
    ["@material-ui/core/ExpansionPanelDetails" :as ExpansionPanelDetails]
    ["@material-ui/core/ExpansionPanelSummary" :as ExpansionPanelSummary]
    ["@material-ui/core/Slider" :as Slider]
    ["@material-ui/core/Switch" :as Switch]
    ["@material-ui/icons/KeyboardArrowRight" :default KeyboardArrowRight]
    ["@material-ui/icons/KeyboardArrowUp" :default KeyboardArrowUp]
    ["react-force-graph-2d" :as ForceGraph2D]
    [athens.db :as db]
    [athens.router :as router]
    [athens.style :as styles]
    [athens.util :as util]
    [cljs.reader :refer [read-string]]
    [clojure.set :as set]
    [datascript.core :as d]
    [re-frame.core :as rf :refer [dispatch subscribe]]
    [reagent.core :as r]
    [reagent.dom :as dom]
    [stylefy.core :as stylefy :refer [use-style]]))


;; all graph refs(react refs) reside in this atom
  ;; saving this to re-frame db is not ideal because of serialization
  ;; and objects losing their refs
(def graph-ref-map (r/atom {}))


;;-------------------------------------------------------------------
;;--- material ui ---


(def m-slider (r/adapt-react-class (.-default Slider)))


(def m-expansion-panel (r/adapt-react-class (.-default ExpansionPanel)))


(def m-expansion-panel-details (r/adapt-react-class (.-default ExpansionPanelDetails)))


(def m-expansion-panel-summary (r/adapt-react-class (.-default ExpansionPanelSummary)))


(def m-switch (r/adapt-react-class (.-default Switch)))


;;-------------------------------------------------------------------
;;--- re-frame stuff ---
;;--- read comments at top of file for more ---


(rf/reg-sub
  :graph/conf
  (fn [db _]
    (:graph-conf db)))


(rf/reg-event-db
  :graph/set-graph-ref
  (fn [db [_ key val]]
    (assoc-in db [:graph-ref key] val)))


(rf/reg-sub
  :graph/ref
  (fn [db [_ key]]
    (get-in db [:graph-ref key])))


(rf/reg-event-db
  :graph/set-conf
  (fn [db [_ key val]]
    (let [n-gc (-> db :graph-conf (assoc key val))]
      (js/localStorage.setItem "graph-conf" n-gc)
      (assoc db :graph-conf n-gc))))


(rf/reg-event-db
  :graph/load-graph-conf
  (fn [db _]
    (let [conf (or (some->> "graph-conf" js/localStorage.getItem read-string)
                   db/default-graph-conf)]
      (js/localStorage.setItem "graph-conf" conf)
      (assoc db :graph-conf conf))))


(dispatch [:graph/load-graph-conf])


;;-------------------------------------------------------------------
;;--- graph data ---


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
        nodes-with-refs    (d/q '[:find ?e ?u ?t (count ?r)
                                  :in $ [?e ...]
                                  :where
                                  [?e :node/title ?t]
                                  [?e :block/uid ?u]
                                  [?r :block/refs ?e]]
                                @db/dsdb nodes-with-refs)
        nodes-without-refs (d/q '[:find ?e ?u ?t ?c
                                  :in $ [?e ...]
                                  :where
                                  [?e :node/title ?t]
                                  [?e :block/uid ?u]
                                  [(get-else $ ?e :always-nil-value 1) ?c]]
                                @db/dsdb nodes-without-refs)
        all-nodes          (map (fn [[e u t val]]
                                  {"id"    e
                                   "uid"   u
                                   "label" t
                                   "val"   val})
                                (concat nodes-with-refs nodes-without-refs))]
    all-nodes))


(defn build-links
  []
  (->> (d/q '[:find ?e ?u ?r
              :where
              [?e :node/title ?t]
              [?e :block/uid ?u]
              [?r :block/refs ?e]]
            @db/dsdb)
       (map (fn [[node-eid node-uid ref]]
              (let [first-parent (-> ref
                                     db/get-parents-recursively
                                     first)]
                {"source"     (:db/id first-parent)
                 "source-uid" node-uid
                 "target"     node-eid
                 "target-uid" (:block/uid first-parent)})))
       (remove (fn [x]
                 (or (= (get x "source") nil)
                     (= (get x "target") nil))))))


(defn linked-nodes
  [all-links node-id]
  (->> all-links
       (map (fn [link]
              (cond
                (= (get link "source") node-id)
                (get link "target")

                (= (get link "target") node-id)
                (get link "source")

                :else nil)))
       (remove nil?)
       set))


(defn n-level-linked
  "Nodes that are n levels away from current node"
  [all-links node-id levels]
  (loop [cur-nodes #{node-id}
         levels    levels]
    (if (= levels 0)
      cur-nodes
      (recur (apply set/union cur-nodes (mapv #(linked-nodes all-links %) cur-nodes))
             (- levels 1)))))


;;-------------------------------------------------------------------
;;--- comps ---


(defn graph-control-style
  [theme]
  {:position        "absolute"
   :right           "10px"
   :font-size       "14px"
   :z-index         2
   ::stylefy/manual [[:.MuiExpansionPanelDetails-root {:flex-flow "column"
                                                       :color     "grey"}
                      [:.switch {:display         "flex"
                                 :justify-content "space-between"
                                 :align-items     "center"}]]
                     [:.MuiSvgIcon-root {:font-size "1.2rem"}]
                     [:.MuiExpansionPanelSummary-content {:justify-content "space-between"}
                      [:&.Mui-expanded {:margin     "5px 0"
                                        :min-height "unset"}]]
                     [:.MuiExpansionPanelSummary-root
                      [:&.Mui-expanded {:min-height "unset"}]]
                     [:.MuiPaper-root {:background (:graph-control-bg theme)
                                       :color      (:graph-control-color theme)
                                       :margin     "0 0 2px 0"}
                      [:&.Mui-expanded {:margin "0 0 5px 0"}]]]})


(defn expansion-panel
  [{:keys [heading controls]} local-node-eid]
  (r/with-let [is-open? (r/atom false)]
              (let [graph-conf @(subscribe [:graph/conf])
                    graph-ref  (get @graph-ref-map (or local-node-eid :global))]
                [m-expansion-panel
                 [m-expansion-panel-summary
                  {:onClick #(swap! is-open? not)}
                  [:<> [:span heading] (if @is-open? [:> KeyboardArrowUp] [:> KeyboardArrowRight])]]
                 [m-expansion-panel-details
                  (doall
                    (for [{:keys [key comp label onChange no-simulation-reheat? props class]} controls]
                      ^{:key key}
                      [:div {:class class} label
                       [comp
                        (merge
                          props
                          {:value    (or (key graph-conf) (key db/default-graph-conf))
                           :color    "primary"
                           :onChange (fn [_ n-val]
                                       (and onChange (onChange n-val))
                                       (rf/dispatch [:graph/set-conf key n-val])
                                       (when-not no-simulation-reheat?
                                         (.d3ReheatSimulation graph-ref)))})]]))]])))


(defn graph-controls
  "Uses a generic expansion panel(not super generic)
   while this comp dictates all the controls and manipulations that can be made to the graph
   Look at comment below for code theme - to get a sense of the structure"
  ([] [graph-controls nil])
  ([local-node-eid]
   (fn []
     (let [graph-conf     @(subscribe [:graph/conf])
           graph-ref      (get @graph-ref-map (or local-node-eid :global))
           theme          (if @(rf/subscribe [:theme/dark]) styles/THEME-DARK styles/THEME-LIGHT)

           ;; code theme
           ;; category -- for eg node-section and section related data
             ;; controls -- for eg node-controls and their props
               ;; props -- for eg orphans? inside a control are props for the editing-comp(for slider or toggle)
               ;; other-keys describe more about the comp
           node-controls  [{:key                   :hlt-link-levels
                            :label                 "No. of link levels to highlight"
                            :props                 {:min   1
                                                    :max   5
                                                    :step  1
                                                    :marks true}
                            :comp                  m-slider
                            :no-simulation-reheat? true}
                           {:key                   :orphans?
                            :label                 "Orphan nodes"
                            :comp                  m-switch
                            :props                 {:checked (:orphans? graph-conf)}
                            :class                 "switch"
                            :no-simulation-reheat? true}
                           {:key                   :daily-notes?
                            :label                 "Daily notes"
                            :comp                  m-switch
                            :props                 {:checked (:daily-notes? graph-conf)}
                            :class                 "switch"
                            :no-simulation-reheat? true}]
           node-section   {:heading  "Nodes"
                           :controls node-controls}


           force-controls [{:key      :link-distance
                            :label    "Link Distance"
                            :props    {:min 5
                                       :max 95}
                            :comp     m-slider
                            :class    "slider"
                            :onChange (fn [val] (and graph-ref (.. graph-ref (d3Force "link") (distance val))))}
                           {:key      :charge-strength
                            :label    "Attraction force"
                            :props    {:min -30
                                       :max 0}
                            :comp     m-slider
                            :class    "slider"
                            :onChange (fn [val] (and graph-ref (.. graph-ref (d3Force "charge") (strength val))))}]
           force-section  {:heading  "Forces"
                           :controls force-controls}

           local-controls [{:key                   :local-depth
                            :label                 "Local Depth"
                            :props                 {:min   1
                                                    :max   5
                                                    :step  1
                                                    :marks true}
                            :class                 "slider"
                            :comp                  m-slider
                            :no-simulation-reheat? true}
                           {:key                   :root-links-only?
                            :label                 "Only root links"
                            :comp                  m-switch
                            :class                 "switch"
                            :props                 {:checked (:root-links-only? graph-conf)}
                            :no-simulation-reheat? true}]
           local-section  {:heading  "Local options"
                           :controls local-controls}]
       [:div (use-style (graph-control-style theme))
        (doall
          (for [{:keys [heading] :as section} (remove nil? [(when-not local-node-eid
                                                              node-section)
                                                            force-section
                                                            (when local-node-eid
                                                              local-section)])]
            ^{:key heading}
            [expansion-panel section local-node-eid]))]))))


(defn graph-root
  "Main graph-root where react-force-graph comp is rendered
   Flow:
    build-links -> find nodes based on conf or build all nodes(local, daily and orphan node filter)
    further filter down links based on nodes(cleaning up)"
  ([] [graph-root nil])
  ([local-node-eid]
   (let [highlight-nodes (r/atom #{})
         highlight-links (r/atom #{})
         dimensions      (r/atom {})]
     (r/create-class
       {:component-did-mount
        (fn [this]
          (let [dom-node   (dom/dom-node this)
                graph-conf @(subscribe [:graph/conf])
                graph-ref  (get @graph-ref-map (or local-node-eid :global))]
            ;; set canvas dimensions
            (swap! dimensions assoc :width (-> dom-node (.. (closest ".graph-page"))
                                               .-parentNode .-clientWidth))
            (swap! dimensions assoc :height (-> dom-node (.. (closest ".graph-page"))
                                                .-parentNode .-clientHeight))
            ;; set init forces for graph
            (when graph-ref
              (.. (.. graph-ref (d3Force "charge"))
                  (distanceMax (/ (min (:width @dimensions)
                                       (:height @dimensions))
                                  2)))
              (let [c-force (.. graph-ref (d3Force "center"))]
                (c-force (/ (:width @dimensions) 2) (/ (:height @dimensions) 2)))

              (.. (.. graph-ref (d3Force "charge")) (strength (:charge-strength graph-conf)))
              (.. (.. graph-ref (d3Force "link")) (distance (:link-distance graph-conf)))
              (.d3ReheatSimulation graph-ref))))

        :component-will-unmount
        (fn [_this]
          (swap! graph-ref-map assoc (or local-node-eid :global) nil))

        :reagent-render
        (fn [local-node-eid]
          (let [dark?                            @(rf/subscribe [:theme/dark])
                graph-conf                       @(subscribe [:graph/conf])
                all-links                        (build-links)
                all-nodes-with-links             (->> all-links (mapcat #(vals %)) set)
                linked-nodes-without-daily-notes (->> all-links
                                                      (remove (fn [link]
                                                                (or (util/is-daily-note (get link "source-uid"))
                                                                    (util/is-daily-note (get link "target-uid")))))
                                                      (mapcat #(vals %))
                                                      set)
                nodes                            (cond->> (if local-node-eid
                                                            (->> (n-level-linked all-links local-node-eid (:local-depth graph-conf))
                                                                 (d/q '[:find ?e ?u ?t (count ?r)
                                                                        :in $ [?e ...]
                                                                        :where
                                                                        [?e :node/title ?t]
                                                                        [?e :block/uid ?u]
                                                                        [?r :block/refs ?e]]
                                                                      @db/dsdb)
                                                                 (map (fn [[e u t _val]]
                                                                        {"id"    e
                                                                         "uid"   u
                                                                         "label" t
                                                                         "val"   (if (= e local-node-eid) 8 1)}))
                                                                 (remove (fn [node-obj]
                                                                           (nil? (get node-obj "uid"))))
                                                                 doall)
                                                            (build-nodes))

                                                   (not (:daily-notes? graph-conf))
                                                   (remove (fn [node]
                                                             (util/is-daily-note (get node "uid"))))

                                                   (not (:orphans? graph-conf))
                                                   (filter (fn [node]
                                                             (contains? all-nodes-with-links (get node "id"))))

                                                   (and (not (:daily-notes? graph-conf))
                                                        (not (:orphans? graph-conf)))
                                                   (filter (fn [node]
                                                             (contains? linked-nodes-without-daily-notes (get node "id")))))

                filtered-nodes-set               (->> nodes (map #(get % "id")) set)

                links                            (cond->> all-links

                                                   (or local-node-eid
                                                       (not (:daily-notes? graph-conf))
                                                       (not (:orphans? graph-conf)))
                                                   (filter (fn [link-obj]
                                                             (and (contains? filtered-nodes-set (get link-obj "source"))
                                                                  (contains? filtered-nodes-set (get link-obj "target")))))

                                                   (and local-node-eid
                                                        (:root-links-only? graph-conf)
                                                        (= (:local-depth graph-conf) 1))
                                                   (filter (fn [link-obj]
                                                             (or (= (get link-obj "source") local-node-eid)
                                                                 (= (get link-obj "target") local-node-eid))))

                                                   true
                                                   (filter (fn [link-obj]
                                                             (or (contains? filtered-nodes-set (get link-obj "source"))
                                                                 (contains? filtered-nodes-set (get link-obj "target"))))))

                theme                            (if dark?
                                                   styles/THEME-DARK
                                                   styles/THEME-LIGHT)]
            [:> ForceGraph2D
             {:graphData        {:nodes nodes
                                 :links links}
              ;; example data
              #_{:nodes [{"id" "foo", "name" "name1", "val" 1}
                         {"id" "bar", "name" "name2", "val" 10}]
                 :links [{"source" "foo", "target" "bar"}]}
              :width            (:width  @dimensions)
              :height           (:height @dimensions)
              :ref              #(swap! graph-ref-map assoc (or local-node-eid :global) %)
              ;; link
              :linkColor        (fn [] (:graph-link-normal theme))
              ;; node
              :nodeCanvasObject (fn [^js node ^js ctx global-scale]
                                  (let [label            (.. node -label)
                                        val              (.. node -val)
                                        node-id          (.. node -id)
                                        x                (.. node -x)
                                        y                (.. node -y)
                                        scale-factor     3
                                        font-size        (/ 10 global-scale)
                                        text-width       (.. ctx (measureText label) -width)
                                        radius           (max 1.3 (-> (js/Math.sqrt val)
                                                                      (/ global-scale)
                                                                      (* scale-factor)))
                                        highlighted?     (contains? @highlight-nodes node-id)
                                        local-root-node? (and local-node-eid node-id (= local-node-eid node-id))]

                                    ;; node color
                                    (set! (.-fillStyle ctx)
                                          (cond
                                            local-root-node? (:graph-node-hlt theme)
                                            (and highlighted? (not local-node-eid)) (:graph-node-hlt theme)
                                            :else (:graph-node-normal theme)))

                                    ;; text
                                    (when (> global-scale 1.75)
                                      (set! (.-font ctx) (str (when (and highlighted? (not local-node-eid))
                                                                "bold ")
                                                              font-size "px IBM Plex Sans, Sans-Serif"))
                                      (.fillText ctx label
                                                 (- x (/ text-width 2))
                                                 (+ y radius font-size)))

                                    (.beginPath ctx)
                                    ;; https://developer.mozilla.org/en-US/docs/Web/API/CanvasRenderingContext2D/arc
                                    (.arc ctx x y radius 0 (* js/Math.PI 2))
                                    (.fill ctx)))
              ;; node actions
              :onNodeClick      (fn [^js node ^js event]
                                  (router/navigate-uid (.. node -uid) event))
              :onNodeHover      (fn [^js node]
                                  (let [_          (reset! highlight-nodes #{})
                                        _          (reset! highlight-links #{})
                                        graph-conf @(rf/subscribe [:graph/conf])]
                                    (when-let [node-id (some-> node (.. -id))]
                                      (reset! highlight-nodes (n-level-linked all-links node-id (:hlt-link-levels graph-conf))))))}]))}))))


(defn page
  "Designed to work with local or global graphs
   Keep in mind block-uid -> db/id (more convenient)"
  ([] [page nil])
  ([block-uid]
   (let [local-node-eid (when block-uid
                          (->> [:block/uid block-uid] (d/pull @db/dsdb '[:db/id])
                               :db/id))]
     [:div.graph-page
      {:style (merge (when local-node-eid {:min-height "500px"})
                     {:position "relative"})}
      [graph-controls local-node-eid]
      [graph-root local-node-eid]])))
