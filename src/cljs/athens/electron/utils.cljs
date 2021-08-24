(ns athens.electron.utils)


(def electron?
  (let [user-agent (.. js/navigator -userAgent toLowerCase)]
    (boolean (re-find #"electron" user-agent))))


;; Electron node libs

(def platform-error "Platform does not support Electron requires.")
(def ^js error-proxy (js/Proxy. #js {} #js {:get (fn [] (throw platform-error))}))


(defn require-or-error
  [x]
  (if electron? (js/require x) error-proxy))


(def electron (require-or-error "electron"))
(def ipcRenderer (.. electron -ipcRenderer))
(def remote (.. electron -remote))
(def app (.. remote -app))
(def version (.. remote -app getVersion))
(def dialog (.. remote -dialog))
(def path (require-or-error "path"))
(def fs (require-or-error "fs"))
(def os (require-or-error "os"))
(def stream (require-or-error "stream"))
(def log (require-or-error "electron-log"))


;; DB utils

(def DB-INDEX "index.transit")
(def IMAGES-DIR-NAME "images")


(defn default-dbs-dir
  "~/Documents on Linux/Mac
  C:\\\\User\\Documents on Windows"
  []
  (.getPath app "documents"))


(defn default-base-dir
  []
  (.resolve path (default-dbs-dir) "athens"))


(defn local-db
  "Returns a map representing a local db.
   Local dbs are uniquely identified by the base-dir."
  [base-dir]
  {:type       :local
   :name       (.basename path base-dir)
   :id         base-dir
   :base-dir   base-dir
   :images-dir (.resolve path base-dir IMAGES-DIR-NAME)
   :db-path    (.resolve path base-dir DB-INDEX)})


(defn local-db-exists?
  [{:keys [db-path] :as db}]
  (when db db-path (.existsSync fs db-path)))


(defn local-db-dir-exists?
  [{:keys [base-dir] :as db}]
  (when db base-dir (.existsSync fs base-dir)))


(defn create-dir-if-needed!
  [dir]
  (when (not (.existsSync fs dir))
    (.mkdirSync fs dir)))


(defn self-hosted-db
  "Returns a map representing a self-hosted db.
   Self-hosted dbs are uniquely identified by the url."
  [name url]
  {:type   :self-hosted
   :name   name
   :id     url
   :url    url
   :ws-url (str "ws://" url "/ws")})


(defn local-db?
  [db]
  (-> db :type (= :local)))


(defn remote-db?
  [db]
  (-> db :type (= :self-hosted)))


(defn db-exists?
  [db]
  (condp = (:type db)
    :local       (local-db-exists? db)
    :self-hosted remote-db? true
    :else        false))
