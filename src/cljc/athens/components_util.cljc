(ns athens.components-util
  (:require
    [athens.components.todo :as todo]))

;; TODO: use metaprogramming to achieve dynamic rendering with both basic components and custom components
(defn render-component
  "Renders a component using its parse tree & its uid."
  [content uid]
  (let [components (concat todo/components)]
    (some (fn [comp]
            (if (re-matches (get comp :match) content)
              ((get comp :render) content uid)
              nil)) components)))
