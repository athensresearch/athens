(ns athens.views.utils
  (:require
    [goog.events :as events]
    [reagent.core :as r]))


;; Calls `callback` when a click is done outside the provided `children` nodes.
;; Note: This won't reattach a document event listener if the callback changes.
;; So you need to make sure you don't need that, and if you need that; this can
;; be rewritten using an `useEffect` to listen/unlisten for clicks on `callback`
;; changes.
(defn track-outside-click
  [callback child]
  (r/with-let [ref (atom nil)
               handler #(when
                          (not (.. @ref (contains (.. % -target))))
                          (callback))
               _ (events/listen js/document "mousedown" handler)]
              [:span
               {:ref #(reset! ref %)}
               child]
              (finally
                (events/unlisten js/document "mousedown" handler))))
