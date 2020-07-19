(ns athens.components.todo
  (:require
    [athens.db :as db]
    [athens.util :refer [now-ts]]
    [re-frame.core :refer [dispatch]]))


(defn todo-on-click
  [uid from-str to-str]
  (let [current-block-content (:block/string (db/get-block [:block/uid uid]))]
    (dispatch [:transact [{:block/uid    uid
                           :block/string (clojure.string/replace
                                           current-block-content
                                           from-str
                                           to-str)
                           :edit/time    (now-ts)}]])))


(def component-todo
  {:match #"\[\[TODO\]\]"
   :render (fn [content uid]
             ((constantly nil) content uid)
             [:input {:type     "checkbox"
                             :on-click #(todo-on-click uid #"\{\{\[\[TODO\]\]\}\}" "{{[[DONE]]}}")}])})


(def component-done
  {:match #"\[\[DONE\]\]"
   :render (fn [content uid]
             ((constantly nil) content uid)
             [:input {:type     "checkbox"
                             :checked  "true"
                             :on-click #(todo-on-click uid #"\{\{\[\[DONE\]\]\}\}" "{{[[TODO]]}}")}])})


(def components [component-todo component-done])
