(ns athens.self-hosted.web.api
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.composite :as composite-ops]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common-events.schema :as schema]
    [athens.common.utils :as utils]
    [athens.dates :as dates]
    [athens.self-hosted.clients :as clients]
    [athens.self-hosted.web.datascript :as web.datascript]
    [clojure.string :as str]
    [compojure.core :as c]
    [datascript.core :as d]
    [muuntaja.middleware :as muuntaja.mw]
    [ring.middleware.basic-authentication :as basic-auth]))


;; Paths

(defn e->eid
  [{:keys [:node/title :block/uid]}]
  (cond
    title [:node/title title]
    uid   [:block/uid uid]))


(defn page-search
  [query]
  (cond
    (= query "@today") (-> (dates/get-day) :title)
    :else              (throw (ex-info "Cannot resolve title." {:page/query query}))))


(defn resolve-root
  [_db {:keys [page/title page/query block/uid] :as x}]
  (cond
    title [:node/title title]
    uid   [:block/uid uid]
    query [:node/title (page-search query)]
    :else (throw (ex-info "Cannot resolve root." x))))


(defn resolve-selector
  [db eid {:keys [block/string block/key]}]
  (let [e (d/entity db eid)]
    (cond
      (not e) nil
      string  (->> e
                   :block/children
                   (filter #(= string (:block/string %)))
                   first
                   e->eid)
      key     (->> e
                   :block/_property-of
                   (filter #(= key (-> % :block/key :node/title)))
                   first
                   e->eid)
      :else   nil)))


;; Different from graph.ops/build-path, paths are [root selectors...]
;; TODO: support order/prop selectors
;; Root examples:
;;   {:page/title "page"}
;;   {:block/uid "uid"}
;;   {:page/query "@today"}
;; Selector examples:
;;   {:block/string "one two"}
;;   [:block/key "three four"]
(defn resolve-path
  [db [root & selectors]]
  (reduce (partial resolve-selector db)
          (resolve-root db root)
          selectors))


(def eid-k-map
  {:node/title :page/title
   :block/uid :block/uid})


(defn eid->position-id
  [[k v]]
  [(eid-k-map k) v])


(defn create-from-selector
  [db parent-eid {:keys [block/string block/key] :as selector}]
  (let [uid      (utils/gen-block-uid)
        eid      [:block/uid uid]
        position (into {:relation (cond
                                    string :last
                                    key    {:page/title key}
                                    :else  (throw (ex-info "Cannot create from selector", selector)))}
                       [(eid->position-id parent-eid)])

        ops      (into [(graph-ops/build-block-new-op db uid position)]
                       (when string
                         [(graph-ops/build-block-save-op db uid string)]))]
    [eid ops]))


(defn resolve-or-create-selector
  [db [eid ops] selector]
  (if-some [existing-eid (resolve-selector db eid selector)]
    [existing-eid ops]
    (let [[created-eid create-ops] (create-from-selector db eid selector)]
      [created-eid (into ops create-ops)])))


(defn resolve-or-create-path
  [db [root & selectors]]
  (reduce (partial resolve-or-create-selector db)
          [(resolve-root db root) []]
          selectors))


(comment
  (resolve-or-create-path common-db/empty-db [{:page/query "@today"} {:block/string "one"} {:block/string "two"} {:block/string "three"}]))


{:event
 {:event/id #uuid "1ccf42c4-516d-4985-9316-2184c933978c", :event/type :op/atomic, :event/op {:op/type :composite/consequence, :op/atomic? false, :op/trigger {:op/type :path/write}, :op/consequences [{:op/type :block/new, :op/atomic? true, :op/args {:block/uid "5f52e2d1d", :block/position {:relation :last, :node/title "September 27, 2022"}}} {:op/type :block/new, :op/atomic? true, :op/args {:block/uid "0566dd31a", :block/position {:relation :last, :block/uid "5f52e2d1d"}}} {:op/type :block/new, :op/atomic? true, :op/args {:block/uid "84a1c3c86", :block/position {:relation :last, :block/uid "0566dd31a"}}} {:op/type :block/new, :op/atomic? true, :op/args {:block/uid "43efd72f5", :block/position {:relation :last, :block/uid "84a1c3c86"}}} {:op/type :block/save, :op/atomic? true, :op/args {:block/uid "43efd72f5", :block/string "four"}}]}, :event/create-time 1664305865053, :event/presence-id "api-test"}, :explain {:event/op {:op/consequences [{:op/args {:block/position {:page/title ["missing required key" "missing required key"], :block/uid ["missing required key" "missing required key" "missing required key"], :relation ["should be either :before or :after" "invalid type" "invalid type"]}}}]}}}


;; Read/Write

(defn read-path
  [conn path]
  (let [db @conn]
    (->> path
         (resolve-path db)
         (common-db/get-internal-representation db))))


(defn write-in-path-evt
  [conn path relation data]
  (when (empty? data)
    (throw (ex-info "No data to write" data)))
  (let [db             @conn
        [eid path-ops] (resolve-or-create-path db path)
        default-pos    (into {:relation (or relation :last)}
                             [(eid->position-id eid)])
        write-ops      (bfs/internal-representation->atomic-ops db data default-pos)]
    (->> (into path-ops write-ops)
         (composite-ops/make-consequence-op {:op/type :path/write})
         common-events/build-atomic-event)))


(defn add-presence-id
  [presence-id event]
  (common-events/add-presence event presence-id))


(defn process-event!
  [datascript fluree config evt]
  (when-not (schema/valid-event? evt)
    (throw (ex-info "Invalid event" {:event   evt
                                     :explain (schema/explain-event evt)})))
  (when (->> evt
             (web.datascript/exec! datascript fluree config)
             :event/status
             (= :accepted))
    (clients/broadcast! evt)
    evt))


;; Routes

(defn ok
  [x]
  {:status 200
   :body x})


;; for convenience with ->>
(defn ret-first
  [x _]
  x)


;; Username is always required non-empty
(defn authenticated?
  [config-pw username pw]
  (if (and (not (str/blank? username))
           (or (not config-pw)
               (= pw config-pw)))
    {:presence-id username}
    false))


(defn make-routes
  [datascript fluree config]
  (let [conn (:conn datascript)
        config-pw (-> config :config :password)]
    (if-not (-> config :config :feature-flags :api)
      (c/routes)
      (->
        (c/routes
          (c/context
            "/api/path" []

            (c/POST
              "/read" {{:keys [path]} :body-params}
              (->> path
                   (read-path conn)
                   ok))

            (c/POST
              "/write" {{:keys [path relation data]} :body-params
                        {:keys [presence-id]}        :basic-authentication}
              (->> (write-in-path-evt conn path relation data)
                   (add-presence-id presence-id)
                   (process-event! datascript fluree config)
                   (ret-first path)
                   (read-path conn)
                   ok))))
        (basic-auth/wrap-basic-authentication (partial authenticated? config-pw))
        muuntaja.mw/wrap-format))))


;; Examples with curl

;; - auth
;; Uses https://en.wikipedia.org/wiki/Basic_access_authentication
;; Username is always needed even if server has no password.
;;   curl -u presence-name:server-password ...
;;   curl -u api-test: ...
;; Remember to use base64 encoding when not using curl https://stackoverflow.com/a/60505090/2116927

;; - content negotiation
;; JSON uses keywords in map keys as strings, but still keeping the namespaces (e.g. :page/title -> "page/title").
;; Returns JSON by default, so you don't need to set the Accept header:
;;   curl -H "Content-Type: application/json" ...
;; To use EDN:
;;   curl -H "Content-Type: application/edn" -H "Accept: application/edn" ...

;; - read page "page", in edn and in json
;; curl -u api-test: -H "Content-Type: application/edn" -H "Accept: application/edn" localhost:3010/api/path/read -X POST -d '{:path [{:page/title "page"}]}'
;; curl -u api-test: -H "Content-Type: application/json" localhost:3010/api/path/read -X POST -d '{"path":[{"page/title":"page"}]}'

;; - write blocks to page
;; curl -X POST localhost:3010/api/path/write -d '{:path [{:page/title "page"}] :data [{:block/string "one" :block/children [{:block/string "two"}]}]}'
;; curl -X POST localhost:3010/api/path/write -d '{"path":[{"page/title":"page"}], "data":[{"block/string":"one", "block/children":[{"block/string":"two"}]}]}'

;; Assume examples below have encoding and auth included like the EDN example above
;; curl -u api-test: -H "Content-Type: application/edn" -H "Accept: application/edn" ...

;; - read todays page
;; curl localhost:3010/api/path/read -X POST -d '{:path [{:page/query "@today"}]}'
;; - read the first block with string "hello" in todays page
;; curl localhost:3010/api/path/read -X POST -d '{:path [{:page/query "@today"} {:block/string "hello"}]}'
;; - read the block with property "prop" in the first block with string "hello" in todays page
;; curl localhost:3010/api/path/read -X POST -d '{:path [{:page/query "@today"} {:block/string "hello"} {:block/key "prop"}]}'
;; - write in the today/one/two/three nested block path, creating it if needed, the child with string "four"
;; curl localhost:3010/api/path/read -X POST -d '{:path [{:page/query "@today"} {:block/string "one"} {:block/string "two"} {:block/string "three"}]}'
