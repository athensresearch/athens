(ns athens.devcards.devtool
  (:require
    [athens.db :as db]
    [athens.devcards.db :refer [new-conn posh-conn! load-real-db-button]]
    [athens.devcards.buttons :refer [button-primary button]]
    [athens.lib.dom.attributes :refer [with-styles]]
    [athens.style :refer [base-styles]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [datascript.core :as d]
    [devcards.core :as devcards :refer [defcard defcard-rg]]
    [garden.core :refer [css]]
    [reagent.core :as r]
    [reagent.ratom :refer [reactive?]]
    [sci.core :as sci]
    [posh.reagent :as posh]
    [shadow.remote.runtime.cljs.browser])
  (:require-macros
    [cljs.core.async.macros :refer [go]]))


(def key-code->key
  {8   :backspace
   9   :tab
   13  :return})


(def initial-state
  {:eval-str
   "(d/q '[:find [?e ...]
       :where [?e :node/title]]
    athens/conn)"
   :tx-reports []})


(defonce state* (r/atom initial-state))


(defcard-rg Import-Styles
  [base-styles])


(defonce conn (new-conn))


(posh-conn! conn)


(defn listener
  [tx-report]
  (swap! state* update :tx-reports conj tx-report))


(d/listen! conn :devtool listener)


(defcard-rg Load-Real-DB
  [load-real-db-button conn])


(defn handler
  []
  (let [n (inc (:max-eid @conn))
        n-child (inc n)]
    (d/transact! conn [{:node/title     (str "Test Page " n)
                        :block/uid      (str "uid-" n)
                        :block/children [{:block/string (str "Test Block" n-child) :block/uid (str "uid-" n-child)}]}])))


(defcard-rg Create-Page
  "Press button and then search \"test\" "
  [button-primary {:on-click-fn handler
                   :label "Create Test Pages and Blocks"}])


(defn eval-with-sci
  [{:keys [eval-str] :as state}]
  (let [bindings {'athens/conn conn
                  'd/pull posh/pull
                  'd/q posh/q}
        [ok? result] (try
                       [true (sci/eval-string eval-str {:bindings bindings})]
                       (catch js/Error e [false e]))]
    (-> state
        (assoc :result result)
        (assoc :error (not ok?)))))


(defn eval-box!
  []
  (swap! state* eval-with-sci))


(defn update-box!
  [s]
  (tap> s)
  (swap! state* assoc :eval-str s))


(defn update-and-eval-box!
  [s]
  (swap! state*
         #(-> %
              (assoc :eval-str s)
              (eval-with-sci))))


(defn handle-box-change!
  [e]
  (update-box! (-> e .-target .-value)))


(defn handle-shift-return!
  [e]
  (.preventDefault e)
  (eval-box!))


(defn insert-tab
  [s pos]
  (str (subs s 0 pos) "  " (subs s pos)))


(defn handle-tab-key!
  [e]
  (let [t (.-target e)
        v (.-value t)
        pos (.-selectionStart t)]
    (.preventDefault e)
    (update-box! (insert-tab v pos))
    (set! (.-selectionEnd t) (+ 2 pos))))


(defn handle-box-key-down!
  [e]
  (let [key-code (.-keyCode e)
        shift? (.-shiftKey e)
        k (key-code->key key-code)]
    (case k
      :return (when shift?
                (handle-shift-return! e))
      :tab (handle-tab-key! e)
      nil)))


(defn browser-component
  [data]
  [:div {:style {:color "blue"}}
   (if (instance? reagent.ratom/Reaction data)
     (pr-str @data)
     (pr-str data))])


(defn error-component
  [error]
  [:div {:style {:color "red"}}
   (str error)])

(defn box-component
  [state _]
  (let [{:keys [eval-str result error]} @state]
    [:div
     [:textarea {:value eval-str
                 :on-change handle-box-change!
                 :on-key-down handle-box-key-down!
                 :style {:width "100%"
                         :min-height "150px"
                         :resize :none
                         :font-size "12px"
                         :font-family "IBM Plex Mono"}}]
     (if-not error
       (browser-component result)
       (error-component result))]))


(defcard-rg Reset-to-all-pages
  (fn []
    [button {:on-click-fn #(do (reset! state* initial-state)
                               (eval-box!))
             :label "Reset"}]))


(defcard-rg Browse-db-box
  box-component
  state*
  {:history true})

(comment
  (deref state*)
  nil)
