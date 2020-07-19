(ns athens.components-util
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
        rendered   (some (fn [comp]
                           (if (re-matches (get comp :match) content)
                             (get comp :render)
                             nil)) components)]
    (if rendered
      (rendered          content uid)
      (default-component content uid))))
