(ns athens.tracing
  (:require
   [reagent.impl.batching :as batching]
   ;; This exists only in newer versions of re-frame-10x
   ;; [day8.reagent.impl.batching :as day8.batching]
   [taoensso.tufte :as tufte]))


(defonce reagent-next-tick batching/next-tick)

;; This exists only in newer version of re-frame-10x:
;; (defonce reframe10x-next-tick day8.batching/next-tick)


(defonce stats-accumulator
  (tufte/add-accumulating-handler! {:ns-pattern "*"}))


(defonce stats-total
  (atom nil))


(defn format-stats [stats]
  (-> stats
      (tufte/format-pstats
       {:columns [:n-calls :p50 :p95 :p99 :max :clock :total]
        :format-id-fn str})
      (println)))


(defn next-tick
  [f]
  ;; Schedule a trace to be emitted after a render if
  ;; there is nothing else scheduled after that render.
  ;; This signals the end of the epoch.

  (reagent-next-tick
   ;; TODO: ideally, should also be wrapping re-frame-10x:
   ;; reframe10x-next-tick day8.batching/next-tick
   (fn []
     (tufte/profile {} (f))
     (when (false? (.-scheduled? batching/render-queue))
       (when-some [m (not-empty @stats-accumulator)]
         (when (some-> m vals first deref :stats)
           (let [stats (-> m vals first)]
             (println "Epoch Stats:")
             (format-stats stats)
             (if @stats-total
               (swap! stats-total tufte/merge-pstats stats)
               (reset! stats-total stats))
             (println "Total Stats:")
             (format-stats @stats-total))))))))


(defn patch-next-tick
  []
  (println "Patching with tufte profiling")
  (set! batching/next-tick next-tick))


;; TODO: assuming this namespace will only be loaded via shadow preloads
(patch-next-tick)
