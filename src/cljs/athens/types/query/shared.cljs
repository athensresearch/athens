(ns athens.types.query.shared)


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
  (let [{:block/keys [uid string properties create edits children] :keys [node/_title]} block
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

