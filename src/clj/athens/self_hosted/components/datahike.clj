(ns athens.self-hosted.components.datahike
  (:require
    [athens.athens-datoms       :as athens-datoms]
    [clojure.tools.logging      :as log]
    [com.stuartsierra.component :as component]
    [datahike.api               :as d]))


(def schema
  [{:db/ident       :schema/version
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :block/uid
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}
   {:db/ident       :remote/db-id
    :db/valueType   :db.type/long
    :db/unique      :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident       :block/title
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}
   {:db/ident       :block/string
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident       :node/title
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}
   {:db/ident       :attrs/lookup
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/many}
   {:db/ident       :block/children
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :block/refs
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}
   {:db/ident       :create/time
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :edit/time
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :block/open
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one}
   {:db/ident       :block/order
    :db/valueType   :db.type/number
    :db/cardinality :db.cardinality/one}
   {:db/ident       :from-history
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one}
   {:db/ident       :from-undo-redo
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one}
   {:db/ident       :page/sidebar
    :db/valueType   :db.type/number
    :db/cardinality :db.cardinality/one}])


(defrecord Datahike
  [config conn]

  component/Lifecycle

  (start
    [component]
    (let [dh-conf          (get-in config [:config :datahike])
          conf-with-schema (assoc dh-conf :initial-tx schema)
          new-db?          (atom false)]
      (if (d/database-exists? dh-conf)
        (log/info "Connecting to existing Datahike database")
        (do
          (log/info "Creating new Datahike database")
          (d/create-database conf-with-schema)
          (reset! new-db? true)))
      (log/info "Starting Datahike connection: " dh-conf)
      (let [connection (d/connect conf-with-schema)]
        (log/debug "Datahike connected")
        (when @new-db?
          (log/debug "Populating fresh db with datoms.")
          (d/transact connection athens-datoms/lan-datoms))
        (assoc component :conn connection))))


  (stop
    [component]
    (log/info "Stopping Datahike")
    (when conn
      (log/info "Releasing conn")
      (d/release conn)
      (assoc component :conn nil))))


(defn new-datahike
  []
  (map->Datahike {}))
