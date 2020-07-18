(ns athens.components.todo)

(def component-todo {:match #"\[\[TODO\]\]"
                     :render (fn [content uid] [:span [:input {:type "checkbox"}]])})

(def component-done {:match #"\[\[DONE\]\]"
                     :render (fn [content uid] [:span [:input {:type    "checkbox"
                                                               :checked "true"}]])})

(def components [component-todo component-done])