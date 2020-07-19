(ns athens.components.components-util
  (:require
    [athens.components.todo :as todo]
    [athens.components.website-embeds :as website-embeds]))


(def components (concat todo/components website-embeds/components))


(defn default-component
  [content uid]
  ((constantly nil) uid)
  [:button content])

;; TODO: use metaprogramming to achieve dynamic rendering with both basic components and custom components
(defn render-component
  "Renders a component using its parse tree & its uid."
  [content uid]
  (let [render     (some (fn [comp]
                           (when (re-matches (:match comp) content)
                             (:render comp))) components)]
    [:span {:on-click (fn [e]
                        (.. e preventDefault)
                        (.. e stopPropagation))}
     (if render
       [render            content uid]
       [default-component content uid])]))
