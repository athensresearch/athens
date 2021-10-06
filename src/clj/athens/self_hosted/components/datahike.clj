(ns athens.self-hosted.components.datahike
  (:require
    [athens.athens-datoms              :as athens-datoms]
    [athens.common-db                  :as common-db]
    [athens.common.logging             :as log]
    [athens.self-hosted.web.datascript :as ds]
    [clojure.pprint                    :as pp]
    [com.stuartsierra.component        :as component]
    [datahike.api                      :as d]))


(def schema
  [{:db/ident       :schema/version
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}
   {:db/ident       :block/uid
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}
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
      (log/info "Starting Datahike connection: " (with-out-str
                                                   (pp/pprint dh-conf)))
      (let [connection (d/connect conf-with-schema)]
        (log/info "Datahike connected")
        (if @new-db?
          (do
            (log/info "Populating fresh Knowledge graph with initial data...")
            (ds/transact! connection "init-lan-datoms" (->> athens-datoms/lan-datoms
                                                            (common-db/linkmaker @connection)
                                                            (common-db/orderkeeper @connection)))
            (log/info "✅ Populated fresh Knowledge graph."))
          (do
            (log/info "Knowledge graph health check...")
            (let [linkmaker-txs   (common-db/linkmaker @connection)
                  orderkeeper-txs (common-db/orderkeeper @connection)]
              (when-not (empty? linkmaker-txs)
                (log/warn "linkmaker fixes#:" (count linkmaker-txs))
                (log/info "linkmaker fixes:" (pr-str linkmaker-txs))
                (d/transact connection linkmaker-txs))
              (when-not (empty? orderkeeper-txs)
                (log/warn "orderkeeper fixes#:" (count orderkeeper-txs))
                (log/info "orderkeeper fixes:" (pr-str orderkeeper-txs))
                (d/transact connection orderkeeper-txs))
              (log/info "✅ Knowledge graph health check."))))
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
