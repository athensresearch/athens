(ns athens.components.todo
  (:require
    [athens.db :as db]
    [athens.util :refer [now-ts]]
    [re-frame.core :refer [dispatch]]))


(defn todo-on-click
  [uid from-str to-str]

  (let [current-block-content (get (db/get-block [:block/uid uid]) :block/string)]
    (dispatch [:transact [{:db/id [:block/uid uid]
                           :block/string (clojure.string/replace
                                           current-block-content
                                           from-str
                                           to-str)
                           :edit/time (now-ts)}]])))


(def component-todo
  {:match #"\[\[TODO\]\]"
   :render (fn [content uid]
             [:span [:input {:type     "checkbox"
                             :class    "component-todo"
                             :on-click (fn [e] 
                                          (.. e preventDefault) 
                                          (.. e stopPropagation) 
                                          (todo-on-click uid #"\{\{\[\[TODO\]\]\}\}" "{{[[DONE]]}}"))
                             :id       (str content uid)}]])})


(def component-done
  {:match #"\[\[DONE\]\]"
   :render (fn [content uid]
             [:span [:input {:type     "checkbox"
                             :class    "component-todo"
                             :checked  "true"
                             :on-click (fn [e] 
                                          (.. e preventDefault) 
                                          (.. e stopPropagation) 
                                          (todo-on-click uid #"\{\{\[\[DONE\]\]\}\}" "{{[[TODO]]}}"))
                             :id       (str content uid)}]])})


(def components [component-todo component-done])
