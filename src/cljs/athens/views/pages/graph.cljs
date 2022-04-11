^:cljstyle/ignore
(ns ^{:doc
      "
      Graph and controls are designed to work with local and global graph
      global graphs vs local graphs -- local graphs have an explicit root node
      and customizations are based on that where as global doesn't have an explicit root"}
 athens.views.pages.graph
  (:require
   ["@chakra-ui/react" :refer [Box Switch VStack FormControl FormLabel Input Accordion AccordionButton AccordionItem AccordionPanel]]
   ["react-force-graph-2d" :as ForceGraph2D]
   [athens.dates :as dates]
   [athens.db :as db]
   [athens.router :as router]
   [clojure.set :as set]
   [datascript.core :as d]
   [re-frame.core :as rf :refer [subscribe]]
   [reagent.core :as r]
   [reagent.dom :as dom]))


(def THEME-DARK
  {:graph-node-normal   "hsla(0, 0%, 100%, 0.57)"
   :graph-node-hlt      "#498eda"
   :graph-link-normal   "#ffffff11"})


(def THEME-LIGHT
  {:graph-node-normal   "#909090"
   :graph-node-hlt      "#0075E1"
   :graph-link-normal   "#cfcfcf"})


;; all graph refs(react refs) reside in this atom
;; saving this to re-frame db is not ideal because of serialization
;; and objects losing their refs
(def graph-ref-map (r/atom {}))


;; -------------------------------------------------------------------
;; --- re-frame stuff ---
;; --- read comments at top of file for more ---


(rf/reg-sub
  :graph/conf
  (fn [db _]
    (-> db :athens/persist :graph-conf)))


(rf/reg-event-fx
  :graph/set-conf
  (fn [{:keys [db]} [_ k v]]
    {:db (update-in db [:athens/persist :graph-conf] #(assoc % k v))
     :dispatch [:posthog/report-feature :graph]}))


(rf/reg-sub
  :graph/ref
  (fn [db [_ key]]
    (get-in db [:graph-ref key])))


(rf/reg-event-db
  :graph/set-graph-ref
  (fn [db [_ key val]]
    (assoc-in db [:graph-ref key] val)))


;; -------------------------------------------------------------------
;; --- graph data ---


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


;; -------------------------------------------------------------------
;; --- comps ---


(defn graph-controls
  [local-node-eid]
  (fn []
    (let [graph-conf     @(subscribe [:graph/conf])
          graph-ref      (get @graph-ref-map (or local-node-eid :global))]
      [:> Accordion {:width "14em"
                     :position "absolute"
                     :bg "background.basement"
                     :overflow "hidden"
                     :p 0
                     :borderRadius "md"
                     :allowToggle true
                     :allowMultiple true
                     :bottom 2
                     :right 2}
       (when-not local-node-eid
         [:> AccordionItem {:borderTop 0}
          [:> AccordionButton {:borderRadius "sm"}
           "Nodes"]
          [:> AccordionPanel
           [:> VStack {:align "stretch"}
            [:> FormControl
             [:> FormLabel "Highlighted link levels"]
             [:> Input {:type "number"
                        :value (or (:hlt-link-levels graph-conf) 1)
                        :min 1
                        :max 5
                        :step 1
                        :onChange (fn [e] (rf/dispatch [:graph/set-conf :hlt-link-levels (.. e -target -value)]))}]]
            [:> Switch {:isChecked (:orphans? graph-conf)
                        :onChange (fn [e]
                                    (rf/dispatch [:graph/set-conf :orphans? (.. e -target -checked)])
                                    (.d3ReheatSimulation graph-ref))}
             "Orphan nodes"]
            [:> Switch {:isChecked (:daily-notes? graph-conf)
                        :onChange (fn [e]
                                    (rf/dispatch [:graph/set-conf :daily-notes? (.. e -target -checked)])
                                    (.d3ReheatSimulation graph-ref))}
             "Daily notes"]]]])
       [:> AccordionItem
        [:> AccordionButton {:borderRadius "sm"}
         "Forces"]
        [:> AccordionPanel
         [:> VStack {:align "stretch"}
          [:> FormControl
           [:> FormLabel "Link distance"]
           [:> Input {:type "number"
                      :value (:link-distance graph-conf)
                      :min 5
                      :max 95
                      :step 10
                      :onChange (fn [e]
                                  ((and graph-ref (.. graph-ref (d3Force "link") (distance (.. e -target -value)))))
                                  (.d3ReheatSimulation graph-ref))}]]
          [:> FormControl
           [:> FormLabel "Attraction force"]
           [:> Input {:type "number"
                      :value (:charge-strength graph-conf)
                      :min -30
                      :max 0
                      :step 5
                      :onChange (fn [e]
                                  ((and graph-ref (.. graph-ref (d3Force "charge") (distance (.. e -target -value)))))
                                  (.d3ReheatSimulation graph-ref))}]]]]]
       (when local-node-eid
         [:> AccordionItem
          [:> AccordionButton {:borderRadius "sm"}
           "Local options"]
          [:> AccordionPanel
           [:> VStack {:align "stretch"}
            [:> FormControl
             [:> FormLabel "Local depth"]
             [:> Input {:type "number"
                        :value (:local-depth graph-conf)
                        :min 1
                        :max 5
                        :step 1
                        :onChange (fn [e] (rf/dispatch [:graph/set-conf :local-depth (.. e -target -value)]))}]]
            [:> Switch {:isChecked (:root-links-only? graph-conf)
                        :onChange (fn [e]
                                    (rf/dispatch [:graph/set-conf :root-links-only? (.. e -target -checked)]))}
             "Only root links?"]]]])])))


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
                dom-root (if local-node-eid ".graph-page" "#app")
                graph-conf @(subscribe [:graph/conf])
                graph-ref  (get @graph-ref-map (or local-node-eid :global))]
            ;; set canvas dimensions
            (swap! dimensions assoc :width (-> dom-node (.. (closest dom-root))
                                               .-parentNode .-clientWidth))
            (swap! dimensions assoc :height (-> dom-node (.. (closest dom-root))
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
                                                                (or (dates/is-daily-note (get link "source-uid"))
                                                                    (dates/is-daily-note (get link "target-uid")))))
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
                                                             (dates/is-daily-note (get node "uid"))))

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
                                                   THEME-DARK
                                                   THEME-LIGHT)]
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
              :onNodeClick (fn [^js node ^js event]
                             (let [shift? (.-shiftKey event)]
                               (rf/dispatch [:reporting/navigation {:source :graph
                                                                    :target :page
                                                                    :pane   (if shift?
                                                                              :right-pane
                                                                              :main-pane)}])
                               (router/navigate-page (.. node -label) event)))
              :onNodeHover (fn [^js node]
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
     [:<>
      [:> Box (if local-node-eid
                {:class "graph-page"
                 :alignSelf "stretch"
                 :justifySelf "stretch"
                 :overflow "hidden"
                 :height "20em"
                 :borderRadius "lg"
                 :bg "background.basement"
                 :position "relative"}
                {:class "graph-page"
                 :gridColumn "1 / -1"
                 :position "fixed"
                 :top 0
                 :left 0
                 :width "100vw"
                 :height "100vh"})
       [graph-root local-node-eid]]
      [graph-controls local-node-eid]])))
