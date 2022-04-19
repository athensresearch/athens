(ns athens.electron.utils
  (:require
    [clojure.string :as str]))


;; Electron node libs

(def electron?
  #?(:electron true
     :cljs     false))


(def platform-require-error-msg "Platform does not support Electron requires.")


(defn require-or-error
  "Returns the result of js/require when in a electron environment, otherwise throws."
  [_x]
  #?(;; See shadow-cljs.edn reader-features for details.
     :electron (js/require _x)
     :cljs     ^js (throw (new js/Error platform-require-error-msg))))


(def electron #(require-or-error "electron"))
(def ipcRenderer #(..  (electron) -ipcRenderer))
(def remote #(.. (electron) -remote))
(def app #(.. (remote) -app))
(def version #(.. (remote) -app getVersion))
(def dialog #(.. (remote) -dialog))

(def path #(require-or-error "path"))
(def fs #(require-or-error "fs"))
(def os #(require-or-error "os"))
(def stream #(require-or-error "stream"))
(def log #(require-or-error "electron-log"))


;; Electron ipcMain Channels

(def ipcMainChannels
  {:toggle-max-or-min-win-channel "toggle-max-or-min-active-win"
   :close-win-channel "close-win"
   :exit-fullscreen-win-channel "exit-fullscreen-win"})


;; DB utils

(def DB-INDEX "index.transit")
(def IMAGES-DIR-NAME "images")


(defn default-dbs-dir
  "~/Documents on Linux/Mac
  C:\\\\User\\Documents on Windows"
  []
  (.getPath (app) "documents"))


(defn default-base-dir
  []
  (.resolve (path) (default-dbs-dir) "athens"))


(defn local-db
  "Returns a map representing a local db.
   Local dbs are uniquely identified by the base-dir."
  [base-dir]
  {:type       :local
   :name       (.basename (path) base-dir)
   :id         base-dir
   :base-dir   base-dir
   :images-dir (.resolve (path) base-dir IMAGES-DIR-NAME)
   :db-path    (.resolve (path) base-dir DB-INDEX)})


(defn in-memory-db
  "Returns a map representing an in-memory db.
   In-memory dbs are uniquely identified by their name."
  [name]
  {:type       :in-memory
   :name       name
   :id         name})


(defn local-db-exists?
  [{:keys [db-path] :as db}]
  (when db db-path (.existsSync (fs) db-path)))


(defn local-db-dir-exists?
  [{:keys [base-dir] :as db}]
  (when db base-dir (.existsSync (fs) base-dir)))


(defn create-dir-if-needed!
  [dir]
  (when (not (.existsSync (fs) dir))
    (.mkdirSync (fs) dir)))


(defn resolve-http-url
  [url]
  (if (or (str/starts-with? url "http://")
          (str/starts-with? url "https://"))
    url
    (str/join ["http://" url])))


(defn resolve-ws-url
  [url]
  (cond
    (str/starts-with? url "http://")  (str "ws://"  (last (str/split url #"http://")) "/ws")
    (str/starts-with? url "https://") (str "wss://" (last (str/split url #"https://")) "/ws")
    :else                             (str "ws://"  url "/ws")))


(defn self-hosted-db
  "Returns a map representing a self-hosted db.
   Self-hosted dbs are uniquely identified by the url."
  [name url password]
  {:type     :self-hosted
   :name     name
   :id       url
   :url      url
   :password password
   :http-url (resolve-http-url url)
   :ws-url   (resolve-ws-url url)})


(defn local-db?
  [db]
  (-> db :type (= :local)))


(defn remote-db?
  [db]
  (-> db :type (= :self-hosted)))


(defn in-memory-db?
  [db]
  (-> db :type (= :in-memory)))


(defn db-exists?
  [db]
  (condp = (:type db)
    :local       (local-db-exists? db)
    :self-hosted true
    :in-memory   true
    false))


(defn get-default-db
  []
  (if electron?
    (local-db (default-base-dir))
    (in-memory-db "In-memory DB")))
