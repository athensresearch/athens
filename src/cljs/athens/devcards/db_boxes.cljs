(ns athens.devcards.db-boxes
  (:require
    [athens.db :as db]
    [athens.views.data-browser :as brws :refer [browser]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [datascript.core :as d]
    [devcards.core :as devcards :refer [defcard defcard-rg]]
    [garden.core :refer [css]]
    [reagent.core :as r]
    [sci.core :as sci])
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:import
    (goog.events
      KeyCodes)))


(defcard "
  # An experiment in browsing the datascript database

  You can use these devcards to explore the Athens datascript database.

  Initial data:
  - Start by loading initial data with the \"Load Real Data\" button.
  - This will load some sample datoms from the ego.datoms file

  Executing queries:
  - The browse-box uses [sci](https://github.com/borkdude/sci) to execute datascript queries.
  - In addition to the (non-side-effecting) clojure.core functions, the following bindings are available:
  - `athens/db` -> the datascript connection, dereference (`@`) to get the current database value
  - `d/q` -> for querying the database
  - `d/pull` -> pull one or more attributes of an entity, returns a map
  - `d/pull-many` -> like `d/pull`, but pulls many entities at once
  - Execute the query by pressing `shift-enter`

  Browsing:
  - The browser is a simple html table translating the query result into rows and cells.
  - Browsing is possible if you've used a pull expression (in a query or with `d/pull` or `d/pull-many`).
  - If you click a link, it will generate a new query and evaluate it.

  History:
  - Devcards keeps a history for us. Use the arrows at the bottom to navigate back to earlier states.

  Possible improvements:
  - Right now navigation is only possible by using a pull expression. By analysing queries it might also be possible for all other queries.
  - No transctions are currently allowed, but this can easily be changed by adding `d/transact` to sci's bindings.
  - There is absolutely no styling, some minimal styling would probably make reading the table easier.
  ")


(def initial-box
  {:str-content
   "(d/q '[:find [(pull ?e [*]) ...]
       :where [?e :node/title]]
    @athens/db)"})


(defonce box-state*
  (r/atom initial-box))


(defn eval-box
  [{:keys [str-content] :as box}]
  (let [bindings {'athens/db db/dsdb
                  'd/q d/q
                  'd/pull d/pull
                  'd/pull-many d/pull-many}
        [ok? result] (try
                       [true (sci/eval-string str-content {:bindings bindings})]
                       (catch js/Error e [false e]))]
    (-> box
        (assoc :result result)
        (assoc :error (not ok?)))))


(defn eval-box!
  []
  (swap! box-state* eval-box))


(defn update-box!
  [s]
  (swap! box-state* assoc :str-content s))


(defn update-and-eval-box!
  [s]
  (swap! box-state*
         #(-> %
              (assoc :str-content s)
              (eval-box))))


(defn load-real-db!
  [conn]
  (go
    (let [res (<! (http/get db/athens-url {:with-credentials? false}))
          {:keys [success body]} res]
      (if success
        (do (d/transact! conn (db/str-to-db-tx body))
            (swap! box-state* eval-box))
        (js/alert "Failed to retrieve data from GitHub")))))


(defn load-real-db-button
  [conn]
  (let [pressed? (r/atom false)
        handler (fn []
                  (swap! pressed? not)
                  (load-real-db! conn))]
    (fn []
      [:button.primary {:disabled @pressed? :on-click handler} "Load Real Data"])))


(defcard-rg Load-Real-DB
  "Downloads the ego db. Takes a few seconds."
  [load-real-db-button db/dsdb])


(defcard-rg Modify-Devcards
  "Increase width to 90% for table"
  [:style (css [:.com-rigsomelight-devcards-container {:width "90%"}]
               [:.com-rigsomelight-devcards_rendered-card {:display "flex";
                                                           :flex-direction "column-reverse"}])]);


(defn pull-entity-str
  ([id]
   (str "(d/pull @athens/db '[*] " id ")"))
  ([attr id]
   (str "(d/pull @athens/db '[*] [" attr " " (pr-str id) "])")))


(defn cell
  [{:keys [value attr id]}]
  (if value
    (cond
      (= :db/id attr)
      [:a {:on-click #(update-and-eval-box! (pull-entity-str (or id value)))
           :style {:cursor :pointer}}
       (str value)]

      (brws/attr-unique? attr)
      [:a {:on-click #(update-and-eval-box! (pull-entity-str attr value))
           :style {:cursor :pointer}}
       (str value)]

      (and (brws/attr-many? attr)
           (brws/attr-ref? attr))
      [:ul (for [v value]
             ^{:key v}
             [:li (cell {:value v
                         :attr :db/id
                         :id (:db/id v)})])]

      (brws/attr-reverse? attr)
      [:ul (for [v value]
             ^{:key v}
             [:li (cell {:value v
                         :attr :db/id
                         :id (:db/id v)})])]

      (brws/attr-many? attr)
      [:ul (for [v value]
             ^{:key v}
             [:li (cell {:value v})])]

      :else
      (str value))
    ""))


(defn error-component
  [error]
  [:div {:style {:color "red"}}
   (str error)])


(defn handle-box-change!
  [e]
  (update-box! (-> e .-target .-value)))


(defn handle-return-key!
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
  (let [key (.. e -keyCode)
        shift? (.. e -shiftKey)]
    (cond
      (= key KeyCodes.ENTER) (when shift? (handle-return-key! e))
      (= key KeyCodes.TAB) (handle-tab-key! e)
      :else nil)))


(defn box-component
  [box-state _]
  (let [{:keys [str-content result error]} @box-state]
    [:div
     [:textarea {:value str-content
                 :on-change handle-box-change!
                 :on-key-down handle-box-key-down!
                 :style {:width "100%"
                         :min-height "150px"
                         :resize :none
                         :font-size "12px"
                         :font-family "IBM Plex Mono"}}]
     (if-not error
       [browser result {:cell-fn cell}]
       [error-component result])]))


(defcard-rg Reset-to-all-pages
  (fn []
    [:button {:on-click #(do (reset! box-state* initial-box)
                             (eval-box!))}
     "Reset"]))


(defcard-rg Browse-db-box
  box-component
  box-state*
  {:history true})
