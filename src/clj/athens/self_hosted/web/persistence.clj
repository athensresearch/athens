(ns athens.self-hosted.web.persistence
  (:refer-clojure :exclude [load])
  (:require
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [datascript.core :as d]))


(def extension ".json")
(def frequency 100)
(def counter (atom 0))


(defn- is-persisted-file?
  [path]
  (and (-> path io/file .isFile)
       (str/ends-with? path extension)))


(defn- id->path
  [persist-base-path id]
  (->> [id extension]
       str/join
       (io/file persist-base-path)
       .getAbsolutePath))


(defn- path->id
  [path]
  (if (is-persisted-file? path)
    (-> path io/file .getName (str/replace extension ""))
    (throw (ex-info "Path is not for a persisted file" {:path path}))))


(defn- persisted
  [persist-base-path]
  (->> persist-base-path
       io/file
       .listFiles
       (map #(.getAbsolutePath %))
       (filter is-persisted-file?)))


(defn- delete-others!
  [persist-base-path id]
  (->> (persisted persist-base-path)
       (remove #{(id->path persist-base-path id)})
       (run! io/delete-file)))


(defn- save!
  [persist-base-path db id]
  (let [path (id->path persist-base-path id)]
    (io/make-parents path)
    (->> db
         d/serializable
         json/write-str
         (spit path))
    path))


(defn load
  [persist-base-path]
  (when-some [path (-> (persisted persist-base-path) first)]
    [(-> path
         slurp
         json/read-str
         d/from-serializable)
     (path->id path)]))


(defn persist!
  [persist-base-path conn {:event/keys [id] :as _event}]
  (when (>= (swap! counter inc) frequency)
    (save! persist-base-path @conn id)
    (delete-others! persist-base-path id)
    (reset! counter 0)
    [@conn id]))


(comment
  ;; Matches what's on src/clj/config.default.edn in [:datascript :persist-base-path]
  ;; On the docker setup there's a config override to /srv/athens/persist
  (def persist-base-path "./athens-data/persist/")

  (persisted persist-base-path)

  (load persist-base-path)

  (id->path persist-base-path "123")

  (save! persist-base-path @(d/create-conn) "123")
  (save! persist-base-path @(d/create-conn) "456")

  (delete-others! persist-base-path "456")

  (persist! persist-base-path (d/create-conn) {:event/id "123"})
  ;;
  )
