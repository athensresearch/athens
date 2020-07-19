(ns athens.components.components-util
  (:require
    [athens.components.default-components :as default-components]))


(defn empty-component
  [content _]
  [:button content])

;; TODO: use metaprogramming to achieve dynamic rendering with both basic components and custom components
(defn render-component
  "Renders a component using its parse tree & its uid."
  [content uid]
  (let [render     (some (fn [comp]
                           (when (re-matches (:match comp) content)
                             (:render comp))) default-components/components)]
    [:span {:on-click (fn [e]
                        (.. e preventDefault)
                        (.. e stopPropagation))}
     (if render
       [render            content uid]
       [empty-component   content uid])]))
