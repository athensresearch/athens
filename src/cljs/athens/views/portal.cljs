^:cljstyle/ignore
(ns athens.views.portal
  (:require
    ["react-dom" :refer [createPortal]]
    [goog.events :as events]
    [reagent.core :as r]))


(defn portal
  [_children click-outside-handler]
  (let [mount                (js/document.getElementById "portal")
        el                   (js/document.createElement "div")
        ref                  (atom nil)
        handle-click-outside (fn [e]
                               (when (not (.. @ref (contains (.. e -target))))
                                 (when (fn? click-outside-handler)
                                   (click-outside-handler))))]
    (r/create-class
      {:display-name           "portal"
       :component-did-mount    (fn [_this]
                                 (when mount
                                   (.. mount (appendChild el)))
                                 (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this]
                                 (when mount
                                   (.. mount (removeChild el)))
                                 (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [children]
                                 (if (not (map? (second children)))
                                   (throw (js/Error "Portal expects a hiccup form with a property map as the second item, e.g. [:div {}]"))
                                   (let [wrapped-children (update-in children [1]
                                                                     #(merge %
                                                                             {:on-mouse-down (fn [e] (.. e preventDefault))
                                                                              :ref           (fn [e] (reset! ref e))}))]
                                     (createPortal (r/as-element wrapped-children) el))))})))
