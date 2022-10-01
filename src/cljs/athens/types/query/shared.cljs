(ns athens.types.query.shared
  (:require
    [athens.common-events.graph.ops :as graph-ops]
    [athens.db :as db]
    [athens.views.blocks.editor                 :as editor]
    [re-frame.core :as rf]
    [reagent.core :as r]))


(defn get-root-page
  [x]
  (merge x
         {":task/page" (:node/title (db/get-root-parent-page (get x ":block/uid")))}))


(defn parse-for-title
  "should be able to pass in a plain string, a wikilink, or both?"
  [s]
  (when (seq s)
    (let [re #"\[\[(.*)\]\]"]
      (cond
        (re-find re s) (second (re-find re s))
        (clojure.string/blank? s) (throw "parse-for-title got an empty string")
        :else s))))


(defn parse-for-uid
  "should be able to pass in a plain string, a wikilink, or both?"
  [s]
  (when (seq s)
    (let [re #"\(\((.*)\)\)"]
      (cond
        (re-find re s) (second (re-find re s))
        (clojure.string/blank? s) (throw "parse-for-title got an empty string")
        :else s))))


(defn get-create-auth-and-time
  [create-event]
  {":create/auth" (get-in create-event [:event/auth :presence/id])
   ":create/time" (get-in create-event [:event/time :time/ts])})


(defn get-last-edit-auth-and-time
  [edit-events]
  (let [last-edit (last edit-events)]
    {":last-edit/auth" (get-in last-edit [:event/auth :presence/id])
     ":last-edit/time" (get-in last-edit [:event/time :time/ts])}))


(defn block-to-flat-map
  [block]
  ;; TODO: we could technically give pages all the properties of tasks and put them on a kanban board...
  (let [{:block/keys [uid string properties create edits _children] :keys [node/_title]} block
        create-auth-and-time    (get-create-auth-and-time create)
        last-edit-auth-and-time (get-last-edit-auth-and-time edits)
        property-keys           (keys properties)
        props-map               (reduce (fn [acc prop-key]
                                          (assoc acc prop-key (get-in properties [prop-key :block/string])))
                                        {}
                                        property-keys)
        merged-map              (merge {":block/uid"    uid
                                        ":block/string" string}
                                       props-map
                                       create-auth-and-time
                                       last-edit-auth-and-time
                                       {":task/status" (parse-for-uid (get props-map ":task/status"))}
                                       {":task/assignee" (parse-for-title (get props-map ":task/assignee"))})]
    #_(when children
        (prn (get merged-map ":task/title") uid children))
    merged-map))


(defn update-card-field
  [id k new-value]
  (rf/dispatch [:graph/update-in [:block/uid id] [k]
                (fn [db prop-uid]
                  [(graph-ops/build-block-save-op db prop-uid new-value)])]))


(defn title-editor
  [uid title]
  (let [value-atom      (r/atom (or title ""))
        show-edit-atom? (r/atom true)
        block-o         {:block/uid uid}]
    (fn []
      (let [enter-fn!   (fn [uid d-key-down]
                          (let [{:keys [target]} d-key-down]

                            (update-card-field uid ":task/title" @value-atom)
                            (reset! show-edit-atom? false)
                            ;; side effect
                            (.blur target)))
            save-fn!    #(do
                           (update-card-field uid ":task/title" @value-atom)
                           (rf/dispatch [:block/save {:uid    uid
                                                      :string @value-atom}]))
            state-hooks {:save-fn                 save-fn!
                         :enter-handler           enter-fn!
                         :idle-fn                 #()
                         :update-fn               #(reset! value-atom %)
                         :read-value              value-atom
                         :show-edit?              show-edit-atom?
                         :esc-handler             (fn [e _uid]
                                                    (reset! value-atom title)
                                                    (.. e -target blur))
                         :tab-handler             #()
                         :backspace-handler       #()
                         :delete-handler          #()
                         :default-verbatim-paste? true
                         :keyboard-navigation?    false
                         :style                   {:opacity 1}
                         :placeholder             "Write your task title here"}]
        [editor/block-editor block-o state-hooks]))))
