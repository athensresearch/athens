(ns athens.self-hosted.web.api
  (:require
   [athens.common-db :as common-db]
   [athens.common-events :as common-events]
   [athens.common-events.graph.atomic :as atomic]
   [athens.common.utils :as utils]
   [athens.self-hosted.web.datascript :as web.datascript]
   [clojure.pprint :as pp]
   [compojure.core :as c]
   [muuntaja.middleware :as muuntaja.mw]))

;; TODO
;; how content negotiation
;; - https://github.com/metosin/reitit/blob/master/modules/reitit-middleware/src/reitit/ring/middleware/muuntaja.clj
;; - https://github.com/metosin/muuntaja
;; - edn, json, md (for gets)
;; basic auth for pw
;; return graph time
;; non-recursive by default
;; principled API
;; - http://clojure-liberator.github.io/liberator/
;; version endpoint

;; Helpers

(defn str-resp
  [x]
  (with-out-str (pp/pprint {:data x})))


(defn ret
  [x _]
  x)


(defn get-block-by-uid
  ([conn uid]
   (if-not uid
     {}
     (common-db/get-internal-representation @conn [:block/uid uid]))))


(defn get-page-by-title
  [conn title-or-titles]
  (cond
    (not title-or-titles)    {}
    (vector title-or-titles) (map (partial get-page-by-title) title-or-titles)
    :else                    (common-db/get-internal-representation @conn [:node/title title-or-titles])))


;; Middleware

(defn wrap-get-block-by-uid
  [conn handler]
  (fn [request]
    (->> request handler (get-block-by-uid conn))))


(defn wrap-get-page-by-title
  [conn handler]
  (fn [request]
    (->> request handler (get-page-by-title conn))))


;; Routes with inline handlers.

(defn make-routes
  [datascript fluree config]
  (let [conn        (:conn datascript)
        ;; password    (-> config :config :password)
        event-exec! (partial web.datascript/exec! conn fluree config)
        ;; TODO: need to change something in make-receive-handler to send stuff in as an event, and broadcast to all
        exec!       (fn [op] (-> op common-events/build-atomic-event event-exec!))]
    (->
     (c/routes
      (c/context
       "/api" []

       ;; Import block/page representation.
       ;; TODO: WIP because we don't have a wire format for the block loc body-params
       #_(c/context
          "/import" []
          (c/POST "/pages" [] "pages IR")
          (c/POST "/blocks" [] "blocks IR + loc"))

       ;; Block
       ;; TODO: allow block API after when we need more than the IR endpoint
       (->>
        (c/context
         "/block" []
         (c/POST "/"       [body-params] (let [uid (utils/gen-block-uid)]
                                           (->> body-params (atomic/make-block-new-op uid) exec! (ret uid))))
         (c/context
          "/:uid" [uid]
          (c/GET  "/"     []    uid)
          (c/DELETE "/"   []    (->> uid atomic/make-block-remove-op exec! (ret nil)))
          (c/POST "/"     [body-params] (->> body-params (atomic/make-block-new-op uid) exec! (ret uid)))
          (c/POST "/save" [body-params] (->> body-params (atomic/make-block-save-op uid) exec! (ret uid)))
          (c/POST "/open" [body-params] (->> body-params (not= "false") (atomic/make-block-open-op uid) exec! (ret uid)))
          (c/POST "/move" [body-params] (->> body-params (atomic/make-block-move-op uid) exec! (ret uid)))))
        ;; Return a block representation for all block calls.
        (partial wrap-get-block-by-uid conn))

       ;; Page
       ;; TODO: allow after page API seems solid
       #_(->
          (c/context
           "/page" []
           (c/GET "/"          []    (common-db/get-all-page-titles @conn))
           (c/context
            "/:title" [title] ; is title is url-encoded? add middleware if not, or helper
            (c/GET "/"        []    title)
            (c/DELETE "/"     []    (->> title atomic/make-page-remove-op exec! (ret nil)))
            (c/POST "/"       []    (->> title atomic/make-page-new-op exec! (ret title)))
            (c/POST "/rename" [body-params] (->> body-params (atomic/make-page-rename-op title) exec! (ret title))) ;; TODO: new title
            (c/POST "/merge"  [body-params] (->> body-params (atomic/make-page-merge-op title) exec! (ret title)))))
          ;; Return a page representation for all page calls.
          (partial wrap-get-page-by-title conn))

       ;; Shortcut
       ;; TODO: WIP because we don't really have a representation for shortcuts, and we might convert them into props.
       #_(c/context
          "/shortcut/:title" [title]
          (c/DELETE "/" [] ":shortcut/remove")
          (c/POST "/" [] ":shortcut/new")
          (c/POST "/move" [] ":shortcut/move"))

       ;; Raw operation.
       #_(c/POST "/op" [body-params] (-> body-params exec! (ret {})))))
     muuntaja.mw/wrap-format)))


;; curl examples
;; curl localhost:3010/api/block/ee770c334
;; curl -X POST localhost:3010/api/block -d '{:block/uid "ee770c334" :relation :last}'
;; curl localhost:3010/api/block/ee770c334
;; curl -X POST localhost:3010/api/block -d '{:block/uid "ee770c334" :relation :last}'
;; curl localhost:3010/api/block/ee770c334
