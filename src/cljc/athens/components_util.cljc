(ns athens.components-util
  (:require
    [athens.components.todo :as todo]))


(defn default-component
  [content uid]
  [:span {:id uid} [:button {:class "component-default"} content]])

;; TODO: use metaprogramming to achieve dynamic rendering with both basic components and custom components
(defn render-component
  "Renders a component using its parse tree & its uid."
  [content uid]
  (let [components (concat todo/components)
        rendered   (some (fn [comp]
                           (if (re-matches (get comp :match) content)
                             (get comp :render)
                             nil)) components)]
    (if rendered
      (rendered          content uid)
      (default-component content uid))))
