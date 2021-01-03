(ns athens.views.portal
  (:require ["react-dom" :refer [createPortal]]
            [reagent.core :as r]
            [goog.events :as events]
            [athens.views.dropdown :as dropdown]
            [stylefy.core :as stylefy]))


(defn portal
  [children click-outside-handler]
  (let [mount (js/document.getElementById "portal")
        el    (js/document.createElement "div")
        ref                  (atom nil)
        handle-click-outside (fn [e]
                               (when (not (.. @ref (contains (.. e -target))))
                                 (click-outside-handler)))]
    (r/create-class
      {:display-name           "portal"
       :component-did-mount    (fn [_this]
                                 (.. mount (appendChild el))
                                 (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this]
                                 (.. mount (removeChild el))
                                 (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [children]
                                 (let [wrapped-children (update-in children [1]
                                                                   #(merge %
                                                                           {:on-mouse-down (fn [e] (.. e preventDefault))
                                                                            :ref           (fn [e] (reset! ref e))}))]
                                   (createPortal (r/as-element wrapped-children) el)))})))


(defn portal-dropdown
  [children x y click-outside-handler]
  (let [mount                (js/document.getElementById "portal")
        el                   (js/document.createElement "div")
        ref                  (atom nil)
        handle-click-outside (fn [e]
                               (when (not (.. @ref (contains (.. e -target))))
                                 (click-outside-handler)))]
    (r/create-class
      {:display-name           "portal"
       :component-did-mount    (fn [_this]
                                 (.. mount (appendChild el))
                                 (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this]
                                 (.. mount (removeChild el))
                                 (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [children]
                                 (createPortal (r/as-element [:div (merge (stylefy/use-style dropdown/dropdown-style)
                                                                          {:style {:position  "fixed"
                                                                                   :left      (str x "px")
                                                                                   :top       (str y "px")}}
                                                                          {:on-mouse-down (fn [e] (.. e preventDefault))
                                                                           :ref           (fn [e] (reset! ref e))})
                                                              [:div (stylefy/use-style dropdown/menu-style) children]]) el))})))





