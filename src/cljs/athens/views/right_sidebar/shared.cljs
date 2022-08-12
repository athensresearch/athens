(ns athens.views.right-sidebar.shared
  (:require
    [athens.reactive :as reactive]
    [re-frame.core :as rf]))

(def NS "athens/right-sidebar")

(defn ns-str
  ([]
   (ns-str ""))
  ([s]
   (str NS s)))

(defn get-open?
  []
  (let [user-page @(rf/subscribe [:presence/user-page])
        sidebar-props (-> (reactive/get-reactive-node-document [:node/title user-page])
                          :block/properties
                          (get NS)
                          :block/properties)
        open? (->> (get sidebar-props (ns-str "/open?"))
                   boolean)]
    open?))

