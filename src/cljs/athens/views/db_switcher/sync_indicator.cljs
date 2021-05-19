(ns athens.views.db-switcher.sync-indicator)

(defn sync-indicator
  ([{:keys [status]}]
   (when (= status "syncing")
     [:circle
      {:cx "calc(100% - 0.125rem)"
       :cy "0.125rem"
       :fill "var(--confirmation-color)"
       :r "0.25rem"
       :stroke "#fff"
       :vectorEffect "non-scaling-stroke"}])))