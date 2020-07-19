(ns athens.components.components-util
  (:require
    [athens.components.todo :as todo]
    [athens.components.website-embeds :as website-embeds]))


(defn default-component
  [content uid]
  ((constantly nil) uid)
  [:span [:button content]])

;; TODO: use metaprogramming to achieve dynamic rendering with both basic components and custom components
(defn render-component
  "Renders a component using its parse tree & its uid."
  [content uid]
  (let [components (concat todo/components website-embeds/components)
        render     (some (fn [comp]
                           (when (re-matches (:match comp) content)
                             (:render comp))) components)]
    (if render
      [render            content uid]
      [default-component content uid])))
