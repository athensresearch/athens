(ns athens.self-hosted.web.api
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.composite :as composite-ops]
    [athens.self-hosted.clients :as clients]
    [athens.self-hosted.web.datascript :as web.datascript]
    [compojure.core :as c]
    [muuntaja.middleware :as muuntaja.mw]))


;; Helpers

(defn get-block-by-uid
  [conn uid]
  (if-not uid
    {}
    (common-db/get-internal-representation @conn [:block/uid uid])))


;; Only supports paths consisting of a single page title as string.
;; TODO: support starting with uid, exact child string content, properties, today as path fragments.
;; Will need work on graph-ops/build-path to:
;; - create page if first fragment is eid for page and does not exist
;; - find next block child by exact content
;; We might want to add support to paths in atomic ops, but for now can experiment here.
;; NB: quick capture needs paths too.
(defn add
  [conn [k] data]
  (->> (bfs/internal-representation->atomic-ops @conn data {:page/title k :relation :last})
       (composite-ops/make-consequence-op {:op/type :graph/add-in})
       common-events/build-atomic-event))


(defn process-event!
  [datascript fluree config evt]
  (when (->> evt
             (web.datascript/exec! datascript fluree config)
             :event/status
             (= :accepted))
    (clients/broadcast! evt)
    evt))


(defn ok
  [x]
  {:status 200
   :body x})


;; Routes with inline handlers.

(defn make-routes
  [datascript fluree config]
  (let [conn        (:conn datascript)
        ;; TODO: handle pw
        ;; password    (-> config :config :password)
        ]
    (->
      (c/routes
        (c/context
          "/api" []

          (c/GET
            "/block/:uid" [uid]
            (->> uid (get-block-by-uid conn) ok))

          (c/POST
            "/add" {{:keys [path data]} :body-params}
            (->> (add conn path data)
                 (process-event! datascript fluree config)
                 ok))))
      muuntaja.mw/wrap-format)))


;; curl examples
;; curl localhost:3010/api/block/208a0c787
;; curl -H "Content-Type: application/edn" -H "Accept: application/edn" localhost:3010/api/block/208a0c787
;; curl -H "Content-Type: application/edn" -H "Accept: application/edn" -X POST localhost:3010/api/add -d '{:path ["page"] :data [{:block/string "one" :block/children [{:block/string "two"}]}]}'
;; curl -H "Content-Type: application/json" -H "Accept: application/json" -X POST localhost:3010/api/add -d '{"path":["page"],"data":[{"block/string":"one", "block/children":[{"block/string":"two"}]}]}'
