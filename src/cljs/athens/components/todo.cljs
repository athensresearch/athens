(ns athens.components.todo)


;; TODO: make it clickable & with side effects
(def component-todo
  {:match #"\[\[TODO\]\]"
   :render (fn [content uid]
             [:span [:input {:type  "checkbox"
                             :class "component-todo"
                             :id    (str content uid)}]])})


(def component-done
  {:match #"\[\[DONE\]\]"
   :render (fn [content uid]
             [:span [:input {:type    "checkbox"
                             :class   "component-todo"
                             :checked "true"
                             :id      (str content uid)}]])})


(def components [component-todo component-done])
