(ns athens.components.todo)


(def component-todo
  {:match #"\[\[TODO\]\]"
   :render (fn [content uid]
             [:span [:input {:type "checkbox"
                             :id   (str content uid)}]])})


(def component-done
  {:match #"\[\[DONE\]\]"
   :render (fn [content uid]
             [:span [:input {:type    "checkbox"
                             :checked "true"
                             :id      (str content uid)}]])})


(def components [component-todo component-done])
